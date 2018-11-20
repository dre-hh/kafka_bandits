FROM anapsix/alpine-java
ENV SBT_VERSION=1.2.3

RUN \
  apk add --no-cache bash curl bc && \
  curl -fsL https://github.com/sbt/sbt/releases/download/v$SBT_VERSION/sbt-$SBT_VERSION.tgz | tar xfz - -C /usr/local && \
  $(mv /usr/local/sbt-launcher-packaging-$SBT_VERSION /usr/local/sbt || true) && \
  ln -s /usr/local/sbt/bin/* /usr/local/bin/ && \
  sbt sbtVersion

# Get the dependencies
RUN mkdir /bandits
WORKDIR /bandits
COPY build.sbt /bandits
RUN sbt update
