      ******************************************************************
      * ACCTREC.cpy  -  Account master record layout (fixed length)    *
      * Record length: 60 bytes.  Used by INTCALC batch.               *
      *                                                                *
      * Fixed-length LINE SEQUENTIAL file. Numeric fields are stored *
      * as zoned DISPLAY (external) decimals for portability. *
      * Internal interest math in INTCALC uses COMP-3, as on the *
      * original GS21 asset. *
      ******************************************************************
       01  ACCOUNT-RECORD.
           05  AR-ACCOUNT-ID          PIC X(10).
           05  AR-ACCOUNT-TYPE        PIC X(01).
      *        'O' = Ordinary deposit, 'T' = Time deposit,
      *        'P' = Premium time deposit
           05  AR-BALANCE             PIC 9(11)V99.
      *        current principal in JPY (13 digits, 2 implied decimals)
           05  AR-TERM-DAYS           PIC 9(04).
      *        number of days the interest accrues over
           05  AR-RATE-OVERRIDE       PIC 9(01)V9(04).
      *        per-account rate override; 0 means use standard table
           05  FILLER                 PIC X(29).
