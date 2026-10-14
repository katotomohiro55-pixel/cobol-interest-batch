package com.example.batch;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Simple batch processor for interest calculation without Spring Batch.
 * Reads accounts.dat, calculates interest, and writes output file.
 */
public class BatchConfiguration {
    
    private final InterestCalculator calculator = new InterestCalculator();
    
    public void run(String inputFile, String outputFile) throws IOException {
        // Read input
        List<AccountRecord> accounts = readInputFile(inputFile);
        
        // Process and write output
        writeOutputFile(accounts, outputFile);
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
    
    private void writeOutputFile(List<AccountRecord> accounts, String outputPath) throws IOException {
        Path path = Paths.get(outputPath);
        Files.createDirectories(path.getParent());
        
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputPath))) {
            long totalInterest = 0;
            
            // Write detail records
            for (AccountRecord account : accounts) {
                BigDecimal appliedRate = calculator.calculateAppliedRate(account);
                long interestYen = calculator.calculateInterestYen(account);
                
                DetailRecord detail = new DetailRecord(
                    account.getAccountId(),
                    account.getAccountType(),
                    appliedRate,
                    interestYen
                );
                
                writer.write(detail.toFixedFormat());
                writer.newLine();
                
                totalInterest += interestYen;
            }
            
            // Write trailer
            TrailerRecord trailer = new TrailerRecord(accounts.size(), totalInterest);
            writer.write(trailer.toFixedFormat());
            writer.newLine();
        }
    }
}
