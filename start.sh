#!/bin/sh

java -cp ./dredd-0.0.1-SNAPSHOT-standalone.jar clojure.main -i @/dredd/server.clj -e "(dredd.server/start-and-wait)"
