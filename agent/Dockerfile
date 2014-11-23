FROM haskell:7.8
MAINTAINER David Flemström <david.flemstrom@gmail.com>

RUN cabal update
WORKDIR /magellan-agent
ADD . /magellan-agent
RUN cabal install

ENTRYPOINT ["/magellan-agent/dist/build/magellan-agent/magellan-agent"]
