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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.skife.jdbi.v2.IDBI;
import org.skife.jdbi.v2.Transaction;
import org.skife.jdbi.v2.TransactionStatus;

import com.google.inject.Inject;

import com.ning.billing.payment.api.PaymentMethod;
import com.ning.billing.payment.api.PaymentStatus;
import com.ning.billing.payment.dao.RefundModelDao.RefundStatus;
import com.ning.billing.payment.retry.PluginFailureRetryService.PluginFailureRetryServiceScheduler;
import com.ning.billing.util.ChangeType;
import com.ning.billing.util.callcontext.CallContext;
import com.ning.billing.util.dao.EntityAudit;
import com.ning.billing.util.dao.EntityHistory;
import com.ning.billing.util.dao.TableName;

public class AuditedPaymentDao implements PaymentDao {


    private final PaymentSqlDao paymentSqlDao;
    private final PaymentAttemptSqlDao paymentAttemptSqlDao;
    private final PaymentMethodSqlDao paymentMethodSqlDao;
    private final RefundSqlDao refundSqlDao;

    @Inject
    public AuditedPaymentDao(final IDBI dbi, final PluginFailureRetryServiceScheduler timedoutSchduler) {
        this.paymentSqlDao = dbi.onDemand(PaymentSqlDao.class);
        this.paymentAttemptSqlDao = dbi.onDemand(PaymentAttemptSqlDao.class);
        this.paymentMethodSqlDao = dbi.onDemand(PaymentMethodSqlDao.class);
        this.refundSqlDao = dbi.onDemand(RefundSqlDao.class);
    }


    @Override
    public PaymentAttemptModelDao insertNewAttemptForPayment(final UUID paymentId,
                                                             final PaymentAttemptModelDao attempt, final CallContext context) {

        return paymentAttemptSqlDao.inTransaction(new Transaction<PaymentAttemptModelDao, PaymentAttemptSqlDao>() {
            @Override
            public PaymentAttemptModelDao inTransaction(final PaymentAttemptSqlDao transactional, final TransactionStatus status)
                    throws Exception {
                final PaymentAttemptModelDao savedAttempt = insertPaymentAttemptFromTransaction(attempt, context, transactional);
                final PaymentSqlDao transPaymentSqlDao = transactional.become(PaymentSqlDao.class);
                updatePaymentAmountFromTransaction(paymentId, savedAttempt.getRequestedAmount(), context, transPaymentSqlDao);
                return savedAttempt;
            }
        });
    }


    @Override
    public PaymentModelDao insertPaymentWithAttempt(final PaymentModelDao payment, final PaymentAttemptModelDao attempt, final CallContext context) {

        return paymentSqlDao.inTransaction(new Transaction<PaymentModelDao, PaymentSqlDao>() {

            @Override
            public PaymentModelDao inTransaction(final PaymentSqlDao transactional,
                                                 final TransactionStatus status) throws Exception {
                final PaymentModelDao result = insertPaymentFromTransaction(payment, context, transactional);
                final PaymentAttemptSqlDao transactionalAttempt = transactional.become(PaymentAttemptSqlDao.class);
                insertPaymentAttemptFromTransaction(attempt, context, transactionalAttempt);
                return result;
            }
        });
    }

    private PaymentModelDao insertPaymentFromTransaction(final PaymentModelDao payment, final CallContext context, final PaymentSqlDao transactional) {
        transactional.insertPayment(payment, context);
        final PaymentModelDao savedPayment = transactional.getPayment(payment.getId().toString());
        final Long recordId = transactional.getRecordId(savedPayment.getId().toString());
        final EntityHistory<PaymentModelDao> history = new EntityHistory<PaymentModelDao>(savedPayment.getId(), recordId, savedPayment, ChangeType.INSERT);
        transactional.insertHistoryFromTransaction(history, context);

        final Long historyRecordId = transactional.getHistoryRecordId(recordId);
        final EntityAudit audit = new EntityAudit(TableName.PAYMENT_HISTORY, historyRecordId, ChangeType.INSERT);
        transactional.insertAuditFromTransaction(audit, context);
        return savedPayment;
    }

    private PaymentAttemptModelDao insertPaymentAttemptFromTransaction(final PaymentAttemptModelDao attempt, final CallContext context, final PaymentAttemptSqlDao transactional) {
        transactional.insertPaymentAttempt(attempt, context);
        final PaymentAttemptModelDao savedAttempt = transactional.getPaymentAttempt(attempt.getId().toString());
        final Long recordId = transactional.getRecordId(savedAttempt.getId().toString());
        final EntityHistory<PaymentAttemptModelDao> history = new EntityHistory<PaymentAttemptModelDao>(savedAttempt.getId(), recordId, savedAttempt, ChangeType.INSERT);
        transactional.insertHistoryFromTransaction(history, context);
        final Long historyRecordId = transactional.getHistoryRecordId(recordId);
        final EntityAudit audit = new EntityAudit(TableName.PAYMENT_ATTEMPT_HISTORY, historyRecordId, ChangeType.INSERT);
        transactional.insertAuditFromTransaction(audit, context);
        return savedAttempt;
    }

    @Override
    public PaymentAttemptModelDao getPaymentAttempt(final UUID attemptId) {
        return paymentAttemptSqlDao.inTransaction(new Transaction<PaymentAttemptModelDao, PaymentAttemptSqlDao>() {
            @Override
            public PaymentAttemptModelDao inTransaction(final PaymentAttemptSqlDao transactional, final TransactionStatus status)
                    throws Exception {
                return transactional.getPaymentAttempt(attemptId.toString());
            }
        });
    }

    @Override
    public void updateStatusForPaymentWithAttempt(final UUID paymentId,
                                                  final PaymentStatus paymentStatus,
                                                  final String gatewayErrorCode,
                                                  final String gatewayErrorMsg,
                                                  final String extFirstPaymentRefId,
                                                  final String extSecondPaymentRefId,
                                                  final UUID attemptId,
                                                  final CallContext context) {
        paymentSqlDao.inTransaction(new Transaction<Void, PaymentSqlDao>() {

            @Override
            public Void inTransaction(final PaymentSqlDao transactional,
                                      final TransactionStatus status) throws Exception {
                updatePaymentStatusFromTransaction(paymentId, paymentStatus, extFirstPaymentRefId, extSecondPaymentRefId, context, transactional);
                final PaymentAttemptSqlDao transPaymentAttemptSqlDao = transactional.become(PaymentAttemptSqlDao.class);
                updatePaymentAttemptStatusFromTransaction(attemptId, paymentStatus, gatewayErrorCode, gatewayErrorMsg, context, transPaymentAttemptSqlDao);
                return null;
            }
        });
    }

    private void updatePaymentAmountFromTransaction(final UUID paymentId, final BigDecimal amount, final CallContext context, final PaymentSqlDao transactional) {
        transactional.updatePaymentAmount(paymentId.toString(), amount, context);
        final PaymentModelDao savedPayment = transactional.getPayment(paymentId.toString());
        final Long recordId = transactional.getRecordId(savedPayment.getId().toString());
        final EntityHistory<PaymentModelDao> history = new EntityHistory<PaymentModelDao>(savedPayment.getId(), recordId, savedPayment, ChangeType.UPDATE);
        transactional.insertHistoryFromTransaction(history, context);
        final Long historyRecordId = transactional.getHistoryRecordId(recordId);
        final EntityAudit audit = new EntityAudit(TableName.PAYMENT_HISTORY, historyRecordId, ChangeType.UPDATE);
        transactional.insertAuditFromTransaction(audit, context);
    }

    private void updatePaymentStatusFromTransaction(final UUID paymentId, final PaymentStatus paymentStatus, final String extFirstPaymentRefId, final String extSecondPaymentRefId, final CallContext context, final PaymentSqlDao transactional) {
        transactional.updatePaymentStatusAndExtRef(paymentId.toString(), paymentStatus.toString(), extFirstPaymentRefId, extSecondPaymentRefId, context);
        final PaymentModelDao savedPayment = transactional.getPayment(paymentId.toString());
        final Long recordId = transactional.getRecordId(savedPayment.getId().toString());
        final EntityHistory<PaymentModelDao> history = new EntityHistory<PaymentModelDao>(savedPayment.getId(), recordId, savedPayment, ChangeType.UPDATE);
        transactional.insertHistoryFromTransaction(history, context);
        final Long historyRecordId = transactional.getHistoryRecordId(recordId);
        final EntityAudit audit = new EntityAudit(TableName.PAYMENT_HISTORY, historyRecordId, ChangeType.UPDATE);
        transactional.insertAuditFromTransaction(audit, context);
    }

    private void updatePaymentAttemptStatusFromTransaction(final UUID attemptId, final PaymentStatus processingStatus, final String gatewayErrorCode, final String gatewayErrorMsg, final CallContext context, final PaymentAttemptSqlDao transactional) {
        transactional.updatePaymentAttemptStatus(attemptId.toString(), processingStatus.toString(), gatewayErrorCode, gatewayErrorMsg);
        final PaymentAttemptModelDao savedAttempt = transactional.getPaymentAttempt(attemptId.toString());
        final Long recordId = transactional.getRecordId(savedAttempt.getId().toString());
        final EntityHistory<PaymentAttemptModelDao> history = new EntityHistory<PaymentAttemptModelDao>(savedAttempt.getId(), recordId, savedAttempt, ChangeType.UPDATE);
        transactional.insertHistoryFromTransaction(history, context);
        final Long historyRecordId = transactional.getHistoryRecordId(recordId);
        final EntityAudit audit = new EntityAudit(TableName.PAYMENT_ATTEMPT_HISTORY, historyRecordId, ChangeType.UPDATE);
        transactional.insertAuditFromTransaction(audit, context);
    }

    @Override
    public PaymentMethodModelDao insertPaymentMethod(final PaymentMethodModelDao paymentMethod, final CallContext context) {
        return paymentMethodSqlDao.inTransaction(new Transaction<PaymentMethodModelDao, PaymentMethodSqlDao>() {
            @Override
            public PaymentMethodModelDao inTransaction(final PaymentMethodSqlDao transactional, final TransactionStatus status)
                    throws Exception {
                return insertPaymentMethodInTransaction(transactional, paymentMethod, context);
            }
        });
    }

    private PaymentMethodModelDao insertPaymentMethodInTransaction(final PaymentMethodSqlDao transactional, final PaymentMethodModelDao paymentMethod, final CallContext context) {
        transactional.insertPaymentMethod(paymentMethod, context);
        final PaymentMethodModelDao savedPaymentMethod = transactional.getPaymentMethod(paymentMethod.getId().toString());
        final Long recordId = transactional.getRecordId(savedPaymentMethod.getId().toString());
        final EntityHistory<PaymentMethodModelDao> history = new EntityHistory<PaymentMethodModelDao>(savedPaymentMethod.getId(), recordId, savedPaymentMethod, ChangeType.INSERT);
        transactional.insertHistoryFromTransaction(history, context);
        final Long historyRecordId = transactional.getHistoryRecordId(recordId);
        final EntityAudit audit = new EntityAudit(TableName.PAYMENT_METHOD_HISTORY, historyRecordId, ChangeType.INSERT);
        transactional.insertAuditFromTransaction(audit, context);
        return savedPaymentMethod;
    }

    @Override
    public List<PaymentMethodModelDao> refreshPaymentMethods(final UUID accountId, final List<PaymentMethodModelDao> paymentMethods, final CallContext context) {
        return paymentMethodSqlDao.inTransaction(new Transaction<List<PaymentMethodModelDao>, PaymentMethodSqlDao>() {

            @Override
            public List<PaymentMethodModelDao> inTransaction(final PaymentMethodSqlDao transactional, final TransactionStatus status) throws Exception {
                final List<PaymentMethodModelDao> existingPaymentMethods = getPaymentMethodsInTransaction(transactional, accountId);

                final Set<String> externalPaymentIdProcessed = new HashSet<String>();
                for (final PaymentMethodModelDao finalPaymentMethod : paymentMethods) {
                    boolean isExistingPaymentMethod = false;

                    for (final PaymentMethodModelDao existingPaymentMethod : existingPaymentMethods) {
                        if (existingPaymentMethod.equals(finalPaymentMethod)) {
                            // We already have it - nothing to do
                            isExistingPaymentMethod = true;
                            break;
                        } else if (existingPaymentMethod.equalsButActive(finalPaymentMethod)) {
                            // We already have it but its status has changed - update it accordingly
                            // Note - in the remote system, the payment method will always be active
                            undeletedPaymentMethodInTransaction(transactional, existingPaymentMethod.getId());
                            isExistingPaymentMethod = true;
                            break;
                        }
                        // Otherwise, we don't have it
                    }

                    if (!isExistingPaymentMethod) {
                        insertPaymentMethodInTransaction(transactional, finalPaymentMethod, context);
                    }

                    externalPaymentIdProcessed.add(finalPaymentMethod.getExternalId());
                }

                // Finally, mark as deleted the ones that don't exist in the specified list (remote system)
                for (final PaymentMethodModelDao existingPaymentMethod : existingPaymentMethods) {
                    if (!externalPaymentIdProcessed.contains(existingPaymentMethod.getExternalId())) {
                        deletedPaymentMethodInTransaction(transactional, existingPaymentMethod.getId());
                    }
                }

                return getPaymentMethodsInTransaction(transactional, accountId);
            }
        });
    }

    @Override
    public RefundModelDao insertRefund(final RefundModelDao refundInfo, final CallContext context) {
        return refundSqlDao.inTransaction(new Transaction<RefundModelDao, RefundSqlDao>() {

            @Override
            public RefundModelDao inTransaction(RefundSqlDao transactional,
                    TransactionStatus status) throws Exception {

                transactional.insertRefund(refundInfo, context);
                final RefundModelDao savedRefund = transactional.getRefund(refundInfo.getId().toString());
                final Long recordId = transactional.getRecordId(savedRefund.getId().toString());
                final EntityHistory<RefundModelDao> history = new EntityHistory<RefundModelDao>(savedRefund.getId(), recordId, savedRefund, ChangeType.INSERT);
                transactional.insertHistoryFromTransaction(history, context);
                final Long historyRecordId = transactional.getHistoryRecordId(recordId);
                final EntityAudit audit = new EntityAudit(TableName.REFUND_HISTORY, historyRecordId, ChangeType.INSERT);
                transactional.insertAuditFromTransaction(audit, context);
                return savedRefund;
            }
        });
    }


    @Override
    public void updateRefundStatus(final UUID refundId,
            final RefundStatus refundStatus, final CallContext context) {
        refundSqlDao.inTransaction(new Transaction<Void, RefundSqlDao>() {

            @Override
            public Void inTransaction(RefundSqlDao transactional,
                    TransactionStatus status) throws Exception {
                transactional.updateStatus(refundId.toString(), refundStatus.toString());

                final RefundModelDao savedRefund = transactional.getRefund(refundId.toString());
                final Long recordId = transactional.getRecordId(savedRefund.getId().toString());
                final EntityHistory<RefundModelDao> history = new EntityHistory<RefundModelDao>(savedRefund.getId(), recordId, savedRefund, ChangeType.UPDATE);
                transactional.insertHistoryFromTransaction(history, context);
                final Long historyRecordId = transactional.getHistoryRecordId(recordId);
                final EntityAudit audit = new EntityAudit(TableName.REFUND_HISTORY, historyRecordId, ChangeType.UPDATE);
                transactional.insertAuditFromTransaction(audit, context);
                return null;
            }
        });
    }

    @Override
    public RefundModelDao getRefund(final UUID refundId) {
        return refundSqlDao.inTransaction(new Transaction<RefundModelDao, RefundSqlDao>() {

            @Override
            public RefundModelDao inTransaction(RefundSqlDao transactional,
                    TransactionStatus status) throws Exception {
                return transactional.getRefund(refundId.toString());
            }
        });
    }


    @Override
    public List<RefundModelDao> getRefundsForPayment(final UUID paymentId) {
        return refundSqlDao.inTransaction(new Transaction<List<RefundModelDao>, RefundSqlDao>() {

            @Override
            public List<RefundModelDao> inTransaction(RefundSqlDao transactional,
                    TransactionStatus status) throws Exception {
                return transactional.getRefundsForPayment(paymentId.toString());
            }
        });
    }


    @Override
    public List<RefundModelDao> getRefundsForAccount(final UUID accountId) {
        return refundSqlDao.inTransaction(new Transaction<List<RefundModelDao>, RefundSqlDao>() {

            @Override
            public List<RefundModelDao> inTransaction(RefundSqlDao transactional,
                    TransactionStatus status) throws Exception {
                return transactional.getRefundsForAccount(accountId.toString());
            }
        });
    }

    @Override
    public PaymentMethodModelDao getPaymentMethod(final UUID paymentMethodId) {
        return getPaymentMethodInTransaction(paymentMethodSqlDao, paymentMethodId);
    }

    private PaymentMethodModelDao getPaymentMethodInTransaction(final PaymentMethodSqlDao transactional, final UUID paymentMethodId) {
        return transactional.getPaymentMethod(paymentMethodId.toString());
    }

    @Override
    public List<PaymentMethodModelDao> getPaymentMethods(final UUID accountId) {
        return getPaymentMethodsInTransaction(paymentMethodSqlDao, accountId);
    }

    private List<PaymentMethodModelDao> getPaymentMethodsInTransaction(final PaymentMethodSqlDao transactional, final UUID accountId) {
        return transactional.getPaymentMethods(accountId.toString());
    }

    @Override
    public void deletedPaymentMethod(final UUID paymentMethodId) {
        deletedPaymentMethodInTransaction(paymentMethodSqlDao, paymentMethodId);
    }

    private void deletedPaymentMethodInTransaction(final PaymentMethodSqlDao transactional, final UUID paymentMethodId) {
        transactional.markPaymentMethodAsDeleted(paymentMethodId.toString());
    }

    @Override
    public void undeletedPaymentMethod(final UUID paymentMethodId) {
        undeletedPaymentMethodInTransaction(paymentMethodSqlDao, paymentMethodId);
    }

    private void undeletedPaymentMethodInTransaction(final PaymentMethodSqlDao transactional, final UUID paymentMethodId) {
        transactional.unmarkPaymentMethodAsDeleted(paymentMethodId.toString());
    }

    @Override
    public List<PaymentModelDao> getPaymentsForInvoice(final UUID invoiceId) {
        return paymentSqlDao.getPaymentsForInvoice(invoiceId.toString());
    }

    @Override
    public PaymentModelDao getLastPaymentForPaymentMethod(final UUID accountId, final UUID paymentMethodId) {
        return paymentSqlDao.getLastPaymentForAccountAndPaymentMethod(accountId.toString(), paymentMethodId.toString());
    }

    @Override
    public PaymentModelDao getPayment(final UUID paymentId) {
        return paymentSqlDao.getPayment(paymentId.toString());
    }

    @Override
    public List<PaymentModelDao> getPaymentsForAccount(final UUID accountId) {
        return paymentSqlDao.getPaymentsForAccount(accountId.toString());
    }

    @Override
    public List<PaymentAttemptModelDao> getAttemptsForPayment(final UUID paymentId) {
        return paymentAttemptSqlDao.getPaymentAttempts(paymentId.toString());
    }

}
