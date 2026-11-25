
import csv
from pathlib import Path
from collections import defaultdict

ROOT = Path(__file__).resolve().parents[1]
DATASET = ROOT / "datasets" / "extracted"
OUTPUT = ROOT / "datasets" / "java-build-candidates.csv"

# Catégories pertinentes pour notre domaine.
TARGET_CATEGORIES = {
    "Java compilation",
    "dependencies",
    "configuration",
    "wrong JDK version",
    "file not found",
    "build files parsing/interpretation",
    "packaging",
}

# Nombre maximal de cas par catégorie et par année.
MAX_PER_CATEGORY = 5

selected = []

for csv_file in sorted(DATASET.rglob("builds.csv")):
    year = csv_file.parent.name

    with csv_file.open(
            encoding="utf-8-sig",
            newline=""
    ) as f:
        rows = list(csv.DictReader(f))

    failed = [
        row for row in rows
        if row.get("status", "").strip().upper() == "F"
    ]

    groups = defaultdict(list)

    for row in failed:
        category = row.get("error_category", "").strip()
        error_type = row.get("error_type", "").strip()

        if category not in TARGET_CATEGORIES:
            continue

        # Éviter de prendre plusieurs fois exactement
        # le même type d'erreur dans une catégorie.
        key = (category, error_type or "unknown")
        groups[key].append(row)

    category_counts = defaultdict(int)

    for (category, error_type), candidates in sorted(groups.items()):
        if category_counts[category] >= MAX_PER_CATEGORY:
            continue

        # Sélection déterministe pour rendre le résultat reproductible.
        candidates.sort(
            key=lambda r: (
                r.get("name", ""),
                r.get("compiler_message", "")
            )
        )

        row = candidates[0]

        selected.append({
            "year": year,
            "name": row.get("name", ""),
            "status": row.get("status", ""),
            "tool": row.get("tool", ""),
            "error_category": category,
            "error_type": error_type,
            "compiler_message": row.get("compiler_message", ""),
            "code": row.get("code", ""),
            "stars": row.get("stars", ""),
            "forks": row.get("forks", ""),
        })

        category_counts[category] += 1

fields = [
    "year",
    "name",
    "status",
    "tool",
    "error_category",
    "error_type",
    "compiler_message",
    "code",
    "stars",
    "forks",
]

OUTPUT.parent.mkdir(parents=True, exist_ok=True)

with OUTPUT.open("w", encoding="utf-8-sig", newline="") as f:
    writer = csv.DictWriter(f, fieldnames=fields)
    writer.writeheader()
    writer.writerows(selected)

print(f"Candidats sélectionnés : {len(selected)}")
print(f"Fichier généré : {OUTPUT}")

print("\nRépartition :")
counts = defaultdict(int)

for row in selected:
    counts[(row["year"], row["error_category"])] += 1

for (year, category), count in sorted(counts.items()):
    print(f"{year} | {category}: {count}")
