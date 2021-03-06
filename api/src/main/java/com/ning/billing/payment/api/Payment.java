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
package com.ning.billing.payment.api;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.joda.time.DateTime;

import com.ning.billing.catalog.api.Currency;
import com.ning.billing.util.entity.Entity;

public interface Payment extends Entity {

    public UUID getAccountId();

    public UUID getInvoiceId();

    public UUID getPaymentMethodId();

    public Integer getPaymentNumber();

    public BigDecimal getAmount();

    public BigDecimal getPaidAmount();

    public DateTime getEffectiveDate();

    public Currency getCurrency();

    public PaymentStatus getPaymentStatus();

    public List<PaymentAttempt> getAttempts();

    public String getExtFirstPaymentIdRef();

    public String getExtSecondPaymentIdRef();

    public interface PaymentAttempt {

        public UUID getId();

        public DateTime getEffectiveDate();

        public String getGatewayErrorCode();

        public String getGatewayErrorMsg();

        public PaymentStatus getPaymentStatus();
    }
}
