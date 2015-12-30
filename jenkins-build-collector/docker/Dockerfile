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
		maven \
		\
		procps \
	&& rm -rf /var/lib/apt/lists/*
	
RUN apt-get update && apt-get install -y unzip && rm -rf /var/lib/apt/lists/*

#RUN echo 'deb http://archive.ubuntu.com/ubuntu trusty-backports main restricted universe multiverse' > /etc/apt/sources.list.d/trusty-backports.list

# Default to UTF-8 file.encoding
ENV LANG C.UTF-8
ENV JAVA_VERSION 8 
ENV JAVA_HOME /usr/lib/jvm/java-8-oracle

# and https://github.com/docker-library/java/issues/19#issuecomment-70546872
#ENV CA_CERTIFICATES_JAVA_VERSION 20130815

RUN echo 'deb http://ppa.launchpad.net/webupd8team/java/ubuntu trusty main' >> /etc/apt/sources.list && \
    echo 'deb-src http://ppa.launchpad.net/webupd8team/java/ubuntu trusty main' >> /etc/apt/sources.list && \
    apt-key adv --keyserver keyserver.ubuntu.com --recv-keys C2518248EEA14886 && \
    apt-get update && \
    echo oracle-java${JAVA_VERSION}-installer shared/accepted-oracle-license-v1-1 select true | sudo /usr/bin/debconf-set-selections && \
    apt-get install -y --force-yes --no-install-recommends oracle-java${JAVA_VERSION}-installer oracle-java${JAVA_VERSION}-set-default



ENV SPRING_DATA_MONGODB_DATABASE=dashboard
ENV SPRING_DATA_MONGODB_HOST=10.0.1.1
ENV SPRING_DATA_MONGODB_PORT=9999
ENV SPRING_DATA_MONGODB_USERNAME=db
ENV SPRING_DATA_MONGODB_PASSWORD=dbpass

RUN \
  mkdir /hygieia

COPY properties-builder.sh /hygieia/


WORKDIR /hygieia

VOLUME ["/hygieia/Jenkins_logs"]

ADD pom.xml .

RUN mvn install -Pdeploy

CMD ./properties-builder.sh &&\
java -jar /hygieia/target/dependency/jenkins-build-collector-1.5.0.jar --spring.config.location=/hygieia/application.properties