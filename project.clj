(defproject test-kafka "0.2.0-SNAPSHOT"
  :description "An in-process Kafka and ZooKeeper runner for testing in Clojure"
  :url "http://github.com/bts/test-kafka"
  :license {:name "MIT License"
            :url "http://opensource.org/licenses/MIT"}
  :dependencies [[org.apache.zookeeper/zookeeper "3.4.6"
                  :exclusions [com.sun.jmx/jmxri
                               com.sun.jdmk/jmxtools
                               javax.jms/jms
                               junit
                               log4j
                               org.slf4j/slf4j-log4j12]]
                 [org.apache.kafka/kafka_2.10 "0.8.2.1"]
                 [commons-io/commons-io "2.4"]
                 [com.101tec/zkclient "0.3"
                  :exclusions [log4j]]]
  :profiles {:dev {:dependencies [[org.clojure/clojure "1.7.0-alpha5"]
                                  [zookeeper-clj "0.9.3"
                                   :exclusions [log4j]]
                                  ;; send zookeeper-clj logging to slf4j:
                                  [org.slf4j/slf4j-api "1.7.12"]
                                  [org.slf4j/log4j-over-slf4j "1.7.12"]
                                  ;[org.slf4j/slf4j-simple "1.7.12"]
                                  [org.slf4j/slf4j-nop "1.7.12"]
                                  [org.apache.kafka/kafka-clients "0.8.2.1"]]
                   :exclusions [org.slf4j/slf4j-api]}})
