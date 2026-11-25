
$ErrorActionPreference = "Stop"

$Root = "C:\Users\ADMIN\Desktop\Ensias\s5\projet_S5_pfa\devpilot"
$InputPath = Join-Path $Root "datasets\golden-set-priority-review-v2.csv"
$OutputPath = Join-Path $Root "datasets\golden-set-priority-review-v3.csv"

if (Test-Path -LiteralPath $OutputPath) {
    $rows = @(Import-Csv -LiteralPath $OutputPath)
    Write-Host "Reprise depuis le fichier v3 existant."
}
elseif (Test-Path -LiteralPath $InputPath) {
    $rows = @(Import-Csv -LiteralPath $InputPath)
    Write-Host "Initialisation depuis le fichier v2."
}
else {
    throw "Fichier d'entrée introuvable : $InputPath"
}

if ($rows.Count -eq 0) {
    throw "Le fichier d'entrée ne contient aucune ligne."
}

# Garantir la présence des colonnes de revue.
$requiredColumns = @(
    "evidence_verified",
    "distinct_case_final",
    "answerable",
    "review_decision",
    "review_notes"
)

foreach ($row in $rows) {
    foreach ($column in $requiredColumns) {
        if ($null -eq $row.PSObject.Properties[$column]) {
            $row | Add-Member -NotePropertyName $column -NotePropertyValue "PENDING"
        }
    }
}

function Read-Choice {
    param(
        [string]$Prompt,
        [string[]]$Allowed
    )

    while ($true) {
        $value = (Read-Host $Prompt).Trim().ToUpperInvariant()

        if ([string]::IsNullOrWhiteSpace($value)) {
            return "PENDING"
        }

        if ($value -eq "Q") {
            return "QUIT"
        }

        if ($Allowed -contains $value) {
            return $value
        }

        Write-Host "Valeur invalide. Choix : $($Allowed -join ', '), Q pour quitter." -ForegroundColor Yellow
    }
}

function Save-Review {
    param([object[]]$Data)

    $Data | Export-Csv -LiteralPath $OutputPath -NoTypeInformation -Encoding UTF8
    Write-Host "Sauvegarde effectuée : $OutputPath" -ForegroundColor Green
}

Write-Host ""
Write-Host "REVUE MANUELLE DES 11 CANDIDATS"
Write-Host "YES / NO / PENDING ; KEEP / EXCLUDE / PENDING"
Write-Host "Entrée vide = PENDING ; Q = sauvegarder et quitter."
Write-Host ""

for ($i = 0; $i -lt $rows.Count; $i++) {
    $row = $rows[$i]

    Write-Host ""
    Write-Host "============================================"
    Write-Host "CANDIDAT : $($row.candidate_id)"
    Write-Host "============================================"
    Write-Host "Symptôme : $($row.observed_symptom)"
    Write-Host "Extrait : $($row.evidence_excerpt)"
    Write-Host "Hypothèse initiale : $($row.root_cause_hypothesis)"
    Write-Host "Confiance initiale : $($row.root_cause_confidence)"
    Write-Host "Famille : $($row.incident_family)"
    Write-Host ""
    Write-Host "Consulte le log original complet avant de répondre."

    $value = Read-Choice "Evidence vérifiée dans le log original ? [YES/NO/PENDING]" @("YES", "NO", "PENDING")
    if ($value -eq "QUIT") {
        Save-Review $rows
        return
    }
    $row.evidence_verified = $value

    $value = Read-Choice "Cas distinct ? [YES/NO/PENDING]" @("YES", "NO", "PENDING")
    if ($value -eq "QUIT") {
        Save-Review $rows
        return
    }
    $row.distinct_case_final = $value

    $value = Read-Choice "Le corpus actuel peut-il répondre ? [YES/NO/PENDING]" @("YES", "NO", "PENDING")
    if ($value -eq "QUIT") {
        Save-Review $rows
        return
    }
    $row.answerable = $value

    $value = Read-Choice "Décision [KEEP/EXCLUDE/PENDING]" @("KEEP", "EXCLUDE", "PENDING")
    if ($value -eq "QUIT") {
        Save-Review $rows
        return
    }
    $row.review_decision = $value

    $note = Read-Host "Note de revue (facultative ; Q pour quitter)"
    if ($note.Trim().ToUpperInvariant() -eq "Q") {
        Save-Review $rows
        return
    }
    if (-not [string]::IsNullOrWhiteSpace($note)) {
        $row.review_notes = $note.Trim()
    }

    if ($row.review_decision -eq "KEEP" -and
        ($row.evidence_verified -ne "YES" -or
         $row.distinct_case_final -ne "YES")) {
        Write-Host "ATTENTION : KEEP est incohérent sans preuve vérifiée et cas distinct." -ForegroundColor Yellow
        $row.review_decision = "PENDING"
    }

    # Sauvegarde après chaque candidat.
    Save-Review $rows
}

Write-Host ""
Write-Host "Revue terminée pour toutes les lignes." -ForegroundColor Green
Save-Review $rows
