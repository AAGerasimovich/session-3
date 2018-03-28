package ru.sbt.jschool.session3.problem1;


import java.util.*;

/**
 */
public class AccountServiceImpl implements AccountService {
    protected FraudMonitoring fraudMonitoring;

    private Map<Long, Account> accounts = new HashMap<>();
    private Set<Payment> payments = new HashSet<>();
  //  private Set<Long>




    public AccountServiceImpl(FraudMonitoring fraudMonitoring) {
        this.fraudMonitoring = fraudMonitoring;
    }

    @Override public Result create(long clientID, long accountID, float initialBalance, Currency currency) {
        if (accounts.containsKey(accountID)){
            return Result.ALREADY_EXISTS;
        }
        if(fraudMonitoring.check(clientID)){
            return Result.FRAUD;
        }
        Account acc = new Account(clientID, accountID, currency, initialBalance);
        accounts.put(accountID, acc);
        return Result.OK;
    }

    @Override public List<Account> findForClient(long clientID) {
        List<Account> acc = new ArrayList<>();

        for(Account account: accounts.values()){
            if (account.getClientID()==clientID){
                acc.add(account);
            }
        }
        return acc;
    }

    @Override public Account find(long accountID) {

        return accounts.get(accountID);
    }

    @Override public Result doPayment(Payment payment) {
        if (payments.contains(payment) || (payment.getPayerAccountID()==payment.getRecipientAccountID())){
            return Result.ALREADY_EXISTS;
        } else {
            payments.add(payment);
        }
        if (fraudMonitoring.check(payment.getRecipientID()) || fraudMonitoring.check(payment.getPayerID())){
            return Result.FRAUD;
        }
        Account recipient = find(payment.getRecipientAccountID());
        if (recipient==null || recipient.getClientID() != payment.getRecipientID()){
            return Result.RECIPIENT_NOT_FOUND;
        }
        if (recipient.getBalance() == Long.MAX_VALUE){
            return Result.FRAUD;
        }

        Account payer = find(payment.getPayerAccountID());
        if (payer==null || payer.getClientID() != payment.getPayerID()){
            return Result.PAYER_NOT_FOUND;
        }


        payer.setBalance(payer.getBalance() - payment.getAmount());
        if(payer.getCurrency()!=recipient.getCurrency()) {
            recipient.setBalance(recipient.getBalance() + payer.getCurrency().to(payment.getAmount(), recipient.getCurrency()));
        } else {
            recipient.setBalance(recipient.getBalance() + payment.getAmount());
        }

        return Result.OK;
    }
}
