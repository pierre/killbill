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

package com.ning.billing.overdue;

import org.skife.config.Config;
import org.skife.config.Default;

import com.ning.billing.config.KillbillConfig;
import com.ning.billing.config.NotificationConfig;


public interface OverdueProperties extends NotificationConfig, KillbillConfig {
    @Override
    @Config("killbill.overdue.engine.notifications.sleep")
    @Default("500")
    public long getSleepTimeMs();

    @Override
    @Config("killbill.overdue.engine.notifications.off")
    @Default("false")
    public boolean isNotificationProcessingOff();

    @Config("killbill.overdue.maxNumberOfMonthsInFuture")
    @Default("36")
    public int getNumberOfMonthsInFuture();

    @Config("killbill.overdue.uri")
    @Default("jar:///com/ning/billing/irs/overdue/Config.xml")
    public String getConfigURI();
}
