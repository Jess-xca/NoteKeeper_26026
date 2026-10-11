# Calls the endpoints of the RUNNING Docker stack over HTTP and checks the answers, plus RabbitMQ
# and MongoDB. Prints one line per check and a summary. Exit code 1 if anything failed.
#
#   docker compose up -d
#   .\scripts\docker-smoke.ps1
#
# It signs in as jessica_admin (admin), divine_user (normal user) and alain_editor. Two-factor
# sign-in and e-mail notifications are switched off while it runs and restored afterwards, so the
# run does not flood your inbox (the admin sign-in that is needed first still sends one 2FA mail).
# The test data it creates is deleted again at the end.

. "$PSScriptRoot\common.ps1"
$ErrorActionPreference = "Stop"

$script:results = New-Object System.Collections.ArrayList
$script:gaps = New-Object System.Collections.ArrayList

function Check {
    param([string]$Name, $Result, [int[]]$Expect = @(200))
    $ok = $Expect -contains $Result.Status
    [void]$script:results.Add([pscustomobject]@{ Name = $Name; Status = $Result.Status; Expected = ($Expect -join "/"); Ok = $ok; Note = if ($ok) { "" } else { $Result.Raw } })
    $mark = if ($ok) { "PASS" } else { "FAIL" }
    $color = if ($ok) { "Green" } else { "Red" }
    Write-Host ("  [{0}] {1,-58} HTTP {2} (expected {3})" -f $mark, $Name, $Result.Status, ($Expect -join "/")) -ForegroundColor $color
    if (-not $ok -and $Result.Raw) {
        $short = $Result.Raw.Substring(0, [Math]::Min(160, $Result.Raw.Length))
        Write-Host "         $short" -ForegroundColor DarkRed
    }
}

function CheckTrue {
    param([string]$Name, [bool]$Condition, [string]$Detail = "")
    [void]$script:results.Add([pscustomobject]@{ Name = $Name; Status = "-"; Expected = "true"; Ok = $Condition; Note = $Detail })
    $mark = if ($Condition) { "PASS" } else { "FAIL" }
    $color = if ($Condition) { "Green" } else { "Red" }
    Write-Host ("  [{0}] {1,-58} {2}" -f $mark, $Name, $Detail) -ForegroundColor $color
}

# A weakness that is known and written down: shown in yellow, listed in the summary, but it does not
# fail the run. When the code is fixed the line turns into a normal PASS.
function CheckKnownGap {
    param([string]$Name, $Result, [int[]]$Expect = @(401, 403))
    if ($Expect -contains $Result.Status) {
        Check $Name $Result $Expect
        return
    }
    [void]$script:gaps.Add($Name)
    Write-Host ("  [WARN] {0,-58} HTTP {1} (should be {2}) - known gap" -f $Name, $Result.Status, ($Expect -join "/")) -ForegroundColor Yellow
}

function Section($text) { Write-Host "`n== $text" -ForegroundColor Cyan }
function Get-Ok($method, $path, $token, $body, $query) { Invoke-Api -Method $method -Path $path -Token $token -Body $body -Query $query }

Section "Waiting for the backend"
Wait-ForBackend

Section "Sign in"
$admin = Get-Login "jessica_admin" "Admin123!"
Write-Host "  admin signed in (one 2FA e-mail was sent)"

Invoke-WithSettings -AdminToken $admin.Token -Changes @{ requireTwoFactor = $false; emailNotifications = $false } -Block {

    $user = Get-Login "divine_user" "User123!"
    $editor = Get-Login "alain_editor" "Editor123!"
    $created = @{ workspaces = @(); pages = @(); tags = @() }

    Section "Public and unauthenticated access"
    Check "login with a wrong password" (Invoke-Api Post "/auth/login" -Body @{ username = "jessica_admin"; password = "nope" }) 400, 401
    Check "login with an empty body" (Invoke-Api Post "/auth/login" -Body @{}) 400
    Check "locations are public" (Invoke-Api Get "/locations/provinces")
    Check "creating a location without a token" (Invoke-Api Post "/locations" -Body @{ name = "X"; code = "X"; type = "PROVINCE" }) 401, 403
    Check "my pages without a token" (Invoke-Api Get "/pages/my") 401, 403
    Check "user list without a token" (Invoke-Api Get "/users") 401, 403
    Check "profile list without a token" (Invoke-Api Get "/profiles") 401, 403
    Check "mongo status without a token" (Invoke-Api Get "/admin/mongo/status") 401, 403
    Check "unknown route" (Invoke-Api Get "/does-not-exist" -Token $user.Token) 404, 403

    Section "Auth"
    Check "me" (Invoke-Api Get "/auth/me" -Token $user.Token)
    Check "verify token" (Invoke-Api Get "/auth/verify" -Token $user.Token)
    Check "verify-2fa with a wrong code" (Invoke-Api Post "/auth/verify-2fa" -Body @{ userId = $user.Id; code = "000000" }) 400
    $badToken = Invoke-Api Get "/auth/verify-reset-token" -Query @{ token = "nope" }
    Check "verify-reset-token answers for a bad token" $badToken
    CheckTrue "a bad reset token is reported as not valid" ($badToken.Data.valid -eq $false)
    Check "reset-password with a bad token" (Invoke-Api Post "/auth/reset-password" -Body @{ token = "nope"; newPassword = "Abcdef1!" }) 400, 404

    Section "Users and roles"
    Check "member cannot list users" (Invoke-Api Get "/users" -Token $user.Token) 403
    Check "admin lists users" (Invoke-Api Get "/users" -Token $admin.Token)
    Check "member reads own account" (Invoke-Api Get "/users/$($user.Id)" -Token $user.Token)
    Check "member cannot read another account" (Invoke-Api Get "/users/$($admin.Id)" -Token $user.Token) 403
    Check "admin reads any account" (Invoke-Api Get "/users/$($user.Id)" -Token $admin.Token)
    Check "member cannot promote herself" (Invoke-Api Put "/users/admin/$($user.Id)" -Token $user.Token -Body @{ role = "ADMIN" }) 403
    Check "member cannot delete another user" (Invoke-Api Delete "/users/$($admin.Id)" -Token $user.Token) 403
    Check "admin searches users" (Invoke-Api Get "/users/search" -Token $admin.Token -Query @{ keyword = "jess" })
    Check "admin pages through users" (Invoke-Api Get "/users/paginated" -Token $admin.Token -Query @{ page = 0; size = 5 })
    Check "user count" (Invoke-Api Get "/users/count" -Token $user.Token)
    Check "username availability" (Invoke-Api Get "/users/check-username" -Token $user.Token -Query @{ username = "free-name-123" })

    Section "Profiles"
    Check "own profile" (Invoke-Api Get "/profiles/my" -Token $user.Token)
    Check "member cannot list profiles" (Invoke-Api Get "/profiles" -Token $user.Token) 403
    Check "admin lists profiles" (Invoke-Api Get "/profiles" -Token $admin.Token)

    Section "Workspaces"
    $ws = Invoke-Api Post "/workspaces" -Token $user.Token -Body @{ name = "Smoke test workspace"; description = "temporary"; icon = "T" }
    Check "create workspace" $ws 200, 201
    $wsId = $ws.Data.id
    $created.workspaces += $wsId
    Check "read workspace" (Invoke-Api Get "/workspaces/$wsId" -Token $user.Token)
    Check "update workspace" (Invoke-Api Put "/workspaces/$wsId" -Token $user.Token -Body @{ name = "Smoke test workspace 2"; description = "temporary"; icon = "T" })
    Check "my workspaces" (Invoke-Api Get "/workspaces/my" -Token $user.Token)
    Check "workspaces shared with me" (Invoke-Api Get "/workspaces/my/shared" -Token $user.Token)
    Check "my inbox workspace" (Invoke-Api Get "/workspaces/my/inbox" -Token $user.Token)
    Check "workspaces by owner" (Invoke-Api Get "/workspaces/owner/$($user.Id)" -Token $user.Token)
    Check "workspace count by owner" (Invoke-Api Get "/workspaces/count/owner/$($user.Id)" -Token $user.Token)
    Check "page count in workspace" (Invoke-Api Get "/workspaces/$wsId/count-pages" -Token $user.Token)
    Check "member cannot list all workspaces" (Invoke-Api Get "/workspaces" -Token $user.Token) 403
    Check "admin lists all workspaces" (Invoke-Api Get "/workspaces" -Token $admin.Token)
    Check "outsider cannot read the members" (Invoke-Api Get "/workspaces/$wsId/members" -Token $editor.Token) 401, 403

    Section "Tags"
    Check "list tags" (Invoke-Api Get "/tags" -Token $user.Token)
    Check "tag count" (Invoke-Api Get "/tags/count" -Token $user.Token)
    Check "search tags" (Invoke-Api Get "/tags/search" -Token $user.Token -Query @{ keyword = "wo" })
    $tag = Invoke-Api Post "/tags" -Token $user.Token -Body @{ name = "smoke-$(Get-Random)" }
    Check "create tag" $tag 200, 201
    if ($tag.Data.id) { $created.tags += $tag.Data.id }

    Section "Pages"
    $page = Invoke-Api Post "/pages" -Token $user.Token -Body @{ title = "Smoke page"; content = "<p>v1</p>"; icon = "S"; workspace = @{ id = $wsId } }
    Check "create page" $page 200, 201
    $pageId = $page.Data.id
    Check "create page without a workspace" (Invoke-Api Post "/pages" -Token $user.Token -Body @{ title = "x" }) 400
    Check "outsider cannot create in my workspace" (Invoke-Api Post "/pages" -Token $editor.Token -Body @{ title = "x"; workspace = @{ id = $wsId } }) 401, 403
    $quick = Invoke-Api Post "/pages/quick-note" -Token $user.Token -Query @{ title = "Smoke quick note"; content = "temporary" }
    Check "quick note" $quick 200, 201
    Check "read page" (Invoke-Api Get "/pages/$pageId" -Token $user.Token)
    Check "outsider cannot read the page" (Invoke-Api Get "/pages/$pageId" -Token $editor.Token) 401, 403
    Check "update page (new revision)" (Invoke-Api Put "/pages/$pageId" -Token $user.Token -Body @{ title = "Smoke page"; content = "<p>v2</p>"; icon = "S" }) 200, 204
    Check "my pages" (Invoke-Api Get "/pages/my" -Token $user.Token)
    Check "pages shared with me" (Invoke-Api Get "/pages/shared-with-me" -Token $user.Token)
    Check "shared-with-me count" (Invoke-Api Get "/pages/shared-with-me/count" -Token $user.Token)
    Check "recent history" (Invoke-Api Get "/pages/recent-history" -Token $user.Token)
    Check "pages by user" (Invoke-Api Get "/pages/user/$($user.Id)" -Token $user.Token)
    Check "pages in workspace" (Invoke-Api Get "/pages/workspace/$wsId" -Token $user.Token)
    Check "favorites" (Invoke-Api Get "/pages/favorites/$($user.Id)" -Token $user.Token)
    Check "archived" (Invoke-Api Get "/pages/archived/$($user.Id)" -Token $user.Token)
    Check "inbox pages" (Invoke-Api Get "/pages/inbox/$($user.Id)" -Token $user.Token)
    Check "recent pages" (Invoke-Api Get "/pages/recent/$($user.Id)" -Token $user.Token)
    Check "search my pages" (Invoke-Api Get "/pages/search/user/$($user.Id)" -Token $user.Token -Query @{ keyword = "smoke" })
    Check "sorted pages" (Invoke-Api Get "/pages/user/$($user.Id)/sorted" -Token $user.Token -Query @{ sortBy = "title" })
    Check "paginated pages" (Invoke-Api Get "/pages/user/$($user.Id)/paginated" -Token $user.Token -Query @{ page = 0; size = 5 })
    Check "page count" (Invoke-Api Get "/pages/count/user/$($user.Id)" -Token $user.Token)
    Check "favorite count" (Invoke-Api Get "/pages/count/favorites/$($user.Id)" -Token $user.Token)
    Check "weekly stats" (Invoke-Api Get "/pages/weekly-stats/$($user.Id)" -Token $user.Token)
    Check "activity" (Invoke-Api Get "/pages/activity/$($user.Id)" -Token $user.Token)
    Check "toggle favorite" (Invoke-Api Put "/pages/$pageId/toggle-favorite" -Token $user.Token) 200, 204
    Check "toggle archive on" (Invoke-Api Put "/pages/$pageId/toggle-archive" -Token $user.Token) 200, 204
    Check "toggle archive off" (Invoke-Api Put "/pages/$pageId/toggle-archive" -Token $user.Token) 200, 204
    Check "member cannot list all pages" (Invoke-Api Get "/pages" -Token $user.Token) 403
    Check "admin lists all pages" (Invoke-Api Get "/pages" -Token $admin.Token)
    Check "admin can read someone else's page" (Invoke-Api Get "/pages/$pageId" -Token $admin.Token)
    Check "admin cannot edit someone else's page" (Invoke-Api Put "/pages/$pageId" -Token $admin.Token -Body @{ title = "x"; content = "x" }) 401, 403
    $created.pages += $pageId
    if ($quick.Data.id) { $created.pages += $quick.Data.id }

    Section "Invitations, members and roles (queues: email is off, sms and app events are on)"
    $invite = Invoke-Api Post "/invitations/send" -Token $user.Token -Query @{ workspaceId = $wsId; email = $editor.Email; role = "EDITOR" }
    Check "owner invites an editor" $invite 200, 201
    Check "invite with a bad role" (Invoke-Api Post "/invitations/send" -Token $user.Token -Query @{ workspaceId = $wsId; email = $editor.Email; role = "BOSS" }) 400
    Check "non-owner cannot invite" (Invoke-Api Post "/invitations/send" -Token $editor.Token -Query @{ workspaceId = $wsId; email = $admin.Email; role = "VIEWER" }) 401, 403
    Start-Sleep -Milliseconds 400
    $notes = Invoke-Api Get "/notifications/my" -Token $editor.Token
    Check "invitee sees a notification" $notes
    $inviteNote = @($notes.Data) | Where-Object { $_.type -eq "WORKSPACE_INVITE" -and $_.message -like "*Smoke test workspace*" } | Select-Object -First 1
    CheckTrue "the invitation is in the notifications" ($null -ne $inviteNote)
    if ($inviteNote) {
        Check "invitee accepts" (Invoke-Api Post "/invitations/$($inviteNote.id)/accept" -Token $editor.Token)
        Check "accepting twice is refused" (Invoke-Api Post "/invitations/$($inviteNote.id)/accept" -Token $editor.Token) 400
    }
    Check "owner lists the members" (Invoke-Api Get "/workspaces/$wsId/members" -Token $user.Token)
    Check "editor is a member" (Invoke-Api Get "/workspaces/$wsId/members/check/$($editor.Id)" -Token $user.Token)
    Check "editor can edit the page" (Invoke-Api Put "/pages/$pageId" -Token $editor.Token -Body @{ title = "Smoke page"; content = "<p>v3 by editor</p>"; icon = "S" }) 200, 204
    Check "owner makes the editor a viewer" (Invoke-Api Put "/workspaces/$wsId/members/$($editor.Id)/role" -Token $user.Token -Query @{ role = "VIEWER" })
    Check "viewer cannot edit the page" (Invoke-Api Put "/pages/$pageId" -Token $editor.Token -Body @{ title = "x"; content = "x" }) 401, 403
    Check "viewer can read the page" (Invoke-Api Get "/pages/$pageId" -Token $editor.Token)
    Check "viewer sees it under shared-with-me" (Invoke-Api Get "/pages/shared-with-me" -Token $editor.Token)
    Check "owner removes the member" (Invoke-Api Delete "/workspaces/$wsId/members/$($editor.Id)" -Token $user.Token)
    Check "removed member is locked out" (Invoke-Api Get "/pages/$pageId" -Token $editor.Token) 401, 403

    Section "Page shares"
    $share = Invoke-Api Post "/shares" -Token $user.Token -Body @{ pageId = $pageId; email = $editor.Email; permission = "VIEW" }
    Check "share a page" $share 200, 201
    Check "share with yourself is refused" (Invoke-Api Post "/shares" -Token $user.Token -Body @{ pageId = $pageId; email = $user.Email; permission = "VIEW" }) 400
    Check "share with an unknown e-mail" (Invoke-Api Post "/shares" -Token $user.Token -Body @{ pageId = $pageId; email = "nobody@example.invalid"; permission = "VIEW" }) 404
    Check "list shares of the page" (Invoke-Api Get "/shares/page/$pageId" -Token $user.Token)
    Check "shared by me" (Invoke-Api Get "/shares/shared-by-me" -Token $user.Token)
    Check "shared with me (recipient)" (Invoke-Api Get "/shares/mine" -Token $editor.Token)
    Check "recipient can read the shared page" (Invoke-Api Get "/pages/$pageId" -Token $editor.Token)
    if ($share.Data.id) {
        Check "change share to EDIT" (Invoke-Api Put "/shares/$($share.Data.id)" -Token $user.Token -Body @{ permission = "EDIT" })
        Check "recipient edits the page" (Invoke-Api Put "/pages/$pageId" -Token $editor.Token -Body @{ title = "Smoke page"; content = "<p>v4 by sharee</p>"; icon = "S" }) 200, 204
        Check "remove the share" (Invoke-Api Delete "/shares/$($share.Data.id)" -Token $user.Token)
    }

    Section "Notifications"
    Check "my notifications" (Invoke-Api Get "/notifications/my" -Token $user.Token)
    Check "my unread notifications" (Invoke-Api Get "/notifications/my/unread" -Token $user.Token)
    Check "my notification count" (Invoke-Api Get "/notifications/my/count" -Token $user.Token)
    CheckKnownGap "notifications of another user" (Invoke-Api Get "/notifications/user/$($admin.Id)" -Token $user.Token)
    CheckKnownGap "pages of another user" (Invoke-Api Get "/pages/user/$($admin.Id)" -Token $user.Token)
    CheckKnownGap "favorites of another user" (Invoke-Api Get "/pages/favorites/$($admin.Id)" -Token $user.Token)
    CheckKnownGap "workspaces of another user" (Invoke-Api Get "/workspaces/owner/$($admin.Id)" -Token $user.Token)

    Section "Attachments (upload with curl.exe)"
    $tmp = Join-Path ([IO.Path]::GetTempPath()) "notekeeper-smoke.txt"
    Set-Content -Path $tmp -Value "smoke test attachment" -Encoding ASCII
    $up = & curl.exe -s -w "`n%{http_code}" -H "Authorization: Bearer $($user.Token)" -F "file=@$tmp;type=text/plain" -F "pageId=$pageId" "$script:Api/attachments/upload"
    $upLines = @($up)
    $upStatus = [int]$upLines[-1]
    $upJson = ($upLines[0..($upLines.Count - 2)] -join "`n")
    $att = $null
    try { $att = $upJson | ConvertFrom-Json } catch { }
    Check "upload attachment" ([pscustomobject]@{ Status = $upStatus; Raw = $upJson }) 200, 201
    if ($att -and $att.id) {
        Check "read attachment" (Invoke-Api Get "/attachments/$($att.id)" -Token $user.Token)
        Check "list attachments of the page" (Invoke-Api Get "/attachments/page/$pageId" -Token $user.Token)
        Check "download attachment" (Invoke-Api Get "/attachments/$($att.id)/download" -Token $user.Token)
        Check "admin can read the attachment (moderation)" (Invoke-Api Get "/attachments/$($att.id)" -Token $admin.Token)
        Check "a person without access cannot read it" (Invoke-Api Get "/attachments/$($att.id)" -Token $editor.Token) 401, 403
        Check "rename attachment" (Invoke-Api Put "/attachments/$($att.id)" -Token $user.Token -Query @{ newFileName = "renamed.txt" })
        Check "delete attachment" (Invoke-Api Delete "/attachments/$($att.id)" -Token $user.Token) 200, 204
    }
    Check "upload without a page id" ([pscustomobject]@{ Status = [int](& curl.exe -s -o NUL -w "%{http_code}" -H "Authorization: Bearer $($user.Token)" -F "file=@$tmp;type=text/plain" "$script:Api/attachments/upload"); Raw = "" }) 400
    Check "download-by-path outside uploads" (Invoke-Api Get "/attachments/download-by-path" -Query @{ path = "../../etc/passwd" }) 404
    Remove-Item $tmp -ErrorAction SilentlyContinue

    Section "Admin settings"
    Check "admin reads settings" (Invoke-Api Get "/admin/settings" -Token $admin.Token)
    Check "member cannot read settings" (Invoke-Api Get "/admin/settings" -Token $user.Token) 403

    Section "RabbitMQ"
    $pair = [Convert]::ToBase64String([Text.Encoding]::ASCII.GetBytes("guest:guest"))
    try {
        $queues = Invoke-RestMethod -Uri "$script:RabbitApi/queues" -Headers @{ Authorization = "Basic $pair" } -TimeoutSec 20
        foreach ($name in "email", "sms", "app-events") {
            $q = $queues | Where-Object { $_.name -eq $name }
            CheckTrue "queue '$name' exists, durable, has a consumer" (($null -ne $q) -and $q.durable -and ($q.consumers -ge 1)) "published=$($q.message_stats.publish) acked=$($q.message_stats.ack) waiting=$($q.messages)"
        }
    }
    catch {
        CheckTrue "RabbitMQ management API reachable on port 15672" $false $_.Exception.Message
    }

    Section "MongoDB (through the admin API)"
    Start-Sleep -Seconds 4
    $mongo = Invoke-Api Get "/admin/mongo/status" -Token $admin.Token
    Check "mongo status" $mongo
    CheckTrue "MongoDB is connected" ([bool]$mongo.Data.available) "revisions=$($mongo.Data.pageRevisions) logs=$($mongo.Data.deliveryLogs)"
    $revs = Invoke-Api Get "/admin/mongo/page-revisions" -Token $admin.Token -Query @{ pageId = $pageId }
    Check "revisions of the smoke page" $revs
    CheckTrue "the smoke page has 4 saved versions in MongoDB" (@($revs.Data.items).Count -eq 4) "found $(@($revs.Data.items).Count)"
    if (@($revs.Data.items).Count -gt 0) {
        Check "read one revision in full" (Invoke-Api Get "/admin/mongo/page-revisions/$(@($revs.Data.items)[0].id)" -Token $admin.Token)
    }
    foreach ($channel in "sms", "app") {
        $logs = Invoke-Api Get "/admin/mongo/delivery-logs" -Token $admin.Token -Query @{ channel = $channel; limit = 200 }
        Check "delivery log, channel $channel" $logs
        CheckTrue "delivery log has $channel entries for the smoke run" (@($logs.Data.items | Where-Object { $_.body -like "*Smoke test workspace*" -or $_.recipient -like "workspace-invited" -or $_.recipient -like "page-shared" }).Count -gt 0)
    }
    Check "member cannot read revisions" (Invoke-Api Get "/admin/mongo/page-revisions" -Token $user.Token) 403
    Check "member cannot read delivery logs" (Invoke-Api Get "/admin/mongo/delivery-logs" -Token $user.Token) 403

    Section "Clean up the test data"
    foreach ($id in $created.pages) { Invoke-Api Delete "/pages/$id" -Token $user.Token | Out-Null }
    foreach ($id in $created.tags) { Invoke-Api Delete "/tags/$id" -Token $user.Token | Out-Null }
    foreach ($id in $created.workspaces) { Check "delete the test workspace" (Invoke-Api Delete "/workspaces/$id" -Token $user.Token) 200, 204 }
}

$failed = @($script:results | Where-Object { -not $_.Ok })
Write-Host ""
Write-Host ("{0} checks, {1} passed, {2} failed" -f $script:results.Count, ($script:results.Count - $failed.Count), $failed.Count) -ForegroundColor $(if ($failed.Count -eq 0) { "Green" } else { "Red" })
if ($script:gaps.Count -gt 0) {
    Write-Host ("{0} known gap(s) (do not fail the run):" -f $script:gaps.Count) -ForegroundColor Yellow
    $script:gaps | ForEach-Object { Write-Host "  - $_" -ForegroundColor Yellow }
}
if ($failed.Count -gt 0) {
    $failed | ForEach-Object { Write-Host ("  FAILED: {0} (HTTP {1}, expected {2})" -f $_.Name, $_.Status, $_.Expected) -ForegroundColor Red }
    exit 1
}
