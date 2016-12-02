/*
 * Copyright © 2016 Cisco Systems, Inc and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.cisco.ctao.sparkapi;

import java.util.ArrayList;
import java.util.List;

public class SparkQueryParams {
    private final List<String[]> params = new ArrayList<>();

    public SparkQueryParams add(final String key, final String value) {
        params.add(new String[] {key, value});
        return this;
    }

    public List<String[]> getParams() {
        return params;
    }
}
