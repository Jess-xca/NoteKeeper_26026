# Fills the running Docker stack with demo data through the real API, so every screen has something
# to show and MongoDB / RabbitMQ get real traffic.
#
#   docker compose up -d          (wait until the backend has started)
#   .\scripts\seed-demo-data.ps1
#
# What it does, as the three seeded accounts (Jessica admin, Alain editor, Divine user):
#   - Jessica: two workspaces with pages, tags, a favorite, an archived page, a quick note
#   - invites Alain (EDITOR) and Divine (VIEWER) to "Product Launch"; they accept
#   - Alain: his own workspace with pages, and he adds a page to Jessica's workspace
#   - Divine: quick notes, opens a few pages (Recent history), is refused when she tries to edit
#   - Jessica shares one page directly with Divine
# Several saves per page, so MongoDB page_revisions gets more than one version of the same page.
# The invitations and the share send real e-mail to the addresses of the seeded accounts.
#
# Safe to run twice: it stops if "Product Launch" already exists. Use -Force to add another round.

param([switch]$Force)

. "$PSScriptRoot\common.ps1"
$ErrorActionPreference = "Stop"

function Step($text) { Write-Host "`n== $text" -ForegroundColor Cyan }
function Done($text) { Write-Host "  + $text" -ForegroundColor Green }

function Assert-Status($result, [int[]]$ok, $what) {
    if ($ok -notcontains $result.Status) {
        throw "$what failed: HTTP $($result.Status) $($result.Raw)"
    }
}

function New-Workspace($user, $name, $description, $icon) {
    $r = Invoke-Api -Method Post -Path "/workspaces" -Token $user.Token -Body @{ name = $name; description = $description; icon = $icon }
    Assert-Status $r @(200, 201) "Create workspace '$name'"
    Done "workspace '$name'"
    $r.Data
}

function New-Page($user, $workspaceId, $title, $html, $icon, $tagNames) {
    $body = @{ title = $title; content = $html; icon = $icon; workspace = @{ id = $workspaceId } }
    $tagList = @()
    foreach ($n in @($tagNames)) { if ($n -and $script:Tags.ContainsKey($n)) { $tagList += @{ tag = @{ id = $script:Tags[$n] } } } }
    if ($tagList.Count -gt 0) { $body.pageTags = $tagList }
    $r = Invoke-Api -Method Post -Path "/pages" -Token $user.Token -Body $body
    if (($r.Status -notin @(200, 201)) -and ($tagList.Count -gt 0)) {
        $body.Remove("pageTags")
        $r = Invoke-Api -Method Post -Path "/pages" -Token $user.Token -Body $body
    }
    Assert-Status $r @(200, 201) "Create page '$title'"
    Done "page '$title'"
    $r.Data
}

function Save-Again($user, $page, $html, $title) {
    $t = if ($title) { $title } else { $page.title }
    $r = Invoke-Api -Method Put -Path "/pages/$($page.id)" -Token $user.Token -Body @{ title = $t; content = $html; icon = $page.icon; coverImage = $page.coverImage }
    Assert-Status $r @(200, 204) "Update page '$($page.title)'"
    Done "saved a new version of '$t'"
}

function Invite-And-Accept($owner, $guest, $workspace, $role) {
    $send = Invoke-Api -Method Post -Path "/invitations/send" -Token $owner.Token -Query @{ workspaceId = $workspace.id; email = $guest.Email; role = $role }
    Assert-Status $send @(200, 201) "Invite $($guest.Username)"
    Start-Sleep -Milliseconds 400
    $mine = Invoke-Api -Method Get -Path "/notifications/my" -Token $guest.Token
    Assert-Status $mine @(200) "Read notifications of $($guest.Username)"
    $invite = @($mine.Data) | Where-Object { $_.type -eq "WORKSPACE_INVITE" -and $_.message -like "*$($workspace.name)*" } | Select-Object -First 1
    if (-not $invite) { throw "No invitation for '$($workspace.name)' found for $($guest.Username)" }
    $accept = Invoke-Api -Method Post -Path "/invitations/$($invite.id)/accept" -Token $guest.Token
    Assert-Status $accept @(200) "Accept invitation"
    Done "$($guest.Username) joined '$($workspace.name)' as $role"
}

Step "Waiting for the backend"
Wait-ForBackend

Step "Signing in (this sends one 2FA e-mail for the admin)"
$jessica = Get-Login "jessica_admin" "Admin123!"
Done "jessica_admin"

Invoke-WithSettings -AdminToken $jessica.Token -Changes @{ requireTwoFactor = $false } -Block {

    $alain = Get-Login "alain_editor" "Editor123!"
    $divine = Get-Login "divine_user" "User123!"
    Done "alain_editor and divine_user (2FA is paused for the seeding run)"

    $mineWs = Invoke-Api -Method Get -Path "/workspaces/my" -Token $jessica.Token
    $existing = @($mineWs.Data)

    foreach ($old in ($existing | Where-Object { $_.name -like "Docker demo*" })) {
        Invoke-Api -Method Delete -Path "/workspaces/$($old.id)" -Token $jessica.Token | Out-Null
        Done "removed leftover test workspace '$($old.name)'"
    }
    if (-not $Force -and ($existing | Where-Object { $_.name -eq "Product Launch" })) {
        Write-Host "`nDemo data is already there ('Product Launch' exists). Use -Force to add another round." -ForegroundColor Yellow
        return
    }

    $tagResponse = Invoke-Api -Method Get -Path "/tags" -Token $jessica.Token
    $script:Tags = @{}
    foreach ($t in @($tagResponse.Data)) { $script:Tags[$t.name] = $t.id }

    $rocket = [char]::ConvertFromUtf32(0x1F680)
    $book = [char]::ConvertFromUtf32(0x1F4DA)
    $tools = [char]::ConvertFromUtf32(0x1F6E0)
    $check = [char]::ConvertFromUtf32(0x2705)
    $memo = [char]::ConvertFromUtf32(0x1F4DD)
    $bulb = [char]::ConvertFromUtf32(0x1F4A1)
    $rabbit = [char]::ConvertFromUtf32(0x1F407)
    $lock = [char]::ConvertFromUtf32(0x1F512)
    $cart = [char]::ConvertFromUtf32(0x1F6D2)

    Step "Jessica (admin): workspaces and pages"
    $launch = New-Workspace $jessica "Product Launch" "Everything for the Q4 release" $rocket
    $study = New-Workspace $jessica "Study Notes" "Architecture and messaging notes for the assessment" $book

    $checklist = New-Page $jessica $launch.id "Launch checklist" "<h2>Launch checklist</h2><ul><li>Freeze the code</li></ul>" $check @("Work", "Important", "To-Do")
    Save-Again $jessica $checklist "<h2>Launch checklist</h2><ul><li>Freeze the code</li><li>Run the smoke test</li></ul>"
    Save-Again $jessica $checklist "<h2>Launch checklist</h2><ul><li>Freeze the code</li><li>Run the smoke test</li><li>Send the announcement e-mail</li></ul><p>Owner: Jessica</p>"
    $press = New-Page $jessica $launch.id "Press release draft" "<p>NoteKeeper helps teams keep notes in one place.</p>" $memo @("Work")
    Save-Again $jessica $press "<p>NoteKeeper helps teams keep notes in one place and share them with the right role.</p>"
    $null = New-Page $jessica $launch.id "Roadmap Q4" "<p>Offline mode, page comments, real SMS provider.</p>" $bulb @("Ideas")

    $spring = New-Page $jessica $study.id "Spring Boot security notes" "<p>JWT filter, BCrypt, role checks in PermissionService.</p>" $lock @("Research")
    $rabbitPage = New-Page $jessica $study.id "RabbitMQ in NoteKeeper" "<p>Queues: email, sms, app-events. Workers write delivery_logs in MongoDB.</p>" $rabbit @("Research", "Ideas")
    Save-Again $jessica $rabbitPage "<p>Queues: email, sms, app-events.</p><p>Email goes out through SMTP; sms and app events are recorded in MongoDB delivery_logs.</p>"
    $old = New-Page $jessica $study.id "Old brainstorm" "<p>Superseded by the roadmap.</p>" $bulb @("Personal")

    $fav = Invoke-Api -Method Put -Path "/pages/$($checklist.id)/toggle-favorite" -Token $jessica.Token
    Assert-Status $fav @(200, 204) "Favorite"
    Done "favorited 'Launch checklist'"
    $arch = Invoke-Api -Method Put -Path "/pages/$($old.id)/toggle-archive" -Token $jessica.Token
    Assert-Status $arch @(200, 204) "Archive"
    Done "archived 'Old brainstorm'"

    $quick = Invoke-Api -Method Post -Path "/pages/quick-note" -Token $jessica.Token -Query @{ title = "Buy the domain name"; content = "Check notekeeper.app and notekeeper.io" }
    Assert-Status $quick @(200, 201) "Quick note"
    Done "quick note in the Inbox"

    Step "Invitations (e-mail + SMS + app event each)"
    Invite-And-Accept $jessica $alain $launch "EDITOR"
    Invite-And-Accept $jessica $divine $launch "VIEWER"

    Step "Alain (editor): own workspace and a page in Jessica's workspace"
    $eng = New-Workspace $alain "Engineering Notes" "API conventions and retrospectives" $tools
    $api = New-Page $alain $eng.id "API design guidelines" "<p>Plural nouns, JSON errors, 403 for missing role.</p>" $tools @("Work", "Research")
    Save-Again $alain $api "<p>Plural nouns, JSON errors, 403 for missing role, 401 for missing token.</p>"
    $null = New-Page $alain $eng.id "Sprint retrospective" "<p>Went well: Docker stack. To improve: SMS provider.</p>" $memo @("Work")
    $alainTask = New-Page $alain $launch.id "Alain's launch tasks" "<p>Write the user guide and record the demo video.</p>" $check @("To-Do")
    Invite-And-Accept $alain $divine $eng "VIEWER"

    Step "Divine (user): quick notes, reading pages, and a refused edit"
    foreach ($note in @(
            @{ t = "Groceries"; c = "Rice, tomatoes, onions" },
            @{ t = "Reading list"; c = "Designing Data-Intensive Applications" })) {
        $q = Invoke-Api -Method Post -Path "/pages/quick-note" -Token $divine.Token -Query @{ title = $note.t; content = $note.c }
        Assert-Status $q @(200, 201) "Quick note"
        Done "quick note '$($note.t)'"
    }
    foreach ($p in @($checklist, $api, $alainTask)) {
        $open = Invoke-Api -Method Get -Path "/pages/$($p.id)" -Token $divine.Token
        Assert-Status $open @(200) "Divine opens '$($p.title)'"
    }
    Done "Divine opened 3 shared pages (they appear in Recent history)"
    $denied = Invoke-Api -Method Put -Path "/pages/$($checklist.id)" -Token $divine.Token -Body @{ title = "Hacked"; content = "x" }
    if ($denied.Status -ne 403) { throw "A viewer should get 403 when editing, got HTTP $($denied.Status)" }
    Done "viewer edit was refused with 403, as it should be"

    Step "Direct page share (e-mail + SMS + app event)"
    $share = Invoke-Api -Method Post -Path "/shares" -Token $jessica.Token -Body @{ pageId = $rabbitPage.id; email = $divine.Email; permission = "VIEW" }
    Assert-Status $share @(200, 201) "Share page"
    Done "'RabbitMQ in NoteKeeper' shared with Divine (view)"

    Start-Sleep -Seconds 6
    Step "Result"
    $status = Invoke-Api -Method Get -Path "/admin/mongo/status" -Token $jessica.Token
    if ($status.Data.available) {
        Write-Host ("  MongoDB: {0} page revisions, {1} delivery log entries" -f $status.Data.pageRevisions, $status.Data.deliveryLogs)
    }
    Write-Host "`nDone. Sign in as jessica_admin / Admin123! and open the dashboard, Shared with me (as divine_user) and MongoDB Data." -ForegroundColor Green
}
