#!/usr/bin/env sh
set -eu
cd "$(dirname "$0")/.."
if [ -f .env ]; then echo '.env already exists; keeping it.'; exit 0; fi
umask 077
{
 printf 'DB_PASSWORD=%s\n' "$(openssl rand -hex 24)"
 printf 'MYSQL_ROOT_PASSWORD=%s\n' "$(openssl rand -hex 24)"
 printf 'JWT_SECRET=%s\n' "$(openssl rand -hex 32)"
 printf 'ADMIN_EMAIL=admin@ceylon.test\n'
 printf 'ADMIN_PASSWORD=%s\n' "$(openssl rand -hex 16)"
} > .env
echo 'Created .env. Keep it private. Find ADMIN_PASSWORD inside it.'
echo 'Next: docker compose up --build -d, then check http://localhost:8080/api/health'
