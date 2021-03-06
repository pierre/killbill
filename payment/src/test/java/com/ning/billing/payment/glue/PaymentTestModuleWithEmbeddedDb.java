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

package com.ning.billing.payment.glue;

import java.util.HashMap;
import java.util.UUID;

import org.mockito.Mockito;

import com.google.inject.Provider;
import com.ning.billing.config.PaymentConfig;
import com.ning.billing.payment.provider.MockPaymentProviderPluginModule;
import com.ning.billing.util.api.TagUserApi;
import com.ning.billing.util.bus.Bus;
import com.ning.billing.util.bus.InMemoryBus;
import com.ning.billing.util.dao.ObjectType;
import com.ning.billing.util.glue.GlobalLockerModule;
import com.ning.billing.util.notificationq.DefaultNotificationQueueService;
import com.ning.billing.util.notificationq.NotificationQueueService;
import com.ning.billing.util.tag.Tag;

public class PaymentTestModuleWithEmbeddedDb extends PaymentModule {
    public static class MockTagApiProvider implements Provider<TagUserApi> {
        @Override
        public TagUserApi get() {
            final TagUserApi api = Mockito.mock(TagUserApi.class);
            Mockito.when(api.getTags(Mockito.<UUID>any(), Mockito.<ObjectType>any())).thenReturn(new HashMap<String, Tag>());
            return api;
        }
    }

    @Override
    protected void installPaymentProviderPlugins(final PaymentConfig config) {
        install(new MockPaymentProviderPluginModule("my-mock"));
    }

    @Override
    protected void configure() {
        super.configure();
        bind(Bus.class).to(InMemoryBus.class).asEagerSingleton();
        bind(NotificationQueueService.class).to(DefaultNotificationQueueService.class).asEagerSingleton();
        install(new GlobalLockerModule());
        bind(TagUserApi.class).toProvider(MockTagApiProvider.class).asEagerSingleton();
    }
}
