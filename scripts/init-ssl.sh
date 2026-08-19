#!/bin/bash
# ============================================
# Initial SSL certificate setup with Certbot
# Run ONCE on EC2 after domain DNS is pointing to this server
# Usage: bash scripts/init-ssl.sh
# ============================================

DOMAIN="docbridge.cloud"
EMAIL="seungheelee1122@gmail.com"

set -e

echo "=== Step 1: Start nginx with HTTP only (for Certbot challenge) ==="

# Create a temporary nginx config for HTTP-only (no SSL yet)
cat > /tmp/nginx-init.conf << 'EOF'
server {
    listen 80;
    server_name docbridge.cloud;

    location /.well-known/acme-challenge/ {
        root /var/www/certbot;
    }

    location / {
        return 200 'Waiting for SSL setup...';
        add_header Content-Type text/plain;
    }
}
EOF

# Stop existing nginx if running
docker compose -f docker-compose.prod.yml stop nginx 2>/dev/null || true

# Start nginx with temp config
docker run -d --name certbot-nginx \
    -p 80:80 \
    -v /tmp/nginx-init.conf:/etc/nginx/conf.d/default.conf:ro \
    -v certbot-webroot:/var/www/certbot \
    nginx:alpine

echo "=== Step 2: Request SSL certificate ==="

docker run --rm \
    -v certbot-certs:/etc/letsencrypt \
    -v certbot-webroot:/var/www/certbot \
    certbot/certbot certonly \
    --webroot \
    --webroot-path=/var/www/certbot \
    --email "$EMAIL" \
    --agree-tos \
    --no-eff-email \
    -d "$DOMAIN"

echo "=== Step 3: Cleanup temp nginx ==="
docker stop certbot-nginx && docker rm certbot-nginx

echo ""
echo "=== SSL certificate issued successfully! ==="
echo "Now start the full stack:"
echo "  docker compose -f docker-compose.prod.yml up -d"
echo ""
echo "Certificate auto-renewal is handled by the certbot service in docker-compose."
