#!/bin/bash
# Tractus-X EDC Connector deployment for ${providerName}
# Generated at ${generatedAt}
# Image: ${tractusxRuntimeImage}
set -e

PROVIDER_NAME="${providerName}"
DEPLOY_DIR="/opt/edc/${providerName}"
CALLBACK_URL="${callbackUrl}"
PROVIDER_ID="${providerId}"
MGMT_PORT="${controlplaneMgmtPort}"

echo "=========================================="
echo "  Deploying Tractus-X EDC: $PROVIDER_NAME"
echo "=========================================="

echo "[1/5] Creating directories..."
mkdir -p "$DEPLOY_DIR/credentials"

echo "[2/5] Generating DID key pair..."
if [ ! -s "$DEPLOY_DIR/credentials/private-key.pem" ]; then
    openssl ecparam -name prime256v1 -genkey -noout -out "$DEPLOY_DIR/credentials/private-key.pem"
    openssl ec -in "$DEPLOY_DIR/credentials/private-key.pem" -pubout -out "$DEPLOY_DIR/credentials/public-key.pem"
fi

echo "[3/5] Setting up docker-compose..."
cp docker-compose.yml "$DEPLOY_DIR/"

echo "[4/5] Pulling image and starting container..."
cd "$DEPLOY_DIR"
docker compose pull
docker compose up -d

echo "[5/5] Waiting for health check..."
HEALTHY=false
for i in 1 2 3 4 5 6 7 8 9 10 11 12; do
    sleep 10
    STATUS=$(docker inspect --format='{{.State.Health.Status}}' "${providerName}-edc" 2>/dev/null || echo "starting")
    echo "  attempt $i: $STATUS"
    if [ "$STATUS" = "healthy" ]; then
        HEALTHY=true
        break
    fi
done

if [ "$HEALTHY" = "true" ]; then
    echo "SUCCESS: container ${providerName}-edc is healthy"
    if [ -n "$CALLBACK_URL" ]; then
        curl -s -X POST "$CALLBACK_URL/provider/updateStatus?id=$PROVIDER_ID&status=RUNNING" \
             -H "Content-Type: application/json" || echo "Warning: callback failed"
    fi
else
    echo "WARNING: container did not become healthy within 2 minutes"
    echo "Check logs: docker logs ${providerName}-edc"
    exit 1
fi

LOCAL_IP=$(hostname -I | awk '{print $1}')
echo ""
echo "=========================================="
echo "  Deployment complete!"
echo "  Management API: http://$LOCAL_IP:$MGMT_PORT/management"
echo "  DSP Protocol:   http://$LOCAL_IP:${controlplaneProtocolPort}/api/v1/dsp"
echo "  Public API:     http://$LOCAL_IP:${dataplanePublicPort}/api/public"
echo "=========================================="
