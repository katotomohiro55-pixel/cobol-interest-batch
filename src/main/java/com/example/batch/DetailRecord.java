package com.example.batch;

import java.math.BigDecimal;

/**
 * Represents a detail record in the output report.
 * Corresponds to WS-DETAIL-LINE in the COBOL program.
 */
public class DetailRecord {
    private String accountId;
    private char accountType;
    private BigDecimal appliedRate;
    private long interestYen;

    public DetailRecord() {
    }

    public DetailRecord(String accountId, char accountType, BigDecimal appliedRate, long interestYen) {
        this.accountId = accountId;
        this.accountType = accountType;
        this.appliedRate = appliedRate;
        this.interestYen = interestYen;
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

    public BigDecimal getAppliedRate() {
        return appliedRate;
    }

    public void setAppliedRate(BigDecimal appliedRate) {
        this.appliedRate = appliedRate;
    }

    public long getInterestYen() {
        return interestYen;
    }

    public void setInterestYen(long interestYen) {
        this.interestYen = interestYen;
    }

    /**
     * Formats the detail record as a fixed 60-byte line.
     * Layout: Account ID (10) + space + Type (1) + space + Rate (6) + space + Interest (11) + spaces (29)
     * Note: Trailing spaces are trimmed to match GnuCOBOL behavior.
     */
    public String toFixedFormat() {
        StringBuilder sb = new StringBuilder(60);
        
        // Account ID (10 chars, left-padded if needed)
        sb.append(String.format("%-10s", accountId));
        
        // Space
        sb.append(' ');
        
        // Account type (1 char)
        sb.append(accountType);
        
        // Space
        sb.append(' ');
        
        // Rate as 9.9999 (6 chars)
        sb.append(String.format("%1.4f", appliedRate));
        
        // Space
        sb.append(' ');
        
        // Interest in yen (11 chars, zero-padded)
        sb.append(String.format("%011d", interestYen));
        
        // Filler (29 spaces)
        while (sb.length() < 60) {
            sb.append(' ');
        }
        
        // Trim trailing spaces to match GnuCOBOL behavior
        return sb.toString().trim();
    }
}
