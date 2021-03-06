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
package com.ning.billing.payment.dao;

import java.util.List;
import java.util.UUID;

import com.ning.billing.payment.api.PaymentMethod;
import com.ning.billing.payment.api.PaymentStatus;
import com.ning.billing.payment.dao.RefundModelDao.RefundStatus;
import com.ning.billing.util.callcontext.CallContext;

public interface PaymentDao {

    // STEPH do we need object returned?
    public PaymentModelDao insertPaymentWithAttempt(final PaymentModelDao paymentInfo, final PaymentAttemptModelDao attempt, final CallContext context);

    public PaymentAttemptModelDao insertNewAttemptForPayment(final UUID paymentId, final PaymentAttemptModelDao attempt, final CallContext context);

    public void updateStatusForPaymentWithAttempt(final UUID paymentId, final PaymentStatus paymentStatus, final String gatewayErrorCode, final String gatewayErrorMsg, final String extFirstPaymentRefId, final String extSecondPaymentRefId, final UUID attemptId, final CallContext context);

    public PaymentAttemptModelDao getPaymentAttempt(final UUID attemptId);

    public List<PaymentModelDao> getPaymentsForInvoice(final UUID invoiceId);

    public List<PaymentModelDao> getPaymentsForAccount(final UUID accountId);

    public PaymentModelDao getLastPaymentForPaymentMethod(final UUID accountId, final UUID paymentMethodId);

    public PaymentModelDao getPayment(final UUID paymentId);

    public List<PaymentAttemptModelDao> getAttemptsForPayment(final UUID paymentId);

    public RefundModelDao insertRefund(RefundModelDao refundInfo, final CallContext context);

    public void updateRefundStatus(UUID refundId, RefundStatus status, final CallContext context);

    public RefundModelDao getRefund(UUID refundId);

    public List<RefundModelDao> getRefundsForPayment(final UUID paymentId);

    public List<RefundModelDao> getRefundsForAccount(final UUID accountId);

    public PaymentMethodModelDao insertPaymentMethod(final PaymentMethodModelDao paymentMethod, final CallContext context);

    public List<PaymentMethodModelDao> refreshPaymentMethods(final UUID accountId, final List<PaymentMethodModelDao> paymentMethods, final CallContext context);

    public PaymentMethodModelDao getPaymentMethod(final UUID paymentMethodId);

    public List<PaymentMethodModelDao> getPaymentMethods(final UUID accountId);

    public void deletedPaymentMethod(final UUID paymentMethodId);

    public void undeletedPaymentMethod(final UUID paymentMethodId);
}
