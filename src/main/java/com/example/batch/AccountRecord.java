package com.example.batch;

import java.math.BigDecimal;

/**
 * Represents an account record from the input file.
 * Corresponds to ACCTREC.cpy in the COBOL program.
 */
public class AccountRecord {
    private String accountId;
    private char accountType;
    private BigDecimal balance;
    private int termDays;
    private BigDecimal rateOverride;

    public AccountRecord() {
    }

    public AccountRecord(String accountId, char accountType, BigDecimal balance,
                         int termDays, BigDecimal rateOverride) {
        this.accountId = accountId;
        this.accountType = accountType;
        this.balance = balance;
        this.termDays = termDays;
        this.rateOverride = rateOverride;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public char getAccountType() {
        return accountType;
    }

    public void setAccountType(char accountType) {
        this.accountType = accountType;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public int getTermDays() {
        return termDays;
    }

    public void setTermDays(int termDays) {
        this.termDays = termDays;
    }

    public BigDecimal getRateOverride() {
        return rateOverride;
    }

    public void setRateOverride(BigDecimal rateOverride) {
        this.rateOverride = rateOverride;
    }

    @Override
    public String toString() {
        return "AccountRecord{" +
                "accountId='" + accountId + '\'' +
                ", accountType=" + accountType +
                ", balance=" + balance +
                ", termDays=" + termDays +
                ", rateOverride=" + rateOverride +
                '}';
    }
}
