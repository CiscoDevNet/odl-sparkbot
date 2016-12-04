/*
 * Copyright © 2016 Cisco Systems, Inc and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.cisco.ctao.sparkbot.core;

import com.ciscospark.Webhook;

import java.net.URI;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** This class uses the Spark SDK to provides an API to  the Spark
 *  'Webhooks' service. Webhooks allow your app to be notified via HTTP when
 *  a specific event occurs on Spark. For example, your app can register a
 *  webhook to be notified when a new message is posted into a specific room.
 *
 *  <p>Events trigger in near real-time allowing your app and backend IT
 *  systems to stay in sync with new content and room activity. Check the
 *  Webhooks Guide and our blog regularly for announcements of additional
 *  webhook resources and event types.
 *
 *  <p>Webhooks created via this API will not appear in a room's
 *  'Integrations' list within the Spark client.
 *
 * @author jmedved
 *
 */
public final class Webhooks {
    private static final Logger LOG = LoggerFactory.getLogger(Webhooks.class);
    private static final SparkApi<Webhook> WEBHOOK_API = new SparkApiImpl<>("/webhooks", Webhook.class);

    private Webhooks() {
        LOG.info("WebhookApi created.");
    }

    /** Gets the implementation of the Webhooks Spark API.
     * @return the Webhooks Spark API
     */
    public static SparkApi<Webhook> api() {
        return WEBHOOK_API;
    }

    /** Get a list of webhooks for the user whose authentications we're using.
     * @param max max number of webhooks to returns; null if not specified
     * @return List of user's webooks
     */
    public static List<Webhook> listWebhooks(final Integer max) {
        LOG.info("listWebhooks: max {}", max);

        SparkQueryParams queryParams = new SparkQueryParams();
        if (max != null) {
            queryParams.add(SparkApi.MAX_KEY, max.toString());
        }
        return WEBHOOK_API.list(queryParams);
    }

    public static Webhook getWebhookDetails(String webhookId) {
        LOG.info("getWebhookDetails: webhookId {}", webhookId);
        return WEBHOOK_API.getDetails(webhookId);
    }


    public static Webhook createWebhook(String name, URI targetURL, String resource,
            String event, String filter, String secret) {
        LOG.info("createWebhook: name {}, targetURL: {}, resource: {}, event: {}, "
                + "filter: {}, secret: {}",
                name, targetURL, resource, event, filter, secret);
        final Webhook webhook = new Webhook();

        // Do not check whether mandatory arguments are present - let the
        // Spark service determine whether it has all that it needs to create
        // a webhook.
        webhook.setName(name);
        webhook.setResource(resource);
        webhook.setEvent(event);
        webhook.setTargetUrl(targetURL);
        webhook.setFilter(filter);
        return WEBHOOK_API.create(webhook);
    }

    public static Webhook updateWebhook(String webhookId, String name, URI targetUrl) {
        LOG.info("updateWebhook: webhookId {}", webhookId);

        final Webhook webhook = new Webhook();
        webhook.setName(name);
        webhook.setTargetUrl(targetUrl);
        return WEBHOOK_API.update(webhookId, webhook);
    }

    public static void deleteWebhook(String webhookId) {
        LOG.info("deleteWebhook: webhookId {}", webhookId);
        WEBHOOK_API.delete(webhookId);
    }
}
