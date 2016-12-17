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
import com.cisco.ctao.sparkbot.core.Teams;
import com.cisco.ctao.sparkbot.core.TypedEventHandler;
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
public class SparkbotApiExamples {
    private static final Logger LOG = LoggerFactory.getLogger(SparkbotApiExamples.class);
    private String originalAccessToken;
    private Long originalHttpPort;
    private String originalUrlPrefix;

    /** This method is the entry point to run the Hello World example.
     * @param accessToken the access token to be used to access Spark
     * @param httpPort http port where to start the webhook server
     */
    public void run(String accessToken, Long httpPort, String urlPrefix) {
        LOG.info("*** SparkbotApiExamples run started.");

        // If specified, override the default access token in the config
        // data store
        overrideDefaultAccessToken(accessToken);
        overrideDefaultHttpPort(httpPort);
        overrideDefaultUrlPrefix(urlPrefix);

        try {
            // Register our example message event handler with Sparkbot
            SparkbotApisMsgHandler msgHandler = new SparkbotApisMsgHandler();
            WebhookServer.registerSparkEventHandler(msgHandler);
            // First, create a team
            Team team = Teams.createTeam("Team Fortress");
            logTeam("Created team...", team);
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
        } finally {
            // Restore the access token to its original configured value
            restoreDefaultUrlPrefix();
            restoreDefaultHttpPort();
            restoreDefaultAccessToken();
        }

        LOG.info("*** SparkbotApiExamples run finished.");
    }

    /** Hello World event handler for messages. Handles message events coming
     *  from Spark. Note that only events specified in the Webhoo's filter
     *  that the app created in Spark are sent to Sparkbot.
     * @author jmedved
     *
     */
    public class SparkbotApisMsgHandler implements TypedEventHandler<Message> {
        private final AtomicInteger eventCounter = new AtomicInteger();

        @Override
        public void handleSparkEvent(String elementId, Message message, TypedEventHandler.EventType eventType) {
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

    private void restoreDefaultAccessToken() {
        String overrideAccessToken = SparkClient.getLastAccessToken();
        if (this.originalAccessToken != null) {
            if (!this.originalAccessToken.equals(overrideAccessToken)) {
                SparkClient.handleAccessTokenChange(this.originalAccessToken);
            }
        } else {
            if (overrideAccessToken != null) {
                SparkClient.handleConfigParmsDelete();
            }
        }
        this.originalAccessToken = null;
    }

    private void overrideDefaultAccessToken(String accessToken) {
        this.originalAccessToken = SparkClient.getLastAccessToken();
        if (accessToken == null) {
            if (this.originalAccessToken == null) {
                LOG.error("Access token not specified - API calls will fail");
            }
        } else {
            // Override the default access token
            SparkClient.handleAccessTokenChange(accessToken);
        }
    }

    private void restoreDefaultHttpPort() {
        Long overrideHttpPort = WebhookServer.getWebhookServerPort();
        LOG.info("restoreDefaultHttpPort: overrideHttpPort {}, originalHttpPort {}",
                overrideHttpPort, this.originalHttpPort);
        if (this.originalHttpPort != null) {
            if (!this.originalHttpPort.equals(overrideHttpPort)) {
                WebhookServer.getInstance().handleHttpPortChange(originalHttpPort);
            }
        } else {
            if (overrideHttpPort != null) {
                WebhookServer.getInstance().handleHttpPortDelete();
            }
        }
        this.originalHttpPort = null;
    }

    private void restoreDefaultUrlPrefix() {
        String overrideUrlPrefix = WebhookServer.getWebhookUrlPrefix();
        LOG.info("restoreDefaultUrlPrefix: overrideUrlPrefix {}, originalUrlPrefix {}",
                overrideUrlPrefix, this.originalUrlPrefix);
        if (this.originalUrlPrefix != null) {
            if (!this.originalUrlPrefix.equals(overrideUrlPrefix)) {
                WebhookServer.getInstance().handleUrlPrefixChange(originalUrlPrefix);
            }
        } else {
            if (overrideUrlPrefix != null) {
                WebhookServer.getInstance().handleUrlPrefixDelete();
            }
        }
        this.originalUrlPrefix = null;
    }

    private void overrideDefaultHttpPort(Long overrideHttpPort) {
        this.originalHttpPort = WebhookServer.getWebhookServerPort();
        LOG.info("overrideDefaultHttpParams: overrideHttpPort {}, originalHttpPort {} ",
                overrideHttpPort, this.originalUrlPrefix);

        if (overrideHttpPort == null) {
            if (this.originalHttpPort == null) {
                LOG.error("HTTP Port not specified - webhook handlers may not "
                        + "be called if there is no existing webhook handler");
            }
        } else {
            // Override the default HTTP port and potentially restart
            // the HTTP server
            WebhookServer.getInstance().handleHttpPortChange(overrideHttpPort);
        }
    }

    private void overrideDefaultUrlPrefix(String overrideUrlPrefix) {
        this.originalUrlPrefix = WebhookServer.getWebhookUrlPrefix();
        LOG.info("overrideDefaultUrlPrefix: overrideUrlPrefix {}, originalUrlPrefix {}",
                overrideUrlPrefix, this.originalUrlPrefix);

        if (overrideUrlPrefix == null) {
            if (this.originalUrlPrefix == null) {
                LOG.warn("URL Prefix not specified - webhook handlers will not be called if a webhook is "
                        + "not already specified in Spark");
            }
        } else {
            // Override the default URL Prefix
            WebhookServer.getInstance().handleUrlPrefixChange(overrideUrlPrefix);
        }
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
