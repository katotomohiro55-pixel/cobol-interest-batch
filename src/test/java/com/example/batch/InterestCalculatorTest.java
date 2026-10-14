package com.example.batch;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the InterestCalculator, verifying the exact behavior specified in SPEC.md.
 */
public class InterestCalculatorTest {
    
    private final InterestCalculator calculator = new InterestCalculator();
    
    @Test
    public void testRateOverrideTakesPrecedence() {
        // ACC0000005: T account with override 0.0030
        AccountRecord record = new AccountRecord(
            "ACC0000005",
            'T',
            new BigDecimal("100000000.00"),  // 10,000,000 JPY
            365,
            new BigDecimal("0.0030")  // Override beats table rate of 0.0025
        );
        
        BigDecimal appliedRate = calculator.calculateAppliedRate(record);
        assertEquals(new BigDecimal("0.0030"), appliedRate);
        
        long interest = calculator.calculateInterestYen(record);
        assertEquals(30000, interest, "Rate override should result in 30,000 yen interest");
    }
    
    @Test
    public void testProrationWithHalfUpRounding() {
        // ACC0000002: T account, 3,000,000 JPY, 180 days, rate 0.0025
        // Annual interest = 3,000,000 * 0.0025 = 7,500
        // Accrued = 7,500 * 180 / 365 = 3,698.6301... -> 3,698.6301 (4dp) -> 3,699 (half-up)
        AccountRecord record = new AccountRecord(
            "ACC0000002",
            'T',
            new BigDecimal("3000000.00"),
            180,
            BigDecimal.ZERO  // No override, use table rate
        );
        
        long interest = calculator.calculateInterestYen(record);
        assertEquals(3699, interest, "Proration with half-up rounding should give 3,699 yen");
    }
    
    @Test
    public void testLargeBalanceWithRounding() {
        // ACC0000007: O account, 99,999,999 JPY, 365 days, rate 0.0010
        // Annual interest = 99,999,999 * 0.0010 = 99,999.999
        // Accrued = 99,999.999 * 365 / 365 = 99,999.999 -> 100,000 (half-up)
        AccountRecord record = new AccountRecord(
            "ACC0000007",
            'O',
            new BigDecimal("99999999.00"),
            365,
            BigDecimal.ZERO
        );
        
        long interest = calculator.calculateInterestYen(record);
        assertEquals(100000, interest, "Large balance with 99,999.999 should round to 100,000");
    }
    
    @Test
    public void testPremiumWithOverrideAndProration() {
        // ACC0000009: P account with override 0.0055, 42,000,000 JPY, 270 days
        // Annual interest = 42,000,000 * 0.0055 = 231,000
        // Accrued = 231,000 * 270 / 365 = 170,876.7123... -> 170,876.7123 (4dp) -> 170,877 (half-up)
        AccountRecord record = new AccountRecord(
            "ACC0000009",
            'P',
            new BigDecimal("42000000.00"),
            270,
            new BigDecimal("0.0055")
        );
        
        long interest = calculator.calculateInterestYen(record);
        assertEquals(170877, interest, "Override with proration should give 170,877 yen");
    }
    
    @Test
    public void testRoundToZero() {
        // ACC0000010: O account, 500 JPY, 7 days, rate 0.0010
        // Annual interest = 500 * 0.0010 = 0.5
        // Accrued = 0.5 * 7 / 365 = 0.009589... -> 0.0096 (4dp) -> 0 (half-up to yen)
        AccountRecord record = new AccountRecord(
            "ACC0000010",
            'O',
            new BigDecimal("500.00"),
            7,
            BigDecimal.ZERO
        );
        
        long interest = calculator.calculateInterestYen(record);
        assertEquals(0, interest, "Small interest should round to zero yen");
    }
    
    @Test
    public void testOrdinaryRate() {
        AccountRecord record = new AccountRecord(
            "ACC0000001",
            'O',
            new BigDecimal("1500000.00"),
            365,
            BigDecimal.ZERO
        );
        
        BigDecimal appliedRate = calculator.calculateAppliedRate(record);
        assertEquals(new BigDecimal("0.0010"), appliedRate);
    }
    
    @Test
    public void testTimeRate() {
        AccountRecord record = new AccountRecord(
            "ACC0000002",
            'T',
            new BigDecimal("3000000.00"),
            180,
            BigDecimal.ZERO
        );
        
        BigDecimal appliedRate = calculator.calculateAppliedRate(record);
        assertEquals(new BigDecimal("0.0025"), appliedRate);
    }
    
    @Test
    public void testPremiumRate() {
        AccountRecord record = new AccountRecord(
            "ACC0000003",
            'P',
            new BigDecimal("5000000.00"),
            90,
            BigDecimal.ZERO
        );
        
        BigDecimal appliedRate = calculator.calculateAppliedRate(record);
        assertEquals(new BigDecimal("0.0040"), appliedRate);
    }
    
    @Test
    public void testUnknownAccountTypeDefaultsToOrdinary() {
        AccountRecord record = new AccountRecord(
            "ACC0000001",
            'X',  // Unknown type
            new BigDecimal("1500000.00"),
            365,
            BigDecimal.ZERO
        );
        
        BigDecimal appliedRate = calculator.calculateAppliedRate(record);
        assertEquals(new BigDecimal("0.0010"), appliedRate, "Unknown type should default to Ordinary rate");
    }
}
