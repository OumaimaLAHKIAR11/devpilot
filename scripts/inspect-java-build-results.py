import csv
from pathlib import Path
from collections import Counter

ROOT = Path(__file__).resolve().parents[1]
DATASET = ROOT / "datasets" / "extracted"

csv_files = list(DATASET.rglob("builds.csv"))

if not csv_files:
    print("Aucun fichier builds.csv trouve.")
    print("Fichiers CSV disponibles :")
    for path in DATASET.rglob("*.csv"):
        print(path)
    raise SystemExit(1)

for csv_file in csv_files:
    print(f"\n{'=' * 60}")
    print(f"DATASET: {csv_file}")
    print("=" * 60)

    with csv_file.open(
        encoding="utf-8-sig",
        newline=""
    ) as file:
        reader = csv.DictReader(file)
        rows = list(reader)

    print("Colonnes:", reader.fieldnames)
    print("Total builds:", len(rows))

    status_counts = Counter(
        row.get("status", "unknown").strip()
        for row in rows
    )

    print("\nBuilds par statut:")
    for status, count in status_counts.most_common():
        print(f"  {status}: {count}")

    failed = [
        row for row in rows
        if row.get("status", "").strip().upper()
        in ("F", "FAIL", "FAILED", "FAILURE")
    ]

    print("\nFailed builds:", len(failed))

    print("\nCategories d'erreurs:")
    for category, count in Counter(
        row.get("error_category") or "unknown"
        for row in failed
    ).most_common():
        print(f"  {category}: {count}")

    print("\nTypes d'erreurs:")
    for error_type, count in Counter(
        row.get("error_type") or "unknown"
        for row in failed
    ).most_common():
        print(f"  {error_type}: {count}")

    print("\nOutils de build:")
    for tool, count in Counter(
        row.get("tool") or "unknown"
        for row in rows
    ).most_common():
        print(f"  {tool}: {count}")
