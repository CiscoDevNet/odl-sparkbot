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
import com.ciscospark.Membership;
import com.ciscospark.Message;
import com.ciscospark.Room;

import java.lang.reflect.Method;

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
    private static WebhookServer instance;

    private WebhookServer() {
        httpServer = null;
    }

    public static WebhookServer getInstance() {
        if (instance == null) {
            instance = new WebhookServer();
        }
        return instance;
    }

    /** Registers a 'raw' webhook handler.
     * @param handler: the handler to be registered
     */
    public static void registerWebhookHandler(final WebhookEventHandler handler) {
        getInstance().httpHandler.registerWebhookHandler(handler);
    }

    /** Unregisters a 'raw' webhook handler.
     * @param handler: the handler to be registered
     */
    public static void unregisterWebhookHandler(final WebhookEventHandler handler) {
        getInstance().httpHandler.unregisterWebhookHandler(handler);
    }

    /** Get the Spark class for which the handler has been instantiated
     *  (Message, Room, or Membership). Basically, find the 1st method in
     *  the handler class that matches the handler method name.
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

    /** Register a handler to process Spark webhook events.
     * @param handler reference to the handler to be registered. A handler
     *          can be parameterized to a Message, Room, or Membership.
     */
    @SuppressWarnings("unchecked")
    public static <T> void registerSparkEventHandler(final SparkEventHandler<T> handler) {
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

    /** Handles addition or change of configuration parameters.
     * @param port the port on which to listen to requests
     */
    public void handleConfigParmsChange(final Long port) {
        if (port != null) {
            startHttpServer(port.intValue());
        }
    }

    /** Handles deletion of configuration parameters.
     *
     */
    public void handleConfigParmsDelete() {
        stopHttpServer();
    }

    private void startHttpServer(final Integer port) {
        if (port < 1 && port > 65535) {
            LOG.error("SparkViewBotWebHook: http port out of range: {}", port);
            return;
        }

        httpServer = new Server(port);
        httpServer.setHandler(httpHandler);
        try {
            this.httpServer.start();
        } catch (Exception e) {
            LOG.info("Exception: {}", e.toString());
            httpServer = null;
            return;
        }
    }

    private void stopHttpServer() {

        if (httpServer != null) {
            try {
                httpServer.stop();
            } catch (Exception e) {
                LOG.info("Exception: {}", e.toString());
            }
            httpServer = null;
        }
    }
}
