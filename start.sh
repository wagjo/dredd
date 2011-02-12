#!/bin/sh

java -cp ./dredd-1.0.0-SNAPSHOT-standalone.jar clojure.main -i @/dredd/core.clj -e "(dredd.core/start-and-wait)"
