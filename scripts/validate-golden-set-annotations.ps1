$ErrorActionPreference = "Stop"

$Root = Split-Path -Parent $PSScriptRoot
$InputCsv = Join-Path $Root "datasets\golden-set-annotations-v1.csv"
$AuditCsv = Join-Path $Root "datasets\golden-set-annotation-audit-v2.csv"

if (-not (Test-Path -LiteralPath $InputCsv)) {
    throw "Annotations introuvables : $InputCsv"
}

$rows = @(Import-Csv -LiteralPath $InputCsv)

if ($rows.Count -eq 0) {
    throw "Le fichier d'annotations est vide."
}

$required = @(
    "candidate_id",
    "observed_symptom",
    "evidence_excerpt",
    "evidence_status_manual",
    "distinct_case",
    "answerable",
    "corpus_document",
    "review_decision",
    "review_notes"
)

$columns = @($rows[0].PSObject.Properties.Name)
$missing = @($required | Where-Object { $_ -notin $columns })

if ($missing.Count -gt 0) {
    throw "Colonnes manquantes : $($missing -join ', ')"
}

$errors = New-Object System.Collections.Generic.List[string]
$seen = @{}
$audit = New-Object System.Collections.Generic.List[object]

foreach ($row in $rows) {
    $id = $row.candidate_id
    $messages = New-Object System.Collections.Generic.List[string]

    if ([string]::IsNullOrWhiteSpace($id)) {
        $errors.Add("Ligne sans candidate_id.")
        continue
    }

    if ($seen.ContainsKey($id)) {
        $messages.Add("candidate_id duplique")
    } else {
        $seen[$id] = $true
    }

    if ($row.review_decision -notin @("KEEP", "DUPLICATE", "EXCLUDE")) {
        $messages.Add("Decision non finalisee")
    } else {
        if ([string]::IsNullOrWhiteSpace($row.review_notes)) {
            $messages.Add("review_notes manquant")
        }

        if ($row.review_decision -eq "KEEP") {
            if ([string]::IsNullOrWhiteSpace($row.observed_symptom)) {
                $messages.Add("Symptome manquant")
            }
            if ([string]::IsNullOrWhiteSpace($row.evidence_excerpt)) {
                $messages.Add("Preuve manquante")
            }
            if ($row.evidence_status_manual -ne "CONFIRMED") {
                $messages.Add("Preuve non confirmee")
            }
            if ($row.distinct_case -ne "YES") {
                $messages.Add("Cas distinct non confirme")
            }
            if ($row.answerable -notin @("YES", "NO")) {
                $messages.Add("Answerability non decidee")
            }
            if (
                $row.answerable -eq "YES" -and
                [string]::IsNullOrWhiteSpace($row.corpus_document)
            ) {
                $messages.Add("Document corpus manquant")
            }
        }

        if (
            $row.review_decision -eq "DUPLICATE" -and
            $row.distinct_case -ne "NO"
        ) {
            $messages.Add("DUPLICATE exige distinct_case=NO")
        }
    }

    $audit.Add([PSCustomObject]@{
        candidate_id = $id
        review_decision = $row.review_decision
        audit_status = if ($messages.Count -eq 0) { "VALID" } else { "REVIEW_REQUIRED" }
        issues = ($messages -join " | ")
    })

    foreach ($message in $messages) {
        $errors.Add("$id : $message")
    }
}

$audit | Export-Csv -LiteralPath $AuditCsv -NoTypeInformation -Encoding UTF8

$pendingCount = @(
    $rows | Where-Object {
        $_.review_decision -notin @("KEEP", "DUPLICATE", "EXCLUDE")
    }
).Count

Write-Host "`n--- GOLDEN SET AUDIT ---" -ForegroundColor Cyan
Write-Host "Total references : $($rows.Count)"
Write-Host "Pending decisions : $pendingCount"
Write-Host "Issues : $($errors.Count)"
Write-Host "Audit : $AuditCsv"

if ($pendingCount -gt 0 -or $errors.Count -gt 0) {
    Write-Host "`nVALIDATION FAILED" -ForegroundColor Red
    $errors | ForEach-Object { Write-Host $_ -ForegroundColor Yellow }
    throw "Golden Set non valide."
}
else {
    Write-Host "`nVALIDATION PASSED" -ForegroundColor Green
}
