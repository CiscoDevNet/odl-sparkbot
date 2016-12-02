/*
 * Copyright © 2016 Cisco Systems, Inc and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.cisco.ctao.sparkbot.odladapter;

import com.cisco.ctao.sparkbot.core.SparkApi;
import com.cisco.ctao.sparkbot.core.SparkQueryParams;
import com.cisco.ctao.sparkbot.core.TeamMemberships;
import com.ciscospark.TeamMembership;
import com.google.common.base.Preconditions;

import java.util.concurrent.Future;

import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.sparkbot.team.memberships.rev161110.CreateTeamMembershipInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.sparkbot.team.memberships.rev161110.CreateTeamMembershipOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.sparkbot.team.memberships.rev161110.CreateTeamMembershipOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.sparkbot.team.memberships.rev161110.DeleteTeamMembershipInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.sparkbot.team.memberships.rev161110.DeleteTeamMembershipOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.sparkbot.team.memberships.rev161110.DeleteTeamMembershipOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.sparkbot.team.memberships.rev161110.GetTeamMembershipDetailsInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.sparkbot.team.memberships.rev161110.GetTeamMembershipDetailsOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.sparkbot.team.memberships.rev161110.GetTeamMembershipDetailsOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.sparkbot.team.memberships.rev161110.ListTeamMembershipsInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.sparkbot.team.memberships.rev161110.ListTeamMembershipsOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.sparkbot.team.memberships.rev161110.ListTeamMembershipsOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.sparkbot.team.memberships.rev161110.SparkbotTeamMembershipsService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.sparkbot.team.memberships.rev161110.UpdateTeamMembershipInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.sparkbot.team.memberships.rev161110.UpdateTeamMembershipOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.sparkbot.team.memberships.rev161110.UpdateTeamMembershipOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.sparkbot.team.memberships.rev161110.list.team.memberships.input.QueryParameters;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.sparkbot.team.memberships.rev161110.list.team.memberships.output.TeamMembershipList;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.sparkbot.team.memberships.rev161110.list.team.memberships.output.TeamMembershipListBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.sparkbot.team.memberships.rev161110.team.membership.SparkbotTeamMembership;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.sparkbot.team.memberships.rev161110.team.membership.SparkbotTeamMembershipBuilder;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TeamMembershipsServiceImpl
    extends SparkProxyService<SparkbotTeamMembership, TeamMembership, TeamMembershipList>
    implements SparkbotTeamMembershipsService {

    private static final Logger LOG = LoggerFactory.getLogger(TeamMembershipsServiceImpl.class);
    private static final String MISSING_TEAM_MEMBERSHIP_ID = "Missing Team Membership Id.";
    private static final String MISSING_TEAM_MEMBERSHIP_DATA = "Missing Team Membership data.";

    private static SparkQueryParams getQueryParameters(final ListTeamMembershipsInput input) {
        final SparkQueryParams queryParams = new SparkQueryParams();
        QueryParameters qp;
        if (input != null && (qp = input.getQueryParameters()) != null) {
            String teamId = qp.getTeamId();
            if (teamId != null) {
                queryParams.add(SparkApi.TEAM_ID_KEY, teamId);
            }
            Integer max = qp.getMax();
            if (max != null) {
                queryParams.add(SparkApi.MAX_KEY, max.toString());
            }
        }
        return queryParams;
    }

    @Override
    protected SparkbotTeamMembership translate(TeamMembership sparkElement) {
        return new SparkbotTeamMembershipBuilder()
                .setId(sparkElement.getId())
                .setTeamId(sparkElement.getTeamId())
                .setPersonId(sparkElement.getPersonId())
                .setPersonEmail(sparkElement.getPersonEmail())
                .setIsModerator(sparkElement.getIsModerator())
                .setCreated(sparkElement.getCreated().toString())
                .build();
    }

    @Override
    protected TeamMembershipList buildListElement(TeamMembership sparkElement) {
        return new TeamMembershipListBuilder()
                .setSparkbotTeamMembership(translate(sparkElement))
                .build();
    }

    public TeamMembershipsServiceImpl() {
        super(TeamMemberships.api());
    }

    @Override
    public Future<RpcResult<ListTeamMembershipsOutput>> listTeamMemberships(final ListTeamMembershipsInput input) {
        LOG.info("listTeamMemberships input: {}", input);

        final SparkProxyService<SparkbotTeamMembership, TeamMembership, TeamMembershipList>.ReturnValue result =
                list(getQueryParameters(input));
        return RpcResultBuilder.success(new ListTeamMembershipsOutputBuilder()
                .setTeamMembershipList(result.getList())
                .setReturnStatus(result.getRetCode())
                .setErrorMessage(result.getErrorMessage()).build()).buildFuture();
    }

    @Override
    public Future<RpcResult<GetTeamMembershipDetailsOutput>> getTeamMembershipDetails(
            final GetTeamMembershipDetailsInput input) {
        LOG.info("getTeamMembershipDetails input: {}", input);
        Preconditions.checkArgument(input != null, MISSING_TEAM_MEMBERSHIP_ID);

        final SparkProxyService<SparkbotTeamMembership, TeamMembership, TeamMembershipList>.ReturnValue result =
                getDetails(input.getTeamMembershipId());
        return RpcResultBuilder.success(new GetTeamMembershipDetailsOutputBuilder()
                .setSparkbotTeamMembership(result.getElement())
                .setReturnStatus(result.getRetCode())
                .setErrorMessage(result.getErrorMessage()).build()).buildFuture();
    }

    @Override
    public Future<RpcResult<CreateTeamMembershipOutput>> createTeamMembership(final CreateTeamMembershipInput input) {
        LOG.info("createTeamMembership input: {}", input);
        Preconditions.checkArgument(input != null, MISSING_TEAM_MEMBERSHIP_DATA);

        final TeamMembership elementIn = new TeamMembership();
        elementIn.setTeamId(input.getTeamId());
        elementIn.setPersonId(input.getPersonId());
        elementIn.setPersonEmail(input.getPersonEmail());
        elementIn.setIsModerator(input.isIsModerator());

        final SparkProxyService<SparkbotTeamMembership, TeamMembership, TeamMembershipList>.ReturnValue result =
                create(elementIn);
        return RpcResultBuilder.success(new CreateTeamMembershipOutputBuilder()
                .setSparkbotTeamMembership(result.getElement())
                .setReturnStatus(result.getRetCode())
                .setErrorMessage(result.getErrorMessage()).build()).buildFuture();
    }

    @Override
    public Future<RpcResult<UpdateTeamMembershipOutput>> updateTeamMembership(final UpdateTeamMembershipInput input) {
        LOG.info("updateTeamMembership input: {}", input);
        Preconditions.checkArgument(input != null, MISSING_TEAM_MEMBERSHIP_ID);

        final TeamMembership elementIn = new TeamMembership();
        elementIn.setIsModerator(input.isIsModerator());

        final SparkProxyService<SparkbotTeamMembership, TeamMembership, TeamMembershipList>.ReturnValue result =
                update(input.getTeamMembershipId(), elementIn);
        return RpcResultBuilder.success(new UpdateTeamMembershipOutputBuilder()
                .setSparkbotTeamMembership(result.getElement())
                .setReturnStatus(result.getRetCode())
                .setErrorMessage(result.getErrorMessage()).build()).buildFuture();
    }

    @Override
    public Future<RpcResult<DeleteTeamMembershipOutput>> deleteTeamMembership(final DeleteTeamMembershipInput input) {
        LOG.info("deleteTeamMembership input: {}", input);
        Preconditions.checkArgument(input != null, MISSING_TEAM_MEMBERSHIP_ID);

        final SparkProxyService<SparkbotTeamMembership, TeamMembership, TeamMembershipList>.ReturnValue result =
                delete(input.getTeamMembershipId());
        return RpcResultBuilder.success(new DeleteTeamMembershipOutputBuilder()
                .setReturnStatus(result.getRetCode())
                .setErrorMessage(result.getErrorMessage()).build()).buildFuture();
    }
}
