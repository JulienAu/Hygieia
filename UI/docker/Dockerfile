
FROM docker.io/nginx:latest

MAINTAINER julien.audibert@orange.com


COPY default.conf /etc/nginx/conf.d/default.conf.templ
COPY conf-builder.sh /usr/bin/conf-builder.sh
COPY html /usr/share/nginx/html


EXPOSE 80 443

CMD conf-builder.sh &&\
  nginx -g "daemon off;"
