/*
 * Copyright © 2016 Cisco Systems, Inc and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.cisco.ctao.sparkbot.application;

import com.cisco.ctao.sparkbot.core.Messages;
import com.cisco.ctao.sparkbot.core.Rooms;
import com.cisco.ctao.sparkbot.core.SparkClient;
import com.cisco.ctao.sparkbot.core.SparkEventHandler;
import com.cisco.ctao.sparkbot.core.Teams;
import com.cisco.ctao.sparkbot.core.webhooksvr.WebhookServer;
import com.ciscospark.Message;
import com.ciscospark.Room;
import com.ciscospark.Team;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/** This class gives an example how to use Sparkbot and its APIs.
 * @author jmedved
 *
 */
public class HelloWorldApp {
    private static final Logger LOG = LoggerFactory.getLogger(HelloWorldApp.class);
    private String accessToken;

    /** This method is the entry point to run the Hello World example.
     * @param accessToken the access token to be used to access Spark
     */
    public void run(String accessToken) {
        LOG.info("*** HelloWorld run started.");

        setTempAccessToken(accessToken);

        // Register our example message event handler with Sparkbot
        HelloWorldMsgHandler msgHandler = new HelloWorldMsgHandler();
        WebhookServer.registerSparkEventHandler(msgHandler);

        // First, create a team
        Team team = Teams.createTeam("Team Fortress");
        logTeam("Created team", team);

        // Now, list all rooms for the newly created team. To get the rooms
        // for only our newly created team, we specify the team's Id as a
        // query parameter.
        List<Room> rooms = Rooms.listRooms(null, team.getId(), null);
        LOG.info("Team's '{}' rooms:", team.getName());
        for (Room room : rooms) {
            logRoom(" -", room);
        }

        // Send a few messages to the team's room
        Message msg1 = Messages.createMessage(rooms.get(0).getId(), "Message #1");
        logMsg("Sent Message #1...", msg1);
        Message msg2 = Messages.createMessage(rooms.get(0).getId(), "Message #2");
        logMsg("Sent Message #2...", msg2);
        Message msg3 = Messages.createMessage(rooms.get(0).getId(), "Message #3");
        logMsg("Sent Message #3...", msg3);

        // List all messages in the team's room:
        List<Message> msgs = Messages.listMessages(rooms.get(0).getId(), null, null, null, null);
        for (Message message : msgs) {
            logMsg(" -", message);
        }

        // Delete Message #3
        Messages.deleteMessage(msg1.getId());
        logMsg("Deleted Message #3...", msg3);

        // Update Room
        Room updatedRoom = Rooms.updateRoom(rooms.get(0).getId(), "Team Fortress 2");
        logRoom("Updated room...", updatedRoom);

        // Unregister our example message event handler
        WebhookServer.unregisterSparkEventHandler(msgHandler);

        // Delete the team we created at the beginning; this deletes the
        // team's room also and all messages in it.
        Teams.deleteTeam(team.getId());
        logTeam("Deleted team...", team);

        resetTempAccessToken();

        LOG.info("*** HelloWorld run finished.");
    }

    /** Hello World event handler for messages. Handles message events coming
     *  from Spark. Note that only events specified in the Webhoo's filter
     *  that the app created in Spark are sent to Sparkbot.
     * @author jmedved
     *
     */
    public class HelloWorldMsgHandler implements SparkEventHandler<Message> {
        private final AtomicInteger eventCounter = new AtomicInteger();

        @Override
        public void handleSparkEvent(String elementId, Message message, SparkEventHandler.EventType eventType) {
            LOG.info("handleSparkEvent - event #: {}, event type: {}",
                    eventCounter.incrementAndGet(), eventType);
            if (message != null) {
                LOG.info("handleSparkEvent - message: id {}, roomId: {}, roomType: {}, text: '{}, "
                        + "markdown: '{}', personId: {}, personEmail: {}, created: {}, mentionedPeople: {}",
                        message.getId(), message.getRoomId(), message.getRoomType(), message.getText(),
                        message.getMarkdown(), message.getPersonId(), message.getPersonEmail(),
                        message.getCreated(), message.getMentionedPeople());
            } else {
                LOG.info("handleSparkEvent - message: null");
            }
        }
    }

    private void resetTempAccessToken() {
        SparkClient.handleAccessTokenChange(this.accessToken);
    }

    private void setTempAccessToken(String accessToken) {
        this.accessToken = SparkClient.getLastAccessToken();
        SparkClient.handleAccessTokenChange(accessToken);
    }

    private static void logMsg(String intro, Message msg) {
        LOG.info("{} message id: {}, text: '{}', created: {}",
                intro, msg.getId(), msg.getText(), msg.getCreated());
    }

    private static void logRoom(String intro, Room room) {
        LOG.info("{} room title: {}, id: {}, created: {}",
                intro, room.getTitle(), room.getId(), room.getCreated());
    }

    private static void logTeam(String intro, Team team) {
        LOG.info("{} team: '{}', id: {}, created: {}",
                intro, team.getName(), team.getId(), team.getCreated());
    }
}
