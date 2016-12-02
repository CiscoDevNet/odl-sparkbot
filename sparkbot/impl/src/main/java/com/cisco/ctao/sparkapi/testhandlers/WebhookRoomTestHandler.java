/*
 * Copyright © 2016 Cisco Systems, Inc and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.cisco.ctao.sparkapi.testhandlers;

import com.cisco.ctao.sparkapi.SparkEventHandler;
import com.ciscospark.Room;

import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebhookRoomTestHandler implements SparkEventHandler<Room> {
    private static final Logger LOG = LoggerFactory.getLogger(WebhookRoomTestHandler.class);
    private final AtomicInteger eventCounter = new AtomicInteger();

    @Override
    public void handleSparkEvent(String elementId, Room room, EventType eventType) {
        LOG.info("WebhookRoomTestHandler - event #: {}, event type {}\n",
                eventCounter.incrementAndGet(), eventType);
        if (room != null) {
            LOG.info("WebhookRoomTestHandler - room\n id: {}\n title: {}\n "
                    + "teamId: {}\n created: {}\n lastActivity: {}\n isLocked {}",
                    room.getId(), room.getTitle(), room.getTeamId(),
                    room.getCreated(), room.getLastActivity(), room.getIsLocked());
        } else {
            LOG.info("WebhookRoomTestHandler - room: null");
        }
    }

    public AtomicInteger getEventCounter() {
        return eventCounter;
    }
}