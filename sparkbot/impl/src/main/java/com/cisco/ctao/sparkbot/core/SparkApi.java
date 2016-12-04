/*
 * Copyright © 2016 Cisco Systems, Inc and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.cisco.ctao.sparkbot.core;

import java.util.List;

/** A generic Java interface providing an object-oriented facade to the
 *  Spark REST 'list/getDetails/create/update/delete' interface.
 * @author jmedved
 *
 * @param <T>: element type (Message, Room, Webhook, Team, ...)
 */
public interface SparkApi<T> {

    String MAX_KEY = "max";
    String PERSON_ID_KEY = "personId";
    String TEAM_ID_KEY = "teamId";
    String ROOM_ID_KEY = "roomId";
    String PERSON_EMAIL_KEY = "personEmail";
    String ROOM_TYPE_KEY = "type";
    String BEFORE_KEY = "before";
    String BEFORE_MESSAGE_KEY = "beforeMessage";
    String MENTIONED_PEOPLE_KEY = "mentionedPeople";

    /** Send a query to Spark to list all elements of a given type. A user
     *  can refine the query by specifying a filter that limits the returned
     *  elements to those that match the filter.
     * @param queryParams: the query filter in key-value format, specific to
     *                     the element type
     * @return the list of returned elements
     */
    List<T> list(final SparkQueryParams queryParams);

    /** Gets from Spark detailed information for the specified element.
     * @param elementId the spark id of the element
     * @return detailed information about the specified element
     */
    T getDetails(final String elementId);

    /** Requests Spark to create a new element.
     * @param element Reference to an element object containing the
     *                 element data.
     * @return element, as created by Spark
     */
    T create(final T element);

    /** Requests Spark to update an existing element.
     * @param elementId the spark id of the element to be updated
     * @param element Reference to an element object containing the
     *                 element data.
     * @return element, as updated by Spark
     */
    T update(final String elementId, final T element);

    /** Requests Spark to delete an existing element.
     * @param elementId the spark id of the element to be deleted
     */
    void delete(final String elementId);
}
