# Nginx proxy config for ${providerName}
# Add this to your main nginx.conf server block

# ${providerName} - DID document (did:web resolution)
location = /${providerName}/did.json {
    alias /opt/edc/${providerName}/did.json;
    default_type application/did+json;
    add_header Cache-Control "public, max-age=300";
}

# ${providerName} - Controlplane Management API
location /${providerName}/management/ {
    proxy_pass http://${deployHost}:${controlplaneMgmtPort}/management/;
    proxy_set_header Host $host;
    proxy_set_header X-Real-IP $remote_addr;
}

# ${providerName} - Controlplane Protocol API
location /${providerName}/protocol/ {
    proxy_pass http://${deployHost}:${controlplaneProtocolPort}/protocol/;
    proxy_set_header Host $host;
    proxy_set_header X-Real-IP $remote_addr;
}

# ${providerName} - Dataplane Public API
location /${providerName}/public/ {
    proxy_pass http://${deployHost}:${dataplanePublicPort}/public/;
    proxy_set_header Host $host;
    proxy_set_header X-Real-IP $remote_addr;
}
