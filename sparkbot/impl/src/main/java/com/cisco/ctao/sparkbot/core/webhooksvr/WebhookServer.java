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
    public void registerWebhookHandler(final WebhookEventHandler handler) {
        httpHandler.registerWebhookHandler(handler);
    }

    /** Unregisters a 'raw' webhook handler.
     * @param handler: the handler to be registered
     */
    public void unregisterWebhookHandler(final WebhookEventHandler handler) {
        httpHandler.unregisterWebhookHandler(handler);
    }

    @SuppressWarnings("unchecked")
    public static <T> void registerSparkEventHandler(final SparkEventHandler<T> handler) {
        Method eventHandler = handler.getClass().getMethods()[0];
        if ("handleSparkEvent".equals(eventHandler.getName())) {
            Class<?> clazz = eventHandler.getParameterTypes()[1];
            if (Message.class.isAssignableFrom(clazz)) {
                SparkEventProcessor<Message> evtProc = getInstance().msgEventProcessor;
                if (evtProc.registerHandler((SparkEventHandler<Message>) handler) == 0) {
                    getInstance().registerWebhookHandler(evtProc);
                }
            } else if (Room.class.isAssignableFrom(clazz)) {
                SparkEventProcessor<Room> evtProc = getInstance().roomEventProcessor;
                if (evtProc.registerHandler((SparkEventHandler<Room>) handler) == 0) {
                    getInstance().registerWebhookHandler(evtProc);
                }
            } else if (Membership.class.isAssignableFrom(clazz)) {
                SparkEventProcessor<Membership> evtProc = getInstance().membershipEventProcessor;
                if (evtProc.registerHandler((SparkEventHandler<Membership>) handler) == 0) {
                    getInstance().registerWebhookHandler(evtProc);
                }
            } else {
                LOG.error("Invalid event handler object, sparkEventHandler method {}", clazz.getName());
            }
        } else {
            LOG.error("Invalid event handler method, sparkEventHandler method {}", eventHandler.getName());
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> void unregisterSparkEventHandler(final SparkEventHandler<T> handler) {
        Method eventHandler = handler.getClass().getMethods()[0];
        if ("handleSparkEvent".equals(eventHandler.getName())) {
            Class<?> clazz = eventHandler.getParameterTypes()[1];
            if (Message.class.isAssignableFrom(clazz)) {
                SparkEventProcessor<Message> evtProc = getInstance().msgEventProcessor;
                if (evtProc.unregisterHandler((SparkEventHandler<Message>) handler) == 0) {
                    getInstance().unregisterWebhookHandler(evtProc);
                }
            } else if (Room.class.isAssignableFrom(clazz)) {
                SparkEventProcessor<Room> evtProc = getInstance().roomEventProcessor;
                if (evtProc.unregisterHandler((SparkEventHandler<Room>) handler) == 0) {
                    getInstance().unregisterWebhookHandler(evtProc);
                }
            } else if (Membership.class.isAssignableFrom(clazz)) {
                SparkEventProcessor<Membership> evtProc = getInstance().membershipEventProcessor;
                if (evtProc.unregisterHandler((SparkEventHandler<Membership>) handler) == 0) {
                    getInstance().unregisterWebhookHandler(evtProc);
                }
            } else {
                LOG.error("Invalid event handler object, sparkEventHandler method {}", clazz.getName());
            }
        } else {
            LOG.error("Invalid event handler method, sparkEventHandler method {}", eventHandler.getName());
        }
    }

    /** Registers a message webhook handler.
     * @param handler: the handler to be registered
     */
    public void registerWebhookMessageHandler(final SparkEventHandler<Message> handler) {
        if (httpHandler != null) {
            LOG.info("Registering SparkEventHandler<Message> {}", handler);
            if (msgEventProcessor.registerHandler(handler) == 0) {
                registerWebhookHandler(msgEventProcessor);
            }
        }
    }

    /** Unregisters a message webhook handler.
     * @param handler: the handler to be registered
     */
    public void unregisterWebhookMessageHandler(final SparkEventHandler<Message> handler) {
        if (httpHandler != null) {
            LOG.info("Unregistering SparkEventHandler<Message> {}", handler);
            if (msgEventProcessor.unregisterHandler(handler) == 0) {
                unregisterWebhookHandler(msgEventProcessor);
            }
        }
    }

    /** Register an application webhook room handler.
     * @param handler: the handler to be registered
     */
    public void registerWebhookRoomHandler(final SparkEventHandler<Room> handler) {
        LOG.info("Registering SparkEventHandler<Room> {}", handler);
        if (roomEventProcessor.registerHandler(handler) == 0) {
            registerWebhookHandler(roomEventProcessor);
        }
    }

    /** Unregister an application webhook room handler.
     * @param handler: the handler to be registered
     */
    public void unregisterWebhookRoomHandler(final SparkEventHandler<Room> handler) {
        LOG.info("Unregistering SparkEventHandler<Room> {}", handler);
        if (roomEventProcessor.unregisterHandler(handler) == 0) {
            unregisterWebhookHandler(roomEventProcessor);
        }
    }

    /** Register an application webhook room handler.
     * @param handler: the handler to be registered
     */
    public void registerWebhookMembershipHandler(final SparkEventHandler<Membership> handler) {
        LOG.info("Registering SparkEventHandler<Membership> {}", handler);
        if (membershipEventProcessor.registerHandler(handler) == 0) {
            registerWebhookHandler(membershipEventProcessor);
        }
    }

    /** Unregister an application webhook room handler.
     * @param handler: the handler to be registered
     */
    public void unregisterWebhookMembershipHandler(final SparkEventHandler<Membership> handler) {
        LOG.info("Unregistering SparkEventHandler<Membership> {}", handler);
        if (membershipEventProcessor.unregisterHandler(handler) == 0) {
            unregisterWebhookHandler(membershipEventProcessor);
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
