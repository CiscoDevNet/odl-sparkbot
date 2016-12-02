/*
 * Copyright © 2016 Cisco Systems, Inc and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.cisco.ctao.sparkbot.core.testhandlers;

import com.cisco.ctao.sparkbot.core.SparkEventHandler;
import com.ciscospark.Membership;

import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebhookMembershipTestHandler implements SparkEventHandler<Membership> {
    private static final Logger LOG = LoggerFactory.getLogger(WebhookMessageTestHandler.class);
    private final AtomicInteger eventCounter = new AtomicInteger();

    @Override
    public void handleSparkEvent(String elementId, Membership membership, EventType eventType) {
        LOG.info("WebhookMembershipTestHandler - event #: {}, event type {}\n",
                eventCounter.incrementAndGet(), eventType);
        if (membership != null) {
            LOG.info("WebhookMembershipTestHandler - membership:\n id {}\n roomId: {}\n personId: {}\n text: "
                    + "'{}'\n personEmail: '{}'\n isModerator: {}\n created: {}\n ",
                    membership.getId(), membership.getRoomId(), membership.getPersonId(),
                    membership.getPersonEmail(),membership.getIsModerator(),
                    membership.getCreated());
        } else {
            LOG.info("WebhookMembershipTestHandler - membership: null");
        }
    }

    public AtomicInteger getEventCounter() {
        return eventCounter;
    }
}
