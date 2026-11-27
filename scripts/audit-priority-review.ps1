
$ErrorActionPreference = "Stop"

$Root = "C:\Users\ADMIN\Desktop\Ensias\s5\projet_S5_pfa\devpilot"
$Path = Join-Path $Root "datasets\golden-set-priority-review-v3.csv"

$rows = @(Import-Csv -LiteralPath $Path)

if ($rows.Count -ne 11) {
    throw "Expected 11 candidates."
}

$incomplete = @(
    $rows | Where-Object {
        $_.evidence_verified -notin @("YES", "NO") -or
        $_.distinct_case_final -notin @("YES", "NO") -or
        $_.answerable_final -notin @("YES", "NO") -or
        $_.review_decision_final -notin @("KEEP", "EXCLUDE") -or
        [string]::IsNullOrWhiteSpace($_.review_notes_final)
    }
)

$invalidKeep = @(
    $rows | Where-Object {
        $_.review_decision_final -eq "KEEP" -and
        ($_.evidence_verified -ne "YES" -or
         $_.distinct_case_final -ne "YES")
    }
)

$kept = @(
    $rows | Where-Object { $_.review_decision_final -eq "KEEP" }
)

$excluded = @(
    $rows | Where-Object { $_.review_decision_final -eq "EXCLUDE" }
)

Write-Host "`n--- PRIORITY REVIEW AUDIT ---" -ForegroundColor Cyan
Write-Host "Total candidates: $($rows.Count)"
Write-Host "Incomplete: $($incomplete.Count)"
Write-Host "Invalid KEEP decisions: $($invalidKeep.Count)"
Write-Host "KEEP: $($kept.Count)"
Write-Host "EXCLUDE: $($excluded.Count)"

if ($incomplete.Count -gt 0 -or $invalidKeep.Count -gt 0) {
    Write-Host "`nREVIEW NOT COMPLETE" -ForegroundColor Yellow
    $incomplete | Select-Object candidate_id,
        evidence_verified, distinct_case_final,
        answerable_final, review_decision_final |
        Format-Table -AutoSize
}
else {
    Write-Host "`nSTRUCTURAL REVIEW COMPLETE" -ForegroundColor Green
    Write-Host "Human decisions are recorded for all 11 candidates."
}
