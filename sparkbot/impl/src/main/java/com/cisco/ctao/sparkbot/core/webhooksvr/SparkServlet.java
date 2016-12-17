/*
 * Copyright © 2016 Cisco Systems, Inc and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.cisco.ctao.sparkbot.core.webhooksvr;

import com.cisco.ctao.sparkbot.core.RawEventHandler;
import com.cisco.ctao.sparkbot.core.webhooksvr.RequestHeaderData.RequestHeaderDataBuilder;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** HTTP Handler for the Sparkbot app.
 * @author johnburn, jmedved
 *
 */
class SparkServlet extends HttpServlet {
    private static final long serialVersionUID = 5221908472085737227L;
    private static final Logger LOG = LoggerFactory.getLogger(SparkServlet.class);
    private final transient List<RawEventHandler> webhookHandlers = Collections.synchronizedList(new ArrayList<>());
    private final transient Gson gson = new Gson();
    private final String name;

    /** Constructor - registers a "default" logging webhook handler.
     *
     */
    SparkServlet(String name) {
        this.name = name;
        registerWebhookHandler(new LoggingWebHookHandler());
    }

    /** Register an application webhook 'raw' handler.
     * @param handler the handler to be registered
     * @param filter if specified, create a webhook in Spark with parameters
     *           as specified in the filter
     */
    public void registerWebhookHandler(RawEventHandler handler) {
        LOG.info("registerWebhookHandler '{}': handler {}, filter {}", name, handler);
        webhookHandlers.add(handler);
    }

    /** Unregister an application webhook 'raw' handler.
     * @param handler the handler to be unregistered
     */
    public void unregisterWebhookHandler(RawEventHandler handler) {
        LOG.info("unregisterWebhookHandler '{}, handler {}", name, handler);
        webhookHandlers.remove(handler);
    }

    private void methodNotAllowed(HttpServletRequest request, HttpServletResponse response) {
        try {
            response.setContentType("text/html");
            response.getWriter().println("<h1>Method '" + request.getMethod() + "' not supported. </h1>");
            response.getWriter().println("session=" + request.getSession(true).getId());
            response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        } catch (IOException e) {
            LOG.error("Servlet '{}': Could not create a response, request {}", name, request, e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        methodNotAllowed(request, response);
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        methodNotAllowed(request, response);
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        methodNotAllowed(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        LOG.info("Handler '{}' doPost: request {}", name, request);

        final String method = request.getMethod();
        final String uri = request.getRequestURI().trim();
        try {
            if (method.compareToIgnoreCase("POST") == 0 || method.compareToIgnoreCase("PUT") == 0) {
                final String payload = IOUtils.toString(request.getInputStream()).trim();
                processHttpMessage(request, uri, payload);
                response.setStatus(HttpServletResponse.SC_OK);
            } else {
                response.setContentType("text/html");
                response.getWriter().println("<h1> Method '" + method + "' not allowed. </h1>");
                response.getWriter().println("session=" + request.getSession(true).getId());
                response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            }
        } catch (IOException e) {
            LOG.error("Hanlder '{}' doPost: Could not create a response, request {}", name, request, e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    /** Parses the incoming HTTP request and calls all registered handlers with
     *  the parsed data.
     * @param request the incoming request
     * @param uri UIR for the request
     * @param payload payload from the request
     * @return true if ALL OK, return false and use the setErrorResponse routine
     */
    private boolean processHttpMessage(final HttpServletRequest request, final String uri, final String payload) {
        final RequestHeaderData headers = getRequestHeaderData(request);
        LOG.debug("Handler '{}' processHttpMessage: payload {}, uri {}", name, payload, uri);

        try {
            final WebhookEvent msg = gson.fromJson(payload, WebhookEvent.class);
            LOG.debug("processHttpMessage handling request for {} registered handler(s)",
                    webhookHandlers.size());
            for (RawEventHandler handler : webhookHandlers) {
                handler.handleWebhookEvent(msg, headers);
            }
        } catch (JsonSyntaxException e) {
            LOG.error("Handler '{}' processHttpMessage: Invalid json syntax", name, e);
            return false;
        }
        return true;
    }

    /** Creates the RequestHeaderData DTO.
     * @param request the webhook request as it came into the HTTP server
     * @return the RequestHeaderData DTO that is passed on to registered app handlers
     */
    private RequestHeaderData getRequestHeaderData(final HttpServletRequest request) {
        final RequestHeaderDataBuilder rdb = new RequestHeaderDataBuilder();
        for (Enumeration<String> e = request.getHeaderNames(); e.hasMoreElements(); ) {
            String header = e.nextElement();
            String value = request.getHeader(header);
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
                    LOG.error("Hanlder '{}' Unknown Header: {}, Value: {}", name, header, value);
                    break;
            }
        }
        return rdb.build();
    }

    /** WebHookHandler that logs all incoming requests. Registered by default
     *  when the HTTP handler is constructed.
     * @author jmedved
     *
     */
    private class LoggingWebHookHandler implements RawEventHandler {
        private final AtomicInteger eventCnt = new AtomicInteger(0);

        @Override
        public void handleWebhookEvent(final WebhookEvent msg, final RequestHeaderData requestData) {
            if (msg != null) {
                LOG.info("Handler '{}' LoggingWebHookHandler - webook event #{}: {}",
                        name, eventCnt.incrementAndGet(), msg.toString());
            } else {
                LOG.info("Handler '{}' LoggingWebHookHandler - webook event: null", name);
            }
        }
    }
}