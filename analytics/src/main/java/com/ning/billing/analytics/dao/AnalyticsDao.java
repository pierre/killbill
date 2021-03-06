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

package com.ning.billing.analytics.dao;

import java.util.List;

import com.ning.billing.analytics.api.TimeSeriesData;
import com.ning.billing.analytics.model.BusinessAccount;
import com.ning.billing.analytics.model.BusinessAccountTag;
import com.ning.billing.analytics.model.BusinessInvoice;
import com.ning.billing.analytics.model.BusinessInvoiceItem;
import com.ning.billing.analytics.model.BusinessInvoicePayment;
import com.ning.billing.analytics.model.BusinessOverdueStatus;
import com.ning.billing.analytics.model.BusinessSubscriptionTransition;

public interface AnalyticsDao {

    TimeSeriesData getAccountsCreatedOverTime();

    TimeSeriesData getSubscriptionsCreatedOverTime(final String productType, final String slug);

    BusinessAccount getAccountByKey(final String accountKey);

    List<BusinessSubscriptionTransition> getTransitionsByKey(final String externalKey);

    List<BusinessInvoice> getInvoicesByKey(final String accountKey);

    List<BusinessAccountTag> getTagsForAccount(final String accountKey);

    List<BusinessInvoiceItem> getInvoiceItemsForInvoice(final String invoiceId);

    List<BusinessOverdueStatus> getOverdueStatusesForBundleByKey(final String externalKey);

    List<BusinessInvoicePayment> getInvoicePaymentsForAccountByKey(final String accountKey);
}
