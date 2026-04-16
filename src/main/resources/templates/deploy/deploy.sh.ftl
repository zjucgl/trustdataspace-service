#!/bin/bash
# EDC Connector deployment script for ${providerName}
# Generated at ${generatedAt}
set -e

PROVIDER_NAME="${providerName}"
DEPLOY_DIR="/opt/edc/${providerName}"
CALLBACK_URL="${callbackUrl}"
PROVIDER_ID="${providerId}"

echo "=========================================="
echo "  Deploying EDC Connector: $PROVIDER_NAME"
echo "=========================================="

# 1. Create directory structure
echo "[1/6] Creating directories..."
mkdir -p $DEPLOY_DIR/{jars,config,credentials}

# 2. Copy JARs (from shared location or download)
echo "[2/6] Checking JAR files..."
JAR_SOURCE="/opt/edc/shared/jars"
if [ ! -d "$JAR_SOURCE" ]; then
    echo "ERROR: Shared JAR directory not found at $JAR_SOURCE"
    echo "Please copy EDC v0.10.1 JARs (controlplane.jar, dataplane.jar, identity-hub.jar, sts.jar) to $JAR_SOURCE"
    exit 1
fi
cp $JAR_SOURCE/*.jar $DEPLOY_DIR/jars/

# 3. Generate DID key pair
echo "[3/6] Generating DID key pair..."
openssl ecparam -name prime256v1 -genkey -noout -out $DEPLOY_DIR/credentials/private-key.pem
openssl ec -in $DEPLOY_DIR/credentials/private-key.pem -pubout -out $DEPLOY_DIR/credentials/public-key.pem

# 4. Copy config and docker-compose
echo "[4/6] Setting up configuration..."
cp docker-compose.yml $DEPLOY_DIR/
cp config/* $DEPLOY_DIR/config/ 2>/dev/null || true

# 5. Start containers
echo "[5/6] Starting containers..."
cd $DEPLOY_DIR
docker compose up -d

# 6. Wait and verify
echo "[6/6] Verifying deployment..."
sleep 10
if docker compose ps | grep -q "running"; then
    echo "SUCCESS: All containers are running"
    # Callback to update status
    if [ -n "$CALLBACK_URL" ]; then
        curl -s -X POST "$CALLBACK_URL/provider/updateStatus?id=$PROVIDER_ID&status=RUNNING" \
             -H "Content-Type: application/json" || echo "Warning: callback failed"
    fi
else
    echo "WARNING: Some containers may not be running. Check with: docker compose ps"
fi

echo ""
echo "=========================================="
echo "  Deployment complete!"
echo "  Management API: http://$(hostname -I | awk '{print $1}'):${controlplaneMgmtPort}"
echo "=========================================="
