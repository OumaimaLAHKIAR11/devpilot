$envFile = Join-Path $PSScriptRoot ".env"

if (-not (Test-Path $envFile)) {
    throw "Fichier .env introuvable : $envFile"
}

Get-Content $envFile | ForEach-Object {
    $line = $_.Trim()

    # Ignorer les lignes vides et les commentaires
    if (-not $line -or $line.StartsWith("#")) {
        return
    }

    # Lire NOM=VALEUR, même si la valeur contient des '='
    if ($line -match '^\s*(?:export\s+)?([A-Za-z_][A-Za-z0-9_]*)\s*=(.*)$') {
        $name = $matches[1]
        $value = $matches[2].Trim()

        # Retirer les guillemets extérieurs éventuels
        if ($value.Length -ge 2) {
            if (($value.StartsWith('"') -and $value.EndsWith('"')) -or
                ($value.StartsWith("'") -and $value.EndsWith("'"))) {
                $value = $value.Substring(1, $value.Length - 2)
            }
        }

        Set-Item -Path "Env:$name" -Value $value
    }
}

# Afficher uniquement la présence des variables, jamais leurs valeurs
$required = @(
    "AZURE_OPENAI_API_KEY",
    "AZURE_OPENAI_ENDPOINT",
    "AZURE_EMBEDDING_DEPLOYMENT"
)

foreach ($name in $required) {
    $value = [Environment]::GetEnvironmentVariable($name, "Process")

    if ([string]::IsNullOrWhiteSpace($value)) {
        Write-Host "$name : ABSENT" -ForegroundColor Red
    } else {
        Write-Host "$name : PRESENT" -ForegroundColor Green
    }
}
