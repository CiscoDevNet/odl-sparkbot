/*
 * Copyright © 2016 Cisco Systems, Inc and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.cisco.ctao.sparkapi.webhookserver;

public class WebhookEventData {
    private String id;
    private String roomId;
    private String personId;
    private String personEmail;
    private String created;

    public String getId() {
        return id;
    }

    public String getRoomId() {
        return roomId;
    }

    public String getPersonId() {
        return personId;
    }

    public String getPersonEmail() {
        return personEmail;
    }

    public String getCreated() {
        return created;
    }

    @Override
    public String toString() {
        return    "\n   +-> id:          " + id
                + "\n   +-> roomId:      " + roomId
                + "\n   +-> personId:    " + personId
                + "\n   +-> personEmail: " + personEmail
                + "\n   +-> created:     " + created;
    }
}
