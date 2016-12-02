/*
 * Copyright © 2016 Cisco Systems, Inc and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.cisco.ctao.sparkbot.odladapter;

import com.cisco.ctao.sparkbot.core.SparkApi;
import com.cisco.ctao.sparkbot.core.SparkQueryParams;
import com.ciscospark.SparkException;

import java.util.ArrayList;
import java.util.List;

import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.sparkbot.commons.rev161110.ReturnCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class SparkProxyService<T, S, L> {
    private static final Logger LOG = LoggerFactory.getLogger(SparkProxyService.class);
    private final SparkApi<S> sparkApi;

    SparkProxyService(SparkApi<S> sparkApi) {
        this.sparkApi = sparkApi;
    }

    protected abstract T translate(final S sparkElement);

    protected abstract L buildListElement(final S sparkElement);

    public SparkProxyService<T, S, L>.ReturnValue list(final SparkQueryParams queryParams) {
        final List<L> elemList = new ArrayList<>();
        try {
            for (final S element : sparkApi.list(queryParams)) {
                elemList.add(buildListElement(element));
            }
            return new ReturnValue(elemList, null, ReturnCode.OK, null);
        } catch (SparkException e) {
            LOG.error("{}", e);
            return new ReturnValue(null, null, ReturnCode.SPARKOPERERROR, e.getMessage());
        }
    }

    public SparkProxyService<T, S, L>.ReturnValue getDetails(final String elementId) {
        try {
            final S element = sparkApi.getDetails(elementId);
            return new ReturnValue(null, translate(element), ReturnCode.OK, null);
        } catch (SparkException e) {
            LOG.error("{}", e);
            return new ReturnValue(null, null, ReturnCode.SPARKOPERERROR, e.getMessage());
        }
    }

    public SparkProxyService<T, S, L>.ReturnValue create(final S elementIn) {
        try {
            final S elementOut = sparkApi.create(elementIn);
            return new ReturnValue(null, translate(elementOut), ReturnCode.OK, null);
        } catch (SparkException e) {
            LOG.error("{}", e);
            return new ReturnValue(null, null, ReturnCode.SPARKOPERERROR, e.getMessage());
        }
    }

    public SparkProxyService<T, S, L>.ReturnValue update(final String elementId, final S elementIn) {
        try {
            final S elementOut = sparkApi.update(elementId, elementIn);
            return new ReturnValue(null, translate(elementOut), ReturnCode.OK, null);
        } catch (SparkException e) {
            LOG.error("{}", e);
            return new ReturnValue(null, null, ReturnCode.SPARKOPERERROR, e.getMessage());
        }
    }

    public SparkProxyService<T, S, L>.ReturnValue delete(final String elementId) {
        try {
            sparkApi.delete(elementId);
            return new ReturnValue(null, null, ReturnCode.OK, null);
        } catch (SparkException e) {
            LOG.error("{}", e);
            return new ReturnValue(null, null, ReturnCode.SPARKOPERERROR, e.getMessage());
        }
    }

    public class ReturnValue {
        private final List<L> list;
        private final T element;
        private final ReturnCode retCode;
        private final String errorMessage;

        private ReturnValue(final List<L> list, final T element, final ReturnCode retCode, final String errorMessage) {
            this.list = list;
            this.element = element;
            this.retCode = retCode;
            this.errorMessage = errorMessage;
        }

        public List<L> getList() {
            return list;
        }

        public T getElement() {
            return element;
        }

        public ReturnCode getRetCode() {
            return retCode;
        }

        public String getErrorMessage() {
            return errorMessage;
        }
    }
}
