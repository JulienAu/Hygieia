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

RUN echo 'deb http://httpredir.debian.org/debian jessie-backports main' > /etc/apt/sources.list.d/jessie-backports.list

# Default to UTF-8 file.encoding
ENV LANG C.UTF-8

ENV JAVA_VERSION 8u66
ENV JAVA_DEBIAN_VERSION 8u66-b17-1~bpo8+1

# see https://bugs.debian.org/775775
# and https://github.com/docker-library/java/issues/19#issuecomment-70546872
ENV CA_CERTIFICATES_JAVA_VERSION 20140324

RUN set -x \
	&& apt-get update \
	&& apt-get install -y \
		openjdk-8-jdk="$JAVA_DEBIAN_VERSION" \
		ca-certificates-java="$CA_CERTIFICATES_JAVA_VERSION" \
	&& rm -rf /var/lib/apt/lists/*

# see CA_CERTIFICATES_JAVA_VERSION notes above
RUN /var/lib/dpkg/info/ca-certificates-java.postinst configure

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
