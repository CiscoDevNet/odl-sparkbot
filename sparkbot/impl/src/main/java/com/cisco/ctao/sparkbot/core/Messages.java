/*
 * Copyright © 2016 Cisco Systems, Inc and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.cisco.ctao.sparkbot.core;

import com.ciscospark.Message;
import com.google.common.base.Preconditions;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** This class uses the Spark SDK to provide an API to the Spark
 *  'Messages' service. Messages are how we communicate in a room. In Spark,
 *  each message is displayed on its own line along with a timestamp and
 *  sender information. Use this API to list, create, and delete messages.
 *
 *  <p>A message can contain plain text, rich text and file attachments.
 *
 *  <p>Just like in the Spark app, you must be a member of the room in order
 *  to target it with this API.
 *
 * @author jmedved
 *
 */
public final class Messages {
    private static final Logger LOG = LoggerFactory.getLogger(Messages.class);
    private static final SparkApi<Message> MESSAGE_API = new SparkApiImpl<>("/messages", Message.class);

    private Messages() {
        LOG.info("MessageApi created.");
    }

    /** Gets the implementation of the Message Spark API.
     * @return the message Spark API
     */
    public static SparkApi<Message> api() {
        return MESSAGE_API;
    }

    /** Get a list of messages for the specified Spark room.
     * @param roomId: Room from which to get the message
     * @param mentionedPeople: List messages for a person, by personId or me
     * @param before: List messages sent before a date and time, in ISO8601 format
     * @param beforeMessage: List messages sent before a message, by ID.
     * @param max: Limit the maximum number of messages in the response
     * @return: List of retrieved messages.
     */
    public static List<Message> listMessages(final String roomId, final String mentionedPeople,
            final String before, final String beforeMessage, final Integer max) {
        LOG.info("listMessages: roomId {}, mentionedPeople {}, before {}, beforeMessage {}, max {}",
                roomId, mentionedPeople, before, beforeMessage, max);
        Preconditions.checkArgument(roomId != null, "RoomId must be specified");

        final SparkQueryParams queryParams = new SparkQueryParams().add(SparkApi.ROOM_ID_KEY, roomId);
        if (before != null) {
            queryParams.add(SparkApi.BEFORE_KEY, before);
        }
        if (beforeMessage != null) {
            queryParams.add(SparkApi.BEFORE_MESSAGE_KEY, beforeMessage);
        }
        if (mentionedPeople != null) {
            queryParams.add(SparkApi.MENTIONED_PEOPLE_KEY, mentionedPeople);
        }
        if (max != null) {
            queryParams.add(SparkApi.MAX_KEY, max.toString());
        }
        return MESSAGE_API.list(queryParams);
    }

    /** Get details for a message from Spark.
     * @param messageId: id of the message for which details should be retrieved
     * @return message details
     */
    public static Message getMessageDetails(final String messageId) {
        LOG.info("getMessageDetails: messageId '{}'", messageId);
        return MESSAGE_API.getDetails(messageId);
    }

    /** POST a message to the Spark service.
     * @param roomId: Room to which to post the message
     * @param toPersonId: Person to which to post the message (optional)
     * @param toPersonEmail: Email of the person to which to post the
     *                       message (optional)
     * @param text: Text of the message
     * @param markdown: The message in markdown format
     * @return: the newly created message that was posted to Spark
     */
    public static Message createMessage(final String roomId, final String toPersonId,
            final String toPersonEmail, final String text, final String markdown) {
        LOG.info("createMessage: roomId {}, toPersonId {}, toPersonEmail {}, text {}, markdown {}",
                roomId, toPersonId, toPersonEmail, text, markdown);

        final Message message = new Message();
        message.setRoomId(roomId);
        message.setPersonId(toPersonId);
        message.setPersonEmail(toPersonEmail);
        message.setText(text);
        message.setMarkdown(markdown);

        return MESSAGE_API.create(message);
    }

    /** Delete a message from Spark.
     * @param messageId: id of the message to be deleted
     */
    public static void deleteMessage(final String messageId) {
        LOG.info("deleteMessqge: messageId '{}'", messageId);
        MESSAGE_API.delete(messageId);
    }
}
