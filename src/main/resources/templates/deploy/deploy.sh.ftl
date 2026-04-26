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

# 2. Download JARs via OSS pre-signed URLs (valid 24h from generation time)
echo "[2/6] Fetching JAR files..."
EDC_VERSION="${edcVersion}"
JAR_CACHE_DIR="${EDC_JAR_CACHE:-/opt/edc/shared/jars/$EDC_VERSION}"

mkdir -p "$JAR_CACHE_DIR/extensions"
mkdir -p "$DEPLOY_DIR/jars/extensions"

fetch_jar() {
    local rel_path="$1"
    local url="$2"
    local cache_path="$JAR_CACHE_DIR/$rel_path"
    if [ -z "$url" ]; then
        echo "ERROR: empty URL for $rel_path"
        exit 1
    fi
    if [ ! -s "$cache_path" ]; then
        echo "  -> $rel_path"
        if ! wget -q -O "$cache_path.tmp" "$url"; then
            echo "ERROR: failed to download $rel_path (URL may have expired; re-download deploy.sh)"
            rm -f "$cache_path.tmp"
            exit 1
        fi
        if [ ! -s "$cache_path.tmp" ]; then
            echo "ERROR: $rel_path downloaded empty (URL may have expired)"
            rm -f "$cache_path.tmp"
            exit 1
        fi
        mv "$cache_path.tmp" "$cache_path"
    fi
    cp "$cache_path" "$DEPLOY_DIR/jars/$rel_path"
}

fetch_jar "controlplane.jar" "${controlplaneJarUrl}"
fetch_jar "dataplane.jar"    "${dataplaneJarUrl}"
fetch_jar "identity-hub.jar" "${identityHubJarUrl}"
fetch_jar "sts.jar"          "${stsJarUrl}"

# @if:s3
fetch_jar "extensions/data-plane-aws-s3.jar" "${s3ExtensionJarUrl}"
echo "  + extension: s3"
# @endif
# @if:jdbc
fetch_jar "extensions/data-plane-jdbc.jar" "${jdbcExtensionJarUrl}"
echo "  + extension: jdbc"
# @endif
# @if:sftp
fetch_jar "extensions/data-plane-sftp.jar" "${sftpExtensionJarUrl}"
echo "  + extension: sftp"
# @endif

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
