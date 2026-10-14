package com.example.batch;

import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.ItemStreamWriter;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.ExecutionContext;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Writes detail records and a trailer record to the output file.
 * Corresponds to the COBOL REPORT-FILE output.
 */
public class ReportItemWriter implements ItemWriter<DetailRecord>, ItemStreamWriter<DetailRecord> {
    
    private final String outputFilePath;
    private BufferedWriter writer;
    private int recordCount = 0;
    private long interestTotal = 0;
    
    public ReportItemWriter(String outputFilePath) {
        this.outputFilePath = outputFilePath;
    }
    
    @Override
    public void open() throws ItemStreamException {
        try {
            Path path = Paths.get(outputFilePath);
            Files.createDirectories(path.getParent());
            writer = new BufferedWriter(new FileWriter(outputFilePath));
        } catch (IOException e) {
            throw new ItemStreamException("Failed to open output file: " + outputFilePath, e);
        }
    }
    
    @Override
    public void write(List<? extends DetailRecord> items) throws Exception {
        for (DetailRecord record : items) {
            String line = record.toFixedFormat();
            writer.write(line);
            writer.newLine();
            
            recordCount++;
            interestTotal += record.getInterestYen();
        }
    }
    
    @Override
    public void close() throws ItemStreamException {
        try {
            // Write trailer record
            TrailerRecord trailer = new TrailerRecord(recordCount, interestTotal);
            writer.write(trailer.toFixedFormat());
            writer.newLine();
            
            if (writer != null) {
                writer.close();
            }
        } catch (IOException e) {
            throw new ItemStreamException("Failed to close output file: " + outputFilePath, e);
        }
    }
    
    @Override
    public void update(ExecutionContext executionContext) throws ItemStreamException {
        // Not needed for this simple implementation
    }
}
