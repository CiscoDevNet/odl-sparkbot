/*
 * Copyright © 2016 Cisco Systems, Inc and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.cisco.ctao.sparkapi;

import com.ciscospark.Team;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** This class uses the Spark SDK to provide an API to the Spark
 *  'Teams' service. Teams are groups of people with a set of rooms that
 *  are visible to all members of that team. This API is used to manage
 *  the teams themselves. Teams are create and deleted with this API.
 *  You can also update a team to change its team, for example.
 *
 *  <p>To manage people in a team see the Team Memberships API.
 *
 *  <p>To manage team rooms see the Rooms API.
 *
 * @author jmedved
 *
 */
public class Teams {
    private static final Logger LOG = LoggerFactory.getLogger(Teams.class);
    private static final SparkApi<Team> TEAM_API = new SparkApiImpl<>("/teams", Team.class);

    private Teams() {
        LOG.info("Teams API created.");
    }

    /** Gets the implementation of the Team Spark API.
     * @return the Team Spark API
     */
    public static SparkApi<Team> api() {
        return TEAM_API;
    }

    /** Get a list of teams for the user whose authentications we're using.
     * @param max: max number of teams to returns
     * @return List of user's teams
     */
    public static List<Team> listTeams(final Integer max) {
        LOG.info("listTeams: max {}", max);

        SparkQueryParams queryParams = new SparkQueryParams();
        if (max != null) {
            queryParams.add(SparkApi.MAX_KEY, max.toString());
        }
        return TEAM_API.list(queryParams);
    }

    /** Get details for a Team from Spark.
     * @param teamId: id of the Team for which details should be retrieved
     * @return Team details
     */
    public static Team getTeamDetails(final String teamId) {
        LOG.info("getTeamDetails: TeamId '{}'", teamId);
        return TEAM_API.getDetails(teamId);
    }

    /** Create a new Team in Spark .
     * @param name: team name
     * @return: the newly created Team that was created in to Spark
     */
    public static Team createTeam(final String name) {
        LOG.info("createTeam: name {}", name);

        final Team ream = new Team();
        ream.setName(name);
        return TEAM_API.create(ream);
    }

    /** Update a new Team in Spark .
     * @param teamId: id of the Team  which should be updated
     * @param teamName: team name
     * @return: the newly created Team that was created in to Spark
     */
    public static Team updateTeam(final String teamId, final String teamName) {
        LOG.info("createTeam: name {}", teamName);

        final Team team = new Team();
        team.setName(teamName);
        return TEAM_API.update(teamId, team);
    }

    /** Delete a Team from Spark.
     * @param teamId: id of the Team to be deleted
     */
    public static void deleteTeam(final String teamId) {
        LOG.info("deleteMessqge: TeamId '{}'", teamId);
        TEAM_API.delete(teamId);
    }
}
