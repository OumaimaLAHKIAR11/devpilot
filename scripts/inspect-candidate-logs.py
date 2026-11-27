
import csv
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
DATASET = ROOT / "datasets" / "extracted" / "build-data"
CANDIDATES = ROOT / "datasets" / "java-build-candidates.csv"

OUTPUT_CSV = ROOT / "datasets" / "candidate-log-review.csv"
OUTPUT_TXT = ROOT / "datasets" / "candidate-log-excerpts.txt"

def read_log(path):
    """Read a log while tolerating encoding differences."""
    for encoding in ("utf-8", "cp1252", "latin-1"):
        try:
            return path.read_text(encoding=encoding)
        except UnicodeDecodeError:
            continue
    return path.read_text(encoding="utf-8", errors="replace")


def clean(value):
    if not value:
        return ""
    return value.strip()


def expected_log_name(project_name):
    # Example: org/repository -> org_repository.fail
    return clean(project_name).replace("/", "_") + ".fail"


def extract_excerpt(content, compiler_message, max_chars=3500):
    lines = content.splitlines()

    message = clean(compiler_message)

    # Prefer the context around the compiler message when it is usable.
    if message and message.lower() not in {"na", "unknown", "none"}:
        matching = [
            i for i, line in enumerate(lines)
            if message.lower() in line.lower()
        ]

        if matching:
            index = matching[0]
            start = max(0, index - 8)
            end = min(len(lines), index + 18)
            excerpt = "\n".join(lines[start:end])
            return excerpt[:max_chars]

    # Fallback: retain the last part of the build log.
    return "\n".join(lines[-60:])[-max_chars:]


with CANDIDATES.open(
        encoding="utf-8-sig",
        newline=""
) as file:
    candidates = list(csv.DictReader(file))

report = []
text_sections = []

for index, candidate in enumerate(candidates, start=1):
    year = clean(candidate.get("year"))
    project = clean(candidate.get("name"))
    compiler_message = clean(candidate.get("compiler_message"))

    expected_name = expected_log_name(project)
    log_path = DATASET / year / "logs" / expected_name

    result = {
        **candidate,
        "expected_log_name": expected_name,
        "log_exists": log_path.is_file(),
        "log_size_bytes": "",
        "excerpt": "",
    }

    text_sections.append(
        f"\n{'=' * 90}\n"
        f"CANDIDATE {index}\n"
        f"Year: {year}\n"
        f"Project: {project}\n"
        f"Category: {candidate.get('error_category')}\n"
        f"Error type: {candidate.get('error_type')}\n"
        f"Compiler message: {compiler_message}\n"
        f"Expected log: {expected_name}\n"
    )

    if log_path.is_file():
        content = read_log(log_path)
        excerpt = extract_excerpt(content, compiler_message)

        result["log_size_bytes"] = log_path.stat().st_size
        result["excerpt"] = excerpt.replace("\n", " ")[:1000]

        text_sections.append(
            f"Log size: {log_path.stat().st_size} bytes\n"
            f"\n--- LOG EXCERPT ---\n{excerpt}\n"
        )
    else:
        text_sections.append(
            "STATUS: LOG NOT FOUND\n"
        )

    report.append(result)

OUTPUT_CSV.parent.mkdir(parents=True, exist_ok=True)

fields = list(report[0].keys()) if report else []

with OUTPUT_CSV.open(
        "w",
        encoding="utf-8-sig",
        newline=""
) as file:
    writer = csv.DictWriter(file, fieldnames=fields)
    writer.writeheader()
    writer.writerows(report)

OUTPUT_TXT.write_text(
    "\n".join(text_sections),
    encoding="utf-8"
)

matched = sum(1 for row in report if row["log_exists"])
unmatched = len(report) - matched

print(f"Total candidates : {len(report)}")
print(f"Logs found       : {matched}")
print(f"Logs missing     : {unmatched}")
print(f"CSV report       : {OUTPUT_CSV}")
print(f"Log excerpts     : {OUTPUT_TXT}")

print("\nMissing logs:")

for row in report:
    if not row["log_exists"]:
        print(
            f"- {row['year']} | {row['name']} "
            f"| expected={row['expected_log_name']}"
        )
