      ******************************************************************
      * INTCALC.cbl                                                    *
      * Interest-calculation batch for a retail bank.                  *
      *                                                                *
      * Reads a fixed-length account master file (ACCOUNT-FILE),       *
      * computes accrued interest per account using an account-type    *
      * rate table with per-account override, day-count proration on   *
      * a 365-day basis, and half-up rounding to the yen, then writes  *
      * a fixed-length report record per account plus a trailer with   *
      * record count and interest total (control totals).              *
      *                                                                *
      * This program is intentionally representative of legacy GS21    *
      * COBOL assets: COMP-3 fields, COPY book, PERFORM loops,         *
      * level-88 conditions, and ROUNDED arithmetic.                   *
      ******************************************************************
       IDENTIFICATION DIVISION.
       PROGRAM-ID. INTCALC.
       AUTHOR. MIGRATION-DEMO.

       ENVIRONMENT DIVISION.
       INPUT-OUTPUT SECTION.
       FILE-CONTROL.
           SELECT ACCOUNT-FILE ASSIGN TO "ACCTIN"
               ORGANIZATION IS LINE SEQUENTIAL
               FILE STATUS IS WS-ACCT-STATUS.
           SELECT REPORT-FILE ASSIGN TO "RPTOUT"
               ORGANIZATION IS LINE SEQUENTIAL
               FILE STATUS IS WS-RPT-STATUS.

       DATA DIVISION.
       FILE SECTION.
       FD  ACCOUNT-FILE.
       COPY ACCTREC.

       FD  REPORT-FILE.
       01  REPORT-RECORD              PIC X(60).

       WORKING-STORAGE SECTION.
       01  WS-STATUS-FLAGS.
           05  WS-ACCT-STATUS         PIC X(02).
           05  WS-RPT-STATUS          PIC X(02).
           05  WS-EOF-FLAG            PIC X(01) VALUE 'N'.
               88  END-OF-FILE        VALUE 'Y'.

      * Standard interest-rate table (annual, as a decimal fraction).
      * Ordinary = 0.0010, Time = 0.0025, Premium time = 0.0040.
       01  WS-RATE-TABLE.
           05  WS-RATE-ORDINARY       PIC S9(01)V9(04) COMP-3
                                      VALUE 0.0010.
           05  WS-RATE-TIME           PIC S9(01)V9(04) COMP-3
                                      VALUE 0.0025.
           05  WS-RATE-PREMIUM        PIC S9(01)V9(04) COMP-3
                                      VALUE 0.0040.

       01  WS-WORK-FIELDS.
           05  WS-APPLIED-RATE        PIC S9(01)V9(04) COMP-3.
           05  WS-ANNUAL-INTEREST     PIC S9(13)V9(04) COMP-3.
           05  WS-ACCR-INTEREST       PIC S9(13)V9(04) COMP-3.
           05  WS-INTEREST-YEN        PIC S9(11)      COMP-3.

       01  WS-CONTROL-TOTALS.
           05  WS-REC-COUNT           PIC 9(07) VALUE 0.
           05  WS-INT-TOTAL           PIC S9(13) VALUE 0.

      * Output detail record layout (fixed 60 bytes).
       01  WS-DETAIL-LINE.
           05  DL-ACCOUNT-ID          PIC X(10).
           05  FILLER                 PIC X(01) VALUE ' '.
           05  DL-ACCOUNT-TYPE        PIC X(01).
           05  FILLER                 PIC X(01) VALUE ' '.
           05  DL-RATE                PIC 9(01).9(04).
           05  FILLER                 PIC X(01) VALUE ' '.
           05  DL-INTEREST            PIC 9(11).
           05  FILLER                 PIC X(29) VALUE SPACES.

      * Output trailer record layout (fixed 60 bytes).
       01  WS-TRAILER-LINE.
           05  FILLER                 PIC X(09) VALUE 'TRAILER  '.
           05  TL-REC-COUNT           PIC 9(07).
           05  FILLER                 PIC X(01) VALUE ' '.
           05  TL-INT-TOTAL           PIC 9(13).
           05  FILLER                 PIC X(30) VALUE SPACES.

       PROCEDURE DIVISION.
       0000-MAIN.
           PERFORM 1000-OPEN-FILES
           PERFORM 2000-PROCESS-RECORDS
               UNTIL END-OF-FILE
           PERFORM 3000-WRITE-TRAILER
           PERFORM 9000-CLOSE-FILES
           STOP RUN.

       1000-OPEN-FILES.
           OPEN INPUT  ACCOUNT-FILE
           OPEN OUTPUT REPORT-FILE
           IF WS-ACCT-STATUS NOT = '00'
               DISPLAY 'ERROR OPENING ACCOUNT-FILE: ' WS-ACCT-STATUS
               STOP RUN
           END-IF
           PERFORM 2100-READ-ACCOUNT.

       2000-PROCESS-RECORDS.
           PERFORM 2200-COMPUTE-INTEREST
           PERFORM 2300-WRITE-DETAIL
           ADD 1 TO WS-REC-COUNT
           ADD WS-INTEREST-YEN TO WS-INT-TOTAL
           PERFORM 2100-READ-ACCOUNT.

       2100-READ-ACCOUNT.
           READ ACCOUNT-FILE
               AT END
                   SET END-OF-FILE TO TRUE
           END-READ.

       2200-COMPUTE-INTEREST.
      *    Determine the applicable annual rate.
           IF AR-RATE-OVERRIDE > 0
               MOVE AR-RATE-OVERRIDE TO WS-APPLIED-RATE
           ELSE
               EVALUATE AR-ACCOUNT-TYPE
                   WHEN 'O'
                       MOVE WS-RATE-ORDINARY TO WS-APPLIED-RATE
                   WHEN 'T'
                       MOVE WS-RATE-TIME     TO WS-APPLIED-RATE
                   WHEN 'P'
                       MOVE WS-RATE-PREMIUM  TO WS-APPLIED-RATE
                   WHEN OTHER
                       MOVE WS-RATE-ORDINARY TO WS-APPLIED-RATE
               END-EVALUATE
           END-IF
      *    Annual interest = balance * applied-rate.
           COMPUTE WS-ANNUAL-INTEREST ROUNDED =
               AR-BALANCE * WS-APPLIED-RATE
      *    Prorate over the term on a 365-day basis.
           COMPUTE WS-ACCR-INTEREST ROUNDED =
               WS-ANNUAL-INTEREST * AR-TERM-DAYS / 365
      *    Round half-up to whole yen.
           COMPUTE WS-INTEREST-YEN ROUNDED = WS-ACCR-INTEREST.

       2300-WRITE-DETAIL.
           MOVE AR-ACCOUNT-ID    TO DL-ACCOUNT-ID
           MOVE AR-ACCOUNT-TYPE  TO DL-ACCOUNT-TYPE
           MOVE WS-APPLIED-RATE  TO DL-RATE
           MOVE WS-INTEREST-YEN  TO DL-INTEREST
           MOVE WS-DETAIL-LINE   TO REPORT-RECORD
           WRITE REPORT-RECORD.

       3000-WRITE-TRAILER.
           MOVE WS-REC-COUNT  TO TL-REC-COUNT
           MOVE WS-INT-TOTAL  TO TL-INT-TOTAL
           MOVE WS-TRAILER-LINE TO REPORT-RECORD
           WRITE REPORT-RECORD.

       9000-CLOSE-FILES.
           CLOSE ACCOUNT-FILE
           CLOSE REPORT-FILE.
