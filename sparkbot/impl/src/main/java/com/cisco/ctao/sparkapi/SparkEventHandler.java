/*
 * Copyright © 2016 Cisco Systems, Inc and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.cisco.ctao.sparkapi;

public interface SparkEventHandler<T> {

    enum EventType {
        Created,
        Updated,
        Deleted
    }

    void handleSparkEvent(String elementId, T element, EventType eventType);
}
