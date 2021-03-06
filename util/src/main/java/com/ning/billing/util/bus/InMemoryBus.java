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

package com.ning.billing.util.bus;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;

import org.skife.jdbi.v2.sqlobject.mixins.Transmogrifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.AsyncEventBus;

public class InMemoryBus implements Bus {


    private static final Logger log = LoggerFactory.getLogger(InMemoryBus.class);

    private final EventBusDelegate delegate;
    private final AtomicBoolean isInitialized;

    public class EventBusDelegate extends AsyncEventBus {

        private final Executor executor;
        private final ThreadGroup grp;

        public EventBusDelegate(final String name, final ThreadGroup grp, final Executor executor) {
            super(name, executor);
            this.executor = executor;
            this.grp = grp;
        }

        public void completeDispatch() {
            dispatchQueuedEvents();
        }

        // No way to really 'stop' an executor; what we do is:
        // i) disallow any new events into the queue
        // ii) empty the queue
        //
        // That will only work if the event submitter handles EventBusException correctly when posting.
        //
        public void stop() {
        }
    }

    public InMemoryBus() {

        final ThreadGroup group = new ThreadGroup(DefaultBusService.EVENT_BUS_GROUP_NAME);
        final Executor executor = Executors.newCachedThreadPool(new ThreadFactory() {
            @Override
            public Thread newThread(final Runnable r) {
                return new Thread(group, r, DefaultBusService.EVENT_BUS_TH_NAME);
            }
        });

        this.delegate = new EventBusDelegate(DefaultBusService.EVENT_BUS_IDENTIFIER, group, executor);
        this.isInitialized = new AtomicBoolean(false);
    }

    @Override
    public void register(final Object handlerInstance) throws EventBusException {
        checkInitialized("register");
        delegate.register(handlerInstance);
    }

    @Override
    public void unregister(final Object handlerInstance) throws EventBusException {
        checkInitialized("unregister");
        delegate.unregister(handlerInstance);
    }

    @Override
    public void post(final BusEvent event) throws EventBusException {
        checkInitialized("post");
        delegate.post(event);
    }

    @Override
    public void postFromTransaction(final BusEvent event, final Transmogrifier dao) throws EventBusException {
        checkInitialized("postFromTransaction");
        delegate.post(event);
    }

    @Override
    public void start() {
        if (isInitialized.compareAndSet(false, true)) {
            log.info("InMemoryBus started...");

        }
    }

    private void checkInitialized(final String operation) throws EventBusException {
        if (!isInitialized.get()) {
            throw new EventBusException(String.format("Attempting operation %s on an non initialized bus",
                                                      operation));
        }
    }

    @Override
    public void stop() {
        if (isInitialized.compareAndSet(true, false)) {
            log.info("InMemoryBus stopping...");
            delegate.completeDispatch();
            delegate.stop();
            log.info("InMemoryBus stopped...");
        }
    }
}
