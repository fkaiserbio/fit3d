#!/bin/bash
docker run --name fit3d-web-mongodb -d mongo
docker run --name fit3d-web -dt \
    -p 32772:8080 \
    --volume=/srv/pdb:/srv/pdb \
    --restart=always \
    --link fit3d-web-mongodb:fit3d-web-mongodb \
    bigm/fit3d-web
