package com.example.batch;

import java.math.BigDecimal;

/**
 * Utility class for parsing account records from fixed-length input.
 * This is no longer used as the parsing is done inline in BatchConfiguration.
 * Kept for reference.
 */
public class AccountItemReader {
    
    /**
     * Parses a fixed-length line into an AccountRecord.
     * Layout per ACCTREC.cpy:
     * - Account ID: positions 1-10 (X(10))
     * - Account Type: position 11 (X(01))
     * - Balance: positions 12-24 (9(11)V99, 2 implied decimals)
     * - Term Days: positions 25-28 (9(04))
     * - Rate Override: positions 29-33 (9(01)V9(04))
     * - Filler: positions 34-60 (X(29))
     */
    public static AccountRecord parseLine(String line) {
        if (line.length() < 33) {
            throw new IllegalArgumentException("Line too short: " + line);
        }
        
        String accountId = line.substring(0, 10).trim();
        char accountType = line.charAt(10);
        
        // Balance: positions 11-23 (0-indexed: 11-23), which is 13 chars representing 9(11)V99
        String balanceStr = line.substring(11, 24).trim();
        BigDecimal balance = new BigDecimal(balanceStr).divide(new BigDecimal("100"));
        
        // Term days: positions 24-27 (0-indexed: 24-27), which is 4 chars
        String termDaysStr = line.substring(24, 28).trim();
        int termDays = Integer.parseInt(termDaysStr);
        
        // Rate override: positions 28-32 (0-indexed: 28-32), which is 6 chars for 9(01)V9(04)
        String rateOverrideStr = line.substring(28, 33).trim();
        BigDecimal rateOverride = new BigDecimal(rateOverrideStr);
        
        return new AccountRecord(accountId, accountType, balance, termDays, rateOverride);
    }
}
