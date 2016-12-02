/*
 * Copyright © 2016 Cisco Systems, Inc and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.cisco.ctao.sparkbot.core.testhandlers;

import com.cisco.ctao.sparkbot.core.SparkEventHandler;
import com.ciscospark.Message;

import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebhookMessageTestHandler implements SparkEventHandler<Message> {
    private static final Logger LOG = LoggerFactory.getLogger(WebhookMessageTestHandler.class);
    private final AtomicInteger eventCounter = new AtomicInteger();

    @Override
    public void handleSparkEvent(String elementId, Message message, EventType eventType) {
        LOG.info("WebhookMessageTestHandler - event #: {}, event type {}\n",
                eventCounter.incrementAndGet(), eventType);
        if (message != null) {
            LOG.info("WebhookMessageTestHandler - message:\n id {}\n "
                    + "roomId: {}\n roomType: {}\n text: '{}'\n markdown: '{}'\n "
                    + "personId: {}\n personEmail: {}\n created: {}\n mentionedPeople: {}",
                    message.getId(), message.getRoomId(), message.getRoomType(), message.getText(),
                    message.getMarkdown(), message.getPersonId(), message.getPersonEmail(),
                    message.getCreated(), message.getMentionedPeople());
        } else {
            LOG.info("WebhookMessageTestHandler - message: null");
        }
    }

    public AtomicInteger getEventCounter() {
        return eventCounter;
    }
}
