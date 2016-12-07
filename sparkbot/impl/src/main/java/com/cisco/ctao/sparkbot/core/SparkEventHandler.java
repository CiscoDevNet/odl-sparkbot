/*
 * Copyright © 2016 Cisco Systems, Inc and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.cisco.ctao.sparkbot.core;

/** Defines the API for the Sparkbot Spark Event handler.
 * @author jmedved
 *
 * @param <T> Instantiate with one of the the following resource types:
 *          Message, Room, Membership
 */
@FunctionalInterface
public interface SparkEventHandler<T> {

    /** Defines the event type.
     * @author jmedved
     *
     */
    enum EventType {
        CREATED,
        UPDATED,
        DELETED
    }

    /** Signature for the Sparkbot Spark Event handler. Instantiate with the
     *  desired resource type (Message, Room, Membership) to receive events
     *  for the resource. Have your class implement this API and register an
     *  instance of it using the WebhookServer.registerSparkEventHandler()
     *  API. This method is called when an event for the registered resource
     *  occurs.
     * @param elementId Id of the resource for which the event occurred
     * @param element The resource that
     * @param eventType Type of event - 'created', 'updated', 'deleted'
     */
    void handleSparkEvent(String elementId, T element, EventType eventType);
}
