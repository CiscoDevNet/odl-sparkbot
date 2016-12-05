/*
 * Copyright © 2016 Cisco Systems, Inc and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.cisco.ctao.sparkbot.core;

import com.ciscospark.Membership;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** This class uses the Spark SDK to provide an API to the Spark
 *  'Memberships' service. Memberships represent a person's relationship to
 *  a room. Use this API to list members of any room that you're in or create
 *  memberships to invite someone to a room. Memberships can also be updated
 *  to make someone a moderator or deleted to remove them from the room.
 *
 *  <p>Just like in the Spark app, you must be a member of the room in order
 *  to list its memberships or invite people.
 *
 * @author jmedved
 *
 */
public class Memberships {
    private static final Logger LOG = LoggerFactory.getLogger(Membership.class);
    private static final SparkApi<Membership> MEMBERSHIP_API = new SparkApiImpl<>("/memberships", Membership.class);

    private Memberships() {
        LOG.info("MembershipsApi created.");
    }

    /** Gets the implementation of the Memberships Spark API.
     * @return the Memberships Spark API
     */
    public static SparkApi<Membership> api() {
        return MEMBERSHIP_API;
    }

    /** Lists all room memberships. By default, lists memberships for rooms
     * to which the authenticated user belongs. Use query parameters to filter
     *  the response. Use roomId to list memberships for a room, by ID. Use
     *  either personId or personEmail to filter the results.
     *
     * @param roomId Limit results to a specific room, by ID.; null if
     *          not specified
     * @param personId Limit results to a specific person, by ID; null
     *          if not specified
     * @param personEmail Limit results to a specific person, by email
     *          address; null if not specified
     * @param max maximum number of memberships to return; null if not
     *          specified
     * @return list of membership objects
     */
    public static List<Membership> listMemberships(final String roomId, final String personId,
            final String personEmail, final Integer max) {
        LOG.info("listMemberships: roomId {}, personId {}, personEmail {}, max {}",
                roomId, personId, personEmail, max);

        SparkQueryParams queryParams = new SparkQueryParams();
        if (roomId != null) {
            queryParams.add(SparkApi.ROOM_ID_KEY, roomId);

        }
        if (personId != null) {
            queryParams.add(SparkApi.PERSON_ID_KEY, personId);

        }
        if (personEmail != null) {
            queryParams.add(SparkApi.PERSON_EMAIL_KEY, personEmail);

        }
        if (max != null) {
            queryParams.add(SparkApi.MAX_KEY, max.toString());
        }
        return MEMBERSHIP_API.list(queryParams);
    }

    /** Get details for a Membership from Spark.
     * @param membershipId id of the Membership for which details should be retrieved
     * @return Membership details
     */
    public static Membership getMembershipDetails(final String membershipId) {
        LOG.info("getMembershipDetails: membershipId '{}'", membershipId);
        return MEMBERSHIP_API.getDetails(membershipId);
    }

    /** Create a new Membership in Spark Add someone to a room by Person ID
     *  or email address; optionally making them a moderator.
     * @param roomId Room where to create the membership
     * @param personId Person for whom to create the membership
     * @param personEmail Email of the person  for whom to create the
     *                     membership
     * @param isModerator make the person a moderator (optional)
     * @return the newly created Membership that was created in Spark
     */
    public static Membership createMembership(final String roomId, final String personId,
            final String personEmail, final boolean isModerator) {
        LOG.info("listMemberships: roomId {}, personId {}, personEmail {}, isModerator {}",
                roomId, personId, personEmail, isModerator);

        final Membership membership = new Membership();
        membership.setRoomId(roomId);
        membership.setPersonId(personId);
        membership.setPersonEmail(personEmail);
        membership.setIsModerator(isModerator);
        return MEMBERSHIP_API.create(membership);
    }

    /** Update a Membership .
     * @param membershipId id of the Membership which should be updated
     * @param isModerator Update value - give or revoke moderator rights
     * @return updated Membership object
     */
    public static Membership updateMembership(final String membershipId, final boolean isModerator) {
        LOG.info("updateMembership: membershipId {}, isModerator {}",
                membershipId, isModerator);
        final Membership membership = new Membership();
        membership.setIsModerator(isModerator);
        return MEMBERSHIP_API.update(membershipId, membership);
    }

    /** Delete a Membership.
     * @param membershipId id of the Membership to be deleted
     */
    public static void deleteMembership(final String membershipId) {
        LOG.info("deleteMembership: id '{}'", membershipId);
        MEMBERSHIP_API.delete(membershipId);
    }

}
