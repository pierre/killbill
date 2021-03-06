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

package com.ning.billing.overdue.notification;

import org.joda.time.DateTime;

import com.ning.billing.junction.api.Blockable;
import com.ning.billing.ovedue.notification.OverdueCheckPoster;

public class MockOverdueCheckPoster implements OverdueCheckPoster {

    @Override
    public void insertOverdueCheckNotification(final Blockable overdueable,
                                               final DateTime futureNotificationTime) {
        // TODO Auto-generated method stub

    }

    @Override
    public void clearNotificationsFor(final Blockable blockable) {
        // TODO Auto-generated method stub

    }
}
