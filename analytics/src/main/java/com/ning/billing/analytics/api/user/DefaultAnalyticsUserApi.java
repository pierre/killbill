/*
 * Copyright 2010-2012 Ning, Inc.
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

package com.ning.billing.analytics.api.user;

import java.util.List;
import java.util.UUID;

import javax.inject.Inject;

import com.ning.billing.analytics.api.TimeSeriesData;
import com.ning.billing.analytics.dao.AnalyticsDao;
import com.ning.billing.analytics.model.BusinessAccount;
import com.ning.billing.analytics.model.BusinessAccountTag;
import com.ning.billing.analytics.model.BusinessInvoice;
import com.ning.billing.analytics.model.BusinessInvoiceItem;
import com.ning.billing.analytics.model.BusinessInvoicePayment;
import com.ning.billing.analytics.model.BusinessOverdueStatus;
import com.ning.billing.analytics.model.BusinessSubscriptionTransition;

public class DefaultAnalyticsUserApi implements AnalyticsUserApi {

    private final AnalyticsDao analyticsDao;

    @Inject
    public DefaultAnalyticsUserApi(final AnalyticsDao analyticsDao) {
        this.analyticsDao = analyticsDao;
    }

    @Override
    public TimeSeriesData getAccountsCreatedOverTime() {
        return analyticsDao.getAccountsCreatedOverTime();
    }

    @Override
    public TimeSeriesData getSubscriptionsCreatedOverTime(final String productType, final String slug) {
        return analyticsDao.getSubscriptionsCreatedOverTime(productType, slug);
    }

    // Note: the following is not exposed in api yet, as the models need to be extracted first

    public BusinessAccount getAccountByKey(final String accountKey) {
        return analyticsDao.getAccountByKey(accountKey);
    }

    public List<BusinessSubscriptionTransition> getTransitionsForBundle(final String externalKey) {
        return analyticsDao.getTransitionsByKey(externalKey);
    }

    public List<BusinessInvoice> getInvoicesForAccount(final String accountKey) {
        return analyticsDao.getInvoicesByKey(accountKey);
    }

    public List<BusinessAccountTag> getTagsForAccount(final String accountKey) {
        return analyticsDao.getTagsForAccount(accountKey);
    }

    public List<BusinessOverdueStatus> getOverdueStatusesForBundle(final String externalKey) {
        return analyticsDao.getOverdueStatusesForBundleByKey(externalKey);
    }

    public List<BusinessInvoiceItem> getInvoiceItemsForInvoice(final UUID invoiceId) {
        return analyticsDao.getInvoiceItemsForInvoice(invoiceId.toString());
    }

    public List<BusinessInvoicePayment> getInvoicePaymentsForAccount(final String accountKey) {
        return analyticsDao.getInvoicePaymentsForAccountByKey(accountKey);
    }
}
