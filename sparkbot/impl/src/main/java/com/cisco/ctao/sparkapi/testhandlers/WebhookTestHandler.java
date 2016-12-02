/*
 * Copyright © 2016 Cisco Systems, Inc and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.cisco.ctao.sparkapi.testhandlers;

import com.cisco.ctao.sparkapi.WebhookEventHandler;
import com.cisco.ctao.sparkapi.webhookserver.RequestHeaderData;
import com.cisco.ctao.sparkapi.webhookserver.WebhookEvent;
import com.cisco.ctao.sparkapi.webhookserver.WebhookEventData;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebhookTestHandler implements WebhookEventHandler {
    private static final Logger LOG = LoggerFactory.getLogger(WebhookTestHandler.class);

    @Override
    public void handleWebhookEvent(final WebhookEvent msg, final RequestHeaderData requestData) {
        LOG.info("WebhookTestHandler event - Message:\n id: {}\n name: {}\n "
                + "targetURL: {}\n resource: {}\n event: {}\n created: {}\n "
                + "actorId: {}\n orgId: {}",
                msg.getId(), msg.getName(), msg.getTargetUrl(), msg.getResource(),
                msg.getEvent(), msg.getCreated(), msg.getActorID(), msg.getOrgId());

        final WebhookEventData data = msg.getData();
        if (data != null) {
            LOG.info("Data:\n msgId: {}\n roomId: {}\n personId: {}\n "
                    + "personEmail: {}\n created: {}",
                    data.getId(), data.getRoomId(), data.getPersonId(),
                    data.getPersonEmail(), data.getCreated());
        } else {
            LOG.info("Message data: null");
        }
    }
}
