(defproject dredd "0.0.1-SNAPSHOT"
  :description "Simple Online Automated Judge System in Clojure"
  :dependencies [[org.clojure/clojure "1.2.0"]
                 [org.clojure/clojure-contrib "1.2.0"]
                 [clj-ldap "0.0.2"]
                 [org.neo4j/neo4j "1.2"]
                 [compojure "0.6.0-RC4"]
                 [ring/ring-jetty-adapter "0.3.6"]
                 [hiccup "0.3.4"]
                 [clj-time "0.3.0-SNAPSHOT"]
                 [borneo "0.1.0"]
                 [incanter "1.2.3"]]
  :dev-dependencies [[swank-clojure "1.3.0-SNAPSHOT"]]
  :warn-on-reflection true
  :main dredd.core
  :jvm-opts ["-Dfile.encoding=utf-8"
             "-Dswank.encoding=utf-8"
             "-Xms256m"
             "-Xmx512m"
             "-XX:MaxPermSize=256m"])
