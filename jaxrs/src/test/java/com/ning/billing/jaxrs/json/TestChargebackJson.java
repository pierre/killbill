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

package com.ning.billing.jaxrs.json;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.ning.billing.jaxrs.JaxrsTestSuite;

public class TestChargebackJson extends JaxrsTestSuite {
    private static final ObjectMapper mapper = new ObjectMapper();

    static {
        mapper.registerModule(new JodaModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Test(groups = "fast")
    public void testJson() throws Exception {
        final DateTime requestedDate = new DateTime(DateTimeZone.UTC);
        final DateTime effectiveDate = new DateTime(DateTimeZone.UTC);
        final BigDecimal chargebackAmount = BigDecimal.TEN;
        final String paymentId = UUID.randomUUID().toString();
        final String reason = UUID.randomUUID().toString();
        final List<AuditLogJson> auditLogs = createAuditLogsJson();
        final ChargebackJson chargebackJson = new ChargebackJson(requestedDate, effectiveDate, chargebackAmount, paymentId,
                                                                 reason, auditLogs);
        Assert.assertEquals(chargebackJson.getRequestedDate(), requestedDate);
        Assert.assertEquals(chargebackJson.getEffectiveDate(), effectiveDate);
        Assert.assertEquals(chargebackJson.getChargebackAmount(), chargebackAmount);
        Assert.assertEquals(chargebackJson.getPaymentId(), paymentId);
        Assert.assertEquals(chargebackJson.getReason(), reason);
        Assert.assertEquals(chargebackJson.getAuditLogs(), auditLogs);

        final String asJson = mapper.writeValueAsString(chargebackJson);
        final ChargebackJson fromJson = mapper.readValue(asJson, ChargebackJson.class);
        Assert.assertEquals(fromJson, chargebackJson);
    }
}
