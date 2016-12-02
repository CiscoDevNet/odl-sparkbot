/*
 * Copyright © 2016 Cisco Systems, Inc and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.cisco.ctao.sparkproxy;

import com.cisco.ctao.sparkapi.SparkApi;
import com.cisco.ctao.sparkapi.SparkQueryParams;
import com.cisco.ctao.sparkapi.Webhooks;
import com.ciscospark.Webhook;
import com.google.common.base.Preconditions;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.Future;

import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.sparkbot.commons.rev161110.ReturnCode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.sparkbot.webhooks.rev161117.CreateWebhookInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.sparkbot.webhooks.rev161117.CreateWebhookOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.sparkbot.webhooks.rev161117.CreateWebhookOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.sparkbot.webhooks.rev161117.DeleteWebhookInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.sparkbot.webhooks.rev161117.DeleteWebhookOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.sparkbot.webhooks.rev161117.DeleteWebhookOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.sparkbot.webhooks.rev161117.GetWebhookDetailsInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.sparkbot.webhooks.rev161117.GetWebhookDetailsOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.sparkbot.webhooks.rev161117.GetWebhookDetailsOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.sparkbot.webhooks.rev161117.ListWebhooksInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.sparkbot.webhooks.rev161117.ListWebhooksOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.sparkbot.webhooks.rev161117.ListWebhooksOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.sparkbot.webhooks.rev161117.SparkbotWebhooksService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.sparkbot.webhooks.rev161117.UpdateWebhookInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.sparkbot.webhooks.rev161117.UpdateWebhookOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.sparkbot.webhooks.rev161117.UpdateWebhookOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.sparkbot.webhooks.rev161117.list.webhooks.input.QueryParameters;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.sparkbot.webhooks.rev161117.list.webhooks.output.SparkbotWebhooks;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.sparkbot.webhooks.rev161117.list.webhooks.output.SparkbotWebhooksBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.sparkbot.webhooks.rev161117.webhook.SparkbotWebhook;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.sparkbot.webhooks.rev161117.webhook.SparkbotWebhookBuilder;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** This class implements a yang-defined proxy-service to the Spark 'Webhooks' API.
 * @author jmedved
 *
 */
public class WebhooksServiceImpl
    extends SparkProxyService<SparkbotWebhook, Webhook, SparkbotWebhooks>
    implements SparkbotWebhooksService {

    private static final Logger LOG = LoggerFactory.getLogger(WebhooksServiceImpl.class);
    private static final String MISSING_WEBHOOK_ID = "Missing Webhook Id";
    private static final String MISSING_WEBHOOK_DATA = "Missing Webhook data.";

    private static SparkQueryParams getQueryParameters(final ListWebhooksInput input) {
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
    protected SparkbotWebhook translate(Webhook webhook) {
        return new SparkbotWebhookBuilder()
                .setId(webhook.getId())
                .setCreated(webhook.getCreated().toString())
                .setName(webhook.getName())
                .setEvent(webhook.getEvent())
                .setResource(webhook.getResource())
                .setTargetURL(webhook.getTargetUrl().toString())
                .build();
    }

    @Override
    protected SparkbotWebhooks buildListElement(Webhook webhook) {
        return new SparkbotWebhooksBuilder()
                .setSparkbotWebhook(translate(webhook))
                .build();
    }

    public WebhooksServiceImpl() {
        super(Webhooks.api());
    }

    @Override
    public Future<RpcResult<ListWebhooksOutput>> listWebhooks(final ListWebhooksInput input) {
        LOG.info("listWebhooks input: {}", input);

        final SparkProxyService<SparkbotWebhook, Webhook, SparkbotWebhooks>.ReturnValue result =
                list(getQueryParameters(input));
        return RpcResultBuilder.success(new ListWebhooksOutputBuilder()
                .setSparkbotWebhooks(result.getList())
                .setReturnStatus(result.getRetCode())
                .setErrorMessage(result.getErrorMessage()).build()).buildFuture();
    }

    @Override
    public Future<RpcResult<GetWebhookDetailsOutput>> getWebhookDetails(final GetWebhookDetailsInput input) {
        LOG.info("getWebhookDetails input: {}", input);
        Preconditions.checkArgument(input != null, MISSING_WEBHOOK_ID);

        final SparkProxyService<SparkbotWebhook, Webhook, SparkbotWebhooks>.ReturnValue result =
                getDetails(input.getWebhookId());
        return RpcResultBuilder.success(new GetWebhookDetailsOutputBuilder()
                .setSparkbotWebhook(result.getElement())
                .setReturnStatus(result.getRetCode())
                .setErrorMessage(result.getErrorMessage()).build()).buildFuture();
    }

    @Override
    public Future<RpcResult<CreateWebhookOutput>> createWebhook(final CreateWebhookInput input) {
        LOG.info("createWebhook input: {}", input);
        Preconditions.checkArgument(input != null, MISSING_WEBHOOK_DATA);

        try {
            final Webhook webhook = new Webhook();
            webhook.setName(input.getName());
            webhook.setTargetUrl(new URI(input.getTargetURL()));
            webhook.setResource(input.getResource());
            webhook.setFilter(input.getFilter());
            webhook.setEvent(input.getEvent());

            final SparkProxyService<SparkbotWebhook, Webhook, SparkbotWebhooks>.ReturnValue result =
                    create(webhook);
            return RpcResultBuilder.success(new CreateWebhookOutputBuilder()
                    .setSparkbotWebhook(result.getElement())
                    .setReturnStatus(result.getRetCode())
                    .setErrorMessage(result.getErrorMessage()).build()).buildFuture();
        } catch (URISyntaxException e) {
            LOG.error("createWebhook: Invalid target URI '{}m exception '", input.getTargetURL(), e);
            return RpcResultBuilder.success(new CreateWebhookOutputBuilder()
                    .setReturnStatus(ReturnCode.INVALIDPARAMETER)
                    .setErrorMessage("Invalid target URI ").build()).buildFuture();
        }
    }

    @Override
    public Future<RpcResult<UpdateWebhookOutput>> updateWebhook(final UpdateWebhookInput input) {
        LOG.info("updateWebhook input: {}", input);
        Preconditions.checkArgument(input != null, MISSING_WEBHOOK_ID);

        try {
            final Webhook webhook = new Webhook();
            webhook.setId(input.getWebhookId());
            webhook.setName(input.getName());
            webhook.setTargetUrl(new URI(input.getTargetURL()));

            final SparkProxyService<SparkbotWebhook, Webhook, SparkbotWebhooks>.ReturnValue result =
                    update(input.getWebhookId(), webhook);
            return RpcResultBuilder.success(new UpdateWebhookOutputBuilder()
                    .setSparkbotWebhook(result.getElement())
                    .setReturnStatus(result.getRetCode())
                    .setErrorMessage(result.getErrorMessage()).build()).buildFuture();
        } catch (URISyntaxException e) {
            LOG.error("createWebhook: Invalid target URI '{}, exception'", input.getTargetURL(), e);
            return RpcResultBuilder.success(new UpdateWebhookOutputBuilder()
                    .setReturnStatus(ReturnCode.INVALIDPARAMETER)
                    .setErrorMessage("Invalid target URI ").build()).buildFuture();
        }
    }

    @Override
    public Future<RpcResult<DeleteWebhookOutput>> deleteWebhook(final DeleteWebhookInput input) {
        LOG.info("deleteWebhook input: {}", input);
        Preconditions.checkArgument(input != null, MISSING_WEBHOOK_ID);

        final SparkProxyService<SparkbotWebhook, Webhook, SparkbotWebhooks>.ReturnValue result =
                delete(input.getWebhookId());
        return RpcResultBuilder.success(new DeleteWebhookOutputBuilder()
                .setReturnStatus(result.getRetCode())
                .setErrorMessage(result.getErrorMessage()).build()).buildFuture();
    }
}
