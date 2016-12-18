/*
 * Copyright © 2016 Cisco Systems, Inc and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.cisco.ctao.sparkbot.core.webhooksvr;

import com.cisco.ctao.sparkbot.core.RawEventHandler;
import org.eclipse.jetty.servlet.ServletHolder;


public class RawEventHandlerReg {
    private final String handlerWebhookId;
    private final RawEventHandler handler;
    private final ServletHolder servletHolder;
    private final WebhookFilter filter;

    RawEventHandlerReg(String handlerSparkId, RawEventHandler handler, ServletHolder servletHolder,
            WebhookFilter filter) {
        this.handlerWebhookId = handlerSparkId;
        this.filter = filter;
        this.servletHolder = servletHolder;
        this.handler = handler;
    }

    RawEventHandlerReg(RawEventHandlerReg src) {
        this.handlerWebhookId = src.handlerWebhookId;
        this.filter = src.filter;
        this.servletHolder = src.servletHolder;
        this.handler = src.handler;
    }

    public String getHandlerWebhookId() {
        return handlerWebhookId;
    }

    public RawEventHandler getHandler() {
        return handler;
    }

    public ServletHolder getServletHolder() {
        return servletHolder;
    }

    public WebhookFilter getFilter() {
        return filter;
    }
}
