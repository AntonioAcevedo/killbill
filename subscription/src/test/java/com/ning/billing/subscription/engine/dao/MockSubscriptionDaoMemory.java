/*
 * Copyright 2010-2013 Ning, Inc.
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

package com.ning.billing.subscription.engine.dao;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.UUID;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ning.billing.callcontext.InternalCallContext;
import com.ning.billing.callcontext.InternalTenantContext;
import com.ning.billing.catalog.api.CatalogService;
import com.ning.billing.catalog.api.ProductCategory;
import com.ning.billing.catalog.api.TimeUnit;
import com.ning.billing.clock.Clock;
import com.ning.billing.notificationq.api.NotificationEvent;
import com.ning.billing.notificationq.api.NotificationQueue;
import com.ning.billing.notificationq.api.NotificationQueueService;
import com.ning.billing.notificationq.api.NotificationQueueService.NoSuchNotificationQueue;
import com.ning.billing.subscription.api.SubscriptionBase;
import com.ning.billing.subscription.api.migration.AccountMigrationData;
import com.ning.billing.subscription.api.migration.AccountMigrationData.BundleMigrationData;
import com.ning.billing.subscription.api.migration.AccountMigrationData.SubscriptionMigrationData;
import com.ning.billing.subscription.api.timeline.SubscriptionDataRepair;
import com.ning.billing.subscription.api.transfer.TransferCancelData;
import com.ning.billing.subscription.api.user.DefaultSubscriptionBase;
import com.ning.billing.subscription.api.user.DefaultSubscriptionBaseBundle;
import com.ning.billing.subscription.api.user.SubscriptionBaseBundle;
import com.ning.billing.subscription.api.user.SubscriptionBuilder;
import com.ning.billing.subscription.engine.core.DefaultSubscriptionBaseService;
import com.ning.billing.subscription.engine.core.SubscriptionNotificationKey;
import com.ning.billing.subscription.events.SubscriptionBaseEvent;
import com.ning.billing.subscription.events.SubscriptionBaseEvent.EventType;
import com.ning.billing.subscription.events.user.ApiEvent;
import com.ning.billing.subscription.events.user.ApiEventType;
import com.ning.billing.util.entity.dao.EntitySqlDao;
import com.ning.billing.util.entity.dao.EntitySqlDaoWrapperFactory;

import com.google.inject.Inject;

public class MockSubscriptionDaoMemory implements SubscriptionDao {

    protected static final Logger log = LoggerFactory.getLogger(SubscriptionDao.class);

    private final List<SubscriptionBaseBundle> bundles;
    private final List<SubscriptionBase> subscriptions;
    private final TreeSet<SubscriptionBaseEvent> events;
    private final Clock clock;
    private final NotificationQueueService notificationQueueService;
    private final CatalogService catalogService;

    @Inject
    public MockSubscriptionDaoMemory(final Clock clock,
                                     final NotificationQueueService notificationQueueService,
                                     final CatalogService catalogService) {
        super();
        this.clock = clock;
        this.catalogService = catalogService;
        this.notificationQueueService = notificationQueueService;
        this.bundles = new ArrayList<SubscriptionBaseBundle>();
        this.subscriptions = new ArrayList<SubscriptionBase>();
        this.events = new TreeSet<SubscriptionBaseEvent>();
    }

    public void reset() {
        bundles.clear();
        subscriptions.clear();
        events.clear();
    }

    @Override
    public List<SubscriptionBaseBundle> getSubscriptionBundleForAccount(final UUID accountId, final InternalTenantContext context) {
        final List<SubscriptionBaseBundle> results = new ArrayList<SubscriptionBaseBundle>();
        for (final SubscriptionBaseBundle cur : bundles) {
            if (cur.getAccountId().equals(accountId)) {
                results.add(cur);
            }
        }
        return results;
    }

    @Override
    public List<SubscriptionBaseBundle> getSubscriptionBundlesForKey(final String bundleKey, final InternalTenantContext context) {
        final List<SubscriptionBaseBundle> results = new ArrayList<SubscriptionBaseBundle>();
        for (final SubscriptionBaseBundle cur : bundles) {
            if (cur.getExternalKey().equals(bundleKey)) {
                results.add(cur);
            }
        }
        return results;
    }

    @Override
    public SubscriptionBaseBundle getSubscriptionBundleFromId(final UUID bundleId, final InternalTenantContext context) {
        for (final SubscriptionBaseBundle cur : bundles) {
            if (cur.getId().equals(bundleId)) {
                return cur;
            }
        }
        return null;
    }

    @Override
    public List<SubscriptionBaseBundle> getSubscriptionBundlesForAccountAndKey(final UUID accountId, final String bundleKey, final InternalTenantContext context) {
        final List<SubscriptionBaseBundle> results = new ArrayList<SubscriptionBaseBundle>();
        for (final SubscriptionBaseBundle cur : bundles) {
            if (cur.getExternalKey().equals(bundleKey) && cur.getAccountId().equals(accountId)) {
                results.add(cur);
            }
        }
        return results;
    }

    @Override
    public SubscriptionBaseBundle createSubscriptionBundle(final DefaultSubscriptionBaseBundle bundle, final InternalCallContext context) {
        bundles.add(bundle);
        return getSubscriptionBundleFromId(bundle.getId(), context);
    }

    @Override
    public SubscriptionBase getSubscriptionFromId(final UUID subscriptionId, final InternalTenantContext context) {
        for (final SubscriptionBase cur : subscriptions) {
            if (cur.getId().equals(subscriptionId)) {
                return buildSubscription((DefaultSubscriptionBase) cur, context);
            }
        }
        return null;
    }

    @Override
    public UUID getAccountIdFromSubscriptionId(final UUID subscriptionId, final InternalTenantContext context) {
        throw new UnsupportedOperationException();
    }

    /*
    @Override
    public List<SubscriptionBase> getSubscriptionsForAccountAndKey(final UUID accountId, final String bundleKey, final InternalTenantContext callcontext) {

        for (final SubscriptionBaseBundle cur : bundles) {
            if (cur.getExternalKey().equals(bundleKey) && cur.getAccountId().equals(bundleKey)) {
                return getSubscriptions(cur.getId(), callcontext);
            }
        }
        return Collections.emptyList();
    }
    */

    @Override
    public void createSubscription(final DefaultSubscriptionBase subscription, final List<SubscriptionBaseEvent> initialEvents,
                                   final InternalCallContext context) {
        synchronized (events) {
            events.addAll(initialEvents);
            for (final SubscriptionBaseEvent cur : initialEvents) {
                recordFutureNotificationFromTransaction(null, cur.getEffectiveDate(), new SubscriptionNotificationKey(cur.getId()), context);
            }
        }
        final SubscriptionBase updatedSubscription = buildSubscription(subscription, context);
        subscriptions.add(updatedSubscription);
    }

    @Override
    public void recreateSubscription(final DefaultSubscriptionBase subscription, final List<SubscriptionBaseEvent> recreateEvents, final InternalCallContext context) {
        synchronized (events) {
            events.addAll(recreateEvents);
            for (final SubscriptionBaseEvent cur : recreateEvents) {
                recordFutureNotificationFromTransaction(null, cur.getEffectiveDate(), new SubscriptionNotificationKey(cur.getId()), context);
            }
        }
    }

    @Override
    public List<SubscriptionBase> getSubscriptions(final UUID bundleId, final InternalTenantContext context) {
        final List<SubscriptionBase> results = new ArrayList<SubscriptionBase>();
        for (final SubscriptionBase cur : subscriptions) {
            if (cur.getBundleId().equals(bundleId)) {
                results.add(buildSubscription((DefaultSubscriptionBase) cur, context));
            }
        }
        return results;
    }

    @Override
    public Map<UUID, List<SubscriptionBase>> getSubscriptionsForAccount(final InternalTenantContext context) {
        final Map<UUID, List<SubscriptionBase>> results = new HashMap<UUID, List<SubscriptionBase>>();
        for (final SubscriptionBase cur : subscriptions) {
            if (results.get(cur.getBundleId()) == null) {
                results.put(cur.getBundleId(), new LinkedList<SubscriptionBase>());
            }
            results.get(cur.getBundleId()).add(buildSubscription((DefaultSubscriptionBase) cur, context));
        }
        return results;
    }

    @Override
    public List<SubscriptionBaseEvent> getEventsForSubscription(final UUID subscriptionId, final InternalTenantContext context) {
        synchronized (events) {
            final List<SubscriptionBaseEvent> results = new LinkedList<SubscriptionBaseEvent>();
            for (final SubscriptionBaseEvent cur : events) {
                if (cur.getSubscriptionId().equals(subscriptionId)) {
                    results.add(cur);
                }
            }
            return results;
        }
    }

    @Override
    public List<SubscriptionBaseEvent> getPendingEventsForSubscription(final UUID subscriptionId, final InternalTenantContext context) {
        synchronized (events) {
            final List<SubscriptionBaseEvent> results = new LinkedList<SubscriptionBaseEvent>();
            for (final SubscriptionBaseEvent cur : events) {
                if (cur.isActive() &&
                    cur.getEffectiveDate().isAfter(clock.getUTCNow()) &&
                    cur.getSubscriptionId().equals(subscriptionId)) {
                    results.add(cur);
                }
            }
            return results;
        }
    }

    @Override
    public SubscriptionBase getBaseSubscription(final UUID bundleId, final InternalTenantContext context) {
        for (final SubscriptionBase cur : subscriptions) {
            if (cur.getBundleId().equals(bundleId) &&
                cur.getCurrentPlan().getProduct().getCategory() == ProductCategory.BASE) {
                return buildSubscription((DefaultSubscriptionBase) cur, context);
            }
        }
        return null;
    }

    @Override
    public void createNextPhaseEvent(final DefaultSubscriptionBase subscription, final SubscriptionBaseEvent nextPhase, final InternalCallContext context) {
        cancelNextPhaseEvent(subscription.getId(), context);
        insertEvent(nextPhase, context);
    }

    private SubscriptionBase buildSubscription(final DefaultSubscriptionBase in, final InternalTenantContext context) {
        final DefaultSubscriptionBase subscription = new DefaultSubscriptionBase(new SubscriptionBuilder(in), null, clock);
        if (events.size() > 0) {
            subscription.rebuildTransitions(getEventsForSubscription(in.getId(), context), catalogService.getFullCatalog());
        }
        return subscription;

    }

    @Override
    public void updateChargedThroughDate(final DefaultSubscriptionBase subscription, final InternalCallContext context) {
        boolean found = false;
        final Iterator<SubscriptionBase> it = subscriptions.iterator();
        while (it.hasNext()) {
            final SubscriptionBase cur = it.next();
            if (cur.getId().equals(subscription.getId())) {
                found = true;
                it.remove();
                break;
            }
        }
        if (found) {
            subscriptions.add(subscription);
        }
    }

    @Override
    public void cancelSubscription(final DefaultSubscriptionBase subscription, final SubscriptionBaseEvent cancelEvent,
                                   final InternalCallContext context, final int seqId) {
        synchronized (events) {
            cancelNextPhaseEvent(subscription.getId(), context);
            insertEvent(cancelEvent, context);
        }
    }

    @Override
    public void cancelSubscriptions(final List<DefaultSubscriptionBase> subscriptions, final List<SubscriptionBaseEvent> cancelEvents, final InternalCallContext context) {
        synchronized (events) {
            for (int i = 0; i < subscriptions.size(); i++) {
                cancelSubscription(subscriptions.get(i), cancelEvents.get(i), context, 0);
            }
        }
    }

    @Override
    public void changePlan(final DefaultSubscriptionBase subscription, final List<SubscriptionBaseEvent> changeEvents, final InternalCallContext context) {
        synchronized (events) {
            cancelNextChangeEvent(subscription.getId());
            cancelNextPhaseEvent(subscription.getId(), context);
            events.addAll(changeEvents);
            for (final SubscriptionBaseEvent cur : changeEvents) {
                recordFutureNotificationFromTransaction(null, cur.getEffectiveDate(), new SubscriptionNotificationKey(cur.getId()), context);
            }
        }
    }

    private void insertEvent(final SubscriptionBaseEvent event, final InternalCallContext context) {
        synchronized (events) {
            events.add(event);
            recordFutureNotificationFromTransaction(null, event.getEffectiveDate(), new SubscriptionNotificationKey(event.getId()), context);
        }
    }

    private void cancelNextPhaseEvent(final UUID subscriptionId, final InternalTenantContext context) {
        final SubscriptionBase curSubscription = getSubscriptionFromId(subscriptionId, context);
        if (curSubscription.getCurrentPhase() == null ||
            curSubscription.getCurrentPhase().getDuration().getUnit() == TimeUnit.UNLIMITED) {
            return;
        }

        synchronized (events) {

            final Iterator<SubscriptionBaseEvent> it = events.descendingIterator();
            while (it.hasNext()) {
                final SubscriptionBaseEvent cur = it.next();
                if (cur.getSubscriptionId() != subscriptionId) {
                    continue;
                }
                if (cur.getType() == EventType.PHASE &&
                    cur.getEffectiveDate().isAfter(clock.getUTCNow())) {
                    cur.deactivate();
                    break;
                }

            }
        }
    }

    private void cancelNextChangeEvent(final UUID subscriptionId) {

        synchronized (events) {

            final Iterator<SubscriptionBaseEvent> it = events.descendingIterator();
            while (it.hasNext()) {
                final SubscriptionBaseEvent cur = it.next();
                if (cur.getSubscriptionId() != subscriptionId) {
                    continue;
                }
                if (cur.getType() == EventType.API_USER &&
                    ApiEventType.CHANGE == ((ApiEvent) cur).getEventType() &&
                    cur.getEffectiveDate().isAfter(clock.getUTCNow())) {
                    cur.deactivate();
                    break;
                }
            }
        }
    }

    @Override
    public void uncancelSubscription(final DefaultSubscriptionBase subscription, final List<SubscriptionBaseEvent> uncancelEvents,
                                     final InternalCallContext context) {

        synchronized (events) {
            boolean foundCancel = false;
            final Iterator<SubscriptionBaseEvent> it = events.descendingIterator();
            while (it.hasNext()) {
                final SubscriptionBaseEvent cur = it.next();
                if (cur.getSubscriptionId() != subscription.getId()) {
                    continue;
                }
                if (cur.getType() == EventType.API_USER &&
                    ((ApiEvent) cur).getEventType() == ApiEventType.CANCEL) {
                    cur.deactivate();
                    foundCancel = true;
                    break;
                }
            }
            if (foundCancel) {
                for (final SubscriptionBaseEvent cur : uncancelEvents) {
                    insertEvent(cur, context);
                }
            }
        }
    }

    @Override
    public void migrate(final UUID accountId, final AccountMigrationData accountData, final InternalCallContext context) {
        synchronized (events) {

            for (final BundleMigrationData curBundle : accountData.getData()) {
                final DefaultSubscriptionBaseBundle bundleData = curBundle.getData();
                for (final SubscriptionMigrationData curSubscription : curBundle.getSubscriptions()) {
                    final DefaultSubscriptionBase subData = curSubscription.getData();
                    for (final SubscriptionBaseEvent curEvent : curSubscription.getInitialEvents()) {
                        events.add(curEvent);
                        recordFutureNotificationFromTransaction(null, curEvent.getEffectiveDate(),
                                                                new SubscriptionNotificationKey(curEvent.getId()), context);

                    }
                    subscriptions.add(subData);
                }
                bundles.add(bundleData);
            }
        }
    }

    @Override
    public SubscriptionBaseEvent getEventById(final UUID eventId, final InternalTenantContext context) {
        synchronized (events) {
            for (final SubscriptionBaseEvent cur : events) {
                if (cur.getId().equals(eventId)) {
                    return cur;
                }
            }
        }
        return null;
    }

    private void recordFutureNotificationFromTransaction(final EntitySqlDaoWrapperFactory<EntitySqlDao> transactionalDao, final DateTime effectiveDate,
                                                         final NotificationEvent notificationKey, final InternalCallContext context) {
        try {
            final NotificationQueue subscriptionEventQueue = notificationQueueService.getNotificationQueue(DefaultSubscriptionBaseService.SUBSCRIPTION_SERVICE_NAME,
                                                                                                           DefaultSubscriptionBaseService.NOTIFICATION_QUEUE_NAME);
            subscriptionEventQueue.recordFutureNotificationFromTransaction(null, effectiveDate, notificationKey, context.getUserToken(), context.getAccountRecordId(), context.getTenantRecordId());
        } catch (NoSuchNotificationQueue e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Map<UUID, List<SubscriptionBaseEvent>> getEventsForBundle(final UUID bundleId, final InternalTenantContext context) {
        return null;
    }

    @Override
    public void repair(final UUID accountId, final UUID bundleId, final List<SubscriptionDataRepair> inRepair,
                       final InternalCallContext context) {
    }

    @Override
    public void transfer(final UUID srcAccountId, final UUID destAccountId, final BundleMigrationData data,
                         final List<TransferCancelData> transferCancelData, final InternalCallContext fromContext,
                         final InternalCallContext toContext) {
    }
}
