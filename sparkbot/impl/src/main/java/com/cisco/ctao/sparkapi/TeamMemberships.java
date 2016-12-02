/*
 * Copyright © 2016 Cisco Systems, Inc and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.cisco.ctao.sparkapi;

import com.ciscospark.TeamMembership;
import com.google.common.base.Preconditions;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** This class uses the Spark SDK to provide an API to the Spark
 *  'Team Memberships' service. Team Memberships represent a person's
 *  relationship to a team. Use this API to list members of any team that
 *  you're in or create memberships to invite someone to a team. Team
 *  memberships can also be updated to make someone a moderator or deleted
 *  to remove them from the team.
 *
 *  <p>Just like in the Spark app, your app or bot must be a member of the
 *  team in order to list its memberships or invite people
 *
 * @author jmedved
 *
 */
public class TeamMemberships {
    private static final Logger LOG = LoggerFactory.getLogger(TeamMemberships.class);
    private static final SparkApi<TeamMembership> TEAM_MEMBERSHIP_API =
            new SparkApiImpl<>("/team/memberships", TeamMembership.class);

    private TeamMemberships() {
        LOG.info("TeamMemberships API created.");
    }

    /** Gets the implementation of the Team Memberships Spark API.
     * @return the Team Memberships Spark API
     */
    public static SparkApi<TeamMembership> api() {
        return TEAM_MEMBERSHIP_API;
    }

    /** Get a list of Memberships for the user whose authentications we're
     *  using.
     * @param teamId: id of the Team for which details should should be
     *                retrieved
     * @param max: max number of TeamMemberships to returns
     * @return List of user's TeamMembership objects
     */
    public static List<TeamMembership> listTeamMemberships(final String teamId, final Integer max) {
        LOG.info("listTeamMemberships: max {}", max);
        Preconditions.checkArgument(teamId != null, "RoomId must be specified");

        SparkQueryParams queryParams = new SparkQueryParams().add(SparkApi.TEAM_ID_KEY, teamId);
        if (max != null) {
            queryParams.add(SparkApi.MAX_KEY, max.toString());
        }
        return TEAM_MEMBERSHIP_API.list(queryParams);
    }

    /** Get details for a TeamMembership from Spark.
     * @param teamMembershipId: id of the TeamMembership for which details
     *                          should be retrieved
     * @return TeamMembership details
     */
    public static TeamMembership getTeamMembershipDetails(final String teamMembershipId) {
        LOG.info("getTeamMembershipDetails: TeamMembershipId '{}'", teamMembershipId);
        return TEAM_MEMBERSHIP_API.getDetails(teamMembershipId);
    }

    /** Create a new TeamMembership in Spark. Add someone to a team by Person
     *  ID or email address; optionally making them a moderator.
     * @param teamId: TeamMembership id
     * @param personId: ID of a person for whom we're creating the team
     *                  membership
     * @param personEmail: email of a person for whom we're creating the team
     *                  membership
     * @param isModerator: indicates whether a person should be made
     *                     a moderator
     * @return: the newly created TeamMembership that was created in
     *          to Spark
     */
    public static TeamMembership createTeamMembership(final String teamId, final String personId,
            final String personEmail, final boolean isModerator) {
        LOG.info("createTeamMembership: name {}, personId {}, personEmail {}, isModerator {}",
                teamId, personId, personEmail, isModerator);
        Preconditions.checkArgument(teamId != null, "RoomId must be specified");

        final TeamMembership teamMembership = new TeamMembership();
        teamMembership.setTeamId(teamId);
        teamMembership.setPersonId(personId);
        teamMembership.setPersonEmail(personEmail);
        teamMembership.setIsModerator(isModerator);
        return TEAM_MEMBERSHIP_API.create(teamMembership);
    }

    /** Update an existing TeamMembership in Spark .
     * @param teamMembershipId: id of the TeamMembership  which should be
     *                          updated
     * @param isModerator: indicates whether a user should be made moderator
     * @return: the updated TeamMembership that was created in to Spark
     */
    public static TeamMembership updateTeamMembership(final String teamMembershipId,
            final boolean isModerator) {
        LOG.info("createTeamMembership: id {}, isModerator {}", teamMembershipId, isModerator);

        final TeamMembership teamMembership = new TeamMembership();
        teamMembership.setIsModerator(isModerator);
        return TEAM_MEMBERSHIP_API.update(teamMembershipId, teamMembership);
    }

    /** Delete a TeamMembership from Spark.
     * @param teamMembershipId: id of the TeamMembership to be deleted
     */
    public static void deleteTeamMembership(final String teamMembershipId) {
        LOG.info("deleteMessqge: teamMembershipId '{}'", teamMembershipId);
        TEAM_MEMBERSHIP_API.delete(teamMembershipId);
    }
}
