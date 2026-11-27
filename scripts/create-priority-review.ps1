
$ErrorActionPreference = "Stop"

$Root = "C:\Users\ADMIN\Desktop\Ensias\s5\projet_S5_pfa\devpilot"

$InputPath = Join-Path $Root "datasets\golden-set-manual-review-v3.csv"
$OutputPath = Join-Path $Root "datasets\golden-set-priority-review-v1.csv"

if (-not (Test-Path $InputPath)) {
    throw "Input file not found: $InputPath"
}

$rows = @(Import-Csv -LiteralPath $InputPath)

if ($rows.Count -ne 56) {
    throw "Expected 56 rows, got $($rows.Count)"
}

$priority = @(
    $rows | Where-Object {
        $_.evidence_review_required -eq "YES"
    }
)

if ($priority.Count -ne 11) {
    throw "Expected 11 priority candidates, got $($priority.Count)"
}

# Keep only the fields useful for manual examination.
$review = foreach ($row in $priority) {
    [PSCustomObject]@{
        candidate_id = $row.candidate_id
        observed_symptom = $row.observed_symptom
        evidence_excerpt = $row.evidence_excerpt
        root_cause_hypothesis = $row.root_cause_hypothesis_initial
        root_cause_confidence = $row.root_cause_confidence_initial
        incident_family = $row.incident_family_initial

        evidence_verified = "PENDING"
        distinct_case_final = "PENDING"
        answerable_final = "PENDING"
        review_decision_final = "PENDING"
        review_notes_final = ""
    }
}

$review | Export-Csv -LiteralPath $OutputPath `
    -NoTypeInformation -Encoding UTF8

$check = @(Import-Csv -LiteralPath $OutputPath)

if ($check.Count -ne 11) {
    throw "Export verification failed."
}

Write-Host "`nPRIORITY REVIEW CREATED" -ForegroundColor Green
Write-Host "Candidates: $($check.Count)"
Write-Host "Output: $OutputPath"

$check |
    Select-Object candidate_id, observed_symptom,
        root_cause_hypothesis, evidence_verified,
        distinct_case_final, answerable_final,
        review_decision_final |
    Format-Table -AutoSize
