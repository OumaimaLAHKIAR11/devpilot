
import csv
import re
from pathlib import Path
from collections import Counter

ROOT = Path(__file__).resolve().parents[1]
DATASET = ROOT / "datasets" / "extracted" / "build-data"
INPUT = ROOT / "datasets" / "candidate-log-review.csv"

OUTPUT_CSV = ROOT / "datasets" / "candidate-evidence-review.csv"
OUTPUT_TXT = ROOT / "datasets" / "candidate-evidence-excerpts.txt"

PATTERNS = [
    r"\bBUILD FAILED\b",
    r"\bBUILD FAILURE\b",
    r"\[ERROR\]",
    r"\berror:",
    r"\bCaused by:",
    r"\bException\b",
    r"\bFAILED\b",
    r"\bCould not\b",
    r"\bCannot\b",
    r"\bnot found\b",
    r"\bNo such\b",
    r"\bmissing\b",
    r"\bdoes not exist\b",
    r"\bnot supported\b",
    r"\bmust specify\b",
    r"\bfailed to\b",
    r"\bcompilation failed\b",
    r"\bUnsupportedClassVersionError\b",
]

ERROR_REGEX = re.compile(
    "|".join(f"(?:{pattern})" for pattern in PATTERNS),
    re.IGNORECASE
)

IGNORE_PATTERNS = [
    r"^\s*at\s+[\w.$]+\(",
    r"^\s*\.\.\.\s+\d+\s+more",
    r"^\s*Downloading:",
    r"^\s*Downloaded:",
    r"^\s*Progress\s+\(",
]

IGNORE_REGEX = re.compile(
    "|".join(IGNORE_PATTERNS),
    re.IGNORECASE
)


def read_log(path):
    for encoding in ("utf-8", "cp1252", "latin-1"):
        try:
            return path.read_text(encoding=encoding)
        except UnicodeDecodeError:
            continue

    return path.read_text(encoding="utf-8", errors="replace")


def normalize(line):
    return re.sub(r"\s+", " ", line).strip()


def extract_evidence(content, context=2, max_blocks=12):
    lines = content.splitlines()
    hits = []

    for index, line in enumerate(lines):
        normalized = normalize(line)

        if not normalized or IGNORE_REGEX.search(normalized):
            continue

        if ERROR_REGEX.search(normalized):
            hits.append(index)

    # Merge nearby matches into blocks.
    blocks = []

    for index in hits:
        start = max(0, index - context)
        end = min(len(lines), index + context + 1)

        if blocks and start <= blocks[-1][1]:
            blocks[-1] = (
                blocks[-1][0],
                max(blocks[-1][1], end)
            )
        else:
            blocks.append((start, end))

    excerpts = []

    for start, end in blocks[:max_blocks]:
        excerpt = "\n".join(
            f"{i + 1}: {lines[i]}"
            for i in range(start, end)
        )
        excerpts.append(excerpt)

    return hits, excerpts


with INPUT.open(
        encoding="utf-8-sig",
        newline=""
) as f:
    candidates = list(csv.DictReader(f))

report = []
text_output = []
stats = Counter()

for index, candidate in enumerate(candidates, start=1):
    year = candidate["year"]
    project = candidate["name"]
    filename = candidate["expected_log_name"]

    log_path = DATASET / year / "logs" / filename

    row = dict(candidate)
    row["evidence_status"] = "log_missing"
    row["evidence_hits"] = 0
    row["evidence_excerpt"] = ""

    text_output.append(
        f"\n{'=' * 90}\n"
        f"CANDIDATE {index}\n"
        f"Project: {project}\n"
        f"Year: {year}\n"
        f"Category: {candidate['error_category']}\n"
        f"Error type: {candidate['error_type']}\n"
        f"Log: {filename}\n"
    )

    if not log_path.is_file():
        report.append(row)
        stats["missing"] += 1
        text_output.append("LOG NOT FOUND\n")
        continue

    content = read_log(log_path)
    hits, excerpts = extract_evidence(content)

    # Verify that the log contains a failure marker.
    failure_marker = bool(
        re.search(
            r"\bBUILD FAILED\b|\bBUILD FAILURE\b",
            content,
            re.IGNORECASE
        )
    )

    if not hits:
        status = "manual_review_no_error_match"
    elif not failure_marker:
        status = "manual_review_no_build_marker"
    else:
        status = "evidence_extracted_manual_review"

    row["evidence_status"] = status
    row["evidence_hits"] = len(hits)
    row["evidence_excerpt"] = " || ".join(excerpts)[:3000]

    report.append(row)
    stats[status] += 1

    text_output.append(
        f"Evidence status: {status}\n"
        f"Matched lines: {len(hits)}\n"
    )

    for excerpt in excerpts:
        text_output.append(
            f"\n--- EVIDENCE BLOCK ---\n{excerpt}\n"
        )

OUTPUT_CSV.parent.mkdir(parents=True, exist_ok=True)

with OUTPUT_CSV.open(
        "w",
        encoding="utf-8-sig",
        newline=""
) as f:
    writer = csv.DictWriter(
        f,
        fieldnames=list(report[0].keys())
    )
    writer.writeheader()
    writer.writerows(report)

OUTPUT_TXT.write_text(
    "\n".join(text_output),
    encoding="utf-8"
)

print("Candidates:", len(report))
print("\nEvidence status:")

for status, count in sorted(stats.items()):
    print(f"{status}: {count}")

print("\nGenerated:")
print(OUTPUT_CSV)
print(OUTPUT_TXT)
