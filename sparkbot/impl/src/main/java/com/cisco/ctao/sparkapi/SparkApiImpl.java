/*
 * Copyright © 2016 Cisco Systems, Inc and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.cisco.ctao.sparkapi;

import com.ciscospark.NotAuthenticatedException;
import com.ciscospark.RequestBuilder;
import com.ciscospark.SparkException;
import com.google.common.base.Preconditions;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SparkApiImpl<T> implements SparkApi<T> {
    private static final Logger LOG = LoggerFactory.getLogger(SparkApiImpl.class);
    private static final String MISSING_ELEMENT_ID = "elementId must be specified";
    private static final String MISSING_ELEMENT = "element must be specified";
    private static final String AUTHENTICATION_ERR_MSG = "Request authentication failure";
    private final String path;
    private final Class<T> apiType;


    SparkApiImpl(final String path, final Class<T> apiType) {
        this.path = path;
        this.apiType = apiType;
    }

    @Override
    public List<T> list(final SparkQueryParams queryParams) {
        LOG.info("list: queryParams {}", queryParams);
        final RequestBuilder<T> builder = SparkClient
                .getSpark()
                .getRequestBuilder(apiType, path);

        if (queryParams != null) {
            for (String[] kv : queryParams.getParams()) {
                builder.queryParam(kv[0], kv[1]);
            }
        }
        final List<T> elements = new ArrayList<>();
        try {
            builder.iterate()
                .forEachRemaining(elem -> {
                    elements.add(elem);
                });
            LOG.info("Elements retrieved: {}", elements.size());
        } catch (NotAuthenticatedException e) {
            throw new SparkException(AUTHENTICATION_ERR_MSG, e.getCause());
        }
        return elements;
    }

    @Override
    public T getDetails(final String elementId) {
        LOG.info("getDetails<{}>: elementId {}", apiType, elementId);
        Preconditions.checkArgument(elementId != null, MISSING_ELEMENT_ID);
        try {
            return SparkClient
                    .getSpark()
                    .getRequestBuilder(apiType, path)
                    .path("/" + elementId)
                    .get();
        } catch (NotAuthenticatedException e) {
            throw new SparkException(AUTHENTICATION_ERR_MSG, e.getCause());
        }
    }

    @Override
    public T update(final String elementId, final T element) {
        LOG.info("update<{}>: elementId {}, element {}", apiType, elementId, element);
        Preconditions.checkArgument(elementId != null, MISSING_ELEMENT_ID);
        Preconditions.checkArgument(element != null, MISSING_ELEMENT);
        try {
            return SparkClient
                    .getSpark()
                    .getRequestBuilder(apiType, path)
                    .path("/" + elementId)
                    .put(element);
        } catch (NotAuthenticatedException e) {
            throw new SparkException(AUTHENTICATION_ERR_MSG, e.getCause());
        }

    }

    @Override
    public void delete(final String elementId) {
        LOG.info("delete<{}>: elementId {}", apiType, elementId);
        Preconditions.checkArgument(elementId != null, MISSING_ELEMENT_ID);
        try {
            SparkClient
                    .getSpark()
                    .getRequestBuilder(apiType, path)
                    .path("/" + elementId)
                    .delete();
        } catch (NotAuthenticatedException e) {
            throw new SparkException(AUTHENTICATION_ERR_MSG, e.getCause());
        }
    }

    @Override
    public T create(final T element) {
        LOG.info("create<{}>: element {}", apiType, element);
        Preconditions.checkArgument(element != null, MISSING_ELEMENT);
        try {
            return SparkClient
                    .getSpark()
                    .getRequestBuilder(apiType, path)
                    .post(element);
        } catch (NotAuthenticatedException e) {
            throw new SparkException(AUTHENTICATION_ERR_MSG, e.getCause());
        }
    }
}
