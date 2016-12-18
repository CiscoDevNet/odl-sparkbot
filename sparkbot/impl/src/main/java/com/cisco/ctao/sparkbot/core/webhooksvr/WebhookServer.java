/*
 * Copyright © 2016 Cisco Systems, Inc and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.cisco.ctao.sparkbot.core.webhooksvr;

import com.cisco.ctao.sparkbot.core.Memberships;
import com.cisco.ctao.sparkbot.core.Messages;
import com.cisco.ctao.sparkbot.core.RawEventHandler;
import com.cisco.ctao.sparkbot.core.Rooms;
import com.cisco.ctao.sparkbot.core.TypedEventHandler;
import com.cisco.ctao.sparkbot.core.Webhooks;
import com.ciscospark.Membership;
import com.ciscospark.Message;
import com.ciscospark.Room;
import com.ciscospark.SparkException;
import com.ciscospark.Webhook;

import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Implements the HTTP server that catches the Webhook callouts from Spark.
 * @author johnburn
 *
 */
public final class WebhookServer {
    private static final Logger LOG = LoggerFactory.getLogger(WebhookServer.class);
    private static final String EVT_HANDLER_METHOD_NAME = "handleSparkEvent";
    private static final Map<RawEventHandler, RawEventHandlerReg> REGISTRATIONS = new HashMap<>();

    private static WebhookServer instance;

    private final SparkServlet sparkServlet = new SparkServlet("Default");
    private final SparkEventProcessor<Message> msgEventProcessor =
            new SparkEventProcessor<>(Messages.api(), "messages");
    private final SparkEventProcessor<Room> roomEventProcessor =
            new SparkEventProcessor<>(Rooms.api(), "rooms");
    private final SparkEventProcessor<Membership> membershipEventProcessor =
            new SparkEventProcessor<>(Memberships.api(), "memberships");

    private Server httpServer;
    private Integer httpPort;
    private URI urlPrefix;
    private ServletContextHandler context;

    private WebhookServer() {
        httpServer = null;
    }

    public static Long getWebhookServerPort() {
        if (getInstance().httpPort != null) {
            return getInstance().httpPort.longValue();
        } else {
            return null;
        }
    }

    public static String getWebhookUrlPrefix() {
        if (getInstance().urlPrefix != null) {
            return getInstance().urlPrefix.toString();
        } else {
            return null;
        }
    }

    public static WebhookServer getInstance() {
        if (instance == null) {
            instance = new WebhookServer();
        }
        return instance;
    }

    /** Registers a 'raw' webhook handler.
     * @param handler the handler to be registered
     */
    public static void registerRawEventHandler(final RawEventHandler handler) {
        registerRawEventHandler(handler, null);
    }

    /** Registers a 'raw' webhook handler. IF a filter is specified,
     *  the registration creates a separate servlet in the sparkbot Jetty
     *  server and a webhook in Spark. The webhook's 'event' and 'resource'
     *  parameters are set to the values specified in the filter.
     * @param handler the handler to be registered
     * @param filter if specified, create a webhook in Spark with parameters
     *           as specified in the filter. The filter specifies webhook
     *           parameters and a path in the local server that will be used
     *           for this handler only (in the handler's servlet).
     */
    public static void registerRawEventHandler(final RawEventHandler handler, final WebhookFilter filter) {
        LOG.info("registerRawEventHandler: handler {}, filter {}", handler, filter);
        if (filter != null) {
            // Create a new servlet for the handler
            if (REGISTRATIONS.get(handler) == null) {
                Webhook webhook = createWebhook(filter);

                final SparkServlet servlet = new SparkServlet(filter.getName());
                servlet.registerRawEventHandler(handler);
                final ServletHolder sh = new ServletHolder(servlet);
                getInstance().context.addServlet(sh, "/" + filter.getName());
                try {
                    sh.start();
                    final String webhookId = (webhook != null) ? webhook.getId() : null;
                    REGISTRATIONS.put(handler, new RawEventHandlerReg(webhookId, handler, sh, filter));
                } catch (Exception e) {
                    LOG.error("registerRawEventHandler: failed to start servlet {}, ", filter.getName(), e);
                }
            } else {
                LOG.error("Handler '{}' already registered", filter.getName());
            }
        } else {
            // Register the handler with the default servlet
            getInstance().sparkServlet.registerRawEventHandler(handler);
        }
    }

    /** Unregisters a 'raw' webhook handler.
     * @param handler: the handler to be registered
     */
    public static void unregisterRawEventHandler(final RawEventHandler handler) {
        LOG.info("unregisterRawEventHandler: handler {}", handler);

        if (!getInstance().sparkServlet.unregisterRawEventHandler(handler)) {
            RawEventHandlerReg reg = REGISTRATIONS.get(handler);
            if (reg != null) {
                RawEventHandlerReg reg1 = new RawEventHandlerReg(reg);
                REGISTRATIONS.remove(handler);
                try {
                    reg1.getServletHolder().stop();
                    reg1.getServletHolder().getServlet().destroy();
                    Webhooks.deleteWebhook(reg1.getHandlerWebhookId());
                } catch (Exception e) {
                    LOG.error("Could not stop and/or destroy servlet for handler {}", handler, e);
                    LOG.error("Restarting the Webhook HTTP Server.");
                    getInstance().stopHttpServer();
                    getInstance().startHttpServer(getInstance().httpPort);
                }
            } else {
                LOG.info("unregisterRawEventHandler: handler '{}' not found", handler);
            }
        }
    }

    /** Register a handler to process Spark webhook events.
     * @param handler reference to the handler to be registered. A handler
     *          can be parameterized to a Message, Room, or Membership.
     */
    public static <T> void registerSparkEventHandler(final TypedEventHandler<T> handler) {
        registerSparkEventHandler(handler, null);
    }

    /** Register a handler to process Spark webhook events.
     * @param handler reference to the handler to be registered. A handler
     *          can be parameterized to a Message, Room, or Membership.
     * @param filter if specified, create a webhook in Spark with parameters
     *           as specified in the filter
     */
    @SuppressWarnings("unchecked")
    public static <T> void registerSparkEventHandler(final TypedEventHandler<T> handler,
            final WebhookFilter filter) {
        Class<?> clazz = findEventHandlerClass(handler);
        if (clazz != null) {
            if (Message.class.isAssignableFrom(clazz)) {
                SparkEventProcessor<Message> evtProc = getInstance().msgEventProcessor;
                if (evtProc.registerHandler((TypedEventHandler<Message>) handler) == 0) {
                    registerRawEventHandler(evtProc);
                }
            } else if (Room.class.isAssignableFrom(clazz)) {
                SparkEventProcessor<Room> evtProc = getInstance().roomEventProcessor;
                if (evtProc.registerHandler((TypedEventHandler<Room>) handler) == 0) {
                    registerRawEventHandler(evtProc);
                }
            } else if (Membership.class.isAssignableFrom(clazz)) {
                SparkEventProcessor<Membership> evtProc = getInstance().membershipEventProcessor;
                if (evtProc.registerHandler((TypedEventHandler<Membership>) handler) == 0) {
                    registerRawEventHandler(evtProc);
                }
            } else {
                LOG.error("Invalid event handler object, sparkEventHandler method {}", clazz.getName());
            }
        }
    }

    /** Unregister a previously registered Spark webhook event handler.
     * @param handler reference to the handler to be unregistered.
     */
    @SuppressWarnings("unchecked")
    public static <T> void unregisterSparkEventHandler(final TypedEventHandler<T> handler) {
        Class<?> clazz = findEventHandlerClass(handler);
        if (clazz != null) {
            if (Message.class.isAssignableFrom(clazz)) {
                SparkEventProcessor<Message> evtProc = getInstance().msgEventProcessor;
                if (evtProc.unregisterHandler((TypedEventHandler<Message>) handler) == 0) {
                    unregisterRawEventHandler(evtProc);
                }
            } else if (Room.class.isAssignableFrom(clazz)) {
                SparkEventProcessor<Room> evtProc = getInstance().roomEventProcessor;
                if (evtProc.unregisterHandler((TypedEventHandler<Room>) handler) == 0) {
                    unregisterRawEventHandler(evtProc);
                }
            } else if (Membership.class.isAssignableFrom(clazz)) {
                SparkEventProcessor<Membership> evtProc = getInstance().membershipEventProcessor;
                if (evtProc.unregisterHandler((TypedEventHandler<Membership>) handler) == 0) {
                    unregisterRawEventHandler(evtProc);
                }
            } else {
                LOG.error("Invalid event handler object, sparkEventHandler method {}", clazz.getName());
            }
        }
    }

    /** Handles addition or change of Webhook Server's HTTP Port.
     * @param port the port on which to listen to requests
     */
    public void handleHttpPortChange(final Long port) {
        LOG.info("handleHttpPortChange: port {}, httpPort {}", port, httpPort);
        if (port != null) {
            Integer tmpPort = port.intValue();
            if ((httpPort != null && (!httpPort.equals(tmpPort))) || (httpPort == null)) {
                httpPort = tmpPort;
                stopHttpServer();
                startHttpServer(tmpPort);
            }
        }
    }

    /** Handles addition or change of the schema/host/port prefix used in
     *  targetURLs for the webhooks created by default when a Spark Event
     *  handler is being registered.
     * @param urlPfxString the prefix string for the target URLs
     */
    public void handleUrlPrefixChange(String urlPfxString) {
        LOG.info("handleUrlPrefixChange: urlPfxString: {}", urlPfxString);
        if (urlPfxString != null) {
            try {
                URI tmpPrefix = new URI(urlPfxString);
                if (urlPrefix != null && (!urlPrefix.equals(tmpPrefix))) {
                    updateWebhookTargetUrls(urlPrefix, tmpPrefix);
                }
                urlPrefix = tmpPrefix;
            } catch (URISyntaxException e) {
                LOG.error("handleUrlPrefixChange: Invalid URL Prefix {} ", urlPfxString, e);
            }
        }
    }

    /** Handles the deletion of the HTTP port configuration.
     *
     */
    public void handleHttpPortDelete() {
        LOG.info("handleHttpPortDelete");
        stopHttpServer();
    }

    /** Handles the deletion of the URL prefix configuration.
     *
     */
    public void handleUrlPrefixDelete() {
        LOG.info("handleUrlPrefixDelete: urlPrefix {}", urlPrefix);
        urlPrefix = null;
    }

    /** Get the Spark class for which the handler has been instantiated
     *  (Message, Room, or Membership). Basically, find the 1st method in
     *  the handler class that matches the handler method name and get the
     *  type of its resource parameter.
     * @param handler reference to a handler from which to get the class
     * @return the class for which the handler has been instantiated
     */
    private static <T> Class<?> findEventHandlerClass(final TypedEventHandler<T> handler) {
        for (Method m : handler.getClass().getMethods()) {
            if (EVT_HANDLER_METHOD_NAME.equals(m.getName())) {
                Class<?>[] handlerParams = m.getParameterTypes();
                if (handlerParams.length == 3) {
                    return m.getParameterTypes()[1];
                } else {
                    LOG.error("Spark event handler in class {} has invalid number of parameters {}",
                            handler.getClass().getName(), handlerParams.length);
                }
            }
        }
        LOG.error("Spark event handler not found in class {}", handler.getClass());
        return null;
    }

    private void updateWebhookTargetUrls(URI oldTargetPrefix, URI newTargetPrefix) {
        LOG.info("updateWebhookTargetUrls: oldTargetPrefix {}, newTargetPrefix {}",
                oldTargetPrefix, newTargetPrefix);
        try {
            List<Webhook> webhooks = Webhooks.listWebhooks(null);
            for (Webhook wh : webhooks) {
                URI targetUrl = wh.getTargetUrl();

                if (targetUrl.getScheme().equals(oldTargetPrefix.getScheme())
                        && targetUrl.getHost().equals(oldTargetPrefix.getHost())
                        && targetUrl.getPort() == oldTargetPrefix.getPort()) {
                    updateWebhookTargetUrl(wh, targetUrl, newTargetPrefix);
                }
            }
        } catch (SparkException e) {
            LOG.error("updateWebhookTargetUrls: Spark error, could not update webhooks with the new URL prefix", e);
        }
    }

    private void updateWebhookTargetUrl(Webhook wh, URI oldTargetPrefix, URI newTargetPrefix) {
        LOG.info("updateWebhookTargetUrl: wh {}, oldTargetPrefix {}, oldTargetPrefix {}",
                wh, oldTargetPrefix, newTargetPrefix);
        try {
            URI newUrl = new URI(newTargetPrefix.getScheme(), newTargetPrefix.getUserInfo(),
                    newTargetPrefix.getHost(), newTargetPrefix.getPort(), oldTargetPrefix.getPath(),
                    null, null);
            Webhooks.updateWebhook(wh.getId(), wh.getName(), newUrl);
        } catch (URISyntaxException e) {
            LOG.error("updateWebhookTargetUrl: Could not create new URL , oldTargetPrefix {}, newTargetPrefix {}",
                    oldTargetPrefix.toASCIIString(), newTargetPrefix.toASCIIString(), e);
        }
    }

    private void startHttpServer(final Integer port) {
        LOG.info("startHttpServer {}", port);
        if (port < 1 && port > 65535) {
            LOG.error("SparkViewBotWebHook: http port out of range: {}", port);
            return;
        }

        this.httpPort = port;
        this.httpServer = new Server(port);

        reconcileHandlersWithWebhooks();

        try {
            this.httpServer.start();
        } catch (Exception e) {
            LOG.error("startHttpServer: failed to start the server, ", e);
            this.httpServer = null;
            this.httpPort = null;
            return;
        }
        ServletHolder sh = new ServletHolder(new HelloServlet("What a World"));
        context.addServlet(sh,"/wow");
        try {
            sh.start();
        } catch (Exception e) {
            LOG.error("startHttpServer: failed to dynamically start the wow servlet, ", e);
        }
    }

    private void reconcileHandlersWithWebhooks() {
        LOG.info("reconcileHandlersWithWebhooks");

        // Delete all existing webhooks from Spark
        cleanupWebhooks();
        // Create new context for the Webhook HTTP Server
        context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        httpServer.setHandler(context);

        // Add the default RAW handler
        context.addServlet(new ServletHolder(sparkServlet),"/*");

        // Temporary, just for testing
        context.addServlet(new ServletHolder(new HelloServlet("Buongiorno Mondo")),"/it/*");
        context.addServlet(new ServletHolder(new HelloServlet("Bonjour le Monde")),"/fr/*");
        context.addServlet(new ServletHolder(new HelloServlet("Guten Morgen Welt")),"/de");

        // Recreate registrations for all our registered handlers
        final Collection<RawEventHandlerReg> regValues = cloneRegistrationValues();
        REGISTRATIONS.clear();
        for (RawEventHandlerReg reg : regValues) {
            registerRawEventHandler(reg.getHandler(), reg.getFilter());
        }
    }

    private void stopHttpServer() {
        if (this.httpServer != null) {
            try {
                this.httpServer.stop();
            } catch (Exception e) {
                LOG.info("stopHttpServer: Exception: ", e);
            }
            httpServer.destroy();
            this.httpServer = null;
            this.httpPort = null;
            cleanupWebhooks();
        }
    }

    private static void cleanupWebhooks() {
        try {
            for (Webhook wh : Webhooks.listWebhooks(null)) {
                Webhooks.deleteWebhook(wh.getId());
            }
        } catch (SparkException e) {
            LOG.error("Error cleaning up existing sparkbot webhooks in Spark - sync required at a later time", e);
        }
    }

    private static Collection<RawEventHandlerReg> cloneRegistrationValues() {
        final Collection<RawEventHandlerReg> clonedRegs = new ArrayList<>();
        for (Entry<RawEventHandler, RawEventHandlerReg> entry : REGISTRATIONS.entrySet()) {
            clonedRegs.add(new RawEventHandlerReg(entry.getValue()));
        }
        return clonedRegs;
    }

    private static Webhook createWebhook(final WebhookFilter filter) {
        if (WebhookServer.getWebhookUrlPrefix() == null) {
            LOG.error("createWebhook: URL prefix not specified");
            return null;
        }
        URI webhookUrl;
        try {
            webhookUrl = new URI(getWebhookUrlPrefix() + "/" + filter.getName());
        } catch (URISyntaxException e) {
            LOG.error("createWebhook: Invalid URL syntax", e);
            return null;
        }
        LOG.info("webhookUrl {}", webhookUrl);
        try {
            final Webhook webhook = Webhooks.createWebhook(filter.getName(), webhookUrl,
                    filter.getResource(), filter.getEvent(), filter.getFilter(), filter.getSecret());
            LOG.info("createWebhook: webhook created {}", webhook);
            return webhook;
        } catch (SparkException e) {
            LOG.error("createWebhook: Failed to create webhook", e);
            return null;
        }

    }
}
