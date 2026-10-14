# Build and run the legacy INTCALC batch with GnuCOBOL (cobc).
#
#   make build   compile INTCALC
#   make run     run against data/accounts.dat -> build/report.dat
#   make verify  diff build/report.dat against data/expected_output.dat
#   make clean   remove build artefacts

COBC      ?= cobc
# -x: build an executable program; -fixed: source is fixed-format COBOL.
COBFLAGS  ?= -x -fixed
SRC       := src/cobol/INTCALC.cbl
COPYDIR   := src/cobol
BIN       := INTCALC
INPUT     := data/accounts.dat
EXPECTED  := data/expected_output.dat
ACTUAL    := build/report.dat

.PHONY: all build run verify clean
all: verify

build: $(SRC) $(COPYDIR)/ACCTREC.cpy
	mkdir -p build
	$(COBC) $(COBFLAGS) -I$(COPYDIR) -o build/$(BIN) $(SRC)

run: build
	cp $(INPUT) build/ACCTIN
	cd build && ./$(BIN)
	@echo "--- INTCALC finished; locating output ---"
	@ls -l build || true
	mv build/RPTOUT $(ACTUAL)

verify: run
	@echo "===== EXPECTED ($(EXPECTED)) ====="
	@cat -A $(EXPECTED) || true
	@echo "===== ACTUAL ($(ACTUAL)) ====="
	@cat -A $(ACTUAL) || true
	@echo "===== DIFF (expected vs actual) ====="
	@if diff -u -b $(EXPECTED) $(ACTUAL); then \
		echo "PASS: output matches baseline"; \
	else \
		echo "FAIL: output differs from baseline"; exit 1; \
	fi

clean:
	rm -rf build
