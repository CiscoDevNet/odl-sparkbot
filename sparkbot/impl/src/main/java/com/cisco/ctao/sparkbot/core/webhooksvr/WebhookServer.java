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
import com.cisco.ctao.sparkbot.core.Rooms;
import com.cisco.ctao.sparkbot.core.SparkEventHandler;
import com.cisco.ctao.sparkbot.core.WebhookEventHandler;
import com.cisco.ctao.sparkbot.core.Webhooks;
import com.ciscospark.Membership;
import com.ciscospark.Message;
import com.ciscospark.Room;
import com.ciscospark.SparkException;
import com.ciscospark.Webhook;

import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import org.eclipse.jetty.server.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Implements the HTTP server that catches the Webhook callouts from Spark.
 * @author johnburn
 *
 */
public final class WebhookServer {
    private static final Logger LOG = LoggerFactory.getLogger(WebhookServer.class);
    private static final String EVT_HANDLER_METHOD_NAME = "handleSparkEvent";

    private final HttpEventProcessor httpHandler = new HttpEventProcessor();
    private final SparkEventProcessor<Message> msgEventProcessor =
            new SparkEventProcessor<>(Messages.api(), "messages");
    private final SparkEventProcessor<Room> roomEventProcessor =
            new SparkEventProcessor<>(Rooms.api(), "rooms");
    private final SparkEventProcessor<Membership> membershipEventProcessor =
            new SparkEventProcessor<>(Memberships.api(), "memberships");

    private Server httpServer;
    private Integer httpPort;
    private URI urlPrefix;
    private static WebhookServer instance;

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
    public static void registerWebhookHandler(final WebhookEventHandler handler) {
        registerWebhookHandler(handler, null);
    }

    /** Registers a 'raw' webhook handler.
     * @param handler the handler to be registered
     * @param filter if specified, create a webhook in Spark with parameters
     *           as specified in the filter
     */
    public static void registerWebhookHandler(final WebhookEventHandler handler, final WebhookFilter filter) {
        getInstance().httpHandler.registerWebhookHandler(handler, filter);
    }

    /** Unregisters a 'raw' webhook handler.
     * @param handler: the handler to be registered
     */
    public static void unregisterWebhookHandler(final WebhookEventHandler handler) {
        getInstance().httpHandler.unregisterWebhookHandler(handler);
    }

    /** Register a handler to process Spark webhook events.
     * @param handler reference to the handler to be registered. A handler
     *          can be parameterized to a Message, Room, or Membership.
     */
    public static <T> void registerSparkEventHandler(final SparkEventHandler<T> handler) {
        registerSparkEventHandler(handler, null);
    }

    /** Register a handler to process Spark webhook events.
     * @param handler reference to the handler to be registered. A handler
     *          can be parameterized to a Message, Room, or Membership.
     * @param filter if specified, create a webhook in Spark with parameters
     *           as specified in the filter
     */
    @SuppressWarnings("unchecked")
    public static <T> void registerSparkEventHandler(final SparkEventHandler<T> handler, final WebhookFilter filter) {
        Class<?> clazz = findEventHandlerClass(handler);
        if (clazz != null) {
            if (Message.class.isAssignableFrom(clazz)) {
                SparkEventProcessor<Message> evtProc = getInstance().msgEventProcessor;
                if (evtProc.registerHandler((SparkEventHandler<Message>) handler) == 0) {
                    registerWebhookHandler(evtProc);
                }
            } else if (Room.class.isAssignableFrom(clazz)) {
                SparkEventProcessor<Room> evtProc = getInstance().roomEventProcessor;
                if (evtProc.registerHandler((SparkEventHandler<Room>) handler) == 0) {
                    registerWebhookHandler(evtProc);
                }
            } else if (Membership.class.isAssignableFrom(clazz)) {
                SparkEventProcessor<Membership> evtProc = getInstance().membershipEventProcessor;
                if (evtProc.registerHandler((SparkEventHandler<Membership>) handler) == 0) {
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
    public static <T> void unregisterSparkEventHandler(final SparkEventHandler<T> handler) {
        Class<?> clazz = findEventHandlerClass(handler);
        if (clazz != null) {
            if (Message.class.isAssignableFrom(clazz)) {
                SparkEventProcessor<Message> evtProc = getInstance().msgEventProcessor;
                if (evtProc.unregisterHandler((SparkEventHandler<Message>) handler) == 0) {
                    unregisterWebhookHandler(evtProc);
                }
            } else if (Room.class.isAssignableFrom(clazz)) {
                SparkEventProcessor<Room> evtProc = getInstance().roomEventProcessor;
                if (evtProc.unregisterHandler((SparkEventHandler<Room>) handler) == 0) {
                    unregisterWebhookHandler(evtProc);
                }
            } else if (Membership.class.isAssignableFrom(clazz)) {
                SparkEventProcessor<Membership> evtProc = getInstance().membershipEventProcessor;
                if (evtProc.unregisterHandler((SparkEventHandler<Membership>) handler) == 0) {
                    unregisterWebhookHandler(evtProc);
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
        LOG.info("handleHttpPortChange: port {}", port);
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
    private static <T> Class<?> findEventHandlerClass(final SparkEventHandler<T> handler) {
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
        if (port < 1 && port > 65535) {
            LOG.error("SparkViewBotWebHook: http port out of range: {}", port);
            return;
        }

        this.httpPort = port;
        this.httpServer = new Server(port);
        this.httpServer.setHandler(httpHandler);
        try {
            this.httpServer.start();
        } catch (Exception e) {
            LOG.error("Failed to start the webhook HTTP server, Exception: {}", e.toString());
            this.httpServer = null;
            this.httpPort = null;
            return;
        }
    }

    private void stopHttpServer() {

        if (this.httpServer != null) {
            try {
                this.httpServer.stop();
            } catch (Exception e) {
                LOG.info("Exception: {}", e.toString());
            }
            this.httpServer = null;
            this.httpPort = null;
        }
    }
}
