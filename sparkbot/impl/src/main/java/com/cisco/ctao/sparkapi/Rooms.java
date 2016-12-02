/*
 * Copyright © 2016 Cisco Systems, Inc and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package com.cisco.ctao.sparkapi;
import com.ciscospark.Room;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** This class uses the Spark SDK to provide an API to the Spark
 *  'Rooms' service. Rooms are virtual meeting places where people post
 *  messages and collaborate to get work done. This API is used to manage
 *  the rooms themselves. Rooms are create and deleted with this API.
 *  You can also update a room to change its title, for example.
 *
 *  <p>To create a team room, specify the a teamId in POST payload. Note
 *  that once a room is added to a team, it cannot be moved. To learn more
 *  about managing teams, see the Teams API.
 *
 *  <p>To manage people in a room see the Memberships API.
 *
 *  <p>To post content see the Messages API
 *
 * @author jmedved
 *
 */
public final class Rooms {
    private static final Logger LOG = LoggerFactory.getLogger(Rooms.class);
    private static final SparkApi<Room> ROOM_API = new SparkApiImpl<>("/rooms", Room.class);

    private Rooms() {
        LOG.info("RoomApi created.");
    }

    /** Gets the implementation of the Room Spark API.
     * @return the Room Spark API
     */
    public static SparkApi<Room> api() {
        return ROOM_API;
    }

    /** Get form the Spark service all rooms for the an access token.
     * @param max: Query parameter - maximum number of entries to return
     *             (doesn't work)
     * @param teamId: Query parameter - team ID
     * @param roomType: Query parameter - room type
     * @return: List of all rooms for the specified access token
     */
    public static List<Room> listRooms(final Integer max, final String teamId, final String roomType) {
        LOG.info("listRooms: max {}, teamId '{}', roomType '{}'", max, teamId, roomType);

        final SparkQueryParams queryParams = new SparkQueryParams();
        if (max != null) {
            queryParams.add(SparkApi.MAX_KEY, Integer.toString(max));
        }
        if (roomType != null) {
            queryParams.add(SparkApi.ROOM_TYPE_KEY, roomType);
        }
        if (teamId != null) {
            queryParams.add(SparkApi.TEAM_ID_KEY, teamId);
        }
        return ROOM_API.list(queryParams);
    }

    /** Get from the Spark service the details for the specified room.
     * @param roomId: room id for which to get the details
     * @return: details for the specified room
     */
    public static Room getRoomDetails(final String roomId) {
        LOG.info("getRoomDetails: roomId '{}'", roomId);
        return ROOM_API.getDetails(roomId);
    }

    /** Create a room in the Spark service.
     * @param title: Title for the room
     * @param teamId: Optional team ID for the room
     * @return: created Room object
     */
    public static Room createRoom(final String title, final String teamId) {
        LOG.info("createRoom: title '{}', teamId '{}'", title, teamId);
        final Room room = new Room();
        room.setTitle(title);
        room.setTeamId(teamId);
        return ROOM_API.create(room);
    }

    /** Update an existing room in the Spark service.
     * @param roomId: RoomId for the room
     * @param title: Title for the room
     * @return: updated Room object
     */
    public static Room updateRoom(final String roomId, final String title) {
        LOG.info("createRoom: roomId '{}', title '{}'", roomId, title);
        final Room room = new Room();
        room.setTitle(title);
        return ROOM_API.update(roomId, room);
    }

    /** Delete a room from the Spark service.
     * @param roomId: room id for the room to be deleted
     */
    public static void deleteRoom(final String roomId) {
        LOG.info("deleteRoom: roomId '{}'", roomId);
        ROOM_API.delete(roomId);
    }
}
