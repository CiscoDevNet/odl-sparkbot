/*
 * Copyright © 2016 Cisco Systems, Inc and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.cisco.ctao.sparkproxy;

import com.cisco.ctao.sparkapi.Memberships;
import com.cisco.ctao.sparkapi.SparkApi;
import com.cisco.ctao.sparkapi.SparkQueryParams;
import com.ciscospark.Membership;
import com.google.common.base.Preconditions;

import java.util.concurrent.Future;

import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.sparkbot.memberships.rev161110.CreateMembershipInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.sparkbot.memberships.rev161110.CreateMembershipOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.sparkbot.memberships.rev161110.CreateMembershipOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.sparkbot.memberships.rev161110.DeleteMembershipInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.sparkbot.memberships.rev161110.DeleteMembershipOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.sparkbot.memberships.rev161110.DeleteMembershipOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.sparkbot.memberships.rev161110.GetMembershipDetailsInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.sparkbot.memberships.rev161110.GetMembershipDetailsOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.sparkbot.memberships.rev161110.GetMembershipDetailsOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.sparkbot.memberships.rev161110.ListMembershipsInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.sparkbot.memberships.rev161110.ListMembershipsOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.sparkbot.memberships.rev161110.ListMembershipsOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.sparkbot.memberships.rev161110.SparkbotMembershipsService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.sparkbot.memberships.rev161110.UpdateMembershipInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.sparkbot.memberships.rev161110.UpdateMembershipOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.sparkbot.memberships.rev161110.UpdateMembershipOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.sparkbot.memberships.rev161110.list.memberships.input.QueryParameters;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.sparkbot.memberships.rev161110.list.memberships.output.SparkbotMemberships;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.sparkbot.memberships.rev161110.list.memberships.output.SparkbotMembershipsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.sparkbot.memberships.rev161110.membership.SparkbotMembership;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.sparkbot.memberships.rev161110.membership.SparkbotMembershipBuilder;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MembershipsServiceImpl
    extends SparkProxyService<SparkbotMembership, Membership, SparkbotMemberships>
    implements SparkbotMembershipsService {

    private static final Logger LOG = LoggerFactory.getLogger(MembershipsServiceImpl.class);
    private static final String MISSING_MEMBERSHIP_ID = "Missing Membership Id.";
    private static final String MISSING_MEMBERSHIP_DATA = "Missing Membership data.";

    private static SparkQueryParams getQueryParameters(final ListMembershipsInput input) {
        final SparkQueryParams queryParams = new SparkQueryParams();
        QueryParameters qp;
        if (input != null && (qp = input.getQueryParameters()) != null) {
            if (qp.getRoomId() != null) {
                queryParams.add(SparkApi.ROOM_ID_KEY, qp.getRoomId());
            }
            if (qp.getPersonId() != null) {
                queryParams.add(SparkApi.PERSON_ID_KEY, qp.getPersonId());
            }
            if (qp.getPersonEmail() != null) {
                queryParams.add(SparkApi.PERSON_EMAIL_KEY, qp.getPersonEmail());
            }
            if (qp.getMax() != null) {
                queryParams.add(SparkApi.MAX_KEY, qp.getMax().toString());
            }
        }
        return queryParams;
    }

    @Override
    protected SparkbotMembership translate(Membership membership) {
        return new SparkbotMembershipBuilder()
                .setId(membership.getId())
                .setRoomId(membership.getRoomId())
                .setPersonId(membership.getPersonId())
                .setPersonEmail(membership.getPersonEmail())
                .setCreated(membership.getCreated().toString())
                .build();
    }

    @Override
    protected SparkbotMemberships buildListElement(Membership membership) {
        return new SparkbotMembershipsBuilder()
                .setSparkbotMembership(translate(membership))
                .build();
    }

    public MembershipsServiceImpl() {
        super(Memberships.api());
    }

    @Override
    public Future<RpcResult<ListMembershipsOutput>> listMemberships(final ListMembershipsInput input) {
        LOG.info("listMemberships input: {}", input);

        SparkProxyService<SparkbotMembership, Membership, SparkbotMemberships>.ReturnValue result =
                list(getQueryParameters(input));
        return RpcResultBuilder.success(new ListMembershipsOutputBuilder()
                .setSparkbotMemberships(result.getList())
                .setReturnStatus(result.getRetCode())
                .setErrorMessage(result.getErrorMessage()).build()).buildFuture();
    }

    @Override
    public Future<RpcResult<GetMembershipDetailsOutput>> getMembershipDetails(final GetMembershipDetailsInput input) {
        LOG.info("getMembershipDetails input: {}", input);
        Preconditions.checkArgument(input != null, MISSING_MEMBERSHIP_ID);

        SparkProxyService<SparkbotMembership, Membership, SparkbotMemberships>.ReturnValue result =
                getDetails(input.getMembershipId());
        return RpcResultBuilder.success(new GetMembershipDetailsOutputBuilder()
                .setSparkbotMembership(result.getElement())
                .setReturnStatus(result.getRetCode())
                .setErrorMessage(result.getErrorMessage()).build()).buildFuture();
    }

    @Override
    public Future<RpcResult<CreateMembershipOutput>> createMembership(final CreateMembershipInput input) {
        LOG.info("createMembership input: {}", input);
        Preconditions.checkArgument(input != null, MISSING_MEMBERSHIP_DATA);

        Membership membership = new Membership();
        membership.setRoomId(input.getRoomId());
        membership.setPersonId(input.getPersonId());
        membership.setPersonEmail(input.getPersonEmail());
        membership.setIsModerator(input.isIsModerator());

        final SparkProxyService<SparkbotMembership, Membership, SparkbotMemberships>.ReturnValue result =
                create(membership);
        return RpcResultBuilder.success(new CreateMembershipOutputBuilder()
                .setSparkbotMembership(result.getElement())
                .setReturnStatus(result.getRetCode())
                .setErrorMessage(result.getErrorMessage()).build()).buildFuture();
    }

    @Override
    public Future<RpcResult<UpdateMembershipOutput>> updateMembership(final UpdateMembershipInput input) {
        LOG.info("updateMembership input: {}", input);
        Preconditions.checkArgument(input != null && input.getMembershipId() != null, MISSING_MEMBERSHIP_ID);

        Membership membership = new Membership();
        membership.setIsModerator(input.isIsModerator());

        SparkProxyService<SparkbotMembership, Membership, SparkbotMemberships>.ReturnValue result =
                update(input.getMembershipId(), membership);
        return RpcResultBuilder.success(new UpdateMembershipOutputBuilder()
                .setSparkbotMembership(result.getElement())
                .setReturnStatus(result.getRetCode())
                .setErrorMessage(result.getErrorMessage()).build()).buildFuture();
    }

    @Override
    public Future<RpcResult<DeleteMembershipOutput>> deleteMembership(final DeleteMembershipInput input) {
        LOG.info("deleteMembership input: {}", input);
        Preconditions.checkArgument(input != null, MISSING_MEMBERSHIP_ID);

        SparkProxyService<SparkbotMembership, Membership, SparkbotMemberships>.ReturnValue result =
                delete(input.getMembershipId());
        return RpcResultBuilder.success(new DeleteMembershipOutputBuilder()
                .setReturnStatus(result.getRetCode())
                .setErrorMessage(result.getErrorMessage()).build()).buildFuture();
    }
}
