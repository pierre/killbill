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

import java.util.UUID;

import org.joda.time.DateTime;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.ning.billing.jaxrs.JaxrsTestSuite;
import com.ning.billing.util.ChangeType;
import com.ning.billing.util.audit.AuditLog;
import com.ning.billing.util.audit.DefaultAuditLog;
import com.ning.billing.util.callcontext.CallContext;
import com.ning.billing.util.callcontext.CallOrigin;
import com.ning.billing.util.callcontext.DefaultCallContext;
import com.ning.billing.util.callcontext.UserType;
import com.ning.billing.util.clock.Clock;
import com.ning.billing.util.clock.ClockMock;
import com.ning.billing.util.clock.DefaultClock;
import com.ning.billing.util.dao.EntityAudit;
import com.ning.billing.util.dao.TableName;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.joda.JodaModule;

public class TestAuditLogJson extends JaxrsTestSuite {

    private static final ObjectMapper mapper = new ObjectMapper();

    private final Clock clock = new DefaultClock();

    static {
        mapper.registerModule(new JodaModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Test(groups = "fast")
    public void testJson() throws Exception {
        final String changeType = UUID.randomUUID().toString();
        final DateTime changeDate = clock.getUTCNow();
        final String changedBy = UUID.randomUUID().toString();
        final String reasonCode = UUID.randomUUID().toString();
        final String comments = UUID.randomUUID().toString();
        final String userToken = UUID.randomUUID().toString();

        final AuditLogJson auditLogJson = new AuditLogJson(changeType, changeDate, changedBy, reasonCode, comments, userToken);
        Assert.assertEquals(auditLogJson.getChangeType(), changeType);
        Assert.assertEquals(auditLogJson.getChangeDate(), changeDate);
        Assert.assertEquals(auditLogJson.getChangedBy(), changedBy);
        Assert.assertEquals(auditLogJson.getReasonCode(), reasonCode);
        Assert.assertEquals(auditLogJson.getComments(), comments);
        Assert.assertEquals(auditLogJson.getUserToken(), userToken);

        final String asJson = mapper.writeValueAsString(auditLogJson);
        Assert.assertEquals(asJson, "{\"changeType\":\"" + auditLogJson.getChangeType() + "\"," +
                                    "\"changeDate\":\"" + auditLogJson.getChangeDate().toDateTimeISO().toString() + "\"," +
                                    "\"changedBy\":\"" + auditLogJson.getChangedBy() + "\"," +
                                    "\"reasonCode\":\"" + auditLogJson.getReasonCode() + "\"," +
                                    "\"comments\":\"" + auditLogJson.getComments() + "\"," +
                                    "\"userToken\":\"" + auditLogJson.getUserToken() + "\"}");

        final AuditLogJson fromJson = mapper.readValue(asJson, AuditLogJson.class);
        Assert.assertEquals(fromJson, auditLogJson);
    }

    @Test(groups = "fast")
    public void testConstructor() throws Exception {
        final TableName tableName = TableName.ACCOUNT_EMAIL_HISTORY;
        final long recordId = Long.MAX_VALUE;
        final ChangeType changeType = ChangeType.DELETE;
        final EntityAudit entityAudit = new EntityAudit(tableName, recordId, changeType);

        final String userName = UUID.randomUUID().toString();
        final CallOrigin callOrigin = CallOrigin.EXTERNAL;
        final UserType userType = UserType.CUSTOMER;
        final UUID userToken = UUID.randomUUID();
        final ClockMock clock = new ClockMock();
        final CallContext callContext = new DefaultCallContext(userName, callOrigin, userType, userToken, clock);

        final AuditLog auditLog = new DefaultAuditLog(entityAudit, callContext);

        final AuditLogJson auditLogJson = new AuditLogJson(auditLog);
        Assert.assertEquals(auditLogJson.getChangeType(), changeType.toString());
        Assert.assertNotNull(auditLogJson.getChangeDate());
        Assert.assertEquals(auditLogJson.getChangedBy(), userName);
        Assert.assertNull(auditLogJson.getReasonCode());
        Assert.assertNull(auditLogJson.getComments());
        Assert.assertEquals(auditLogJson.getUserToken(), userToken.toString());
    }
}
