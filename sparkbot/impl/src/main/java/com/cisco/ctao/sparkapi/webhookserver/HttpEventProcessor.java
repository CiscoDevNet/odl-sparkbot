/*
 * Copyright © 2016 Cisco Systems, Inc and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.cisco.ctao.sparkapi.webhookserver;

import com.cisco.ctao.sparkapi.WebhookEventHandler;
import com.cisco.ctao.sparkapi.webhookserver.RequestHeaderData.RequestHeaderDataBuilder;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** HTTP Handler for the Sparkbot app.
 * @author johnburn, jmedved
 *
 */
class HttpEventProcessor extends AbstractHandler {
    private static final Logger LOG = LoggerFactory.getLogger(HttpEventProcessor.class);
    private final List<WebhookEventHandler> webhookHandlers = Collections.synchronizedList(new ArrayList<>());
    private final Gson gson = new Gson();

    private String response;
    private int httpRSC;

    /** Constructor - registers a "default" logging webhook handler.
     *
     */
    HttpEventProcessor() {
        registerWebhookHandler(new LoggingWebHookHandler());
    }

    /** Register an application webhook 'raw' handler.
     * @param handler: the handler to be registered
     */
    public void registerWebhookHandler(WebhookEventHandler handler) {
        LOG.info("Registering WebhookHandler {}", handler);
        webhookHandlers.add(handler);
    }

    /** Unregister an application webhook 'raw' handler.
     * @param handler: the handler to be unregistered
     */
    public void unregisterWebhookHandler(WebhookEventHandler handler) {
        LOG.info("Un-registering WebhookHandler {}", handler);
        webhookHandlers.remove(handler);
    }

    @Override
    public void handle(String target, Request baseRequest,
                       HttpServletRequest httpRequest,
                       HttpServletResponse httpResponse) throws IOException, ServletException {
        response = null;
        httpRSC = HttpServletResponse.SC_OK;
        final String method = baseRequest.getMethod().toLowerCase();
        final String uri = baseRequest.getRequestURI().trim();
        final String payload = IOUtils.toString(baseRequest.getInputStream()).trim();

        LOG.debug(">>>> handle: received http message: start");
        LOG.debug("Method: {}, URI: '{}', RemoteAddr: {}", method, uri, baseRequest.getRemoteAddr());

        if (method.compareToIgnoreCase("POST") == 0 || method.compareToIgnoreCase("PUT") == 0) {
            processHttpMessage(baseRequest, uri, payload);
        }

        sendHttpResponse(httpResponse);
        baseRequest.setHandled(true);
        LOG.debug("<<<< handle: received http message: end");
    }

    /** Parses the incoming HTTP request and calls all registered handlers with
     *  the parsed data.
     * @param baseRequest: the incoming request
     * @param uri: UIR for the request
     * @param payload: payload from the request
     * @return true if ALL OK, return false and use the setErrorResponse routine
     */
    private boolean processHttpMessage(final Request baseRequest, final String uri, final String payload) {
        final RequestHeaderData headers = getRequestHeaderData(baseRequest);
        LOG.info("payload: {}, uri {}", payload, uri);

        try {
            final WebhookEvent msg = gson.fromJson(payload, WebhookEvent.class);
            LOG.debug("processHttpMessage handling request for {} registered handler(s)",
                    webhookHandlers.size());
            for (WebhookEventHandler handler : webhookHandlers) {
                handler.handleWebhookEvent(msg, headers);
            }
        } catch (JsonSyntaxException e) {
            LOG.error("processHttpMessage: Invalid json syntax, exception {}", e);
            setErrorResponse(400, "Invalid JSON syntax");
            return false;
        }
        return true;
    }

    /** Creates the RequestHeaderData DTO.
     * @param baseRequest: the webhook request as it came into the HTTP server
     * @return: the RequestHeaderData DTO that is passed on to registered app handlers
     */
    @SuppressWarnings("unchecked")
    private RequestHeaderData getRequestHeaderData(final Request baseRequest) {
        final RequestHeaderDataBuilder rdb = new RequestHeaderDataBuilder();
        for (Enumeration<String> e = baseRequest.getHeaderNames(); e.hasMoreElements(); ) {
            String header = e.nextElement();
            String value = baseRequest.getHeader(header);
            LOG.debug("Header: {}, Value: {}", header, value);
            switch (header) {
                case "x-scheduler-task-id":
                    rdb.setSchedulerTaskId(value);
                    break;
                case "Accept":
                    rdb.setAccept(value);
                    break;
                case "User-Agent":
                    rdb.setUserAgent(value);
                    break;
                case "Content-Type":
                    rdb.setContentType(value);
                    break;
                case "x-scheduled-for":
                    rdb.setScheduledFor(value);
                    break;
                case "X-Forwarded-For":
                    rdb.setForwardedFor(value);
                    break;
                case "Content-Length":
                    rdb.setContentLenght(value);
                    break;
                case "TrackingID":
                    rdb.setTrackingId(value);
                    break;
                case "Host":
                    rdb.setHost(value);
                    break;
                default:
                    LOG.error("Unknown Header: {}, Value: {}", header, value);
                    break;
            }
        }
        return rdb.build();
    }

    private void sendHttpResponse(final HttpServletResponse httpResponse) throws IOException {
        if (response != null) {
            httpResponse.setStatus(httpRSC);
            httpResponse.getWriter().println(response);
        } else {
            httpResponse.setStatus(httpRSC);
        }
        httpResponse.setContentType("text/json;charset=utf-8");
    }

    private void setErrorResponse(final int rsc, final String content) {
        httpRSC = rsc;
        response = "{\"error\":\"" + content + "\"}";
    }

    /** WebHookHandler that logs all incoming requests. Registered by default
     *  when the HTTP handler is constructed.
     * @author jmedved
     *
     */
    private class LoggingWebHookHandler implements WebhookEventHandler {

        @Override
        public void handleWebhookEvent(final WebhookEvent msg, final RequestHeaderData requestData) {
            LOG.info("LoggingWebHookHandler - webook event: {}", msg.toString());
        }
    }
}