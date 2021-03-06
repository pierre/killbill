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

package com.ning.billing.util.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import org.joda.time.DateTime;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import com.ning.billing.util.ChangeType;
import com.ning.billing.util.audit.AuditLog;
import com.ning.billing.util.audit.DefaultAuditLog;
import com.ning.billing.util.callcontext.CallContext;
import com.ning.billing.util.callcontext.DefaultCallContext;

public class AuditLogMapper extends MapperBase implements ResultSetMapper<AuditLog> {

    @Override
    public AuditLog map(final int index, final ResultSet r, final StatementContext ctx) throws SQLException {
        final String tableName = r.getString("table_name");
        final long recordId = r.getLong("record_id");
        final String changeType = r.getString("change_type");
        final DateTime changeDate = getDateTime(r, "change_date");
        final String changedBy = r.getString("changed_by");
        final String reasonCode = r.getString("reason_code");
        final String comments = r.getString("comments");
        final UUID userToken = getUUID(r, "user_token");

        final EntityAudit entityAudit = new EntityAudit(TableName.valueOf(tableName), recordId, ChangeType.valueOf(changeType));
        final CallContext callContext = new DefaultCallContext(changedBy, changeDate, reasonCode, comments, userToken);
        return new DefaultAuditLog(entityAudit, callContext);
    }
}
