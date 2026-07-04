#!/bin/bash
# =============================================================================
# Smart Campus Backend — Deployment Script
# =============================================================================
# Called by Jenkins after building the JAR. Deploys to EC2 via SSH.
# Usage: ./deploy.sh <jar-path> <ec2-host> <ssh-key-path>
# =============================================================================

set -euo pipefail

JAR_PATH="${1:?Usage: ./deploy.sh <jar-path> <ec2-host> <ssh-key-path>}"
EC2_HOST="${2:?Missing EC2 host}"
SSH_KEY="${3:?Missing SSH key path}"

APP_DIR="/opt/smart-campus"
APP_JAR="app.jar"
SSH_USER="ec2-user"
SSH_OPTS="-o StrictHostKeyChecking=no -o ConnectTimeout=10"
HEALTH_URL="http://localhost:8080/api/health"
MAX_RETRIES=20
RETRY_INTERVAL=5

echo "============================================="
echo " Deploying Smart Campus Backend"
echo " Target: ${SSH_USER}@${EC2_HOST}"
echo "============================================="

# --- Step 1: Upload JAR ---
echo "[1/4] Uploading JAR to EC2..."
scp ${SSH_OPTS} -i "${SSH_KEY}" "${JAR_PATH}" "${SSH_USER}@${EC2_HOST}:/tmp/${APP_JAR}"

# --- Step 2: Stop current application ---
echo "[2/4] Stopping current application..."
ssh ${SSH_OPTS} -i "${SSH_KEY}" "${SSH_USER}@${EC2_HOST}" << 'REMOTE_STOP'
sudo systemctl stop smart-campus || true
sleep 2
REMOTE_STOP

# --- Step 3: Deploy new JAR ---
echo "[3/4] Deploying new JAR..."
ssh ${SSH_OPTS} -i "${SSH_KEY}" "${SSH_USER}@${EC2_HOST}" << REMOTE_DEPLOY
# Backup current JAR
if [ -f ${APP_DIR}/${APP_JAR} ]; then
    sudo cp ${APP_DIR}/${APP_JAR} ${APP_DIR}/${APP_JAR}.backup
fi

# Move new JAR into place
sudo mv /tmp/${APP_JAR} ${APP_DIR}/${APP_JAR}
sudo chown smartcampus:smartcampus ${APP_DIR}/${APP_JAR}

# Start application
sudo systemctl start smart-campus
REMOTE_DEPLOY

# --- Step 4: Health check ---
echo "[4/4] Waiting for application to start..."
RETRY_COUNT=0
while [ $RETRY_COUNT -lt $MAX_RETRIES ]; do
    RETRY_COUNT=$((RETRY_COUNT + 1))
    echo "  Health check attempt ${RETRY_COUNT}/${MAX_RETRIES}..."

    HTTP_STATUS=$(ssh ${SSH_OPTS} -i "${SSH_KEY}" "${SSH_USER}@${EC2_HOST}" \
        "curl -s -o /dev/null -w '%{http_code}' ${HEALTH_URL}" 2>/dev/null || echo "000")

    if [ "${HTTP_STATUS}" = "200" ]; then
        echo ""
        echo "✅ Deployment successful! Application is healthy."
        echo ""
        ssh ${SSH_OPTS} -i "${SSH_KEY}" "${SSH_USER}@${EC2_HOST}" "curl -s ${HEALTH_URL}"
        echo ""
        exit 0
    fi

    sleep $RETRY_INTERVAL
done

# --- Rollback on failure ---
echo ""
echo "❌ Health check failed after ${MAX_RETRIES} attempts!"
echo "   Rolling back to previous version..."

ssh ${SSH_OPTS} -i "${SSH_KEY}" "${SSH_USER}@${EC2_HOST}" << 'REMOTE_ROLLBACK'
sudo systemctl stop smart-campus || true
if [ -f /opt/smart-campus/app.jar.backup ]; then
    sudo mv /opt/smart-campus/app.jar.backup /opt/smart-campus/app.jar
    sudo systemctl start smart-campus
    echo "Rollback complete. Previous version restored."
else
    echo "No backup JAR found. Manual intervention required!"
fi
REMOTE_ROLLBACK

exit 1
