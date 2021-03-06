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

package com.ning.billing.entitlement.api.user;

import java.util.UUID;

import org.joda.time.DateTime;

import com.ning.billing.catalog.api.Plan;
import com.ning.billing.catalog.api.PlanPhase;
import com.ning.billing.catalog.api.PriceList;
import com.ning.billing.entitlement.api.SubscriptionTransitionType;
import com.ning.billing.entitlement.api.user.Subscription.SubscriptionState;
import com.ning.billing.entitlement.events.EntitlementEvent.EventType;
import com.ning.billing.entitlement.events.user.ApiEventType;
import com.ning.billing.entitlement.exceptions.EntitlementError;

public class SubscriptionTransitionData /* implements SubscriptionEvent */ {
    private final Long totalOrdering;
    private final UUID subscriptionId;
    private final UUID bundleId;
    private final UUID eventId;
    private final EventType eventType;
    private final ApiEventType apiEventType;
    private final DateTime requestedTransitionTime;
    private final DateTime effectiveTransitionTime;
    private final SubscriptionState previousState;
    private final PriceList previousPriceList;
    private final Plan previousPlan;
    private final PlanPhase previousPhase;
    private final SubscriptionState nextState;
    private final PriceList nextPriceList;
    private final Plan nextPlan;
    private final PlanPhase nextPhase;
    private final Boolean isFromDisk;
    private final Integer remainingEventsForUserOperation;
    private final UUID userToken;

    public SubscriptionTransitionData(final UUID eventId,
                                      final UUID subscriptionId,
                                      final UUID bundleId,
                                      final EventType eventType,
                                      final ApiEventType apiEventType,
                                      final DateTime requestedTransitionTime,
                                      final DateTime effectiveTransitionTime,
                                      final SubscriptionState previousState,
                                      final Plan previousPlan,
                                      final PlanPhase previousPhase,
                                      final PriceList previousPriceList,
                                      final SubscriptionState nextState,
                                      final Plan nextPlan,
                                      final PlanPhase nextPhase,
                                      final PriceList nextPriceList,
                                      final Long totalOrdering,
                                      final UUID userToken,
                                      final Boolean isFromDisk) {
        this.eventId = eventId;
        this.subscriptionId = subscriptionId;
        this.bundleId = bundleId;
        this.eventType = eventType;
        this.apiEventType = apiEventType;
        this.requestedTransitionTime = requestedTransitionTime;
        this.effectiveTransitionTime = effectiveTransitionTime;
        this.previousState = previousState;
        this.previousPriceList = previousPriceList;
        this.previousPlan = previousPlan;
        this.previousPhase = previousPhase;
        this.nextState = nextState;
        this.nextPlan = nextPlan;
        this.nextPriceList = nextPriceList;
        this.nextPhase = nextPhase;
        this.totalOrdering = totalOrdering;
        this.isFromDisk = isFromDisk;
        this.userToken = userToken;
        this.remainingEventsForUserOperation = 0;
    }

    public SubscriptionTransitionData(final SubscriptionTransitionData input, final int remainingEventsForUserOperation) {
        super();
        this.eventId = input.getId();
        this.subscriptionId = input.getSubscriptionId();
        this.bundleId = input.getBundleId();
        this.eventType = input.getEventType();
        this.apiEventType = input.getApiEventType();
        this.requestedTransitionTime = input.getRequestedTransitionTime();
        this.effectiveTransitionTime = input.getEffectiveTransitionTime();
        this.previousState = input.getPreviousState();
        this.previousPriceList = input.getPreviousPriceList();
        this.previousPlan = input.getPreviousPlan();
        this.previousPhase = input.getPreviousPhase();
        this.nextState = input.getNextState();
        this.nextPlan = input.getNextPlan();
        this.nextPriceList = input.getNextPriceList();
        this.nextPhase = input.getNextPhase();
        this.totalOrdering = input.getTotalOrdering();
        this.isFromDisk = input.isFromDisk();
        this.userToken = input.getUserToken();
        this.remainingEventsForUserOperation = remainingEventsForUserOperation;
    }

    public UUID getId() {
        return eventId;
    }

    public UUID getSubscriptionId() {
        return subscriptionId;
    }

    public UUID getBundleId() {
        return bundleId;
    }

    public SubscriptionState getPreviousState() {
        return previousState;
    }

    public Plan getPreviousPlan() {
        return previousPlan;
    }

    public PlanPhase getPreviousPhase() {
        return previousPhase;
    }

    public Plan getNextPlan() {
        return nextPlan;
    }

    public PlanPhase getNextPhase() {
        return nextPhase;
    }

    public SubscriptionState getNextState() {
        return nextState;
    }

    public PriceList getPreviousPriceList() {
        return previousPriceList;
    }

    public PriceList getNextPriceList() {
        return nextPriceList;
    }

    public UUID getUserToken() {
        return userToken;
    }

    public Integer getRemainingEventsForUserOperation() {
        return remainingEventsForUserOperation;
    }

    public SubscriptionTransitionType getTransitionType() {
        return toSubscriptionTransitionType(eventType, apiEventType);
    }

    public static SubscriptionTransitionType toSubscriptionTransitionType(final EventType eventType, final ApiEventType apiEventType) {
        switch (eventType) {
            case API_USER:
                return apiEventType.getSubscriptionTransitionType();
            case PHASE:
                return SubscriptionTransitionType.PHASE;
            default:
                throw new EntitlementError("Unexpected event type " + eventType);
        }
    }

    public DateTime getRequestedTransitionTime() {
        return requestedTransitionTime;
    }

    public DateTime getEffectiveTransitionTime() {
        return effectiveTransitionTime;
    }

    public Long getTotalOrdering() {
        return totalOrdering;
    }

    public Boolean isFromDisk() {
        return isFromDisk;
    }

    public ApiEventType getApiEventType() {
        return apiEventType;
    }

    public EventType getEventType() {
        return eventType;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("SubscriptionTransitionData");
        sb.append("{apiEventType=").append(apiEventType);
        sb.append(", totalOrdering=").append(totalOrdering);
        sb.append(", subscriptionId=").append(subscriptionId);
        sb.append(", bundleId=").append(bundleId);
        sb.append(", eventId=").append(eventId);
        sb.append(", eventType=").append(eventType);
        sb.append(", requestedTransitionTime=").append(requestedTransitionTime);
        sb.append(", effectiveTransitionTime=").append(effectiveTransitionTime);
        sb.append(", previousState=").append(previousState);
        sb.append(", previousPriceList=").append(previousPriceList);
        sb.append(", previousPlan=").append(previousPlan);
        sb.append(", previousPhase=").append(previousPhase);
        sb.append(", nextState=").append(nextState);
        sb.append(", nextPriceList=").append(nextPriceList);
        sb.append(", nextPlan=").append(nextPlan);
        sb.append(", nextPhase=").append(nextPhase);
        sb.append(", isFromDisk=").append(isFromDisk);
        sb.append(", remainingEventsForUserOperation=").append(remainingEventsForUserOperation);
        sb.append(", userToken=").append(userToken);
        sb.append('}');
        return sb.toString();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final SubscriptionTransitionData that = (SubscriptionTransitionData) o;

        if (apiEventType != that.apiEventType) {
            return false;
        }
        if (bundleId != null ? !bundleId.equals(that.bundleId) : that.bundleId != null) {
            return false;
        }
        if (effectiveTransitionTime != null ? effectiveTransitionTime.compareTo(that.effectiveTransitionTime) != 0 : that.effectiveTransitionTime != null) {
            return false;
        }
        if (eventId != null ? !eventId.equals(that.eventId) : that.eventId != null) {
            return false;
        }
        if (eventType != that.eventType) {
            return false;
        }
        if (isFromDisk != null ? !isFromDisk.equals(that.isFromDisk) : that.isFromDisk != null) {
            return false;
        }
        if (nextPhase != null ? !nextPhase.equals(that.nextPhase) : that.nextPhase != null) {
            return false;
        }
        if (nextPlan != null ? !nextPlan.equals(that.nextPlan) : that.nextPlan != null) {
            return false;
        }
        if (nextPriceList != null ? !nextPriceList.equals(that.nextPriceList) : that.nextPriceList != null) {
            return false;
        }
        if (nextState != that.nextState) {
            return false;
        }
        if (previousPhase != null ? !previousPhase.equals(that.previousPhase) : that.previousPhase != null) {
            return false;
        }
        if (previousPlan != null ? !previousPlan.equals(that.previousPlan) : that.previousPlan != null) {
            return false;
        }
        if (previousPriceList != null ? !previousPriceList.equals(that.previousPriceList) : that.previousPriceList != null) {
            return false;
        }
        if (previousState != that.previousState) {
            return false;
        }
        if (remainingEventsForUserOperation != null ? !remainingEventsForUserOperation.equals(that.remainingEventsForUserOperation) : that.remainingEventsForUserOperation != null) {
            return false;
        }
        if (requestedTransitionTime != null ? requestedTransitionTime.compareTo(that.requestedTransitionTime) != 0 : that.requestedTransitionTime != null) {
            return false;
        }
        if (subscriptionId != null ? !subscriptionId.equals(that.subscriptionId) : that.subscriptionId != null) {
            return false;
        }
        if (totalOrdering != null ? !totalOrdering.equals(that.totalOrdering) : that.totalOrdering != null) {
            return false;
        }
        if (userToken != null ? !userToken.equals(that.userToken) : that.userToken != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = totalOrdering != null ? totalOrdering.hashCode() : 0;
        result = 31 * result + (subscriptionId != null ? subscriptionId.hashCode() : 0);
        result = 31 * result + (bundleId != null ? bundleId.hashCode() : 0);
        result = 31 * result + (eventId != null ? eventId.hashCode() : 0);
        result = 31 * result + (eventType != null ? eventType.hashCode() : 0);
        result = 31 * result + (apiEventType != null ? apiEventType.hashCode() : 0);
        result = 31 * result + (requestedTransitionTime != null ? requestedTransitionTime.hashCode() : 0);
        result = 31 * result + (effectiveTransitionTime != null ? effectiveTransitionTime.hashCode() : 0);
        result = 31 * result + (previousState != null ? previousState.hashCode() : 0);
        result = 31 * result + (previousPriceList != null ? previousPriceList.hashCode() : 0);
        result = 31 * result + (previousPlan != null ? previousPlan.hashCode() : 0);
        result = 31 * result + (previousPhase != null ? previousPhase.hashCode() : 0);
        result = 31 * result + (nextState != null ? nextState.hashCode() : 0);
        result = 31 * result + (nextPriceList != null ? nextPriceList.hashCode() : 0);
        result = 31 * result + (nextPlan != null ? nextPlan.hashCode() : 0);
        result = 31 * result + (nextPhase != null ? nextPhase.hashCode() : 0);
        result = 31 * result + (isFromDisk != null ? isFromDisk.hashCode() : 0);
        result = 31 * result + (remainingEventsForUserOperation != null ? remainingEventsForUserOperation.hashCode() : 0);
        result = 31 * result + (userToken != null ? userToken.hashCode() : 0);
        return result;
    }
}
