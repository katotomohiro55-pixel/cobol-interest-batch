package com.example.batch;

import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.core.io.PathResource;

import java.math.BigDecimal;

/**
 * Reads account records from the fixed-length input file.
 * The input is fixed-length (60 bytes) but we'll parse it character by character.
 */
public class AccountItemReader extends FlatFileItemReader<AccountRecord> {
    
    public AccountItemReader(String inputFilePath) {
        setResource(new PathResource(inputFilePath));
        
        // Use a custom line mapper for fixed-length parsing
        setLineMapper(new AccountLineMapper());
    }
    
    /**
     * Custom line mapper that parses fixed-length records.
     * Layout per ACCTREC.cpy:
     * - Account ID: positions 1-10 (X(10))
     * - Account Type: position 11 (X(01))
     * - Balance: positions 12-24 (9(11)V99, 2 implied decimals)
     * - Term Days: positions 25-28 (9(04))
     * - Rate Override: positions 29-33 (9(01)V9(04))
     * - Filler: positions 34-60 (X(29))
     */
    private static class AccountLineMapper implements LineMapper<AccountRecord> {
        @Override
        public AccountRecord mapLine(String line, int lineNumber) throws Exception {
            // Ensure line is at least 33 characters (we need up to rate override)
            if (line.length() < 33) {
                throw new IllegalArgumentException("Line too short: " + line);
            }
            
            String accountId = line.substring(0, 10).trim();
            char accountType = line.charAt(10);
            
            // Balance: positions 11-23 (0-indexed: 11-23), which is 13 chars representing 9(11)V99
            // In COBOL 9(11)V99 means 11 digits before decimal, 2 after
            // The input is zoned decimal, but in the data file it appears as numeric
            String balanceStr = line.substring(11, 24).trim();
            // Convert to BigDecimal (divide by 100 for the 2 implied decimals)
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
}
