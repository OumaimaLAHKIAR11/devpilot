
import csv
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]

SOURCE = ROOT / "datasets" / "candidate-evidence-review.csv"
OUTPUT = ROOT / "datasets" / "golden-set-manual-review.csv"

with SOURCE.open(
        encoding="utf-8-sig",
        newline=""
) as f:
    rows = list(csv.DictReader(f))

fields = [
    "candidate_id",
    "year",
    "name",
    "tool",
    "error_category",
    "error_type",
    "compiler_message",
    "expected_log_name",
    "evidence_status",
    "evidence_excerpt",
    "root_cause_hypothesis",
    "evidence_status_manual",
    "distinct_case",
    "corpus_document",
    "answerable",
    "review_notes",
]

with OUTPUT.open(
        "w",
        encoding="utf-8-sig",
        newline=""
) as f:
    writer = csv.DictWriter(f, fieldnames=fields)
    writer.writeheader()

    for index, row in enumerate(rows, start=1):
        writer.writerow({
            "candidate_id": f"candidate-{index:03d}",
            "year": row["year"],
            "name": row["name"],
            "tool": row["tool"],
            "error_category": row["error_category"],
            "error_type": row["error_type"],
            "compiler_message": row["compiler_message"],
            "expected_log_name": row["expected_log_name"],
            "evidence_status": row["evidence_status"],
            "evidence_excerpt": row["evidence_excerpt"],
            "root_cause_hypothesis": "",
            "evidence_status_manual": "pending",
            "distinct_case": "pending",
            "corpus_document": "",
            "answerable": "pending",
            "review_notes": "",
        })

print(f"Review rows created: {len(rows)}")
print(f"Output: {OUTPUT}")
