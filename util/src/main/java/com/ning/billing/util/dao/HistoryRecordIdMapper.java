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

package com.ning.billing.util.dao;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

public class HistoryRecordIdMapper extends MapperBase implements ResultSetMapper<Mapper> {
    @Override
    public Mapper<Long, Long> map(final int index, final ResultSet resultSet, final StatementContext ctx) throws SQLException {
        final Long recordId = resultSet.getLong("record_id");
        final Long historyRecordId = resultSet.getLong("history_record_id");

        return new Mapper<Long, Long>(recordId, historyRecordId);
    }
}
