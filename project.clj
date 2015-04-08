(defproject test-kafka "0.2.0-SNAPSHOT"
  :description "Minimal in-process Kafka and ZooKeeper runner for testing"
  :url "http://github.com/bts/test-kafka"
  :license {:name "MIT License"
            :url "http://opensource.org/licenses/MIT"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.apache.zookeeper/zookeeper "3.4.6"
                  :exclusions [com.sun.jmx/jmxri
                               com.sun.jdmk/jmxtools
                               javax.jms/jms
                               junit
                               log4j
                               org.slf4j/slf4j-log4j12]]
                 [org.apache.kafka/kafka_2.10 "0.8.2.1"]
                 [commons-io/commons-io "2.4"]
                 [com.101tec/zkclient "0.4"
                  :exclusions [log4j]]]
  :profiles {:dev {:dependencies [[zookeeper-clj "0.9.3"]
                                  [clj-kafka "0.2.8-0.8.1.1"
                                   :exclusions [log4j
                                                org.slf4j/slf4j-simple]]
                                  ;; send kafka logging to slf4j:
                                  [org.slf4j/log4j-over-slf4j "1.7.5"]
                                  [org.slf4j/slf4j-simple "1.6.4"]]}})
