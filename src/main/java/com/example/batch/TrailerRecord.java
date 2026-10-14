package com.example.batch;

/**
 * Represents the trailer record in the output report.
 * Corresponds to WS-TRAILER-LINE in the COBOL program.
 */
public class TrailerRecord {
    private int recordCount;
    private long interestTotal;

    public TrailerRecord() {
    }

    public TrailerRecord(int recordCount, long interestTotal) {
        this.recordCount = recordCount;
        this.interestTotal = interestTotal;
    }

    public int getRecordCount() {
        return recordCount;
    }

    public void setRecordCount(int recordCount) {
        this.recordCount = recordCount;
    }

    public long getInterestTotal() {
        return interestTotal;
    }

    public void setInterestTotal(long interestTotal) {
        this.interestTotal = interestTotal;
    }

    /**
     * Formats the trailer record.
     * Layout: "TRAILER  " (9) + record count (7) + space + interest total (13)
     * Note: No trailing spaces to match GnuCOBOL behavior.
     */
    public String toFixedFormat() {
        StringBuilder sb = new StringBuilder();
        
        // Literal "TRAILER  " (9 chars: TRAILER + 2 spaces)
        sb.append("TRAILER  ");
        
        // Record count (7 chars, zero-padded)
        sb.append(String.format("%07d", recordCount));
        
        // Space
        sb.append(' ');
        
        // Interest total (13 chars, zero-padded)
        sb.append(String.format("%013d", interestTotal));
        
        return sb.toString();
    }
}
