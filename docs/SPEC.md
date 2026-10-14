# INTCALC — Functional Specification (Before state)

This document describes the behaviour of the legacy `INTCALC` COBOL batch so the
migration can be validated against a known-good baseline. **Equivalence of
output is the success criterion** for the migration: the Java program must
produce a byte-identical `expected_output.dat`.

## Purpose

`INTCALC` reads an account master file and computes accrued interest for each
account, writing one detail record per account plus a trailer with control
totals (record count and summed interest).

## Input — `data/accounts.dat`

Fixed-length, 60 bytes per record, LINE SEQUENTIAL. Layout (`ACCTREC.cpy`):

| Field | Positions | PIC | Notes |
|-------|-----------|-----|-------|
| `AR-ACCOUNT-ID`    | 1–10  | `X(10)`        | Account identifier |
| `AR-ACCOUNT-TYPE`  | 11    | `X(01)`        | `O` ordinary, `T` time, `P` premium time |
| `AR-BALANCE`       | 12–24 | `9(11)V99`     | Principal in JPY, 2 implied decimals (zoned) |
| `AR-TERM-DAYS`     | 25–28 | `9(04)`        | Accrual period in days |
| `AR-RATE-OVERRIDE` | 29–33 | `9(01)V9(04)`  | Per-account annual rate; `00000` = use table |
| `FILLER`           | 34–60 | `X(29)`        | Unused |

## Interest rules

1. **Applied annual rate**
   - If `AR-RATE-OVERRIDE > 0`, use it.
   - Otherwise look up by account type:
     - `O` Ordinary = **0.0010**
     - `T` Time = **0.0025**
     - `P` Premium = **0.0040**
     - any other value defaults to the Ordinary rate.
2. **Annual interest** = `balance × applied-rate`, result held to 4 decimals,
   `ROUNDED` (half-up).
3. **Accrued interest** = `annual-interest × term-days ÷ 365`, held to 4
   decimals, `ROUNDED` (half-up). The 365-day divisor is fixed (no leap-year
   handling) — this is deliberate legacy behaviour.
4. **Interest in yen** = accrued interest `ROUNDED` to a whole yen (half-up).

> COBOL `ROUNDED` is **round half away from zero**; all amounts here are
> non-negative, so this is equivalent to **round half up**. The Java port must
> use `RoundingMode.HALF_UP` on `BigDecimal` — never `double`.

## Output — report file (written to `RPTOUT`)

Fixed-length, 60 bytes per record, LINE SEQUENTIAL.

**Detail record** (one per account, `WS-DETAIL-LINE`):

| Field | Positions | Content |
|-------|-----------|---------|
| Account ID | 1–10 | `AR-ACCOUNT-ID` |
| (space)    | 11   | ` ` |
| Account type | 12 | `AR-ACCOUNT-TYPE` |
| (space)    | 13   | ` ` |
| Rate       | 14–19 | applied rate as `9.9999` (e.g. `0.0010`) |
| (space)    | 20   | ` ` |
| Interest   | 21–31 | interest in yen, `9(11)` zero-padded |
| FILLER     | 32–60 | spaces |

**Trailer record** (`WS-TRAILER-LINE`), written once at end:

| Field | Positions | Content |
|-------|-----------|---------|
| Literal | 1–9  | `TRAILER ` + trailing space |
| Record count | 10–16 | `9(07)` zero-padded |
| (space) | 17 | ` ` |
| Interest total | 18–30 | `9(13)` zero-padded, sum of yen interest |
| FILLER | 31–60 | spaces |

## Worked examples (from the provided dataset)

| Account | Type | Balance | Days | Rate | Interest (yen) | Why it matters |
|---------|------|---------|------|------|----------------|----------------|
| ACC0000002 | T | 3,000,000 | 180 | 0.0025 | 3,699 | proration + half-up (3698.63 → 3699) |
| ACC0000005 | T | 10,000,000 | 365 | 0.0030 | 30,000 | rate override beats table |
| ACC0000007 | O | 99,999,999 | 365 | 0.0010 | 100,000 | large balance, 99999.999 → 100000 |
| ACC0000009 | P | 42,000,000 | 270 | 0.0055 | 170,877 | override + proration |
| ACC0000010 | O | 500 | 7 | 0.0010 | 0 | rounds down to zero |

Totals: **10 records**, interest total **312,710**.

## Note on output format

The report is written with `ORGANIZATION IS LINE SEQUENTIAL`. GnuCOBOL trims
trailing spaces from each written line, so `expected_output.dat` stores each
record without trailing padding (the logical record is still 60 bytes). The
CI comparison uses `diff -b` so trailing-whitespace differences never cause a
false failure — only the data content is compared.
