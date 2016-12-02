/*
 * Copyright © 2016 Cisco Systems, Inc and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.cisco.ctao.sparkbot.impl;

import com.cisco.ctao.sparkapi.SparkClient;
import com.cisco.ctao.sparkapi.webhookserver.WebhookServer;
import com.cisco.ctao.sparkproxy.HandlerServiceImpl;
import com.cisco.ctao.sparkproxy.MembershipsServiceImpl;
import com.cisco.ctao.sparkproxy.MessagesServiceImpl;
import com.cisco.ctao.sparkproxy.RoomsServiceImpl;
import com.cisco.ctao.sparkproxy.TeamMembershipsServiceImpl;
import com.cisco.ctao.sparkproxy.TeamsServiceImpl;
import com.cisco.ctao.sparkproxy.WebhooksServiceImpl;

import java.util.Collection;
import org.opendaylight.controller.md.sal.binding.api.ClusteredDataTreeChangeListener;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataTreeIdentifier;
import org.opendaylight.controller.md.sal.binding.api.DataTreeModification;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.RpcRegistration;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.sparkbot.handlers.rev161118.SparkbotHandlersService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.sparkbot.memberships.rev161110.SparkbotMembershipsService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.sparkbot.messages.rev161117.SparkbotMesagesService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.sparkbot.rev150105.SparkBotMasterConfigParms;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.sparkbot.rev150105.SparkBotWebHookParms;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.sparkbot.rev150105.spark.bot.master.config.parms.SparkBotMasterSessionDesc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.sparkbot.rooms.rev161110.SparkbotRoomsService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.sparkbot.team.memberships.rev161110.SparkbotTeamMembershipsService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.sparkbot.teams.rev161110.SparkbotTeamsService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.sparkbot.webhooks.rev161117.SparkbotWebhooksService;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/** The Provider class for the Sparkbot app used to hook it up to the ODL
 *  MD-SAL infrastructure.
 * @author jmedved
 *
 */
public class SparkBotProvider {

    private static final Logger LOG = LoggerFactory.getLogger(SparkBotProvider.class);

    private final DataBroker dataBroker;
    private final RpcProviderRegistry rpcProviderRegistry;

    private SparkBotMasterSessionDescChangeHandler sparkBotMasterSessionDescChangeHandler;
    private SparkBotWebHookParmsChangeHandler sparkBotWebHookParmsChangeHandler;
    private RpcRegistration<SparkbotRoomsService> roomServiceReg;
    private RpcRegistration<SparkbotMesagesService> messageServiceReg;
    private RpcRegistration<SparkbotHandlersService> handlerServiceReg;
    private RpcRegistration<SparkbotWebhooksService> webhookServiceReg;
    private RpcRegistration<SparkbotTeamsService> teamServiceReg;
    private RpcRegistration<SparkbotMembershipsService> membershipsSvcReg;
    private RpcRegistration<SparkbotTeamMembershipsService> teamMemberSvcReg;

    /** Constructor.
     * @param dataBroker: reference to the MD-SAL Data Broker.
     * @param rpcProviderRegistry: reference to the MD-SAL RPC registry.
     */
    public SparkBotProvider(final DataBroker dataBroker, final RpcProviderRegistry rpcProviderRegistry) {
        this.dataBroker = dataBroker;
        this.rpcProviderRegistry = rpcProviderRegistry;
    }

    /**
     * Method called when the blueprint container is created.
     */
    public void init() {
        this.sparkBotMasterSessionDescChangeHandler =
                new SparkBotMasterSessionDescChangeHandler(dataBroker);
        this.sparkBotWebHookParmsChangeHandler =
                new SparkBotWebHookParmsChangeHandler(dataBroker);

        roomServiceReg = rpcProviderRegistry.addRpcImplementation(
                SparkbotRoomsService.class, new RoomsServiceImpl());
        messageServiceReg = rpcProviderRegistry.addRpcImplementation(
                SparkbotMesagesService.class, new MessagesServiceImpl());
        handlerServiceReg = rpcProviderRegistry.addRpcImplementation(
                SparkbotHandlersService.class, new HandlerServiceImpl());
        webhookServiceReg = rpcProviderRegistry.addRpcImplementation(
                SparkbotWebhooksService.class, new WebhooksServiceImpl());
        teamServiceReg = rpcProviderRegistry.addRpcImplementation(
                SparkbotTeamsService.class, new TeamsServiceImpl());
        membershipsSvcReg = rpcProviderRegistry.addRpcImplementation(
                SparkbotMembershipsService.class, new MembershipsServiceImpl());
        teamMemberSvcReg = rpcProviderRegistry.addRpcImplementation(
                SparkbotTeamMembershipsService.class, new TeamMembershipsServiceImpl());

        LOG.info("SparkBotProvider Session Initiated");
    }

    /**
     * Method called when the blueprint container is destroyed.
     */
    public void close() {
        if (sparkBotMasterSessionDescChangeHandler != null) {
            sparkBotMasterSessionDescChangeHandler.close();
            sparkBotMasterSessionDescChangeHandler = null;
        }
        if (sparkBotWebHookParmsChangeHandler != null) {
            sparkBotWebHookParmsChangeHandler.close();
            sparkBotWebHookParmsChangeHandler = null;
        }
        if (roomServiceReg != null) {
            roomServiceReg.close();
            roomServiceReg = null;
        }
        if (messageServiceReg != null) {
            messageServiceReg.close();
            messageServiceReg = null;
        }
        if (handlerServiceReg != null) {
            handlerServiceReg.close();
            handlerServiceReg = null;
        }
        if (webhookServiceReg != null) {
            webhookServiceReg.close();
            webhookServiceReg = null;
        }
        if (teamServiceReg != null) {
            teamServiceReg.close();
            teamServiceReg = null;
        }
        if (membershipsSvcReg != null) {
            membershipsSvcReg.close();
            membershipsSvcReg = null;
        }
        if (teamMemberSvcReg != null) {
            teamMemberSvcReg.close();
            teamMemberSvcReg = null;
        }
        LOG.info("SparkBotProvider Closed");
    }

    private class SparkBotMasterSessionDescChangeHandler implements
            ClusteredDataTreeChangeListener<SparkBotMasterSessionDesc>, AutoCloseable {

        private final Logger log =
                LoggerFactory.getLogger(SparkBotMasterSessionDescChangeHandler.class);

        private final InstanceIdentifier<SparkBotMasterSessionDesc> sessionIid =
                InstanceIdentifier.builder(SparkBotMasterConfigParms.class)
                        .child(SparkBotMasterSessionDesc.class)
                        .build();
        private ListenerRegistration<SparkBotMasterSessionDescChangeHandler> dcReg;

        SparkBotMasterSessionDescChangeHandler(DataBroker dataBroker) {
            dcReg = dataBroker.registerDataTreeChangeListener(
                    new DataTreeIdentifier<>(LogicalDatastoreType.CONFIGURATION, sessionIid), this);
        }

        // handle changes to the batch timer here, or any other new parameters to the top level
        @Override
        public void onDataTreeChanged(Collection<DataTreeModification<SparkBotMasterSessionDesc>> changes) {
            for (DataTreeModification<SparkBotMasterSessionDesc> change : changes) {
                switch (change.getRootNode().getModificationType()) {
                    case WRITE:
                    case SUBTREE_MODIFIED:
                        SparkClient.handleConfigParmsChange(change.getRootNode().getDataBefore(),
                                                              change.getRootNode().getDataAfter());
                        break;
                    case DELETE:
                        SparkClient.handleConfigParmsDelete();
                        break;
                    default:
                        log.error("SparkBotMasterSessionDescChangeHandler: "
                                  + "onDataTreeChanged(SparkBotMasterSessionDesc) non handled modification {}",
                                  change.getRootNode().getModificationType());
                        break;
                }
            }
        }

        @Override
        public void close() {
            dcReg.close();
        }
    }

    private class SparkBotWebHookParmsChangeHandler implements
            ClusteredDataTreeChangeListener<SparkBotWebHookParms>, AutoCloseable {

        private final Logger log = LoggerFactory.getLogger(SparkBotWebHookParmsChangeHandler.class);

        private final InstanceIdentifier<SparkBotWebHookParms> sessionIid =
                InstanceIdentifier.builder(SparkBotWebHookParms.class)
                        .build();
        private ListenerRegistration<SparkBotWebHookParmsChangeHandler> dcReg;

        SparkBotWebHookParmsChangeHandler(final DataBroker dataBroker) {
            dcReg = dataBroker.registerDataTreeChangeListener(
                    new DataTreeIdentifier<>(LogicalDatastoreType.CONFIGURATION, sessionIid), this);
        }

        // handle changes to the batch timer here, or any other new parameters to the top level
        @Override
        public void onDataTreeChanged(final Collection<DataTreeModification<SparkBotWebHookParms>> changes) {
            for (DataTreeModification<SparkBotWebHookParms> change : changes) {
                switch (change.getRootNode().getModificationType()) {
                    case WRITE:
                    case SUBTREE_MODIFIED:
                        WebhookServer.getInstance().handleConfigParmsChange(change.getRootNode().getDataBefore(),
                                change.getRootNode().getDataAfter());
                        break;
                    case DELETE:
                        WebhookServer.getInstance().handleConfigParmsDelete();
                        break;
                    default:
                        log.error("SparkBotWebHookParmsChangeHandler: "
                                        + "onDataTreeChanged(SparkBotWebHookParms) non handled modification {}",
                                change.getRootNode().getModificationType());
                        break;
                }
            }
        }

        @Override
        public void close() {
            dcReg.close();
        }
    }
}