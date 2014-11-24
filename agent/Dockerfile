FROM haskell:7.8
MAINTAINER David Flemström <david.flemstrom@gmail.com>

ADD https://get.docker.com/builds/Linux/x86_64/docker-latest /usr/bin/docker
RUN chmod +x /usr/bin/docker
RUN cabal update
ADD . /source
RUN cd /source && cabal install --bindir=/

ENTRYPOINT ["/magellan-agent"]
