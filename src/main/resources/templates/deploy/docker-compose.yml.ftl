# EDC Connector for ${providerName}
# Generated at ${generatedAt}
version: '3.8'

services:
  ${providerName}-controlplane:
    image: eclipse-temurin:21-jre-alpine
    container_name: ${providerName}-controlplane
    volumes:
      - ./jars/controlplane.jar:/app/controlplane.jar
      - ./config:/app/config
    ports:
      - "${controlplaneMgmtPort}:${controlplaneMgmtPort}"
      - "${controlplaneProtocolPort}:${controlplaneProtocolPort}"
      - "${controlplanePublicPort}:${controlplanePublicPort}"
    environment:
      - EDC_FS_CONFIG=/app/config/controlplane.properties
    entrypoint: ["java", "-jar", "/app/controlplane.jar"]
    restart: unless-stopped

  ${providerName}-dataplane:
    image: eclipse-temurin:21-jre-alpine
    container_name: ${providerName}-dataplane
    volumes:
      - ./jars/dataplane.jar:/app/dataplane.jar
      - ./jars/extensions:/app/extensions
      - ./config:/app/config
# @if:s3
      - ./jars/extensions/data-plane-aws-s3.jar:/app/extensions/data-plane-aws-s3.jar
# @endif
# @if:jdbc
      - ./jars/extensions/data-plane-jdbc.jar:/app/extensions/data-plane-jdbc.jar
# @endif
# @if:sftp
      - ./jars/extensions/data-plane-sftp.jar:/app/extensions/data-plane-sftp.jar
# @endif
    ports:
      - "${dataplanePublicPort}:${dataplanePublicPort}"
    environment:
      - EDC_FS_CONFIG=/app/config/dataplane.properties
      - EDC_DATAPLANE_EXTENSIONS=${enabledDataplaneExtensions}
    entrypoint: ["java", "-cp", "/app/dataplane.jar:/app/extensions/*", "-jar", "/app/dataplane.jar"]
    restart: unless-stopped

  ${providerName}-identity-hub:
    image: eclipse-temurin:21-jre-alpine
    container_name: ${providerName}-identity-hub
    volumes:
      - ./jars/identity-hub.jar:/app/identity-hub.jar
      - ./config:/app/config
    ports:
      - "${identityHubPort}:${identityHubPort}"
    environment:
      - EDC_FS_CONFIG=/app/config/identity-hub.properties
    entrypoint: ["java", "-jar", "/app/identity-hub.jar"]
    restart: unless-stopped

  ${providerName}-sts:
    image: eclipse-temurin:21-jre-alpine
    container_name: ${providerName}-sts
    volumes:
      - ./jars/sts.jar:/app/sts.jar
      - ./config:/app/config
    ports:
      - "${stsPort}:${stsPort}"
    environment:
      - EDC_FS_CONFIG=/app/config/sts.properties
    entrypoint: ["java", "-jar", "/app/sts.jar"]
    restart: unless-stopped
