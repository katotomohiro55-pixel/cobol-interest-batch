package com.example.batch;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test that verifies the Java output matches the COBOL baseline exactly.
 * Tests the full end-to-end flow against data/expected_output.dat.
 */
public class OutputEquivalenceTest {
    
    private final InterestCalculator calculator = new InterestCalculator();
    
    @Test
    public void testFullOutputMatchesBaseline() throws IOException {
        // Read input from data/accounts.dat
        List<AccountRecord> accounts = readInputFile("data/accounts.dat");
        
        // Process each account
        List<DetailRecord> details = new ArrayList<>();
        long totalInterest = 0;
        
        for (AccountRecord account : accounts) {
            BigDecimal appliedRate = calculator.calculateAppliedRate(account);
            long interestYen = calculator.calculateInterestYen(account);
            
            details.add(new DetailRecord(
                account.getAccountId(),
                account.getAccountType(),
                appliedRate,
                interestYen
            ));
            
            totalInterest += interestYen;
        }
        
        // Generate output lines
        List<String> outputLines = new ArrayList<>();
        for (DetailRecord detail : details) {
            outputLines.add(detail.toFixedFormat());
        }
        
        // Add trailer
        TrailerRecord trailer = new TrailerRecord(details.size(), totalInterest);
        outputLines.add(trailer.toFixedFormat());
        
        // Read expected output
        List<String> expectedLines = readExpectedFile("data/expected_output.dat");
        
        // Compare line by line (ignoring trailing whitespace)
        assertEquals(expectedLines.size(), outputLines.size(), 
            "Output should have same number of lines as expected");
        
        for (int i = 0; i < expectedLines.size(); i++) {
            String expected = expectedLines.get(i).trim();
            String actual = outputLines.get(i).trim();
            assertEquals(expected, actual, 
                "Line " + (i + 1) + " should match expected output");
        }
        
        // Verify totals
        assertEquals(10, details.size(), "Should have 10 records");
        assertEquals(312710, totalInterest, "Total interest should be 312,710 yen");
    }
    
    @Test
    public void testSpecificTrickyCases() throws IOException {
        // Read input and verify specific accounts from SPEC.md
        List<AccountRecord> accounts = readInputFile("data/accounts.dat");
        
        // Find and verify ACC0000002 (proration + half-up: 3699)
        AccountRecord acc2 = accounts.stream()
            .filter(a -> a.getAccountId().equals("ACC0000002"))
            .findFirst()
            .orElseThrow();
        assertEquals(3699, calculator.calculateInterestYen(acc2), 
            "ACC0000002 should have 3,699 yen interest");
        
        // Find and verify ACC0000005 (rate override: 30000)
        AccountRecord acc5 = accounts.stream()
            .filter(a -> a.getAccountId().equals("ACC0000005"))
            .findFirst()
            .orElseThrow();
        assertEquals(30000, calculator.calculateInterestYen(acc5), 
            "ACC0000005 should have 30,000 yen interest");
        
        // Find and verify ACC0000007 (large balance: 100000)
        AccountRecord acc7 = accounts.stream()
            .filter(a -> a.getAccountId().equals("ACC0000007"))
            .findFirst()
            .orElseThrow();
        assertEquals(100000, calculator.calculateInterestYen(acc7), 
            "ACC0000007 should have 100,000 yen interest");
        
        // Find and verify ACC0000009 (override + proration: 170877)
        AccountRecord acc9 = accounts.stream()
            .filter(a -> a.getAccountId().equals("ACC0000009"))
            .findFirst()
            .orElseThrow();
        assertEquals(170877, calculator.calculateInterestYen(acc9), 
            "ACC0000009 should have 170,877 yen interest");
        
        // Find and verify ACC0000010 (round to zero: 0)
        AccountRecord acc10 = accounts.stream()
            .filter(a -> a.getAccountId().equals("ACC0000010"))
            .findFirst()
            .orElseThrow();
        assertEquals(0, calculator.calculateInterestYen(acc10), 
            "ACC0000010 should have 0 yen interest");
    }
    
    private List<AccountRecord> readInputFile(String filePath) throws IOException {
        List<AccountRecord> records = new ArrayList<>();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.length() >= 33) {
                    String accountId = line.substring(0, 10).trim();
                    char accountType = line.charAt(10);
                    String balanceStr = line.substring(11, 24).trim();
                    BigDecimal balance = new BigDecimal(balanceStr).divide(new BigDecimal("100"));
                    String termDaysStr = line.substring(24, 28).trim();
                    int termDays = Integer.parseInt(termDaysStr);
                    String rateOverrideStr = line.substring(28, 33).trim();
                    BigDecimal rateOverride = new BigDecimal(rateOverrideStr).divide(new BigDecimal("10000"));
                    
                    records.add(new AccountRecord(accountId, accountType, balance, termDays, rateOverride));
                }
            }
        }
        
        return records;
    }
    
    private List<String> readExpectedFile(String filePath) throws IOException {
        List<String> lines = new ArrayList<>();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        }
        
        return lines;
    }
}
