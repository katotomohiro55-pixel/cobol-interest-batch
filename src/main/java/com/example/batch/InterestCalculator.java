package com.example.batch;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Calculates interest according to the COBOL specification.
 * Implements the exact rounding rules and rate table from SPEC.md.
 */
public class InterestCalculator {
    
    // Standard interest-rate table (annual, as a decimal fraction)
    private static final BigDecimal RATE_ORDINARY = new BigDecimal("0.0010");
    private static final BigDecimal RATE_TIME = new BigDecimal("0.0025");
    private static final BigDecimal RATE_PREMIUM = new BigDecimal("0.0040");
    
    // Divisor for day-count proration (fixed 365-day basis)
    private static final BigDecimal DAYS_PER_YEAR = new BigDecimal("365");
    
    // Scale for intermediate calculations (4 decimal places as in COBOL)
    private static final int SCALE = 4;
    
    /**
     * Calculates the applied annual rate for an account.
     * Rules:
     * - If rateOverride > 0, use it
     * - Otherwise look up by account type:
     *   - O = 0.0010
     *   - T = 0.0025
     *   - P = 0.0040
     *   - any other = 0.0010 (default to Ordinary)
     */
    public BigDecimal calculateAppliedRate(AccountRecord record) {
        if (record.getRateOverride().compareTo(BigDecimal.ZERO) > 0) {
            return record.getRateOverride();
        }
        
        switch (record.getAccountType()) {
            case 'O':
                return RATE_ORDINARY;
            case 'T':
                return RATE_TIME;
            case 'P':
                return RATE_PREMIUM;
            default:
                return RATE_ORDINARY;
        }
    }
    
    /**
     * Calculates accrued interest for an account.
     * Two-step rounding process:
     * 1. Annual interest = balance * applied-rate, rounded to 4 decimals (half-up)
     * 2. Accrued interest = annual-interest * term-days / 365, rounded to 4 decimals (half-up)
     * 3. Interest in yen = accrued interest rounded to whole yen (half-up)
     * 
     * This reproduces the exact COBOL behavior with ROUNDED arithmetic.
     */
    public long calculateInterestYen(AccountRecord record) {
        BigDecimal appliedRate = calculateAppliedRate(record);
        BigDecimal balance = record.getBalance();
        int termDays = record.getTermDays();
        
        // Step 1: Annual interest = balance * applied-rate, rounded to 4 decimals
        BigDecimal annualInterest = balance.multiply(appliedRate)
                .setScale(SCALE, RoundingMode.HALF_UP);
        
        // Step 2: Prorate over term on 365-day basis, rounded to 4 decimals
        BigDecimal termDaysBigDecimal = new BigDecimal(termDays);
        BigDecimal accruedInterest = annualInterest.multiply(termDaysBigDecimal)
                .divide(DAYS_PER_YEAR, SCALE, RoundingMode.HALF_UP);
        
        // Step 3: Round half-up to whole yen
        long interestYen = accruedInterest.setScale(0, RoundingMode.HALF_UP).longValue();
        
        return interestYen;
    }
}
