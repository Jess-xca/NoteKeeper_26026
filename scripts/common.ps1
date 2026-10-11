# Shared helpers for docker-smoke.ps1 and seed-demo-data.ps1.
# Works in Windows PowerShell 5.1 and PowerShell 7.

$script:Api = if ($env:NOTEKEEPER_API) { $env:NOTEKEEPER_API } else { "http://localhost:8080/api" }
$script:RabbitApi = "http://localhost:15672/api"

function Invoke-Api {
    param(
        [Parameter(Mandatory)][string]$Method,
        [Parameter(Mandatory)][string]$Path,
        [string]$Token,
        $Body,
        [hashtable]$Query
    )
    $uri = "$script:Api$Path"
    if ($Query -and $Query.Count -gt 0) {
        $pairs = $Query.GetEnumerator() | ForEach-Object { "$($_.Key)=$([uri]::EscapeDataString([string]$_.Value))" }
        $uri += "?" + ($pairs -join "&")
    }
    $params = @{ Method = $Method; Uri = $uri; UseBasicParsing = $true; Headers = @{} }
    if ($Token) { $params.Headers["Authorization"] = "Bearer $Token" }
    if ($null -ne $Body) {
        $json = $Body | ConvertTo-Json -Depth 8 -Compress
        $params.Body = [Text.Encoding]::UTF8.GetBytes($json)
        $params.ContentType = "application/json; charset=utf-8"
    }

    $status = 0
    $content = ""
    try {
        $response = Invoke-WebRequest @params
        $status = [int]$response.StatusCode
        $content = $response.Content
    }
    catch {
        $response = $_.Exception.Response
        if ($response) {
            $status = [int]$response.StatusCode
            if ($_.ErrorDetails -and $_.ErrorDetails.Message) {
                $content = $_.ErrorDetails.Message
            }
            else {
                try {
                    $reader = New-Object IO.StreamReader($response.GetResponseStream())
                    $content = $reader.ReadToEnd()
                }
                catch { $content = "" }
            }
        }
        else {
            $content = $_.Exception.Message
        }
    }

    $data = $null
    if ($content) {
        $trimmed = $content.TrimStart()
        if ($trimmed.StartsWith("{") -or $trimmed.StartsWith("[")) {
            try { $data = $content | ConvertFrom-Json } catch { $data = $null }
        }
    }
    [pscustomobject]@{ Status = $status; Data = $data; Raw = $content }
}

function Get-Login {
    param([string]$Username, [string]$Password)
    $r = Invoke-Api -Method Post -Path "/auth/login" -Body @{ username = $Username; password = $Password }
    if ($r.Status -ne 200 -or -not $r.Data.token) {
        throw "Login failed for $Username (HTTP $($r.Status)): $($r.Raw)"
    }
    [pscustomobject]@{ Token = $r.Data.token; Id = $r.Data.user.id; Email = $r.Data.user.email; Username = $Username }
}

function Wait-ForBackend {
    param([int]$Seconds = 180)
    $deadline = (Get-Date).AddSeconds($Seconds)
    while ((Get-Date) -lt $deadline) {
        $r = Invoke-Api -Method Get -Path "/locations/provinces"
        if ($r.Status -eq 200) { return }
        Start-Sleep -Seconds 3
    }
    throw "The backend did not answer at $script:Api within $Seconds seconds. Is 'docker compose up' running?"
}

# Runs a script block with some admin settings changed, then puts the original settings back
# (even when the block fails). Changing settings never touches the 2FA e-mail of the one admin
# sign-in that is needed to change them.
function Invoke-WithSettings {
    param(
        [Parameter(Mandatory)]$AdminToken,
        [Parameter(Mandatory)][hashtable]$Changes,
        [Parameter(Mandatory)][scriptblock]$Block
    )
    $original = Invoke-Api -Method Get -Path "/admin/settings" -Token $AdminToken
    if ($original.Status -ne 200) { throw "Could not read settings (HTTP $($original.Status))" }
    $changed = $original.Data | ConvertTo-Json -Depth 4 | ConvertFrom-Json
    foreach ($key in $Changes.Keys) { $changed.$key = $Changes[$key] }
    $put = Invoke-Api -Method Put -Path "/admin/settings" -Token $AdminToken -Body $changed
    if ($put.Status -ne 200) { throw "Could not change settings (HTTP $($put.Status)): $($put.Raw)" }
    try {
        & $Block
    }
    finally {
        $restore = Invoke-Api -Method Put -Path "/admin/settings" -Token $AdminToken -Body $original.Data
        if ($restore.Status -ne 200) {
            Write-Warning "Could not restore the original admin settings. Check the Settings tab. ($($restore.Raw))"
        }
        else {
            Write-Host "  (admin settings restored)" -ForegroundColor DarkGray
        }
    }
}
