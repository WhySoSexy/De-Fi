#!/usr/bin/env bash

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"

docker run -d --rm -p 8888:8888 \
      -v "$SCRIPT_DIR../src/main/resources/application.yaml:/config/application.yaml" \
      -e SPRING_PROFILES_ACTIVE=native \
      hyness/spring-cloud-config-server