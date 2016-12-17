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
    private static final Map<String, RawEventHandlerReg> REGISTRATIONS = new HashMap<>();

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
    public static void registerWebhookHandler(final RawEventHandler handler) {
        registerRawEventHandler(handler, null);
    }

    /** Registers a 'raw' webhook handler. IF a filter is specified,
     *  the registration creates a separate servlet and a webhook is Spark.
     *  The webhook's 'event' and 'resource' parameters are set to the
     *  values specified in the filter.
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
            if (REGISTRATIONS.get(filter.getName()) == null) {
                SparkServlet servlet = null;
                Webhook webhook = null;
                ServletHolder sh;
                try {
                    final URI webhookUrl = new URI(getWebhookUrlPrefix() + "/" + filter.getName());
                    LOG.info("webhookUrl {}", webhookUrl);
                    String webhookId;
                    try {
                        webhook = Webhooks.createWebhook(filter.getName(), webhookUrl,
                                filter.getResource(), filter.getEvent(), filter.getFilter(), filter.getSecret());
                        LOG.info("registerRawEventHandler: webhook created {}", webhook);
                        webhookId = webhook.getId();
                    } catch (Exception e) {
                        LOG.error("registerRawEventHandler: Failed to create webhook", e);
                        webhookId = null;
                    }
                    servlet = new SparkServlet(filter.getName());
                    servlet.registerWebhookHandler(handler);
                    sh = new ServletHolder(servlet);
                    getInstance().context.addServlet(sh, "/" + filter.getName());
                    sh.start();
                    REGISTRATIONS.put(filter.getName(), new RawEventHandlerReg(webhookId, handler, filter));
                    LOG.info("REGISTRATIONS {}", REGISTRATIONS);
                } catch (URISyntaxException e) {
                    LOG.error("registerWebhookHandler: failed to register handler '{}'", filter.getName(), e);
                } catch (Exception e) {
                    LOG.error("registerWebhookHandler: Erro starting servlet '{}'", filter.getName(), e);
                    if (servlet != null) {
                        servlet.unregisterWebhookHandler(handler);
                    }
                    if (webhook != null) {
                        Webhooks.deleteWebhook(webhook.getId());
                    }
                    // sh.stop();
                    // servlet.destroy();
                    REGISTRATIONS.remove(filter.getName());
                }
            } else {
                LOG.error("Handler '{}' already registered", filter.getName());
            }
        } else {
            // Register the handler with the default servlet
            getInstance().sparkServlet.registerWebhookHandler(handler);
        }
    }

    /** Unregisters a 'raw' webhook handler.
     * @param handler: the handler to be registered
     */
    public static void unregisterRawEventHandler(final RawEventHandler handler) {
        getInstance().sparkServlet.unregisterWebhookHandler(handler);
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
                    registerWebhookHandler(evtProc);
                }
            } else if (Room.class.isAssignableFrom(clazz)) {
                SparkEventProcessor<Room> evtProc = getInstance().roomEventProcessor;
                if (evtProc.registerHandler((TypedEventHandler<Room>) handler) == 0) {
                    registerWebhookHandler(evtProc);
                }
            } else if (Membership.class.isAssignableFrom(clazz)) {
                SparkEventProcessor<Membership> evtProc = getInstance().membershipEventProcessor;
                if (evtProc.registerHandler((TypedEventHandler<Membership>) handler) == 0) {
                    registerWebhookHandler(evtProc);
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

    /** Handles addition or change of HTTP Port.
     * @param port the port on which to listen to requests
     */
    public void handleHttpPortChange(final Long port) {
        LOG.info("handleHttpPortChange: port {}, httpPort {}", port, httpPort);
        if (port != null) {
            Integer tmpPort = port.intValue();
            if ((httpPort != null && (!httpPort.equals(tmpPort))) || (httpPort == null)) {
                httpPort = tmpPort;
                startHttpServer(tmpPort);
            }
        }
    }

    /** Handles addition or change of the schema/host/port prefix used in
     *  targetURLs for the webhooks created by default when a Spark Event
     *  handler was registered.
     * @param urlPfxString the prefix string for the target URLs
     */
    public void handleUrlPrefixChange(String urlPfxString) {
        LOG.info("handleUrlPrefixChange: urlPfxString: {}", urlPfxString);
        if (urlPfxString != null) {
            try {
                URI tmpPrefix = new URI(urlPfxString);
                if (urlPrefix != null) {
                    updateWebhookTargetUrls(urlPrefix, tmpPrefix);
                }
                urlPrefix = tmpPrefix;
            } catch (URISyntaxException e) {
                LOG.error("handleUrlPrefixChange: Invalid URL Prefix {} ", urlPfxString, e);
            }
        } else {
            if (urlPrefix != null) {
                handleUrlPrefixDelete();
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
                    updateWebHookTargetUrl(wh, targetUrl, newTargetPrefix);
                }
            }
        } catch (SparkException e) {
            LOG.error("Got error from Saprk - could not update webhooks with changed URL", e);
        }
    }

    private void updateWebHookTargetUrl(Webhook wh, URI targetUrl, URI newTargetPrefix) {
        try {
            URI newUrl = new URI(newTargetPrefix.getScheme(), newTargetPrefix.getUserInfo(),
                    newTargetPrefix.getHost(), newTargetPrefix.getPort(), targetUrl.getPath(),
                    null, null);
            Webhooks.updateWebhook(wh.getId(), wh.getName(), newUrl);
        } catch (URISyntaxException e) {
            LOG.error("handleUrlPrefixChange: Could not create new URL , targetUrl {}, newTargetPrefix {}",
                    targetUrl.toASCIIString(), newTargetPrefix.toASCIIString(), e);
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

        syncSparkbots();

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

    private void syncSparkbots() {
        LOG.info("syncSparkbots");

        // Delete all existing webhooks from Spark
        try {
            for (Webhook wh : Webhooks.listWebhooks(null)) {
                Webhooks.deleteWebhook(wh.getId());
            }
        } catch (SparkException e) {
            LOG.error("Error cleaning up existing sparkbot webhooks in Spark - sync required at a later time", e);
        }

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
        Collection<RawEventHandlerReg> regValues = getValues(REGISTRATIONS);
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
            this.httpServer = null;
            this.httpPort = null;
        }
    }

    private static Collection<RawEventHandlerReg> getValues(Map<String, RawEventHandlerReg> reg) {
        Collection<RawEventHandlerReg> values = new ArrayList<>();
        for (Entry<String, RawEventHandlerReg> entry : reg.entrySet()) {
            values.add(new RawEventHandlerReg(entry.getValue()));
        }
        return values;
    }
}
