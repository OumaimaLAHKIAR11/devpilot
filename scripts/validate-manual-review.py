
import argparse
import csv
import sys
from pathlib import Path
from collections import Counter

ROOT = Path(__file__).resolve().parents[1]
CSV_PATH = ROOT / "datasets" / "golden-set-manual-review.csv"

REQUIRED = {
    "candidate_id",
    "name",
    "expected_log_name",
    "evidence_excerpt",
    "root_cause_hypothesis",
    "evidence_status_manual",
    "distinct_case",
    "corpus_document",
    "answerable",
    "review_notes",
}

STATUSES = {"pending", "confirmed", "probable", "unknown"}
DISTINCT_VALUES = {"pending", "yes", "no"}
ANSWERABLE_VALUES = {"pending", "true", "false"}


def norm(row, field):
    return (row.get(field) or "").strip().lower()


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument(
        "--mode",
        choices=["draft", "final"],
        default="draft"
    )
    args = parser.parse_args()

    if not CSV_PATH.exists():
        print(f"ERROR: CSV not found: {CSV_PATH}")
        sys.exit(1)

    with CSV_PATH.open(
            "r", encoding="utf-8-sig", newline=""
    ) as f:
        reader = csv.DictReader(f)
        rows = list(reader)
        columns = set(reader.fieldnames or [])

    missing = REQUIRED - columns
    if missing:
        print("ERROR: Missing columns:", sorted(missing))
        sys.exit(1)

    errors = []
    ids = set()

    statuses = Counter()
    distinct_counts = Counter()
    answerable_counts = Counter()

    for line, row in enumerate(rows, start=2):
        cid = (row.get("candidate_id") or "").strip()

        if not cid:
            errors.append(f"Row {line}: candidate_id missing")
        elif cid in ids:
            errors.append(f"Row {line}: duplicate candidate_id={cid}")
        ids.add(cid)

        status = norm(row, "evidence_status_manual")
        distinct = norm(row, "distinct_case")
        answerable = norm(row, "answerable")

        statuses[status or "(blank)"] += 1
        distinct_counts[distinct or "(blank)"] += 1
        answerable_counts[answerable or "(blank)"] += 1

        if status not in STATUSES:
            errors.append(
                f"Row {line}: invalid evidence_status_manual={status!r}"
            )

        if distinct not in DISTINCT_VALUES:
            errors.append(
                f"Row {line}: invalid distinct_case={distinct!r}"
            )

        if answerable not in ANSWERABLE_VALUES:
            errors.append(
                f"Row {line}: invalid answerable={answerable!r}"
            )

        if status in {"confirmed", "probable"}:
            if not row.get("root_cause_hypothesis", "").strip():
                errors.append(f"Row {line}: root cause missing")

            if not row.get("review_notes", "").strip():
                errors.append(f"Row {line}: evidence justification missing")

        if distinct == "yes" and status == "unknown":
            errors.append(
                f"Row {line}: unknown root cause cannot be a validated distinct case"
            )

        if distinct == "yes" and status not in {"confirmed", "probable"}:
            errors.append(
                f"Row {line}: distinct=yes requires confirmed/probable evidence"
            )

        if answerable == "true":
            if not row.get("corpus_document", "").strip():
                errors.append(
                    f"Row {line}: answerable=true requires corpus_document"
                )

        if args.mode == "final":
            if status == "pending":
                errors.append(f"Row {line}: evidence review still pending")

            if distinct == "pending":
                errors.append(f"Row {line}: distinct_case still pending")

            if answerable == "pending":
                errors.append(f"Row {line}: answerable still pending")

            if not row.get("review_notes", "").strip():
                errors.append(f"Row {line}: review_notes missing")

            if not row.get("evidence_excerpt", "").strip():
                errors.append(f"Row {line}: evidence_excerpt missing")

    print(f"\nMode: {args.mode}")
    print(f"Rows: {len(rows)}")
    print(f"Unique candidate IDs: {len(ids)}")

    print("\nEvidence status:")
    for key, count in sorted(statuses.items()):
        print(f"  {key}: {count}")

    print("\nDistinct case:")
    for key, count in sorted(distinct_counts.items()):
        print(f"  {key}: {count}")

    print("\nAnswerable:")
    for key, count in sorted(answerable_counts.items()):
        print(f"  {key}: {count}")

    if errors:
        print(f"\nVALIDATION FAILED: {len(errors)} issue(s)")
        for error in errors:
            print(" -", error)
        sys.exit(1)

    print("\nVALIDATION PASSED")

    if args.mode == "draft":
        print("Draft validation only; semantic quality is not certified.")
    else:
        print("Structural final validation passed.")
        print("Human review is still responsible for semantic correctness.")


if __name__ == "__main__":
    main()
