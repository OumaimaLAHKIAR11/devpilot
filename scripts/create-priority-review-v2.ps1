
$ErrorActionPreference = "Stop"

$Root = "C:\Users\ADMIN\Desktop\Ensias\s5\projet_S5_pfa\devpilot"

$FindingsPath = Join-Path $Root "datasets\golden-set-priority-findings-v1.csv"
$AnnotationsPath = Join-Path $Root "datasets\golden-set-annotations-v1.csv"
$OutputPath = Join-Path $Root "datasets\golden-set-priority-review-v2.csv"

$findings = @(Import-Csv -LiteralPath $FindingsPath)
$annotations = @(Import-Csv -LiteralPath $AnnotationsPath)

if ($findings.Count -ne 11) {
    throw "Expected 11 findings."
}

$lookup = @{}

foreach ($finding in $findings) {
    $id = ([string]$finding.candidate_id).Trim()

    if ($lookup.ContainsKey($id)) {
        throw "Duplicate ID: $id"
    }

    $lookup[$id] = $finding
}

$result = foreach ($annotation in $annotations) {
    $id = ([string]$annotation.candidate_id).Trim()

    if (-not $lookup.ContainsKey($id)) {
        continue
    }

    $finding = $lookup[$id]

    [PSCustomObject]@{
        candidate_id = $id
        observed_symptom = $finding.observed_symptom
        evidence_excerpt = $finding.evidence_excerpt
        root_cause_hypothesis = $finding.root_cause_hypothesis
        root_cause_confidence = $finding.root_cause_confidence_initial
        incident_family = $finding.incident_family_initial

        evidence_verified = "PENDING"
        distinct_case_final = "PENDING"
        answerable_final = "PENDING"
        review_decision_final = "PENDING"
        review_notes_final = ""
    }
}

if (@($result).Count -ne 11) {
    throw "Expected 11 matched review rows."
}

$missing = @(
    $result | Where-Object {
        [string]::IsNullOrWhiteSpace($_.root_cause_hypothesis)
    }
)

if ($missing.Count -gt 0) {
    Write-Host "`nMissing hypotheses in source:" -ForegroundColor Yellow
    $missing | Select-Object candidate_id, observed_symptom |
        Format-Table -AutoSize

    throw "Source hypotheses are incomplete. Do not generate final review."
}

$result | Export-Csv -LiteralPath $OutputPath `
    -NoTypeInformation -Encoding UTF8

Write-Host "`nREVIEW CREATED" -ForegroundColor Green
Write-Host "Rows: $(@($result).Count)"
Write-Host "Output: $OutputPath"

$result |
    Select-Object candidate_id, root_cause_hypothesis,
        root_cause_confidence, incident_family |
    Format-Table -AutoSize
