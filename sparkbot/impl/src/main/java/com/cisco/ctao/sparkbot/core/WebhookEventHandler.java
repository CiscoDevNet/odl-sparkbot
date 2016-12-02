/*
 * Copyright © 2016 Cisco Systems, Inc and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.cisco.ctao.sparkbot.core;

import com.cisco.ctao.sparkbot.core.webhooksvr.RequestHeaderData;
import com.cisco.ctao.sparkbot.core.webhooksvr.WebhookEvent;

/** Interface that defines the handling of webhook events in a Sparkbot app.
 * @author jmedved
 *
 */
@FunctionalInterface
public interface WebhookEventHandler {
    /** Method signature to be implemented by an app that wishes to receive.
     *  'raw' webhook events
     * @param msg: The message received from Spark
     * @param requestData: values of various headers present in the request
     */
    void handleWebhookEvent(WebhookEvent msg, RequestHeaderData requestData);
}
