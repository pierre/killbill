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

package com.ning.billing.util.notificationq;

import java.util.UUID;

import org.joda.time.DateTime;

import com.ning.billing.util.entity.EntityBase;

public class DefaultNotification extends EntityBase implements Notification {
    private final long ordering;
    private final String owner;
    private final String createdOwner;
    private final String queueName;
    private final DateTime nextAvailableDate;
    private final PersistentQueueEntryLifecycleState lifecycleState;
    private final String notificationKeyClass;
    private final String notificationKey;
    private final DateTime effectiveDate;
    private final UUID accountId;


    public DefaultNotification(final long ordering, final UUID id, final String createdOwner, final String owner, final String queueName, final DateTime nextAvailableDate,
                               final PersistentQueueEntryLifecycleState lifecycleState,
                               final String notificationKeyClass, final String notificationKey, final UUID accountId, final DateTime effectiveDate) {
        super(id);
        this.ordering = ordering;
        this.owner = owner;
        this.createdOwner = createdOwner;
        this.queueName = queueName;
        this.nextAvailableDate = nextAvailableDate;
        this.lifecycleState = lifecycleState;
        this.notificationKeyClass = notificationKeyClass;
        this.notificationKey = notificationKey;
        this.accountId = accountId;
        this.effectiveDate = effectiveDate;
    }

    public DefaultNotification(final String queueName, final String createdOwner, final String notificationKeyClass, final String notificationKey, final UUID accountId, final DateTime effectiveDate) {
        this(-1L, UUID.randomUUID(), createdOwner, null, queueName, null, PersistentQueueEntryLifecycleState.AVAILABLE, notificationKeyClass, notificationKey, accountId, effectiveDate);
    }

    @Override
    public Long getOrdering() {
        return ordering;
    }

    @Override
    public String getOwner() {
        return owner;
    }

    @Override
    public DateTime getNextAvailableDate() {
        return nextAvailableDate;
    }

    @Override
    public PersistentQueueEntryLifecycleState getProcessingState() {
        return lifecycleState;
    }

    @Override
    public boolean isAvailableForProcessing(final DateTime now) {
        switch (lifecycleState) {
            case AVAILABLE:
                break;
            case IN_PROCESSING:
                // Somebody already got the event, not available yet
                if (nextAvailableDate.isAfter(now)) {
                    return false;
                }
                break;
            case PROCESSED:
                return false;
            default:
                throw new RuntimeException(String.format("Unkwnon IEvent processing state %s", lifecycleState));
        }
        return effectiveDate.isBefore(now);
    }

    @Override
    public String getNotificationKeyClass() {
        return notificationKeyClass;
    }


    @Override
    public String getNotificationKey() {
        return notificationKey;
    }

    @Override
    public DateTime getEffectiveDate() {
        return effectiveDate;
    }

    @Override
    public String getQueueName() {
        return queueName;
    }

    @Override
    public String getCreatedOwner() {
        return createdOwner;
    }

    @Override
    public UUID getAccountId() {
        return accountId;
    }
}
