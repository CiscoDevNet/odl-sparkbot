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
import com.cisco.ctao.sparkbot.core.testhandlers.WebhookRoomTestHandler;
import com.cisco.ctao.sparkbot.core.testhandlers.WebhookTestHandler;
import com.cisco.ctao.sparkbot.core.webhooksvr.WebhookServer;

import java.util.concurrent.Future;

import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.sparkbot.commons.rev161110.ReturnCode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.sparkbot.handlers.rev161118.RegisterTestHandlerInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.sparkbot.handlers.rev161118.RegisterTestHandlerOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.sparkbot.handlers.rev161118.RegisterTestHandlerOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.sparkbot.handlers.rev161118.SparkbotHandlersService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.sparkbot.handlers.rev161118.UnregisterTestHandlerInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.sparkbot.handlers.rev161118.UnregisterTestHandlerOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.sparkbot.handlers.rev161118.UnregisterTestHandlerOutputBuilder;
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

    private WebhookTestHandler testHandler = null;
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
                        WebhookServer.getInstance().unregisterWebhookHandler(testHandler);
                        testHandler = null;
                    }
                    break;
                case MESSAGES:
                    if (msgTestHandler != null) {
                        WebhookServer.getInstance().unregisterWebhookMessageHandler(msgTestHandler);
                        msgTestHandler = null;
                    }
                    break;
                case ROOMS:
                    if (roomTestHandler != null) {
                        WebhookServer.getInstance().unregisterWebhookRoomHandler(roomTestHandler);
                        roomTestHandler = null;
                    }
                    break;
                case MEMBERSHIPS:
                    if (membershipTestHandler != null) {
                        WebhookServer.getInstance().unregisterWebhookMembershipHandler(membershipTestHandler);
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
        LOG.info("Registering test handler, input {}", input);
        final RegisterTestHandlerOutputBuilder ob =
                new RegisterTestHandlerOutputBuilder().setReturnStatus(ReturnCode.OK);

        if (input != null) {
            switch (input.getHandlerType()) {
                case RAW:
                    if (testHandler == null) {
                        testHandler = new WebhookTestHandler();
                        WebhookServer.getInstance().registerWebhookHandler(testHandler);
                    }
                    break;
                case MESSAGES:
                    if (msgTestHandler == null) {
                        msgTestHandler = new WebhookMessageTestHandler();
                        WebhookServer.getInstance().registerWebhookMessageHandler(msgTestHandler);
                    }
                    break;
                case ROOMS:
                    if (roomTestHandler == null) {
                        roomTestHandler = new WebhookRoomTestHandler();
                        WebhookServer.getInstance().registerWebhookRoomHandler(roomTestHandler);
                    }
                    break;
                case MEMBERSHIPS:
                    if (membershipTestHandler == null) {
                        membershipTestHandler = new WebhookMembershipTestHandler();
                        WebhookServer.getInstance().registerWebhookMembershipHandler(membershipTestHandler);
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

}
