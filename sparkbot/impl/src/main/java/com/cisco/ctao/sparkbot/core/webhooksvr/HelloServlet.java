/*
 * Copyright © 2016 Cisco Systems, Inc and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.cisco.ctao.sparkbot.core.webhooksvr;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HelloServlet extends HttpServlet {
    private static final Logger LOG = LoggerFactory.getLogger(HelloServlet.class);
    private static final long serialVersionUID = 4962487983534433213L;
    private String greeting = "Hello World";

    public HelloServlet(){

    }

    public HelloServlet(String greeting) {
        this.greeting = greeting;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        LOG.info("doGet: request {}", request);
        LOG.info("Request method: {}", request.getMethod());
        response.setContentType("text/html");
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().println("<h1>" + greeting + "</h1>");
        response.getWriter().println("session=" + request.getSession(true).getId());
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        LOG.info("doGet: request {}", request);

    }
}
