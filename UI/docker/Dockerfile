FROM registry-os.prod.vdc.p.fti.net:5000/hebex/ubuntu:14.04

MAINTAINER julien.audibert@orange.com

RUN apt-key adv --keyserver hkp://pgp.mit.edu:80 --recv-keys 573BFD6B3D8FBC641079A6ABABF5BD827BD9BF62
RUN echo "deb http://nginx.org/packages/mainline/ubuntu/ trusty nginx" >> /etc/apt/sources.list

ENV NGINX_VERSION 1.9.8-1~trusty

RUN apt-get update && \
    apt-get install -y ca-certificates nginx=${NGINX_VERSION} && \
    rm -rf /var/lib/apt/lists/*

# forward request and error logs to docker log collector
RUN ln -sf /dev/stdout /var/log/nginx/access.log
RUN ln -sf /dev/stderr /var/log/nginx/error.log

COPY default.conf /etc/nginx/conf.d/default.conf.templ
COPY conf-builder.sh /usr/bin/conf-builder.sh
COPY html /usr/share/nginx/html


VOLUME ["/var/cache/nginx"]

EXPOSE 80 443

CMD conf-builder.sh &&\
  nginx -g "daemon off;"
