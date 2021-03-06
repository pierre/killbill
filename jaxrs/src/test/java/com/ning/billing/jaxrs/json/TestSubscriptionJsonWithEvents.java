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

import java.util.List;
import java.util.UUID;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.ning.billing.catalog.api.BillingPeriod;
import com.ning.billing.catalog.api.PhaseType;
import com.ning.billing.catalog.api.PlanPhaseSpecifier;
import com.ning.billing.catalog.api.ProductCategory;
import com.ning.billing.entitlement.api.SubscriptionTransitionType;
import com.ning.billing.entitlement.api.timeline.SubscriptionTimeline;
import com.ning.billing.entitlement.api.user.Subscription;
import com.ning.billing.jaxrs.JaxrsTestSuite;
import com.ning.billing.util.audit.AuditLog;
import com.ning.billing.util.clock.DefaultClock;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

public class TestSubscriptionJsonWithEvents extends JaxrsTestSuite {

    @Test(groups = "fast")
    public void testJson() throws Exception {
        final String subscriptionId = UUID.randomUUID().toString();
        final DateTime requestedDate = DefaultClock.toUTCDateTime(new DateTime(DateTimeZone.UTC));
        final DateTime effectiveDate = DefaultClock.toUTCDateTime(new DateTime(DateTimeZone.UTC));
        final UUID eventId = UUID.randomUUID();
        final List<AuditLogJson> auditLogs = createAuditLogsJson();
        final SubscriptionJsonWithEvents.SubscriptionReadEventJson newEvent = new SubscriptionJsonWithEvents.SubscriptionReadEventJson(eventId.toString(),
                                                                                                                                       BillingPeriod.NO_BILLING_PERIOD.toString(),
                                                                                                                                       requestedDate,
                                                                                                                                       effectiveDate,
                                                                                                                                       UUID.randomUUID().toString(),
                                                                                                                                       UUID.randomUUID().toString(),
                                                                                                                                       SubscriptionTransitionType.CREATE.toString(),
                                                                                                                                       PhaseType.DISCOUNT.toString(),
                                                                                                                                       auditLogs);
        final SubscriptionJsonWithEvents subscriptionJsonWithEvents = new SubscriptionJsonWithEvents(subscriptionId,
                                                                                                     ImmutableList.<SubscriptionJsonWithEvents.SubscriptionReadEventJson>of(newEvent),
                                                                                                     null,
                                                                                                     null,
                                                                                                     null);

        final String asJson = mapper.writeValueAsString(subscriptionJsonWithEvents);

        final SubscriptionJsonWithEvents fromJson = mapper.readValue(asJson, SubscriptionJsonWithEvents.class);
        Assert.assertEquals(fromJson, subscriptionJsonWithEvents);
    }

    @Test(groups = "fast")
    public void testFromSubscription() throws Exception {
        final DateTime requestedDate = DefaultClock.toUTCDateTime(new DateTime(DateTimeZone.UTC));
        final DateTime effectiveDate = DefaultClock.toUTCDateTime(new DateTime(DateTimeZone.UTC));
        final UUID eventId = UUID.randomUUID();
        final List<AuditLogJson> auditLogs = createAuditLogsJson();
        final SubscriptionJsonWithEvents.SubscriptionReadEventJson newEvent = new SubscriptionJsonWithEvents.SubscriptionReadEventJson(eventId.toString(),
                                                                                                                                       BillingPeriod.NO_BILLING_PERIOD.toString(),
                                                                                                                                       requestedDate,
                                                                                                                                       effectiveDate,
                                                                                                                                       UUID.randomUUID().toString(),
                                                                                                                                       UUID.randomUUID().toString(),
                                                                                                                                       SubscriptionTransitionType.CREATE.toString(),
                                                                                                                                       PhaseType.DISCOUNT.toString(),
                                                                                                                                       auditLogs);

        final Subscription subscription = Mockito.mock(Subscription.class);
        Mockito.when(subscription.getId()).thenReturn(UUID.randomUUID());

        final SubscriptionJsonWithEvents subscriptionJsonWithEvents = new SubscriptionJsonWithEvents(subscription,
                                                                                                     ImmutableList.<SubscriptionJsonWithEvents.SubscriptionReadEventJson>of(newEvent),
                                                                                                     null,
                                                                                                     null,
                                                                                                     null);
        Assert.assertEquals(subscriptionJsonWithEvents.getSubscriptionId(), subscription.getId().toString());
        Assert.assertNull(subscriptionJsonWithEvents.getNewEvents());
        Assert.assertNull(subscriptionJsonWithEvents.getDeletedEvents());
        Assert.assertEquals(subscriptionJsonWithEvents.getEvents().size(), 1);
        Assert.assertEquals(subscriptionJsonWithEvents.getEvents().get(0).getEffectiveDate(), newEvent.getEffectiveDate());
        Assert.assertEquals(subscriptionJsonWithEvents.getEvents().get(0).getEventId(), newEvent.getEventId());
        Assert.assertEquals(subscriptionJsonWithEvents.getEvents().get(0).getAuditLogs(), auditLogs);
    }

    @Test(groups = "fast")
    public void testFromSubscriptionTimeline() throws Exception {
        final SubscriptionTimeline.ExistingEvent event = Mockito.mock(SubscriptionTimeline.ExistingEvent.class);
        final DateTime effectiveDate = DefaultClock.toUTCDateTime(new DateTime(DateTimeZone.UTC));
        final UUID eventId = UUID.randomUUID();
        final PlanPhaseSpecifier planPhaseSpecifier = new PlanPhaseSpecifier(UUID.randomUUID().toString(), ProductCategory.BASE,
                                                                             BillingPeriod.NO_BILLING_PERIOD, UUID.randomUUID().toString(),
                                                                             PhaseType.EVERGREEN);
        Mockito.when(event.getEffectiveDate()).thenReturn(effectiveDate);
        Mockito.when(event.getEventId()).thenReturn(eventId);
        Mockito.when(event.getSubscriptionTransitionType()).thenReturn(SubscriptionTransitionType.CREATE);
        Mockito.when(event.getPlanPhaseSpecifier()).thenReturn(planPhaseSpecifier);

        final SubscriptionTimeline subscriptionTimeline = Mockito.mock(SubscriptionTimeline.class);
        Mockito.when(subscriptionTimeline.getId()).thenReturn(UUID.randomUUID());
        Mockito.when(subscriptionTimeline.getExistingEvents()).thenReturn(ImmutableList.<SubscriptionTimeline.ExistingEvent>of(event));

        final UUID bundleId = UUID.randomUUID();

        final SubscriptionJsonWithEvents subscriptionJsonWithEvents = new SubscriptionJsonWithEvents(bundleId, subscriptionTimeline,
                                                                                                     null, ImmutableMap.<UUID, List<AuditLog>>of());
        Assert.assertEquals(subscriptionJsonWithEvents.getSubscriptionId(), subscriptionTimeline.getId().toString());
        Assert.assertNull(subscriptionJsonWithEvents.getNewEvents());
        Assert.assertNull(subscriptionJsonWithEvents.getDeletedEvents());
        Assert.assertEquals(subscriptionJsonWithEvents.getEvents().size(), 1);
        Assert.assertEquals(subscriptionJsonWithEvents.getEvents().get(0).getEffectiveDate(), event.getEffectiveDate());
        Assert.assertEquals(subscriptionJsonWithEvents.getEvents().get(0).getEventId(), event.getEventId().toString());
    }
}
