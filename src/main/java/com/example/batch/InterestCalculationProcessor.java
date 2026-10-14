package com.example.batch;

import org.springframework.batch.item.ItemProcessor;

/**
 * Processes an AccountRecord into a DetailRecord by calculating interest.
 */
public class InterestCalculationProcessor implements ItemProcessor<AccountRecord, DetailRecord> {
    
    private final InterestCalculator calculator;
    
    public InterestCalculationProcessor() {
        this.calculator = new InterestCalculator();
    }
    
    @Override
    public DetailRecord process(AccountRecord record) throws Exception {
        BigDecimal appliedRate = calculator.calculateAppliedRate(record);
        long interestYen = calculator.calculateInterestYen(record);
        
        return new DetailRecord(
            record.getAccountId(),
            record.getAccountType(),
            appliedRate,
            interestYen
        );
    }
}
