
$ErrorActionPreference = "Stop"

$Root = "C:\Users\ADMIN\Desktop\Ensias\s5\projet_S5_pfa\devpilot"

$InputPath = Join-Path $Root "datasets\golden-set-annotations-enriched-v3.csv"
$FindingsPath = Join-Path $Root "datasets\golden-set-priority-findings-v1.csv"
$OutputPath = Join-Path $Root "datasets\golden-set-manual-review-v3.csv"

foreach ($path in @($InputPath, $FindingsPath)) {
    if (-not (Test-Path -LiteralPath $path)) {
        throw "File not found: $path"
    }
}

$rows = @(Import-Csv -LiteralPath $InputPath)
$findings = @(Import-Csv -LiteralPath $FindingsPath)

if ($rows.Count -ne 56) {
    throw "Expected 56 annotations, got $($rows.Count)"
}

if ($findings.Count -ne 11) {
    throw "Expected 11 findings, got $($findings.Count)"
}

# Build findings lookup using normalized candidate IDs.
$lookup = @{}

foreach ($finding in $findings) {
    $key = ([string]$finding.candidate_id).Trim().ToLowerInvariant()

    if ([string]::IsNullOrWhiteSpace($key)) {
        throw "Empty candidate ID in findings."
    }

    if ($lookup.ContainsKey($key)) {
        throw "Duplicate finding ID: $key"
    }

    $lookup[$key] = $finding
}

# Validate and enrich the 56 annotation rows.
$result = New-Object System.Collections.Generic.List[object]
$matched = New-Object System.Collections.Generic.List[string]

foreach ($row in $rows) {
    $data = [ordered]@{}

    foreach ($property in $row.PSObject.Properties) {
        $data[$property.Name] = $property.Value
    }

    $key = ([string]$row.candidate_id).Trim().ToLowerInvariant()

    if ($lookup.ContainsKey($key)) {
        $finding = $lookup[$key]

        # Recover missing fields from the authoritative findings file.
        $data["observed_symptom"] = $finding.observed_symptom
        $data["evidence_excerpt"] = $finding.evidence_excerpt
        $data["root_cause_hypothesis_initial"] = $finding.root_cause_hypothesis
        $data["root_cause_confidence_initial"] = $finding.root_cause_confidence_initial
        $data["incident_family_initial"] = $finding.incident_family_initial
        $data["evidence_review_required"] = "YES"

        $matched.Add($key)
    }
    elseif (-not $data.Contains("evidence_review_required")) {
        $data["evidence_review_required"] = "NO"
    }

    # Add review fields only if they do not already exist.
    $defaults = @{
        evidence_verified = "PENDING"
        distinct_case_final = "PENDING"
        answerable_final = "PENDING"
        review_decision_final = "PENDING"
        review_notes_final = ""
    }

    foreach ($name in $defaults.Keys) {
        if (-not $data.Contains($name)) {
            $data[$name] = $defaults[$name]
        }
    }

    $result.Add([PSCustomObject]$data)
}

$uniqueMatched = @($matched | Select-Object -Unique)
$unmatched = @(
    $lookup.Keys | Where-Object { $_ -notin $uniqueMatched }
)

$priority = @(
    $result | Where-Object {
        $_.evidence_review_required -eq "YES"
    }
)

$missingHypotheses = @(
    $priority | Where-Object {
        [string]::IsNullOrWhiteSpace($_.root_cause_hypothesis_initial)
    }
)

if ($uniqueMatched.Count -ne 11) {
    throw "Expected 11 matched candidates, got $($uniqueMatched.Count)"
}

if ($priority.Count -ne 11) {
    throw "Expected 11 priority rows, got $($priority.Count)"
}

if ($unmatched.Count -gt 0) {
    throw "Unmatched IDs: $($unmatched -join ', ')"
}

if ($missingHypotheses.Count -gt 0) {
    throw "Missing root cause hypotheses: $($missingHypotheses.candidate_id -join ', ')"
}

$result | Export-Csv -LiteralPath $OutputPath `
    -NoTypeInformation -Encoding UTF8

# Re-import the actual file to verify its exported structure.
$check = @(Import-Csv -LiteralPath $OutputPath)

if ($check.Count -ne 56) {
    throw "Export verification failed: expected 56 rows."
}

$checkPriority = @(
    $check | Where-Object {
        $_.evidence_review_required -eq "YES"
    }
)

if ($checkPriority.Count -ne 11) {
    throw "Export verification failed: expected 11 priority rows."
}

Write-Host "`nREVIEW FILE CREATED AND VERIFIED" -ForegroundColor Green
Write-Host "Total rows: $($check.Count)"
Write-Host "Priority rows: $($checkPriority.Count)"
Write-Host "Missing hypotheses: 0"
Write-Host "Output: $OutputPath"

$checkPriority |
    Select-Object candidate_id,
        observed_symptom,
        root_cause_hypothesis_initial,
        evidence_verified,
        distinct_case_final,
        answerable_final,
        review_decision_final |
    Format-Table -AutoSize
