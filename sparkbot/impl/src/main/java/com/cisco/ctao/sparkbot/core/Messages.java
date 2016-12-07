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

import java.net.URI;
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
     * @param roomId List messages for a room, by ID; mandatory, must be
     *          specified
     * @param mentionedPeople List messages where the caller is mentioned by
     *          specifying "me" or the caller personId; null if not specified
     * @param before List messages sent before a date and time, in ISO8601
     *          format; null if not specified
     * @param beforeMessage List messages sent before a message, by ID; null
     *          if not specified
     * @param max Limit the maximum number of messages in the response; null
     *          if not specified
     * @return List of retrieved messages.
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
     * @param messageId id of the message for which details should be retrieved
     * @return message details
     */
    public static Message getMessageDetails(final String messageId) {
        LOG.info("getMessageDetails: messageId '{}'", messageId);
        return MESSAGE_API.getDetails(messageId);
    }

    /** Send a message to a room.
     * @param roomId Room to which to post the message
     * @param text Text of the message
     * @return the newly created message that was posted to the room
     */
    public static Message createMessage(final String roomId, final String text) {
        return createMessage(roomId, null, null, text, null, null);
    }

            /** Send a message to a room or a person. One of roomId, personId or
     *  personEmail must not be null
     * @param roomId Room to which to post the message; mandatory
     * @param toPersonId Person to which to post the message; null if not
     *              specified.
     * @param toPersonEmail Email of the person to which to post the
     *              message (optional); null if not specified
     * @param text Text of the message; null if not specified
     * @param markdown The message in markdown format; null if not specified
     * @param files A URL reference for the message attachment. See the
     *          Content and Attachments Guide for the supported media types.
     * @return the newly created message that was posted to Spark
     */
    public static Message createMessage(final String roomId, final String toPersonId,
            final String toPersonEmail, final String text, final String markdown,
            final URI files) {
        LOG.info("createMessage: roomId {}, toPersonId {}, toPersonEmail {}, text {}, "
                + "markdown {}, files {}",
                roomId, toPersonId, toPersonEmail, text, markdown, files);

        final Message message = new Message();
        message.setRoomId(roomId);
        message.setPersonId(toPersonId);
        message.setPersonEmail(toPersonEmail);
        message.setText(text);
        message.setMarkdown(markdown);
        message.setFiles(files);

        return MESSAGE_API.create(message);
    }

    /** Delete a message from Spark.
     * @param messageId id of the message to be deleted
     */
    public static void deleteMessage(final String messageId) {
        LOG.info("deleteMessqge: messageId '{}'", messageId);
        MESSAGE_API.delete(messageId);
    }
}
