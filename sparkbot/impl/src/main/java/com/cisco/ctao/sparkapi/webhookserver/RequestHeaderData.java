/*
 * Copyright © 2016 Cisco Systems, Inc and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.cisco.ctao.sparkapi.webhookserver;

/** Immutable DTO that carries the information from headers on a POST
 *  request coming from a Spark webhook.
 * @author jmedved
 *
 */
public final class RequestHeaderData {
    private final String accept;
    private final String schedulerTaskId;
    private final String contentType;
    private final String scheduledFor;
    private final String forwardedFor;
    private final String contentLenght;
    private final String trackingId;
    private final String host;
    private final String userAgent;

    /** Constructor for the immutable RequestHeaderData DTO.
     * @param requestHeaderDataBuilder: a builder for the RequestHeaderData DTO
     */
    private RequestHeaderData(final RequestHeaderDataBuilder rqstHdrDataBuilder) {
        this.accept = rqstHdrDataBuilder.accept;
        this.schedulerTaskId = rqstHdrDataBuilder.schedulerTaskId;
        this.contentType = rqstHdrDataBuilder.contentType;
        this.scheduledFor = rqstHdrDataBuilder.scheduledFor;
        this.forwardedFor = rqstHdrDataBuilder.forwardedFor;
        this.contentLenght = rqstHdrDataBuilder.contentLenght;
        this.trackingId = rqstHdrDataBuilder.trackingId;
        this.host = rqstHdrDataBuilder.host;
        this.userAgent = rqstHdrDataBuilder.userAgent;
    }

    public String getAccept() {
        return accept;
    }

    public String getSchedulerTaskId() {
        return schedulerTaskId;

    }

    public String getContentType() {
        return contentType;
    }

    public String getScheduledFor() {
        return scheduledFor;
    }

    public String getForwardedFor() {
        return forwardedFor;
    }

    public String getContentLenght() {
        return contentLenght;
    }

    public String getTrackingId() {
        return trackingId;
    }

    public String getHost() {
        return host;
    }

    public String getUserAgent() {
        return userAgent;
    }

    /** Builder class for the RequestHeaderData DTO.
     * @author jmedved
     */
    public static class RequestHeaderDataBuilder {
        private String accept;          // content of the 'Accept' header
        private String schedulerTaskId; // content of the 'User-Agent' header
        private String contentType;     // content of the 'Content-Type' header
        private String scheduledFor;    // content of the 'x-scheduled-for' header
        private String forwardedFor;    // content of the 'X-Forwarded-For' header
        private String contentLenght;   // content of the 'Content-Length' header
        private String trackingId;      // content of the 'TrackingID' header
        private String host;            // content of the 'Host' header
        private String userAgent;       // content of the 'User-Agent' header

        /** This method builds the RequestHeaderData DTO using its constructor.
         * @return: RequestHeaderData DTO
         */
        public RequestHeaderData build() {
            return new RequestHeaderData(this);
        }

        public RequestHeaderDataBuilder setAccept(String accept) {
            this.accept = accept;
            return this;
        }

        public RequestHeaderDataBuilder setSchedulerTaskId(String schedulerTaskId) {
            this.schedulerTaskId = schedulerTaskId;
            return this;
        }

        public RequestHeaderDataBuilder setContentType(String contentType) {
            this.contentType = contentType;
            return this;
        }

        public RequestHeaderDataBuilder setScheduledFor(String scheduledFor) {
            this.scheduledFor = scheduledFor;
            return this;
        }

        public RequestHeaderDataBuilder setForwardedFor(String forwardedFor) {
            this.forwardedFor = forwardedFor;
            return this;
        }

        public RequestHeaderDataBuilder setContentLenght(String contentLenght) {
            this.contentLenght = contentLenght;
            return this;
        }

        public RequestHeaderDataBuilder setTrackingId(String trackingId) {
            this.trackingId = trackingId;
            return this;
        }

        public RequestHeaderDataBuilder setHost(String host) {
            this.host = host;
            return this;
        }

        public RequestHeaderDataBuilder setUserAgent(String userAgent) {
            this.userAgent = userAgent;
            return this;
        }
    }
}
