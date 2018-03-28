/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ru.sbt.jschool.session3;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.Test;
import ru.sbt.jschool.session3.problem1.Account;
import ru.sbt.jschool.session3.problem1.AccountService;
import ru.sbt.jschool.session3.problem1.AccountServiceImpl;
import ru.sbt.jschool.session3.problem1.Currency;
import ru.sbt.jschool.session3.problem1.FraudMonitoring;
import ru.sbt.jschool.session3.problem1.Payment;
import ru.sbt.jschool.session3.problem1.Result;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static ru.sbt.jschool.session3.problem1.Currency.EUR;
import static ru.sbt.jschool.session3.problem1.Currency.RUR;
import static ru.sbt.jschool.session3.problem1.Currency.RUR_TO_EUR;
import static ru.sbt.jschool.session3.problem1.Currency.USD;

/**
 */
public class AccountServiceImplTest {
    @Test public void testCreateAndFind() throws Exception {
        FraudMonitoringImpl fraudMonitoring = new FraudMonitoringImpl();

        AccountService accountService = new AccountServiceImpl(fraudMonitoring);

        Result res = accountService.create(1, 1, 10f, RUR);

        assertEquals(Result.OK, res);

        res = accountService.create(1, 2, 10f, RUR);

        assertEquals(Result.OK, res);

        Collection<Account> accounts = accountService.findForClient(1);

        assertNotNull("Account should be found for a client 1", accounts);

        assertEquals("Two accounts should be found", 2, accounts.size());
    }

    @Test public void testCreateAndFindByID() throws Exception {
        FraudMonitoringImpl fraudMonitoring = new FraudMonitoringImpl();

        AccountService accountService = new AccountServiceImpl(fraudMonitoring);

        Result res = accountService.create(1, 1, 10f, RUR);

        assertEquals(Result.OK, res);

        res = accountService.create(1, 2, 5f, EUR);

        assertEquals(Result.OK, res);

        Account first = accountService.find(1);

        checkAccount(first, 1, 1, 10f, RUR);

        Account second = accountService.find(2);

        checkAccount(second, 1, 2, 5f, EUR);
    }

    @Test public void testCantCreateDublicate() throws Exception {
        FraudMonitoringImpl fraudMonitoring = new FraudMonitoringImpl();

        AccountService accountService = new AccountServiceImpl(fraudMonitoring);

        Result res = accountService.create(1, 1, 10f, RUR);

        assertEquals(Result.OK, res);

        res = accountService.create(1, 1, 1f, RUR);

        assertEquals("Shouldn't create account for existing ID", Result.ALREADY_EXISTS, res);
    }

    @Test public void testCantCreateFraud() throws Exception {
        FraudMonitoringImpl fraudMonitoring = new FraudMonitoringImpl();

        AccountService accountService = new AccountServiceImpl(fraudMonitoring);

        Result res = accountService.create(1, Long.MAX_VALUE, 0f, USD);

        assertEquals(Result.OK, res);

        fraudMonitoring.thieves.add(1L);

        res = accountService.create(1, 1, 10f, RUR);

        assertEquals(Result.FRAUD, res);
    }

    @Test public void testOKPayment() throws Exception {
        FraudMonitoringImpl fraudMonitoring = new FraudMonitoringImpl();

        AccountService accountService = new AccountServiceImpl(fraudMonitoring);

        Result res = accountService.create(1, Long.MAX_VALUE, 10f, USD);

        assertEquals(Result.OK, res);

        res = accountService.create(2, 2, 0f, USD);

        assertEquals(Result.OK, res);

        Payment payment = new Payment(1, 1, Long.MAX_VALUE, 2, 2, 10f);

        res = accountService.doPayment(payment);

        assertEquals(Result.OK, res);

        Account acc = accountService.find(Long.MAX_VALUE);

        checkAccount(acc, 1, Long.MAX_VALUE, 0f, USD);

        acc = accountService.find(2);

        checkAccount(acc, 2, 2, 10f, USD);
    }

    @Test public void testOKPaymentWithCurrencyConversion() throws Exception {
        FraudMonitoringImpl fraudMonitoring = new FraudMonitoringImpl();

        AccountService accountService = new AccountServiceImpl(fraudMonitoring);

        Result res = accountService.create(1, 1, 10f, USD);

        assertEquals(Result.OK, res);

        res = accountService.create(2, 2, 0f, RUR);

        assertEquals(Result.OK, res);

        Payment payment = new Payment(1, 1, 1, 2, 2, 10f);

        res = accountService.doPayment(payment);

        assertEquals(Result.OK, res);

        Account acc = accountService.find(1);

        checkAccount(acc, 1, 1, 0f, USD);

        acc = accountService.find(2);

        checkAccount(acc, 2, 2, 10f*Currency.RUR_TO_USD, RUR);
    }

    @Test public void testOKPaymentWithCurrencyConversion2() throws Exception {
        FraudMonitoringImpl fraudMonitoring = new FraudMonitoringImpl();

        AccountService accountService = new AccountServiceImpl(fraudMonitoring);

        Result res = accountService.create(1, 1, 5, EUR);

        assertEquals(Result.OK, res);

        res = accountService.create(2, 2, 10, RUR);

        assertEquals(Result.OK, res);

        Payment payment = new Payment(1, 1, 1, 2, 2, 1f);

        res = accountService.doPayment(payment);

        assertEquals(Result.OK, res);

        Account acc = accountService.find(1);

        checkAccount(acc, 1, 1, 4f, EUR);

        acc = accountService.find(2);

        checkAccount(acc, 2, 2, 10 + RUR_TO_EUR, RUR);
    }

    @Test public void testOKPaymentWithCurrencyConversion3() throws Exception {
        FraudMonitoringImpl fraudMonitoring = new FraudMonitoringImpl();

        AccountService accountService = new AccountServiceImpl(fraudMonitoring);

        Result res = accountService.create(1, 1, 100f, RUR);

        assertEquals(Result.OK, res);

        res = accountService.create(2, 2, 10, EUR);

        assertEquals(Result.OK, res);

        Payment payment = new Payment(1, 1, 1, 2, 2, RUR_TO_EUR);

        res = accountService.doPayment(payment);

        assertEquals(Result.OK, res);

        Account acc = accountService.find(1);

        checkAccount(acc, 1, 1, 100f - RUR_TO_EUR, RUR);

        acc = accountService.find(2);

        checkAccount(acc, 2, 2, 11, EUR);
    }

    @Test public void testPayerNotFound() throws Exception {
        FraudMonitoringImpl fraudMonitoring = new FraudMonitoringImpl();

        AccountService accountService = new AccountServiceImpl(fraudMonitoring);

        Result res = accountService.create(1, 1, 100f, RUR);

        assertEquals(Result.OK, res);

        res = accountService.create(2, 2, 10, EUR);

        assertEquals(Result.OK, res);

        Payment payment = new Payment(1, 42, Long.MAX_VALUE, 2, 2, 1f);

        res = accountService.doPayment(payment);

        assertEquals(Result.PAYER_NOT_FOUND, res);
    }

    @Test public void testPayerNotFound2() throws Exception {
        FraudMonitoringImpl fraudMonitoring = new FraudMonitoringImpl();

        AccountService accountService = new AccountServiceImpl(fraudMonitoring);

        Result res = accountService.create(1, 1, 100f, RUR);

        assertEquals(Result.OK, res);

        res = accountService.create(2, 2, 10, EUR);

        assertEquals(Result.OK, res);

        Payment payment = new Payment(1, 1, Long.MAX_VALUE, 2, 2, 1f);

        res = accountService.doPayment(payment);

        assertEquals(Result.PAYER_NOT_FOUND, res);
    }

    @Test public void testRecipientNotFound() throws Exception {
        FraudMonitoringImpl fraudMonitoring = new FraudMonitoringImpl();

        AccountService accountService = new AccountServiceImpl(fraudMonitoring);

        Result res = accountService.create(1, 1, 100f, RUR);

        assertEquals(Result.OK, res);

        res = accountService.create(2, 2, 10, EUR);

        assertEquals(Result.OK, res);

        Payment payment = new Payment(1, 1, 1, 42, 2, 1f);

        res = accountService.doPayment(payment);

        assertEquals(Result.RECIPIENT_NOT_FOUND, res);
    }

    @Test public void testRecipientNotFound2() throws Exception {
        FraudMonitoringImpl fraudMonitoring = new FraudMonitoringImpl();

        AccountService accountService = new AccountServiceImpl(fraudMonitoring);

        Result res = accountService.create(1, 1, 100f, RUR);

        assertEquals(Result.OK, res);

        res = accountService.create(2, 2, 10, EUR);

        assertEquals(Result.OK, res);

        Payment payment = new Payment(1, 1, 1, 2, 42, 1f);

        res = accountService.doPayment(payment);

        assertEquals(Result.RECIPIENT_NOT_FOUND, res);
    }

    @Test public void testDuplicateOperations() throws Exception {
        FraudMonitoringImpl fraudMonitoring = new FraudMonitoringImpl();

        AccountService accountService = new AccountServiceImpl(fraudMonitoring);

        Result res = accountService.create(1, 1, 100f, RUR);

        assertEquals(Result.OK, res);

        res = accountService.create(2, 2, 10, EUR);

        assertEquals(Result.OK, res);

        Payment payment = new Payment(1, 1, 1, 2, 2, RUR_TO_EUR);

        res = accountService.doPayment(payment);

        assertEquals(Result.OK, res);

        res = accountService.doPayment(payment);

        assertEquals(Result.ALREADY_EXISTS, res);

        payment = new Payment(1, 42, 42, 42, 42, RUR_TO_EUR);

        res = accountService.doPayment(payment);

        assertEquals(Result.ALREADY_EXISTS, res);

    }

    private void checkAccount(Account acc, long clientID, long accountID, float balance, Currency currency) {
        assertNotNull(acc);

        assertEquals(clientID, acc.getClientID());

        assertEquals(accountID, acc.getAccountID());

        assertEquals(balance, acc.getBalance(), .01f);

        assertEquals(currency, acc.getCurrency());
    }

    public static class FraudMonitoringImpl implements FraudMonitoring {
        public Set<Long> thieves = new HashSet<>();

        @Override public boolean check(long clientID) {
            return thieves.contains(clientID);
        }
    }
}
