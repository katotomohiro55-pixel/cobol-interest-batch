package com.example.batch;

import java.io.IOException;

/**
 * Main application to run the interest calculation batch job.
 */
public class Application {
    
    public static void main(String[] args) {
        try {
            BatchConfiguration batch = new BatchConfiguration();
            batch.run("data/accounts.dat", "output/java_output.dat");
            
            System.out.println("Batch completed successfully");
            System.out.println("Output written to: output/java_output.dat");
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
