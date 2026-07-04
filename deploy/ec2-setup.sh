#!/bin/bash
# =============================================================================
# EC2 Instance Setup Script — Amazon Linux 2023
# =============================================================================
# Run this script ONCE after launching a fresh EC2 instance:
#   chmod +x ec2-setup.sh && sudo ./ec2-setup.sh
# =============================================================================

set -euo pipefail

echo "============================================="
echo " Smart Campus System — EC2 Setup"
echo "============================================="

# --- System updates ---
echo "[1/6] Updating system packages..."
dnf update -y

# --- Java 21 (Amazon Corretto) ---
echo "[2/6] Installing Java 21 (Amazon Corretto)..."
dnf install -y java-21-amazon-corretto-devel
java -version

# --- Redis 7 ---
echo "[3/6] Installing Redis..."
dnf install -y redis6
# Enable AOF persistence
sed -i 's/^appendonly no/appendonly yes/' /etc/redis6/redis6.conf
# Limit memory to 128MB (t2.micro has 1GB total)
echo "maxmemory 128mb" >> /etc/redis6/redis6.conf
echo "maxmemory-policy allkeys-lru" >> /etc/redis6/redis6.conf
systemctl enable redis6
systemctl start redis6
redis6-cli ping

# --- Application directory ---
echo "[4/6] Creating application directory..."
mkdir -p /opt/smart-campus/logs
useradd -r -s /bin/false smartcampus || true
chown -R smartcampus:smartcampus /opt/smart-campus

# --- Systemd service ---
echo "[5/6] Installing systemd service..."
cat > /etc/systemd/system/smart-campus.service << 'EOF'
[Unit]
Description=Smart Campus System Backend API
After=network.target redis6.service
Wants=redis6.service

[Service]
Type=simple
User=smartcampus
Group=smartcampus
WorkingDirectory=/opt/smart-campus
ExecStart=/usr/bin/java \
  -XX:+UseContainerSupport \
  -XX:MaxRAMPercentage=60.0 \
  -Djava.security.egd=file:/dev/./urandom \
  -jar /opt/smart-campus/app.jar

# Environment file with secrets (create this file manually with your credentials)
EnvironmentFile=/opt/smart-campus/.env

# Restart on failure
Restart=on-failure
RestartSec=10
StartLimitIntervalSec=60
StartLimitBurst=3

# Logging
StandardOutput=append:/opt/smart-campus/logs/stdout.log
StandardError=append:/opt/smart-campus/logs/stderr.log

# Security hardening
NoNewPrivileges=true
ProtectSystem=strict
ReadWritePaths=/opt/smart-campus/logs /opt/smart-campus/uploads

[Install]
WantedBy=multi-user.target
EOF

systemctl daemon-reload
systemctl enable smart-campus

# --- Log rotation ---
echo "[6/6] Configuring log rotation..."
cat > /etc/logrotate.d/smart-campus << 'EOF'
/opt/smart-campus/logs/*.log {
    daily
    missingok
    rotate 7
    compress
    delaycompress
    copytruncate
    notifempty
}
EOF

echo ""
echo "============================================="
echo " Setup Complete!"
echo "============================================="
echo ""
echo " Next steps:"
echo "  1. Create /opt/smart-campus/.env with your secrets (see env-template below)"
echo "  2. SCP your app.jar to /opt/smart-campus/app.jar"
echo "  3. sudo systemctl start smart-campus"
echo "  4. sudo systemctl status smart-campus"
echo "  5. curl http://localhost:8080/api/health"
echo ""
echo " Environment template saved to /opt/smart-campus/.env.template"

# --- Environment template ---
cat > /opt/smart-campus/.env.template << 'ENVEOF'
# =============================================================================
# Smart Campus — Production Environment Variables
# =============================================================================
# Copy this to .env and fill in real values:
#   cp .env.template .env && chmod 600 .env && nano .env
# =============================================================================

# Spring Boot
SPRING_PROFILES_ACTIVE=prod
SERVER_PORT=8080

# MySQL (RDS)
MYSQL_HOST=<your-rds-endpoint>.rds.amazonaws.com
MYSQL_PORT=3306
MYSQL_DB=smartcampus
MYSQL_USER=admin
MYSQL_PASSWORD=<your-rds-password>

# JWT (generate with: openssl rand -base64 48)
APP_JWT_SECRET=<random-secret-at-least-32-characters>
APP_JWT_EXPIRATION_MS=86400000

# AWS S3 (Profile Images)
AWS_ACCESS_KEY_ID=<your-iam-access-key>
AWS_SECRET_ACCESS_KEY=<your-iam-secret-key>
AWS_S3_PROFILE_BUCKET=smart-campus-images-b2
AWS_S3_PROFILE_REGION=ap-south-1
AWS_S3_PROFILE_PUBLIC_BASE_URL=https://<cloudfront-id>.cloudfront.net/

# Google OAuth2
GOOGLE_CLIENT_ID=<your-google-client-id>
GOOGLE_CLIENT_SECRET=<your-google-client-secret>
APP_OAUTH2_FRONTEND_REDIRECT_URL=https://<cloudfront-id>.cloudfront.net/auth/callback

# Mail (SMTP)
SPRING_MAIL_USERNAME=<your-email@gmail.com>
SPRING_MAIL_PASSWORD=<your-gmail-app-password>

# Redis (local on this EC2)
SPRING_DATA_REDIS_HOST=localhost
SPRING_DATA_REDIS_PORT=6379
ENVEOF

chown smartcampus:smartcampus /opt/smart-campus/.env.template
echo " Done."
