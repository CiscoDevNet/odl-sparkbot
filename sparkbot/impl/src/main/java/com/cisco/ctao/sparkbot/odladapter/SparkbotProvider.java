/*
 * Copyright © 2016 Cisco Systems, Inc and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.cisco.ctao.sparkbot.odladapter;

import com.cisco.ctao.sparkbot.core.SparkClient;
import com.cisco.ctao.sparkbot.core.webhooksvr.WebhookServer;

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
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.sparkbot.rev150105.SparkbotMasterConfigParms;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.sparkbot.rev150105.SparkbotWebhookParms;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.sparkbot.rev150105.sparkbot.master.config.parms.SparkbotMasterSessionDesc;
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
public class SparkbotProvider {

    private static final Logger LOG = LoggerFactory.getLogger(SparkbotProvider.class);

    private final DataBroker dataBroker;
    private final RpcProviderRegistry rpcProviderRegistry;

    private SparkbotMasterSessionDescChangeHandler sparkBotMasterSessionDescChangeHandler;
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
    public SparkbotProvider(final DataBroker dataBroker, final RpcProviderRegistry rpcProviderRegistry) {
        this.dataBroker = dataBroker;
        this.rpcProviderRegistry = rpcProviderRegistry;
    }

    /**
     * Method called when the blueprint container is created.
     */
    public void init() {
        this.sparkBotMasterSessionDescChangeHandler =
                new SparkbotMasterSessionDescChangeHandler(dataBroker);
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

    private class SparkbotMasterSessionDescChangeHandler implements
            ClusteredDataTreeChangeListener<SparkbotMasterSessionDesc>, AutoCloseable {

        private final Logger log =
                LoggerFactory.getLogger(SparkbotMasterSessionDescChangeHandler.class);

        private final InstanceIdentifier<SparkbotMasterSessionDesc> sessionIid =
                InstanceIdentifier.builder(SparkbotMasterConfigParms.class)
                        .child(SparkbotMasterSessionDesc.class)
                        .build();
        private ListenerRegistration<SparkbotMasterSessionDescChangeHandler> dcReg;

        SparkbotMasterSessionDescChangeHandler(DataBroker dataBroker) {
            dcReg = dataBroker.registerDataTreeChangeListener(
                    new DataTreeIdentifier<>(LogicalDatastoreType.CONFIGURATION, sessionIid), this);
        }

        // handle changes to the batch timer here, or any other new parameters to the top level
        @Override
        public void onDataTreeChanged(Collection<DataTreeModification<SparkbotMasterSessionDesc>> changes) {
            for (DataTreeModification<SparkbotMasterSessionDesc> change : changes) {
                switch (change.getRootNode().getModificationType()) {
                    case WRITE:
                    case SUBTREE_MODIFIED:
                        SparkClient.handleAccessTokenChange(change.getRootNode().getDataAfter().getAccessToken());
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
            ClusteredDataTreeChangeListener<SparkbotWebhookParms>, AutoCloseable {

        private final Logger log = LoggerFactory.getLogger(SparkBotWebHookParmsChangeHandler.class);

        private final InstanceIdentifier<SparkbotWebhookParms> sessionIid =
                InstanceIdentifier.builder(SparkbotWebhookParms.class)
                        .build();
        private ListenerRegistration<SparkBotWebHookParmsChangeHandler> dcReg;

        SparkBotWebHookParmsChangeHandler(final DataBroker dataBroker) {
            dcReg = dataBroker.registerDataTreeChangeListener(
                    new DataTreeIdentifier<>(LogicalDatastoreType.CONFIGURATION, sessionIid), this);
        }

        // handle changes to the batch timer here, or any other new parameters to the top level
        @Override
        public void onDataTreeChanged(final Collection<DataTreeModification<SparkbotWebhookParms>> changes) {
            for (DataTreeModification<SparkbotWebhookParms> change : changes) {
                switch (change.getRootNode().getModificationType()) {
                    case WRITE:
                    case SUBTREE_MODIFIED:
                        SparkbotWebhookParms dataAfter = change.getRootNode().getDataAfter();
                        WebhookServer.getInstance().handleUrlPrefixChange(dataAfter.getWebhookUrlPrefix());
                        WebhookServer.getInstance().handleHttpPortChange(dataAfter.getWebhookHttpPort());
                        break;
                    case DELETE:
                        WebhookServer.getInstance().handleHttpPortDelete();
                        WebhookServer.getInstance().handleUrlPrefixDelete();
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