# cobol-interest-batch

A small but realistic **legacy COBOL batch** — a retail-bank interest
calculation — provided as a migration target for **Devin**. The goal of the
demo is to have Devin modernize this to **Java 17 / Spring Batch** and *prove*
the result is correct by reproducing the committed baseline output
byte-for-byte.

This repository is modelled on the kind of asset a Fujitsu financial-services
customer would hold on a GS21 mainframe: fixed-length files, a COPY book, COMP-3
internal arithmetic, account-type rate tables with per-account overrides,
day-count proration, and half-up rounding to the yen.

## Before state (what's here today)

```
cobol-interest-batch/
├── src/cobol/
│   ├── INTCALC.cbl          # the batch program
│   └── ACCTREC.cpy          # fixed-length account record (COPY book)
├── data/
│   ├── accounts.dat         # input: 10 fixed-length account records
│   └── expected_output.dat  # golden baseline: detail lines + trailer
├── docs/
│   └── SPEC.md              # functional spec + worked examples
├── .github/workflows/
│   └── cobol-baseline.yml   # CI: build with GnuCOBOL, verify == baseline
├── Makefile
└── README.md
```

### Run the Before locally

Requires [GnuCOBOL](https://gnucobol.sourceforge.io/) (`cobc`):

```bash
make verify
```

This compiles `INTCALC`, runs it against `data/accounts.dat`, and diffs the
result against `data/expected_output.dat`. It should print
`PASS: output matches baseline`. The same steps run in CI
(`.github/workflows/cobol-baseline.yml`).

Baseline summary: **10 records**, interest total **312,710** JPY.

## The migration task (for Devin)

> Modernize the COBOL batch in `src/cobol/` to **Java 17** using **Spring
> Batch**, preserving behaviour exactly. Read `docs/SPEC.md` for the rules.
> The Java program must read `data/accounts.dat` and produce output that is
> **byte-identical** to `data/expected_output.dat` (same fixed-length layout,
> same detail lines, same trailer with record count and interest total).
>
> Requirements:
> - Use `BigDecimal` with `RoundingMode.HALF_UP` for all money math — never
>   `double`. The two-step rounding in `SPEC.md` (annual → 4dp, accrued → 4dp,
>   yen → integer) must be reproduced in the same order.
> - Keep the account-type rate table and the per-account override precedence.
> - Preserve the fixed 60-byte record layout and the `TRAILER` control totals.
> - Add JUnit tests that assert equivalence against `data/expected_output.dat`,
>   including the tricky cases in `SPEC.md` (proration, override, round-to-zero,
>   large balance).
> - Add a GitHub Actions workflow that builds the Java app and runs the tests.
> - Open a pull request describing the before/after and how equivalence is
>   proven.

## Why this is a good Devin use case

- **Analysis**: the data layout and rounding rules are implicit in the COBOL —
  Devin (with DeepWiki) reverse-engineers them into the spec.
- **Equivalence is objective**: success = `diff` is empty. No subjective review
  of "does it look right".
- **It's the residue converters miss**: automated converters handle bulk
  translation, but type-based rate logic, override precedence, and exact
  rounding are where they tend to leave a manual tail. That tail is the Devin
  story.
