#!/bin/bash

/usr/local/bin/lein uberjar

/usr/bin/docker build -t edwardstx/auth .

/usr/bin/docker run -d --restart always -p 127.0.0.1:5002:5002 -v /etc/service:/etc/service --name edwardstx_auth edwardstx/auth
