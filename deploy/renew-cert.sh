#!/bin/sh
# 인증서 갱신 cron용 스크립트. Let's Encrypt 인증서는 90일 유효하고, certbot renew는
# 만료 30일 이내일 때만 실제로 갱신하므로 이 스크립트를 주기적으로(예: 매주) 돌리면 된다.
set -e
cd "$(dirname "$0")"

docker run --rm \
  -v "$(pwd)/nginx/certbot/conf:/etc/letsencrypt" \
  -v "$(pwd)/nginx/certbot/www:/var/www/certbot" \
  certbot/certbot renew --webroot -w /var/www/certbot --quiet

docker exec deploy-nginx-1 nginx -s reload
