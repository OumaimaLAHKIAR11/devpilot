
import argparse
import csv
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
SOURCE = ROOT / "datasets" / "golden-set-manual-review.csv"
OUTPUT = ROOT / "datasets" / "golden-set-manual-review-reviewed.csv"

FIELDS = [
    "evidence_status_manual",
    "distinct_case",
    "answerable",
    "corpus_document",
    "root_cause_hypothesis",
    "review_notes",
]

ALLOWED = {
    "evidence_status_manual": {"confirmed", "probable", "insufficient", "unknown"},
    "distinct_case": {"yes", "no", "uncertain"},
    "answerable": {"yes", "no"},
}


def load_rows(path):
    if not path.exists():
        raise FileNotFoundError(f"Fichier introuvable : {path}")

    with path.open("r", encoding="utf-8-sig", newline="") as f:
        return list(csv.DictReader(f))


def save_rows(rows):
    OUTPUT.parent.mkdir(parents=True, exist_ok=True)

    columns = list(rows[0].keys())
    with OUTPUT.open("w", encoding="utf-8-sig", newline="") as f:
        writer = csv.DictWriter(f, fieldnames=columns)
        writer.writeheader()
        writer.writerows(rows)


def ask(field, current):
    allowed = ALLOWED.get(field)

    # pending est un état interne, pas une réponse valide.
    if current == "pending":
        current = ""

    while True:
        suffix = f" [{current}]" if current else ""
        choices = f" ({'/'.join(sorted(allowed))})" if allowed else ""

        value = input(f"{field}{suffix}{choices}: ").strip()

        if value.lower() == "q":
            return None

        if not value:
            if current:
                return current
            print("Champ obligatoire. Entre une valeur ou q pour quitter.")
            continue

        if allowed and value not in allowed:
            print(f"Valeur invalide. Valeurs possibles : {', '.join(sorted(allowed))}")
            continue

        return value


def show_candidate(row, index, total):
    print("\n" + "=" * 72)
    print(f"CANDIDAT {index}/{total}")
    print("=" * 72)

    for key in [
        "candidate_id",
        "year",
        "name",
        "tool",
        "error_category",
        "error_type",
        "compiler_message",
        "evidence_excerpt",
        "root_cause_hypothesis",
    ]:
        print(f"\n{key}:\n{row.get(key, '')}")


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--limit", type=int, default=0)
    args = parser.parse_args()

    source_rows = load_rows(SOURCE)
    if not source_rows:
        print("Le CSV source est vide.")
        return 1

    # Reprendre les annotations déjà sauvegardées.
    if OUTPUT.exists():
        reviewed_rows = load_rows(OUTPUT)
        reviewed_by_id = {
            row["candidate_id"]: row for row in reviewed_rows
        }

        rows = []
        for source_row in source_rows:
            old = reviewed_by_id.get(source_row["candidate_id"], {})
            merged = dict(source_row)
            for field in FIELDS:
                if old.get(field):
                    merged[field] = old[field]
            rows.append(merged)
    else:
        rows = [dict(row) for row in source_rows]

    pending = [
        row for row in rows
        if any(row.get(field, "pending") in ("", "pending") for field in FIELDS[:3])
    ]

    if args.limit > 0:
        pending = pending[:args.limit]

    print(f"{len(rows)} candidats au total.")
    print(f"{len(pending)} à examiner.")
    print("Tape q à n'importe quel champ pour sauvegarder et quitter.")

    for index, row in enumerate(pending, start=1):
        show_candidate(row, index, len(pending))

        for field in FIELDS:
            result = ask(field, row.get(field, ""))
            if result is None:
                save_rows(rows)
                print(f"\nRevue interrompue. Fichier sauvegardé : {OUTPUT}")
                return 0

            row[field] = result

        save_rows(rows)
        print("Candidat sauvegardé.")

    save_rows(rows)
    print(f"\nRevue terminée. Fichier : {OUTPUT}")
    return 0


if __name__ == "__main__":
    try:
        sys.exit(main())
    except (OSError, csv.Error, KeyError) as exc:
        print(f"Erreur : {exc}")
        sys.exit(1)
