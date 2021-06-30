#/bin/sh

curl -s \
  --url 'http://localhost:5002/idp/connect/token' \
  -H 'content-type: application/x-www-form-urlencoded' \
  -d grant_type=password \
  -d username=nelson \
  -d password=password \
  -d scope='openid profile email' \
  -d 'client_id=nuxeo-client' \
  -d client_secret=secret | jq -r '.access_token'
