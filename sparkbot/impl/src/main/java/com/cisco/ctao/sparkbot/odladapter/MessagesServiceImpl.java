/*
 * Copyright © 2016 Cisco Systems, Inc and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.cisco.ctao.sparkbot.odladapter;

import com.cisco.ctao.sparkbot.core.Messages;
import com.cisco.ctao.sparkbot.core.SparkApi;
import com.cisco.ctao.sparkbot.core.SparkQueryParams;
import com.ciscospark.Message;
import com.google.common.base.Preconditions;

import java.util.concurrent.Future;

import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.sparkbot.messages.rev161117.CreateMessageInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.sparkbot.messages.rev161117.CreateMessageOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.sparkbot.messages.rev161117.CreateMessageOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.sparkbot.messages.rev161117.DeleteMessageInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.sparkbot.messages.rev161117.DeleteMessageOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.sparkbot.messages.rev161117.DeleteMessageOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.sparkbot.messages.rev161117.GetMessageDetailsInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.sparkbot.messages.rev161117.GetMessageDetailsOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.sparkbot.messages.rev161117.GetMessageDetailsOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.sparkbot.messages.rev161117.ListMessagesInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.sparkbot.messages.rev161117.ListMessagesOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.sparkbot.messages.rev161117.ListMessagesOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.sparkbot.messages.rev161117.SparkbotMesagesService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.sparkbot.messages.rev161117.list.messages.input.QueryParameters;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.sparkbot.messages.rev161117.list.messages.output.SparkbotMessages;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.sparkbot.messages.rev161117.list.messages.output.SparkbotMessagesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.sparkbot.messages.rev161117.message.SparkbotMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.sparkbot.messages.rev161117.message.SparkbotMessageBuilder;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Implements an ODL Proxy to the Spark 'messages' service. It's both an
 *  example of the CTAO Spark API (com.cisco.ctao.sparkapi) and a working
 *  proxy.
 *
 * @author jmedved
 *
 */
public class MessagesServiceImpl
    extends SparkProxyService<SparkbotMessage, Message, SparkbotMessages>
    implements SparkbotMesagesService {

    private static final Logger LOG = LoggerFactory.getLogger(MessagesServiceImpl.class);
    private static final String MISSING_MESSAGE_ID = "Missing Message Id";
    private static final String MISSING_MESSAGE_DATA = "Missing Message data.";

    private static SparkQueryParams getQueryParameters(final ListMessagesInput input) {
        final SparkQueryParams queryParams = new SparkQueryParams();
        QueryParameters qp;
        if (input != null && (qp = input.getQueryParameters()) != null) {
            if (qp.getRoomId() != null) {
                queryParams.add(SparkApi.ROOM_ID_KEY, qp.getRoomId());
            }
            if (qp.getBefore() != null) {
                queryParams.add(SparkApi.BEFORE_KEY, qp.getBefore());
            }
            if (qp.getBeforeMessage() != null) {
                queryParams.add(SparkApi.BEFORE_MESSAGE_KEY, qp.getBeforeMessage());
            }
            if (qp.getMentionedPeople() != null) {
                queryParams.add(SparkApi.MENTIONED_PEOPLE_KEY, qp.getMentionedPeople());
            }
            if (qp.getMax() != null) {
                queryParams.add(SparkApi.MAX_KEY, qp.getMax().toString());
            }
        }
        return queryParams;
    }

    @Override
    protected SparkbotMessage translate(final Message msg) {
        return new SparkbotMessageBuilder()
                .setId(msg.getId())
                .setRoomId(msg.getRoomId())
                .setPersonEmail(msg.getPersonEmail())
                .setPersonId(msg.getPersonId())
                .setText(msg.getText())
                .setMarkdown(msg.getMarkdown())
                .setCreated(msg.getCreated().toString())
                .setRoomType(msg.getRoomType())
                .build();
    }

    @Override
    protected SparkbotMessages buildListElement(final Message msg) {
        return new SparkbotMessagesBuilder()
                .setSparkbotMessage(translate(msg))
                .build();
    }

    public MessagesServiceImpl() {
        super(Messages.api());
    }

    @Override
    public Future<RpcResult<ListMessagesOutput>> listMessages(final ListMessagesInput input) {
        LOG.info("listMessages input: {}", input);

        final SparkProxyService<SparkbotMessage, Message, SparkbotMessages>.ReturnValue result =
                list(getQueryParameters(input));
        return RpcResultBuilder.success(new ListMessagesOutputBuilder()
                .setSparkbotMessages(result.getList())
                .setReturnStatus(result.getRetCode())
                .setErrorMessage(result.getErrorMessage()).build()).buildFuture();
    }

    @Override
    public Future<RpcResult<GetMessageDetailsOutput>> getMessageDetails(final GetMessageDetailsInput input) {
        LOG.info("getMessageDetails input: {}", input);
        Preconditions.checkArgument(input != null, MISSING_MESSAGE_ID);

        final SparkProxyService<SparkbotMessage, Message, SparkbotMessages>.ReturnValue result =
                getDetails(input.getMessageId());
        return RpcResultBuilder.success(new GetMessageDetailsOutputBuilder()
                .setSparkbotMessage(result.getElement())
                .setReturnStatus(result.getRetCode())
                .setErrorMessage(result.getErrorMessage()).build()).buildFuture();
    }

    @Override
    public Future<RpcResult<CreateMessageOutput>> createMessage(final CreateMessageInput input) {
        LOG.info("createMessage input: {}", input);
        Preconditions.checkArgument(input != null, MISSING_MESSAGE_DATA);

        final Message message = new Message();
        message.setRoomId(input.getRoomId());
        message.setPersonId(input.getToPersonId());
        message.setPersonEmail(input.getToPersonEmail());
        message.setText(input.getText());
        message.setMarkdown(input.getMarkdown());

        final SparkProxyService<SparkbotMessage, Message, SparkbotMessages>.ReturnValue result =
                create(message);
        return RpcResultBuilder.success(new CreateMessageOutputBuilder()
                .setSparkbotMessage(result.getElement())
                .setReturnStatus(result.getRetCode())
                .setErrorMessage(result.getErrorMessage()).build()).buildFuture();
    }

    @Override
    public Future<RpcResult<DeleteMessageOutput>> deleteMessage(final DeleteMessageInput input) {
        LOG.info("deleteMessage input: {}", input);
        Preconditions.checkArgument(input != null, MISSING_MESSAGE_ID);

        final SparkProxyService<SparkbotMessage, Message, SparkbotMessages>.ReturnValue result =
                delete(input.getMessageId());
        return RpcResultBuilder.success(new DeleteMessageOutputBuilder()
                .setReturnStatus(result.getRetCode())
                .setErrorMessage(result.getErrorMessage()).build()).buildFuture();
    }
}
