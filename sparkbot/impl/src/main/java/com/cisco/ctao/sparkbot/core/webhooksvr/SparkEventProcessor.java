/*
 * Copyright © 2016 Cisco Systems, Inc and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.cisco.ctao.sparkbot.core.webhooksvr;

import com.cisco.ctao.sparkbot.core.SparkApi;
import com.cisco.ctao.sparkbot.core.TypedEventHandler;
import com.cisco.ctao.sparkbot.core.TypedEventHandler.EventType;
import com.cisco.ctao.sparkbot.core.RawEventHandler;
import com.ciscospark.SparkException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SparkEventProcessor<T> implements RawEventHandler {
    private static final Logger LOG = LoggerFactory.getLogger(SparkEventProcessor.class);
    private final SparkApi<T> sparkApi;
    private final String resource;
    private final List<TypedEventHandler<T>> handlers =
            Collections.synchronizedList(new ArrayList<>());

    private void handleCreatedUpdatedEvent(final String elementId, EventType eventType) {
        LOG.debug("{}: handleCreatedUpdatedEvent id {}, resource '{}', registered handlers {}",
                this.getClass().getName(), elementId, resource, handlers.size());

        if (!handlers.isEmpty()) {
            T element;
            try {
                element = sparkApi.getDetails(elementId);
            } catch (SparkException e) {
                element = null;
                LOG.error("handleCreatedUpdatedEvent: Can't retrieve element {}, exception:", elementId, e);
            }
            for (TypedEventHandler<T> handler : handlers) {
                handler.handleSparkEvent(elementId, element, eventType);
            }

        }
    }

    private void handleDeletedEvent(final String elementId) {
        LOG.debug("{}: handleDeletedEvent id {}, resource '{}', registered handlers {}",
                this.getClass().getName(), elementId, resource, handlers.size());
        for (TypedEventHandler<T> handler : handlers) {
            handler.handleSparkEvent(elementId, null, EventType.DELETED);
        }
    }

    public SparkEventProcessor(SparkApi<T> sparkApi, String resource) {
        this.sparkApi = sparkApi;
        this.resource = resource;
    }

    public void handleWebhookEvent(final WebhookEvent webhookMsg, final RequestHeaderData requestData) {
        LOG.debug("handleWebhookEvent, Event: {}", webhookMsg.toString());
        final String elementId;
        final WebhookEventData msgData;
        if (resource.equals(webhookMsg.getResource())
                && (msgData = webhookMsg.getData()) != null
                && (elementId = msgData.getId()) != null) {
            switch (webhookMsg.getEvent()) {
                case "created":
                    handleCreatedUpdatedEvent(elementId, EventType.CREATED);
                    break;
                case "updated":
                    handleCreatedUpdatedEvent(elementId, EventType.UPDATED);
                    break;
                case "deleted":
                    handleDeletedEvent(elementId);
                    break;
                default:
                    LOG.error("handleWebhookEvent: Unknown event {}", webhookMsg.getEvent());
            }
        }
    }

    /** Register an application spark object handler.
     * @param handler: the handler to be registered
     * @return the number of registered handlers *before* this registration
     */
    public int registerHandler(final TypedEventHandler<T> handler) {
        LOG.info("Registering handler {}", handler.getClass().getName());
        final int size = handlers.size();
        handlers.add(handler);
        return size;
    }

    /** Unregister an application spark object handler.
     * @param handler: the handler to be unregistered
     * @return the number of registered handlers *after* this registration
     */
    public int unregisterHandler(final TypedEventHandler<T> handler) {
        LOG.info("Unregistering handler {}", handler.getClass().getName());
        handlers.remove(handler);
        return handlers.size();
    }

}
