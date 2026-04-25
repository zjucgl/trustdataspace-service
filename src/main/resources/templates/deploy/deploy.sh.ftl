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
ENABLED_EXTENSIONS="${enabledDataplaneExtensions}"
REQUIRED_CORE_JARS=("controlplane.jar" "dataplane.jar" "identity-hub.jar" "sts.jar")
if [ ! -d "$JAR_SOURCE" ]; then
    echo "ERROR: Shared JAR directory not found at $JAR_SOURCE"
    echo "Please copy EDC v0.10.1 core JARs to $JAR_SOURCE"
    exit 1
fi
for jar in "${REQUIRED_CORE_JARS[@]}"; do
    if [ ! -f "$JAR_SOURCE/$jar" ]; then
        echo "ERROR: missing core jar $jar in $JAR_SOURCE"
        exit 1
    fi
done

mkdir -p $DEPLOY_DIR/jars/extensions
cp "$JAR_SOURCE"/controlplane.jar "$JAR_SOURCE"/dataplane.jar \
   "$JAR_SOURCE"/identity-hub.jar "$JAR_SOURCE"/sts.jar $DEPLOY_DIR/jars/

EXT_SOURCE="$JAR_SOURCE/extensions"
IFS=',' read -ra EXT_LIST <<< "$ENABLED_EXTENSIONS"
for ext in "${EXT_LIST[@]}"; do
    case "$ext" in
        http) ;;
        s3)   EXT_JAR="data-plane-aws-s3.jar" ;;
        jdbc) EXT_JAR="data-plane-jdbc.jar" ;;
        sftp) EXT_JAR="data-plane-sftp.jar" ;;
        *)    echo "WARNING: unknown extension '$ext', skipped"; continue ;;
    esac
    if [ -n "$EXT_JAR" ]; then
        if [ ! -f "$EXT_SOURCE/$EXT_JAR" ]; then
            echo "ERROR: missing extension jar $EXT_JAR for '$ext' in $EXT_SOURCE"
            exit 1
        fi
        cp "$EXT_SOURCE/$EXT_JAR" $DEPLOY_DIR/jars/extensions/
        echo "  + extension: $ext -> $EXT_JAR"
        unset EXT_JAR
    fi
done

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
