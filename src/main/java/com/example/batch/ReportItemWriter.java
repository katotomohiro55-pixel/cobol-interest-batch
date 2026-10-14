package com.example.batch;

/**
 * Utility class for writing output records.
 * This is no longer used as the writing is done inline in BatchConfiguration.
 * Kept for reference.
 */
public class ReportItemWriter {
    
    /**
     * Formats a detail record as a fixed 60-byte line.
     * This is now handled by DetailRecord.toFixedFormat() directly.
     */
    public static String formatDetailRecord(DetailRecord record) {
        return record.toFixedFormat();
    }
    
    /**
     * Formats a trailer record as a fixed 60-byte line.
     * This is now handled by TrailerRecord.toFixedFormat() directly.
     */
    public static String formatTrailerRecord(TrailerRecord record) {
        return record.toFixedFormat();
    }
}
