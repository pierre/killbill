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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.ning.billing.payment.api.PaymentMethod;
import com.ning.billing.payment.api.PaymentStatus;
import com.ning.billing.payment.dao.RefundModelDao.RefundStatus;
import com.ning.billing.util.callcontext.CallContext;

public class MockPaymentDao implements PaymentDao {

    private final Map<UUID, PaymentModelDao> payments = new HashMap<UUID, PaymentModelDao>();
    private final Map<UUID, PaymentAttemptModelDao> attempts = new HashMap<UUID, PaymentAttemptModelDao>();

    @Override
    public PaymentModelDao insertPaymentWithAttempt(final PaymentModelDao paymentInfo, final PaymentAttemptModelDao attempt,
                                                    final CallContext context) {
        synchronized (this) {
            payments.put(paymentInfo.getId(), paymentInfo);
            attempts.put(attempt.getId(), attempt);
        }
        return paymentInfo;
    }

    @Override
    public PaymentAttemptModelDao insertNewAttemptForPayment(final UUID paymentId,
                                                             final PaymentAttemptModelDao attempt, final CallContext context) {
        synchronized (this) {
            attempts.put(attempt.getId(), attempt);
        }
        return attempt;
    }


    @Override
    public void updateStatusForPaymentWithAttempt(UUID paymentId,
            PaymentStatus paymentStatus, String gatewayErrorCode, String gatewayErrorMsg,
            String extFirstPaymentRefId, String extSecondPaymentRefId,
            UUID attemptId, CallContext context) {
        synchronized (this) {
            final PaymentModelDao entry = payments.remove(paymentId);
            if (entry != null) {
                payments.put(paymentId, new PaymentModelDao(entry, paymentStatus));
            }
            final PaymentAttemptModelDao tmp = attempts.remove(attemptId);
            if (tmp != null) {
                attempts.put(attemptId, new PaymentAttemptModelDao(tmp, paymentStatus, gatewayErrorCode, gatewayErrorMsg));
            }
        }
    }

    @Override
    public PaymentAttemptModelDao getPaymentAttempt(final UUID attemptId) {
        return attempts.get(attemptId);
    }

    @Override
    public List<PaymentModelDao> getPaymentsForInvoice(final UUID invoiceId) {
        final List<PaymentModelDao> result = new ArrayList<PaymentModelDao>();
        synchronized (this) {
            for (final PaymentModelDao cur : payments.values()) {
                if (cur.getInvoiceId().equals(invoiceId)) {
                    result.add(cur);
                }
            }
        }
        return result;
    }

    @Override
    public List<PaymentModelDao> getPaymentsForAccount(final UUID accountId) {
        final List<PaymentModelDao> result = new ArrayList<PaymentModelDao>();
        synchronized (this) {
            for (final PaymentModelDao cur : payments.values()) {
                if (cur.getAccountId().equals(accountId)) {
                    result.add(cur);
                }
            }
        }
        return result;
    }

    @Override
    public PaymentModelDao getPayment(final UUID paymentId) {
        return payments.get(paymentId);
    }

    @Override
    public List<PaymentAttemptModelDao> getAttemptsForPayment(final UUID paymentId) {
        final List<PaymentAttemptModelDao> result = new ArrayList<PaymentAttemptModelDao>();
        synchronized (this) {
            for (final PaymentAttemptModelDao cur : attempts.values()) {
                if (cur.getPaymentId().equals(paymentId)) {
                    result.add(cur);
                }
            }
        }
        return result;
    }

    private final List<PaymentMethodModelDao> paymentMethods = new LinkedList<PaymentMethodModelDao>();

    @Override
    public PaymentMethodModelDao insertPaymentMethod(final PaymentMethodModelDao paymentMethod, final CallContext context) {
        paymentMethods.add(paymentMethod);
        return paymentMethod;
    }

    @Override
    public List<PaymentMethodModelDao> refreshPaymentMethods(final UUID accountId, final List<PaymentMethodModelDao> newPaymentMethods, final CallContext context) {
        throw new UnsupportedOperationException();
    }

    @Override
    public PaymentMethodModelDao getPaymentMethod(final UUID paymentMethodId) {
        for (final PaymentMethodModelDao cur : paymentMethods) {
            if (cur.getId().equals(paymentMethodId)) {
                return cur;
            }
        }
        return null;
    }

    @Override
    public List<PaymentMethodModelDao> getPaymentMethods(final UUID accountId) {
        final List<PaymentMethodModelDao> result = new ArrayList<PaymentMethodModelDao>();
        for (final PaymentMethodModelDao cur : paymentMethods) {
            if (cur.getAccountId().equals(accountId)) {
                result.add(cur);
            }
        }
        return result;
    }

    @Override
    public void deletedPaymentMethod(final UUID paymentMethodId) {
        final Iterator<PaymentMethodModelDao> it = paymentMethods.iterator();
        while (it.hasNext()) {
            final PaymentMethodModelDao cur = it.next();
            if (cur.getId().equals(paymentMethodId)) {
                it.remove();
                break;
            }
        }
    }

    @Override
    public void undeletedPaymentMethod(final UUID paymentMethodId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public RefundModelDao insertRefund(RefundModelDao refundInfo,
            CallContext context) {
        return null;
    }

    @Override
    public void updateRefundStatus(UUID refundId, RefundStatus status,
            CallContext context) {
    }

    @Override
    public RefundModelDao getRefund(UUID refundId) {
        return null;
    }

    @Override
    public List<RefundModelDao> getRefundsForPayment(UUID paymentId) {
        return Collections.emptyList();
    }

    @Override
    public List<RefundModelDao> getRefundsForAccount(UUID accountId) {
        return Collections.emptyList();
    }

    @Override
    public PaymentModelDao getLastPaymentForPaymentMethod(UUID accountId,
            UUID paymentMethodId) {
        return null;
    }
}
