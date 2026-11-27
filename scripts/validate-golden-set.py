
import json
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
GOLDEN_SET = ROOT / "knowledge" / "evaluation" / "golden-set-v1.jsonl"

ALLOWED_DOCS = {
    "docker-build-failures",
    "docker-image-pull-and-tags",
    "github-actions-secrets-and-permissions",
    "github-actions-workflow-syntax-errors",
    "java-compilation-errors",
    "junit-test-failures-and-flaky-tests",
    "maven-compiler-java-version",
    "maven-dependency-resolution",
    "maven-version-conflicts",
    "spring-boot-context-startup-failures",
}

REQUIRED_FIELDS = {
    "id", "source", "repository", "build_id",
    "technology", "query", "failure_excerpt",
    "answerable", "relevant_docs", "notes",
}


def validate():
    errors = []
    cases = []
    seen_ids = set()

    if not GOLDEN_SET.is_file():
        print(f"ERROR: Missing file: {GOLDEN_SET}")
        return 1

    with GOLDEN_SET.open(encoding="utf-8") as file:
        for line_no, line in enumerate(file, 1):
            if not line.strip():
                continue

            try:
                case = json.loads(line)
            except json.JSONDecodeError as exc:
                errors.append(f"Line {line_no}: invalid JSON: {exc}")
                continue

            if not isinstance(case, dict):
                errors.append(f"Line {line_no}: expected JSON object")
                continue

            missing = REQUIRED_FIELDS - case.keys()
            if missing:
                errors.append(
                    f"Line {line_no}: missing fields {sorted(missing)}"
                )
                continue

            case_id = case["id"]
            if not isinstance(case_id, str) or not case_id.strip():
                errors.append(f"Line {line_no}: invalid id")
            elif case_id in seen_ids:
                errors.append(f"Line {line_no}: duplicate id {case_id}")
            else:
                seen_ids.add(case_id)

            for field in (
                    "source", "repository", "build_id",
                    "query", "failure_excerpt", "notes"
            ):
                if not isinstance(case[field], str) or not case[field].strip():
                    errors.append(
                        f"Line {line_no}: {field} must be non-empty"
                    )

            technology = case["technology"]
            if (
                    not isinstance(technology, list)
                    or not technology
                    or not all(
                isinstance(x, str) and x.strip()
                for x in technology
            )
            ):
                errors.append(
                    f"Line {line_no}: technology must be a non-empty "
                    "list of strings"
                )

            if not isinstance(case["answerable"], bool):
                errors.append(
                    f"Line {line_no}: answerable must be boolean"
                )

            docs = case["relevant_docs"]
            if not isinstance(docs, list):
                errors.append(
                    f"Line {line_no}: relevant_docs must be a list"
                )
            else:
                unknown = set(docs) - ALLOWED_DOCS
                if unknown:
                    errors.append(
                        f"Line {line_no}: unknown document IDs {sorted(unknown)}"
                    )

                if case["answerable"] and not docs:
                    errors.append(
                        f"Line {line_no}: answerable case needs relevant_docs"
                    )

                if not case["answerable"] and docs:
                    errors.append(
                        f"Line {line_no}: unanswerable case must have "
                        "empty relevant_docs"
                    )

            cases.append(case)

    if len(cases) != 20:
        errors.append(f"Expected 20 cases, found {len(cases)}")

    if errors:
        print("GOLDEN SET VALIDATION FAILED")
        for error in errors:
            print(f"- {error}")
        return 1

    answerable = sum(c["answerable"] for c in cases)

    print("GOLDEN SET VALIDATION PASSED")
    print(f"Total cases: {len(cases)}")
    print(f"Answerable: {answerable}")
    print(f"Unanswerable: {len(cases) - answerable}")
    print(f"Unique IDs: {len(seen_ids)}")
    return 0


if __name__ == "__main__":
    sys.exit(validate())
