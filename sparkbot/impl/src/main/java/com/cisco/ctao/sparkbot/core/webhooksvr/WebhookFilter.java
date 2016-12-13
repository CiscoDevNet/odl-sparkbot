/*
 * Copyright © 2016 Cisco Systems, Inc and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.cisco.ctao.sparkbot.core.webhooksvr;

public class WebhookFilter {

    private final Events event;
    private final Resources resource;
    private final String filter;
    private final String secret;
    private final String path;

    public enum Resources {
        MESSAGES(0), ROOMS(1), MEMBERSHIPS(2), ALL(3);

        private static final String[] RESOURCES = {"messages", "rooms", "memberships", "all"};
        private final int value;

        Resources(final int value) {
            this.value = value;
        }

        private String getValue() {
            return RESOURCES[value];
        }
    }

    public enum Events {
        CREATED(0), UPDATED(1), DELETED(2), ALL(3);

        private static final String[] EVENTS = {"created", "updated", "deleted", "all"};
        private final int value;

        Events(final int value) {
            this.value = value;
        }

        private String getValue() {
            return EVENTS[value];
        }
    }


    public WebhookFilter(Events event, Resources resource, String filter, String secret, String path) {
        this.event = event;
        this.resource = resource;
        this.filter = filter;
        this.secret = secret;
        this.path = path;
    }

    public String getEvent() {
        return event.getValue();
    }


    public String getResource() {
        return resource.getValue();
    }


    public String getFilter() {
        return filter;
    }


    public String getSecret() {
        return secret;
    }


    public String getPath() {
        return path;
    }
}
