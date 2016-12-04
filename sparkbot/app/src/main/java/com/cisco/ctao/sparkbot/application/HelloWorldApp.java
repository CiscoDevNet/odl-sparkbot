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
import com.cisco.ctao.sparkbot.core.Teams;
import com.ciscospark.Message;
import com.ciscospark.Room;
import com.ciscospark.Team;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/** This class gives an example how to use Sparkbot and its APIs.
 * @author jmedved
 *
 */
public class HelloWorldApp {
    private static final Logger LOG = LoggerFactory.getLogger(HelloWorldApp.class);

    public void run(String accessToken) {
        LOG.info("HelloWorld run started.");

        // First, create a team
        Team team = Teams.createTeam("Team Fortress");
        logTeam(team);

        // Now, list all rooms for the newly created team. To get the rooms
        // for only our newly created team, we specify the team's Id as a
        // query parameter.
        List<Room> rooms = Rooms.listRooms(null, team.getId(), null);
        LOG.info("Team's '{}' rooms:", team.getName());
        for (Room room : rooms) {
            logRoom(room);
        }

        // Send a few messages to the team's room
        Message msg1 = Messages.createMessage(rooms.get(0).getId(), "Message #1");
        logMsg(msg1);

        Message msg2 = Messages.createMessage(rooms.get(0).getId(), "Message #2");
        logMsg(msg2);

        Message msg3 = Messages.createMessage(rooms.get(0).getId(), "Message #3");
        logMsg(msg3);

        // List all messages in the team's room:
        List<Message> msgs = Messages.listMessages(rooms.get(0).getId(), null, null, null, null);
        for (Message message : msgs) {
            logMsg(message);
        }

        // Delete Message #1
        Messages.deleteMessage(msg1.getId());

        // Update Room
        Room updatedRoom = Rooms.updateRoom(rooms.get(0).getId(), "Team Fortress 2");
        logRoom(updatedRoom);

        // Delete the team we created at the beginning; this deletes the
        // team's room also and all messages in it.
        Teams.deleteTeam(team.getId());

        LOG.info("HelloWorld run finished.");
    }

    private static void logMsg(Message msg) {
        LOG.info("Message id: {}, text '{}', created {}", msg.getId(), msg.getText(), msg.getCreated());
    }

    private static void logRoom(Room room) {
        LOG.info("Room: title {}, id {}, created {}", room.getTitle(), room.getId(), room.getCreated());
    }

    private static void logTeam(Team team) {
        LOG.info("Created team '{}', id: {}, created {}", team.getName(), team.getId(), team.getCreated());
    }
}
