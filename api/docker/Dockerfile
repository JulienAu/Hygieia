FROM registry-os.prod.vdc.p.fti.net:443/hebex/ubuntu:14.04

MAINTAINER julien.audibert@orange.com

RUN apt-get update && apt-get install -y --no-install-recommends \
		ca-certificates \
		curl \
		wget \
	&& rm -rf /var/lib/apt/lists/*
	
# procps is very common in build systems, and is a reasonably small package
RUN apt-get update && apt-get install -y --no-install-recommends \
		bzr \
		git \
		mercurial \
		openssh-client \
		subversion \
		\
		procps \
	&& rm -rf /var/lib/apt/lists/*
	
RUN apt-get update && apt-get install -y unzip && rm -rf /var/lib/apt/lists/*

RUN echo 'deb http://archive.ubuntu.com/ubuntu trusty-backports main restricted universe multiverse' > /etc/apt/sources.list.d/trusty-backports.list

# Default to UTF-8 file.encoding
ENV LANG C.UTF-8

ENV JAVA_VERSION 8u45
ENV JAVA_UBUNTU_VERSION 8u45-b14-1



RUN set -x \
	&& apt-get update \
	&& apt-get install -y \
		openjdk-8-jdk="$JAVA_UBUNTU_VERSION"


ENV SPRING_DATA_MONGODB_DATABASE=dashboard
ENV SPRING_DATA_MONGODB_HOST=10.0.1.1
ENV SPRING_DATA_MONGODB_PORT=9999
ENV SPRING_DATA_MONGODB_USERNAME=db
ENV SPRING_DATA_MONGODB_PASSWORD=dbpass


RUN \
  mkdir /hygieia

COPY hygieia /hygieia
COPY properties-builder.sh /hygieia/


WORKDIR /hygieia

VOLUME ["/hygieia/logs"]


EXPOSE 8080
CMD ./properties-builder.sh &&\
  java -Djava.security.egd=file:/dev/./urandom -jar api.jar --spring.config.location=/hygieia/dashboard.properties
