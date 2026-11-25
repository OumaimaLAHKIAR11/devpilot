
import csv
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
CSV_FILE = ROOT / "datasets" / "java-build-candidates.csv"

with CSV_FILE.open(
        encoding="utf-8-sig",
        newline=""
) as f:
    rows = list(csv.DictReader(f))

print("Nombre de candidats :", len(rows))
print("Colonnes :", list(rows[0].keys()) if rows else [])

for index, row in enumerate(rows[:20], start=1):
    print(f"\n--- CANDIDAT {index} ---")
    print("Année          :", row.get("year"))
    print("Nom             :", row.get("name"))
    print("Build tool      :", row.get("tool"))
    print("Catégorie       :", row.get("error_category"))
    print("Type            :", row.get("error_type"))
    print("Compiler message:", row.get("compiler_message"))
    print("Code            :", row.get("code"))
