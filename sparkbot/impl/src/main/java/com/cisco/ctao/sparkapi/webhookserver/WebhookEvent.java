/*
 * Copyright © 2016 Cisco Systems, Inc and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.cisco.ctao.sparkapi.webhookserver;

import java.net.URL;

/** The payload of a request coming from a Spark webhook.
 * @author jmedved
 *
 */
public class WebhookEvent {
    private String id;
    private String name;
    private URL targetUrl;
    private String resource;
    private String event;
    private String orgId;
    private String appId;
    private String status;
    private String created;
    private String actorID;
    private WebhookEventData data;

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public URL getTargetUrl() {
        return targetUrl;
    }

    public String getResource() {
        return resource;
    }

    public String getEvent() {
        return event;
    }

    public String getOrgId() {
        return orgId;
    }

    public String getAppId() {
        return appId;
    }

    public String getStatus() {
        return status;
    }

    public String getCreated() {
        return created;
    }

    public String getActorID() {
        return actorID;
    }

    public WebhookEventData getData() {
        return data;
    }

    @Override
    public String toString() {
        return "\n id:        " + id
                + "\n name:      " + name
                + "\n targetURL: " + targetUrl.toString()
                + "\n resource:  " + resource
                + "\n event:     " + event
                + "\n orgId:     " + orgId
                + "\n appId:     " + appId
                + "\n actorID:   " + actorID
                + "\n status:    " + status
                + "\n created:   " + created
                + "\n event data:" + data.toString();
    }
}
