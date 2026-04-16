# Nginx proxy config for ${providerName}
# Add this to your main nginx.conf server block

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
