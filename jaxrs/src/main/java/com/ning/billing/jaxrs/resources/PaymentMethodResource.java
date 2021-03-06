/*
 * Copyright 2010-2011 Ning, Inc.
 *
 * Ning licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package com.ning.billing.jaxrs.resources;

import java.util.UUID;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.ning.billing.account.api.Account;
import com.ning.billing.account.api.AccountApiException;
import com.ning.billing.account.api.AccountUserApi;
import com.ning.billing.jaxrs.json.PaymentMethodJson;
import com.ning.billing.jaxrs.util.Context;
import com.ning.billing.jaxrs.util.JaxrsUriBuilder;
import com.ning.billing.payment.api.PaymentApi;
import com.ning.billing.payment.api.PaymentApiException;
import com.ning.billing.payment.api.PaymentMethod;
import com.ning.billing.util.api.CustomFieldUserApi;
import com.ning.billing.util.api.TagUserApi;
import com.ning.billing.util.dao.ObjectType;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Singleton
@Path(JaxrsResource.PAYMENT_METHODS_PATH)
public class PaymentMethodResource extends JaxRsResourceBase {

    private final PaymentApi paymentApi;
    private final AccountUserApi accountApi;
    private final Context context;

    @Inject
    public PaymentMethodResource(final JaxrsUriBuilder uriBuilder,
                                 final AccountUserApi accountApi,
                                 final PaymentApi paymentApi,
                                 final TagUserApi tagUserApi,
                                 final CustomFieldUserApi customFieldUserApi,
                                 final Context context) {
        super(uriBuilder, tagUserApi, customFieldUserApi);
        this.paymentApi = paymentApi;
        this.accountApi = accountApi;
        this.context = context;
    }

    @GET
    @Path("/{paymentMethodId:" + UUID_PATTERN + "}")
    @Produces(APPLICATION_JSON)
    public Response getPaymentMethod(@PathParam("paymentMethodId") final String paymentMethodId,
                                     @QueryParam(QUERY_PAYMENT_METHOD_PLUGIN_INFO) @DefaultValue("false") final Boolean withPluginInfo) throws AccountApiException, PaymentApiException {
        PaymentMethod paymentMethod = paymentApi.getPaymentMethodById(UUID.fromString(paymentMethodId));
        final Account account = accountApi.getAccountById(paymentMethod.getAccountId());
        if (withPluginInfo) {
            paymentMethod = paymentApi.getPaymentMethod(account, paymentMethod.getId(), true);
        }
        final PaymentMethodJson json = PaymentMethodJson.toPaymentMethodJson(account, paymentMethod);

        return Response.status(Status.OK).entity(json).build();
    }

    @PUT
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    @Path("/{paymentMethodId:" + UUID_PATTERN + "}")
    public Response updatePaymentMethod(final PaymentMethodJson json,
                                        @PathParam("paymentMethodId") final String paymentMethodId) throws PaymentApiException, AccountApiException {
        final PaymentMethod input = json.toPaymentMethod();
        final PaymentMethod paymentMethod = paymentApi.getPaymentMethodById(UUID.fromString(paymentMethodId));
        final Account account = accountApi.getAccountById(paymentMethod.getAccountId());

        paymentApi.updatePaymentMethod(account, paymentMethod.getId(), input.getPluginDetail());

        return getPaymentMethod(paymentMethod.getId().toString(), false);
    }

    @DELETE
    @Produces(APPLICATION_JSON)
    @Path("/{paymentMethodId:" + UUID_PATTERN + "}")
    public Response deletePaymentMethod(@PathParam("paymentMethodId") final String paymentMethodId,
                                        @QueryParam(QUERY_DELETE_DEFAULT_PM_WITH_AUTO_PAY_OFF) @DefaultValue("false") final Boolean deleteDefaultPaymentMethodWithAutoPayOff,
                                        @HeaderParam(HDR_CREATED_BY) final String createdBy,
                                        @HeaderParam(HDR_REASON) final String reason,
                                        @HeaderParam(HDR_COMMENT) final String comment) throws PaymentApiException, AccountApiException {
        final PaymentMethod paymentMethod = paymentApi.getPaymentMethodById(UUID.fromString(paymentMethodId));
        final Account account = accountApi.getAccountById(paymentMethod.getAccountId());

        paymentApi.deletedPaymentMethod(account, UUID.fromString(paymentMethodId), deleteDefaultPaymentMethodWithAutoPayOff, context.createContext(createdBy, reason, comment));

        return Response.status(Status.OK).build();
    }

    @Override
    protected ObjectType getObjectType() {
        return ObjectType.PAYMENT_METHOD;
    }
}
