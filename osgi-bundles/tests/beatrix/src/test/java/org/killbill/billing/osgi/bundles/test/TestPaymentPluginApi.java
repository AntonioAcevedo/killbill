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

package org.killbill.billing.osgi.bundles.test;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.joda.time.DateTime;

import org.killbill.billing.catalog.api.Currency;
import org.killbill.billing.osgi.bundles.test.dao.TestDao;
import org.killbill.billing.payment.api.PaymentMethodPlugin;
import org.killbill.billing.payment.plugin.api.HostedPaymentPageDescriptorFields;
import org.killbill.billing.payment.plugin.api.HostedPaymentPageFormDescriptor;
import org.killbill.billing.payment.plugin.api.HostedPaymentPageNotification;
import org.killbill.billing.payment.plugin.api.PaymentInfoPlugin;
import org.killbill.billing.payment.plugin.api.PaymentMethodInfoPlugin;
import org.killbill.billing.payment.plugin.api.PaymentPluginApi;
import org.killbill.billing.payment.plugin.api.PaymentPluginApiException;
import org.killbill.billing.payment.plugin.api.PaymentPluginStatus;
import org.killbill.billing.payment.plugin.api.RefundInfoPlugin;
import org.killbill.billing.util.callcontext.CallContext;
import org.killbill.billing.util.callcontext.TenantContext;
import org.killbill.billing.util.entity.Pagination;

public class TestPaymentPluginApi implements PaymentPluginApi {

    private final TestDao testDao;
    private final String name;

    public TestPaymentPluginApi(final String name, final TestDao testDao) {
        this.testDao = testDao;
        this.name = name;
    }

    @Override
    public PaymentInfoPlugin authorizePayment(final UUID uuid, final UUID uuid2, final UUID uuid3, final BigDecimal bigDecimal, final Currency currency, final CallContext callContext) throws PaymentPluginApiException {
        return null;
    }

    @Override
    public PaymentInfoPlugin capturePayment(final UUID uuid, final UUID uuid2, final UUID uuid3, final BigDecimal bigDecimal, final Currency currency, final CallContext callContext) throws PaymentPluginApiException {
        return null;
    }

    @Override
    public PaymentInfoPlugin processPayment(final UUID kbAccountId, final UUID kbPaymentId, final UUID kbPaymentMethodId, final BigDecimal amount, final Currency currency, final CallContext context) throws PaymentPluginApiException {
        testDao.insertProcessedPayment(kbPaymentId, kbPaymentMethodId, amount);
        return new PaymentInfoPlugin() {
            @Override
            public UUID getKbPaymentId() {
                return kbPaymentId;
            }

            @Override
            public BigDecimal getAmount() {
                return amount;
            }

            @Override
            public Currency getCurrency() {
                return currency;
            }

            @Override
            public DateTime getCreatedDate() {
                return new DateTime();
            }

            @Override
            public DateTime getEffectiveDate() {
                return new DateTime();
            }

            @Override
            public PaymentPluginStatus getStatus() {
                return PaymentPluginStatus.PROCESSED;
            }

            @Override
            public String getGatewayError() {
                return null;
            }

            @Override
            public String getGatewayErrorCode() {
                return null;
            }

            @Override
            public String getFirstPaymentReferenceId() {
                return null;
            }

            @Override
            public String getSecondPaymentReferenceId() {
                return null;
            }
        };
    }

    @Override
    public PaymentInfoPlugin voidPayment(final UUID uuid, final UUID uuid2, final UUID uuid3, final CallContext callContext) throws PaymentPluginApiException {
        return null;
    }

    @Override
    public PaymentInfoPlugin getPaymentInfo(final UUID kbAccountId, final UUID kbPaymentId, final TenantContext context) throws PaymentPluginApiException {
        return null;
    }

    @Override
    public Pagination<PaymentInfoPlugin> searchPayments(final String searchKey, final Long offset, final Long limit, final TenantContext tenantContext) throws PaymentPluginApiException {
        return new Pagination<PaymentInfoPlugin>() {
            @Override
            public Long getCurrentOffset() {
                return 0L;
            }

            @Override
            public Long getNextOffset() {
                return null;
            }

            @Override
            public Long getMaxNbRecords() {
                return 0L;
            }

            @Override
            public Long getTotalNbRecords() {
                return 0L;
            }

            @Override
            public Iterator<PaymentInfoPlugin> iterator() {
                return null;
            }
        };
    }

    @Override
    public RefundInfoPlugin processRefund(final UUID kbAccountId, final UUID kbPaymentId, final BigDecimal refundAmount, final Currency currency, final CallContext context) throws PaymentPluginApiException {
        return null;
    }

    @Override
    public List<RefundInfoPlugin> getRefundInfo(final UUID kbAccountId, final UUID kbPaymentId, final TenantContext context) {
        return Collections.<RefundInfoPlugin>emptyList();
    }

    @Override
    public Pagination<RefundInfoPlugin> searchRefunds(final String searchKey, final Long offset, final Long limit, final TenantContext tenantContext) throws PaymentPluginApiException {
        return new Pagination<RefundInfoPlugin>() {
            @Override
            public Long getCurrentOffset() {
                return 0L;
            }

            @Override
            public Long getNextOffset() {
                return null;
            }

            @Override
            public Long getMaxNbRecords() {
                return 0L;
            }

            @Override
            public Long getTotalNbRecords() {
                return 0L;
            }

            @Override
            public Iterator<RefundInfoPlugin> iterator() {
                return null;
            }
        };
    }

    @Override
    public void addPaymentMethod(final UUID kbAccountId, final UUID kbPaymentMethodId, final PaymentMethodPlugin paymentMethodProps, final boolean setDefault, final CallContext context) throws PaymentPluginApiException {
    }

    @Override
    public void deletePaymentMethod(final UUID kbAccountId, final UUID kbPaymentMethodId, final CallContext context) throws PaymentPluginApiException {
    }

    @Override
    public PaymentMethodPlugin getPaymentMethodDetail(final UUID kbAccountId, final UUID kbPaymentMethodId, final TenantContext context) throws PaymentPluginApiException {
        return null;
    }

    @Override
    public void setDefaultPaymentMethod(final UUID kbAccountId, final UUID kbPaymentMethodId, final CallContext context) throws PaymentPluginApiException {
    }

    @Override
    public List<PaymentMethodInfoPlugin> getPaymentMethods(final UUID kbAccountId, final boolean refreshFromGateway, final CallContext context) throws PaymentPluginApiException {
        return Collections.emptyList();
    }

    @Override
    public Pagination<PaymentMethodPlugin> searchPaymentMethods(final String searchKey, final Long offset, final Long limit, final TenantContext tenantContext) throws PaymentPluginApiException {
        return new Pagination<PaymentMethodPlugin>() {
            @Override
            public Long getCurrentOffset() {
                return 0L;
            }

            @Override
            public Long getNextOffset() {
                return null;
            }

            @Override
            public Long getMaxNbRecords() {
                return 0L;
            }

            @Override
            public Long getTotalNbRecords() {
                return 0L;
            }

            @Override
            public Iterator<PaymentMethodPlugin> iterator() {
                return null;
            }
        };
    }

    @Override
    public void resetPaymentMethods(final UUID kbAccountId, final List<PaymentMethodInfoPlugin> paymentMethods) throws PaymentPluginApiException {
    }

    @Override
    public HostedPaymentPageFormDescriptor buildFormDescriptor(final UUID uuid, final HostedPaymentPageDescriptorFields hostedPaymentPageDescriptorFields, final TenantContext tenantContext) {
        return null;
    }

    @Override
    public HostedPaymentPageNotification processNotification(final String s, final TenantContext tenantContext) throws PaymentPluginApiException {
        return null;
    }
}