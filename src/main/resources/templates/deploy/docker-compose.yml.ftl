# Tractus-X EDC Connector for ${providerName}
# Generated at ${generatedAt}
# Image: ${tractusxRuntimeImage}
version: '3.8'

services:
  ${providerName}-edc:
    image: ${tractusxRuntimeImage}
    container_name: ${providerName}-edc
    restart: unless-stopped
    ports:
      - "${controlplaneMgmtPort}:${controlplaneMgmtPort}"
      - "${controlplaneProtocolPort}:${controlplaneProtocolPort}"
      - "${controlplanePublicPort}:${controlplanePublicPort}"
      - "${dataplanePublicPort}:${dataplanePublicPort}"
      - "${identityHubPort}:${identityHubPort}"
    environment:
      EDC_PARTICIPANT_ID: "${participantDid}"
      EDC_IAM_ISSUER_ID: "${participantDid}"
      EDC_PARTICIPANT_CONTEXT_ID: "${participantContextId}"
      TRACTUSX_EDC_PARTICIPANT_BPN: "${participantBpn}"
      WEB_HTTP_PORT: "${controlplaneMgmtPort}"
      WEB_HTTP_PATH: "/api"
      WEB_HTTP_MANAGEMENT_PORT: "${controlplaneMgmtPort}"
      WEB_HTTP_MANAGEMENT_PATH: "/management"
      WEB_HTTP_MANAGEMENT_AUTH_TYPE: "tokenbased"
      WEB_HTTP_MANAGEMENT_AUTH_KEY: "${managementAuthKey}"
      WEB_HTTP_PROTOCOL_PORT: "${controlplaneProtocolPort}"
      WEB_HTTP_PROTOCOL_PATH: "/api/v1/dsp"
      WEB_HTTP_PUBLIC_PORT: "${dataplanePublicPort}"
      WEB_HTTP_PUBLIC_PATH: "/api/public"
      WEB_HTTP_CONTROL_PORT: "${controlplanePublicPort}"
      WEB_HTTP_CONTROL_PATH: "/control"
      WEB_HTTP_CATALOG_PORT: "${identityHubPort}"
      WEB_HTTP_CATALOG_PATH: "/catalog"
      WEB_HTTP_CATALOG_AUTH_TYPE: "tokenbased"
      WEB_HTTP_CATALOG_AUTH_KEY: "${managementAuthKey}"
      EDC_DSP_CALLBACK_ADDRESS: "http://${deployHost}:${controlplaneProtocolPort}/api/v1/dsp"
      EDC_OAUTH_PROVIDER_AUDIENCE: "idsc:IDS_CONNECTORS_ALL"
      EDC_OAUTH_ENDPOINT_AUDIENCE: "http://${deployHost}:${controlplaneProtocolPort}/api/v1/dsp"
      EDC_IAM_STS_OAUTH_TOKEN_URL: "https://stub-sts.local/token"
      EDC_IAM_STS_OAUTH_CLIENT_ID: "stub-client"
      EDC_IAM_STS_OAUTH_CLIENT_SECRET_ALIAS: "sts-client-secret"
      TX_EDC_IAM_STS_DIM_URL: "https://stub-dim.local"
      TX_EDC_VAULT_SECRETS: "sts-client-secret:stub-secret-value"
      TX_EDC_DPF_CONSUMER_PROXY_AUTH_APIKEY: "${managementAuthKey}"
# @if:s3
      EDC_DATAPLANE_AWS_ENDPOINT_OVERRIDE: ""
      EDC_DATAPLANE_AWS_ACCESS_KEY_ID: ""
      EDC_DATAPLANE_AWS_SECRET_ACCESS_KEY: ""
# @endif
    healthcheck:
      test: ["CMD-SHELL", "wget -qO- http://localhost:${controlplaneMgmtPort}/api/check/health || exit 1"]
      interval: 10s
      timeout: 5s
      retries: 30
      start_period: 30s
