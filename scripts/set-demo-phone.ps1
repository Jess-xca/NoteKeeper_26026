# Gives every user in the Docker PostgreSQL database the same demo phone number, so the SMS path
# (RabbitMQ "sms" queue -> MongoDB delivery_logs) runs for every share and invitation.
#
#   .\scripts\set-demo-phone.ps1                       (uses 0791903576)
#   .\scripts\set-demo-phone.ps1 -Phone "0780000000"
#
# SMS is simulated: nothing is sent to a handset. A real text needs a paid provider (see docs).

param([string]$Phone = "0791903576")

$ErrorActionPreference = "Stop"
if ($Phone -notmatch '^[0-9+]{8,15}$') { throw "That does not look like a phone number: $Phone" }

$container = docker compose ps -q postgres
if (-not $container) { throw "The postgres container is not running. Start it with: docker compose up -d" }

docker exec $container psql -U postgres -d notekeeper -c "UPDATE users SET phone_number = '$Phone';"
docker exec $container psql -U postgres -d notekeeper -c "SELECT username, phone_number FROM users ORDER BY username;"
