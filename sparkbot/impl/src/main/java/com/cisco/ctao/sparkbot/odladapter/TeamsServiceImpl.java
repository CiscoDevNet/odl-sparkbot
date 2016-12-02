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
import com.cisco.ctao.sparkbot.core.Teams;
import com.ciscospark.Team;
import com.google.common.base.Preconditions;

import java.util.concurrent.Future;

import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.sparkbot.teams.rev161110.CreateTeamInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.sparkbot.teams.rev161110.CreateTeamOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.sparkbot.teams.rev161110.CreateTeamOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.sparkbot.teams.rev161110.DeleteTeamInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.sparkbot.teams.rev161110.DeleteTeamOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.sparkbot.teams.rev161110.DeleteTeamOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.sparkbot.teams.rev161110.GetTeamDetailsInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.sparkbot.teams.rev161110.GetTeamDetailsOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.sparkbot.teams.rev161110.GetTeamDetailsOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.sparkbot.teams.rev161110.ListTeamsInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.sparkbot.teams.rev161110.ListTeamsOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.sparkbot.teams.rev161110.ListTeamsOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.sparkbot.teams.rev161110.SparkbotTeamsService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.sparkbot.teams.rev161110.UpdateTeamInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.sparkbot.teams.rev161110.UpdateTeamOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.sparkbot.teams.rev161110.UpdateTeamOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.sparkbot.teams.rev161110.list.teams.input.QueryParameters;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.sparkbot.teams.rev161110.list.teams.output.SparkbotTeams;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.sparkbot.teams.rev161110.list.teams.output.SparkbotTeamsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.sparkbot.teams.rev161110.team.SparkbotTeam;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.sparkbot.teams.rev161110.team.SparkbotTeamBuilder;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Implements an ODL Proxy to the Spark 'teams' service. It's both an
 *  example of the CTAO Spark API (com.cisco.ctao.sparkapi) and a working
 *  proxy.
 * @author jmedved
 *
 */
public class TeamsServiceImpl
    extends SparkProxyService<SparkbotTeam, Team, SparkbotTeams>
    implements SparkbotTeamsService {

    private static final Logger LOG = LoggerFactory.getLogger(TeamsServiceImpl.class);
    private static final String MISSING_TEAM_ID = "Missing Team Id.";
    private static final String MISSING_TEAM_DATA = "Missing Team data.";

    private static SparkQueryParams getQueryParameters(final ListTeamsInput input) {
        final SparkQueryParams queryParams = new SparkQueryParams();
        QueryParameters qp;
        if (input != null
                && (qp = input.getQueryParameters()) != null
                && qp.getMax() != null) {
            queryParams.add(SparkApi.MAX_KEY, qp.getMax().toString());
        }
        return queryParams;
    }

    @Override
    protected SparkbotTeam translate(Team team) {
        return new SparkbotTeamBuilder()
                .setId(team.getId())
                .setName(team.getName())
                .setCreated(team.getCreated().toString())
                .build();
    }

    @Override
    protected SparkbotTeams buildListElement(Team team) {
        return new SparkbotTeamsBuilder()
                .setSparkbotTeam(translate(team))
                .build();
    }

    public TeamsServiceImpl() {
        super(Teams.api());
    }

    @Override
    public Future<RpcResult<ListTeamsOutput>> listTeams(final ListTeamsInput input) {
        LOG.info("listTeams input: {}", input);

        SparkProxyService<SparkbotTeam, Team, SparkbotTeams>.ReturnValue result =
                list(getQueryParameters(input));
        return RpcResultBuilder.success(new ListTeamsOutputBuilder()
                .setSparkbotTeams(result.getList())
                .setReturnStatus(result.getRetCode())
                .setErrorMessage(result.getErrorMessage()).build()).buildFuture();
    }

    @Override
    public Future<RpcResult<GetTeamDetailsOutput>> getTeamDetails(final GetTeamDetailsInput input) {
        LOG.info("getTeamDetails input: {}", input);
        Preconditions.checkArgument(input != null, MISSING_TEAM_ID);

        SparkProxyService<SparkbotTeam, Team, SparkbotTeams>.ReturnValue result =
                getDetails(input.getTeamId());
        return RpcResultBuilder.success(new GetTeamDetailsOutputBuilder()
                .setSparkbotTeam(result.getElement())
                .setReturnStatus(result.getRetCode())
                .setErrorMessage(result.getErrorMessage()).build()).buildFuture();
    }

    @Override
    public Future<RpcResult<CreateTeamOutput>> createTeam(final CreateTeamInput input) {
        LOG.info("createTeam input: {}", input);
        Preconditions.checkArgument(input != null, MISSING_TEAM_DATA);

        Team team = new Team();
        team.setName(input.getName());

        SparkProxyService<SparkbotTeam, Team, SparkbotTeams>.ReturnValue result = create(team);
        return RpcResultBuilder.success(new CreateTeamOutputBuilder()
                .setSparkbotTeam(result.getElement())
                .setReturnStatus(result.getRetCode())
                .setErrorMessage(result.getErrorMessage()).build()).buildFuture();
    }

    @Override
    public Future<RpcResult<UpdateTeamOutput>> updateTeam(final UpdateTeamInput input) {
        LOG.info("updateTeam input: {}", input);
        Preconditions.checkArgument(input != null, MISSING_TEAM_ID);

        Team team = new Team();
        team.setName(input.getName());

        SparkProxyService<SparkbotTeam, Team, SparkbotTeams>.ReturnValue result =
                update(input.getTeamId(), team);
        return RpcResultBuilder.success(new UpdateTeamOutputBuilder()
                .setSparkbotTeam(result.getElement())
                .setReturnStatus(result.getRetCode())
                .setErrorMessage(result.getErrorMessage()).build()).buildFuture();
    }

    @Override
    public Future<RpcResult<DeleteTeamOutput>> deleteTeam(final DeleteTeamInput input) {
        LOG.info("deleteTeam input: {}", input);
        Preconditions.checkArgument(input != null, MISSING_TEAM_ID);

        SparkProxyService<SparkbotTeam, Team, SparkbotTeams>.ReturnValue result =
                delete(input.getTeamId());
        return RpcResultBuilder.success(new DeleteTeamOutputBuilder()
                .setReturnStatus(result.getRetCode())
                .setErrorMessage(result.getErrorMessage()).build()).buildFuture();
    }
}
