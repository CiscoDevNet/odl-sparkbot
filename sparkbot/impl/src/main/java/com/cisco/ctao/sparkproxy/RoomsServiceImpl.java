/*
 * Copyright © 2016 Cisco Systems, Inc and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.cisco.ctao.sparkproxy;

import com.cisco.ctao.sparkapi.Rooms;
import com.cisco.ctao.sparkapi.SparkApi;
import com.cisco.ctao.sparkapi.SparkQueryParams;
import com.ciscospark.Room;
import com.google.common.base.Preconditions;

import java.util.concurrent.Future;

import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.sparkbot.rooms.rev161110.CreateRoomInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.sparkbot.rooms.rev161110.CreateRoomOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.sparkbot.rooms.rev161110.CreateRoomOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.sparkbot.rooms.rev161110.DeleteRoomInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.sparkbot.rooms.rev161110.DeleteRoomOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.sparkbot.rooms.rev161110.DeleteRoomOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.sparkbot.rooms.rev161110.GetRoomDetailsInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.sparkbot.rooms.rev161110.GetRoomDetailsOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.sparkbot.rooms.rev161110.GetRoomDetailsOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.sparkbot.rooms.rev161110.ListRoomsInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.sparkbot.rooms.rev161110.ListRoomsOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.sparkbot.rooms.rev161110.ListRoomsOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.sparkbot.rooms.rev161110.SparkbotRoomsService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.sparkbot.rooms.rev161110.UpdateRoomInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.sparkbot.rooms.rev161110.UpdateRoomOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.sparkbot.rooms.rev161110.UpdateRoomOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.sparkbot.rooms.rev161110.list.rooms.input.QueryParameters;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.sparkbot.rooms.rev161110.list.rooms.output.SparkbotRooms;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.sparkbot.rooms.rev161110.list.rooms.output.SparkbotRoomsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.sparkbot.rooms.rev161110.room.SparkbotRoom;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.sparkbot.rooms.rev161110.room.SparkbotRoomBuilder;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** This class implements a yang-defined proxy-service to the Spark 'Rooms' API.
 * @author jmedved
 *
 */
public class RoomsServiceImpl
    extends SparkProxyService<SparkbotRoom, Room, SparkbotRooms>
    implements SparkbotRoomsService {

    private static final Logger LOG = LoggerFactory.getLogger(RoomsServiceImpl.class);
    private static final String MISSING_ROOM_ID = "Missing Room Id";
    private static final String MISSING_ROOM_DATA = "Missing Room data.";

    private static SparkQueryParams getQueryParameters(final ListRoomsInput input) {
        final SparkQueryParams queryParams = new SparkQueryParams();
        QueryParameters qp;
        if (input != null && (qp = input.getQueryParameters()) != null) {
            if (qp.getTeamId() != null) {
                queryParams.add(SparkApi.TEAM_ID_KEY, qp.getTeamId());
            }
            if (qp.getRoomType() != null) {
                queryParams.add(SparkApi.ROOM_TYPE_KEY, qp.getRoomType());
            }
            if (qp.getMax() != null) {
                queryParams.add(SparkApi.MAX_KEY, qp.getMax().toString());
            }
        }
        return queryParams;
    }

    @Override
    protected SparkbotRoom translate(final Room room) {
        return new SparkbotRoomBuilder()
                .setId(room.getId())
                .setIsLocked(room.getIsLocked())
                .setLastActivity(room.getLastActivity().toString())
                .setTitle(room.getTitle())
                .setTeamId(room.getTeamId())
                .setCreated(room.getCreated().toString())
                .build();
    }

    @Override
    protected SparkbotRooms buildListElement(final Room room) {
        return new SparkbotRoomsBuilder()
                .setSparkbotRoom(translate(room))
                .build();
    }

    public RoomsServiceImpl() {
        super(Rooms.api());
    }

    @Override
    public Future<RpcResult<ListRoomsOutput>> listRooms(final ListRoomsInput input) {
        LOG.info("getRooms input: {}", input);

        final SparkProxyService<SparkbotRoom, Room, SparkbotRooms>.ReturnValue result =
                list(getQueryParameters(input));
        return RpcResultBuilder.success(new ListRoomsOutputBuilder()
                .setSparkbotRooms(result.getList())
                .setReturnStatus(result.getRetCode())
                .setErrorMessage(result.getErrorMessage()).build()).buildFuture();
    }

    @Override
    public Future<RpcResult<GetRoomDetailsOutput>> getRoomDetails(final GetRoomDetailsInput input) {
        LOG.info("getRoomDetails input: {}", input);
        Preconditions.checkArgument(input != null, MISSING_ROOM_ID);

        final SparkProxyService<SparkbotRoom, Room, SparkbotRooms>.ReturnValue result =
                getDetails(input.getRoomId());
        return RpcResultBuilder.success(new GetRoomDetailsOutputBuilder()
                .setSparkbotRoom(result.getElement())
                .setReturnStatus(result.getRetCode())
                .setErrorMessage(result.getErrorMessage()).build()).buildFuture();
    }

    @Override
    public Future<RpcResult<CreateRoomOutput>> createRoom(final CreateRoomInput input) {
        LOG.info("createRoom input: {}", input);
        Preconditions.checkArgument(input != null, MISSING_ROOM_DATA);

        final Room room = new Room();
        room.setTitle(input.getTitle());
        room.setTeamId(input.getTeamId());

        final SparkProxyService<SparkbotRoom, Room, SparkbotRooms>.ReturnValue result =
                create(room);
        return RpcResultBuilder.success(new CreateRoomOutputBuilder()
                .setSparkbotRoom(result.getElement())
                .setReturnStatus(result.getRetCode())
                .setErrorMessage(result.getErrorMessage()).build()).buildFuture();
    }

    @Override
    public Future<RpcResult<UpdateRoomOutput>> updateRoom(final UpdateRoomInput input) {
        LOG.info("createRoom input: {}", input);
        Preconditions.checkArgument(input != null, MISSING_ROOM_ID);

        final Room room = new Room();
        room.setTitle(input.getTitle());

        final SparkProxyService<SparkbotRoom, Room, SparkbotRooms>.ReturnValue result =
                update(input.getRoomId(), room);
        return RpcResultBuilder.success(new UpdateRoomOutputBuilder()
                .setSparkbotRoom(result.getElement())
                .setReturnStatus(result.getRetCode())
                .setErrorMessage(result.getErrorMessage()).build()).buildFuture();
    }

    @Override
    public Future<RpcResult<DeleteRoomOutput>> deleteRoom(final DeleteRoomInput input) {
        LOG.info("deleteRoom input: {}", input);
        Preconditions.checkArgument(input != null, MISSING_ROOM_ID);

        final SparkProxyService<SparkbotRoom, Room, SparkbotRooms>.ReturnValue result =
                delete(input.getRoomId());
        return RpcResultBuilder.success(new DeleteRoomOutputBuilder()
                .setReturnStatus(result.getRetCode())
                .setErrorMessage(result.getErrorMessage()).build()).buildFuture();
    }
}
