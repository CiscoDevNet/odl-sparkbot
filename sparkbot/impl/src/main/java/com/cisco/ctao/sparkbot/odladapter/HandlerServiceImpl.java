/*
 * Copyright © 2016 Cisco Systems, Inc and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.cisco.ctao.sparkbot.odladapter;

import com.cisco.ctao.sparkbot.core.testhandlers.WebhookMembershipTestHandler;
import com.cisco.ctao.sparkbot.core.testhandlers.WebhookMessageTestHandler;
import com.cisco.ctao.sparkbot.core.testhandlers.WebhookRawTestHandler;
import com.cisco.ctao.sparkbot.core.testhandlers.WebhookRoomTestHandler;
import com.cisco.ctao.sparkbot.core.webhooksvr.WebhookFilter;
import com.cisco.ctao.sparkbot.core.webhooksvr.WebhookFilter.Events;
import com.cisco.ctao.sparkbot.core.webhooksvr.WebhookFilter.Resources;
import com.cisco.ctao.sparkbot.core.webhooksvr.WebhookServer;

import java.util.concurrent.Future;

import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.sparkbot.commons.rev161110.ReturnCode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.sparkbot.handlers.rev161118.EventType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.sparkbot.handlers.rev161118.RegisterTestHandlerInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.sparkbot.handlers.rev161118.RegisterTestHandlerOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.sparkbot.handlers.rev161118.RegisterTestHandlerOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.sparkbot.handlers.rev161118.ResourceType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.sparkbot.handlers.rev161118.SparkbotHandlersService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.sparkbot.handlers.rev161118.UnregisterTestHandlerInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.sparkbot.handlers.rev161118.UnregisterTestHandlerOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.sparkbot.handlers.rev161118.UnregisterTestHandlerOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.sparkbot.handlers.rev161118.register.test.handler.input.Filter;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Implements a service that registers/unregisters test handlers with
 *  the Sparkbot WebHookServer.
 * @author jmedved
 *
 */
public class HandlerServiceImpl implements SparkbotHandlersService {
    private static final Logger LOG = LoggerFactory.getLogger(HandlerServiceImpl.class);
    private static final String HANDLER_CREATED_ERR = "Test handler already created";

    private WebhookRawTestHandler testHandler = null;
    private WebhookMessageTestHandler msgTestHandler = null;
    private WebhookRoomTestHandler roomTestHandler = null;
    private WebhookMembershipTestHandler membershipTestHandler = null;

    @Override
    public Future<RpcResult<UnregisterTestHandlerOutput>> unregisterTestHandler(UnregisterTestHandlerInput input) {
        LOG.info("Unregistering test handler, input {}", input);
        final UnregisterTestHandlerOutputBuilder ob =
                new UnregisterTestHandlerOutputBuilder().setReturnStatus(ReturnCode.OK);

        if (input != null) {
            switch (input.getHandlerType()) {
                case RAW:
                    if (testHandler != null) {
                        WebhookServer.unregisterRawEventHandler(testHandler);
                        testHandler = null;
                    }
                    break;
                case MESSAGES:
                    if (msgTestHandler != null) {
                        WebhookServer.unregisterSparkEventHandler(msgTestHandler);
                        msgTestHandler = null;
                    }
                    break;
                case ROOMS:
                    if (roomTestHandler != null) {
                        WebhookServer.unregisterSparkEventHandler(roomTestHandler);
                        roomTestHandler = null;
                    }
                    break;
                case MEMBERSHIPS:
                    if (membershipTestHandler != null) {
                        WebhookServer.unregisterSparkEventHandler(membershipTestHandler);
                        membershipTestHandler = null;
                    }
                    break;
                default:
                    LOG.error("unsupported handler type {}", input.getHandlerType());
                    ob.setReturnStatus(ReturnCode.INVALIDPARAMETER);
                    break;
            }
        } else {
            ob.setReturnStatus(ReturnCode.INVALIDPARAMETER);
        }
        return RpcResultBuilder.success(ob.build()).buildFuture();
    }

    @Override
    public Future<RpcResult<RegisterTestHandlerOutput>> registerTestHandler(RegisterTestHandlerInput input) {
        LOG.info("registerTestHandler, input {}", input);
        final RegisterTestHandlerOutputBuilder ob =
                new RegisterTestHandlerOutputBuilder().setReturnStatus(ReturnCode.OK);

        if (input != null) {
            final Filter odlFilter = input.getFilter();
            if (odlFilter != null) {
                switch (input.getHandlerType()) {
                    case RAW:
                        registerRawTestHandler(input.getFilter());
                        break;
                    case MESSAGES:
                        registerMessageTestHandler(input.getFilter());
                        break;
                    case ROOMS:
                        registerRoomTestHandler(input.getFilter());
                        break;
                    case MEMBERSHIPS:
                        registerMembershipTestHandler(input.getFilter());
                        break;
                    default:
                        LOG.error("unsupported handler type {}", input.getHandlerType());
                        ob.setReturnStatus(ReturnCode.INVALIDPARAMETER);
                        break;
                }
            }
        } else {
            ob.setReturnStatus(ReturnCode.INVALIDPARAMETER);
        }
        return RpcResultBuilder.success(ob.build()).buildFuture();
    }

    private void registerMembershipTestHandler(Filter odlFilter) {
        LOG.info("registerMembershipTestHandler, odlFilter {}", odlFilter);
        if (membershipTestHandler == null) {
            membershipTestHandler = new WebhookMembershipTestHandler();
            WebhookServer.registerTypedEventHandler(membershipTestHandler,
                    getEventType(odlFilter.getEvent()),
                    odlFilter.getFilter(), odlFilter.getSecret(), odlFilter.getName());
        } else {
            LOG.error(HANDLER_CREATED_ERR);
        }
    }

    private void registerRoomTestHandler(Filter odlFilter) {
        LOG.info("registerRoomTestHandler, odlFilter {}", odlFilter);
        if (roomTestHandler == null) {
            roomTestHandler = new WebhookRoomTestHandler();
            WebhookServer.registerTypedEventHandler(roomTestHandler,
                    getEventType(odlFilter.getEvent()),
                    odlFilter.getFilter(), odlFilter.getSecret(), odlFilter.getName());
        } else {
            LOG.error(HANDLER_CREATED_ERR);
        }
    }

    private void registerMessageTestHandler(Filter odlFilter) {
        LOG.info("registerMessageTestHandler, odlFilter {}", odlFilter);
        if (msgTestHandler == null) {
            msgTestHandler = new WebhookMessageTestHandler();
            WebhookServer.registerTypedEventHandler(msgTestHandler,
                    getEventType(odlFilter.getEvent()),
                    odlFilter.getFilter(), odlFilter.getSecret(), odlFilter.getName());
        } else {
            LOG.error(HANDLER_CREATED_ERR);
        }
    }

    private void registerRawTestHandler(Filter odlFilter) {
        LOG.info("registerRawTestHandler, odlFilter {}", odlFilter);
        if (testHandler == null) {
            testHandler = new WebhookRawTestHandler();
            WebhookServer.registerRawEventHandler(testHandler, createFilter(odlFilter));
        } else {
            LOG.error(HANDLER_CREATED_ERR);
        }
    }

    private WebhookFilter createFilter(Filter filter) {
        LOG.info("createFilter: filter {}", filter);
        if (filter != null) {
            return new WebhookFilter(getEventType(filter.getEvent()), getResourceType(filter.getResource()),
                    filter.getFilter(), filter.getSecret(), filter.getName());
        } else {
            return null;
        }
    }

    private Resources getResourceType(ResourceType resource) {
        if (resource == null) {
            return null;
        } else if (resource == ResourceType.MESSAGE) {
            return Resources.MESSAGES;
        } else if (resource == ResourceType.ROOM) {
            return Resources.ROOMS;
        } else if (resource == ResourceType.MEMBERSHIP) {
            return Resources.MEMBERSHIPS;
        } else if (resource == ResourceType.ALL) {
            return Resources.ALL;
        }
        return null;
    }

    private Events getEventType(EventType event) {
        if (event == null) {
            return null;
        } else if (event == EventType.CREATED) {
            return Events.CREATED;
        } else if (event == EventType.UPDATED) {
            return Events.UPDATED;
        } else if (event == EventType.DELETED) {
            return Events.DELETED;
        } else if (event == EventType.ALL) {
            return Events.ALL;
        }
        return null;
    }
}
