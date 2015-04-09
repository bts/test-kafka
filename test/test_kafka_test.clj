(ns test-kafka-test
  (:use
    [clojure.test])
  (:require
    [clojure.test :refer :all]
    [test-kafka]
    [zookeeper :as zk])
  (:import
    [org.apache.kafka.clients.producer
     KafkaProducer
     ProducerRecord]
    [org.apache.kafka.common.serialization
     StringSerializer]
    [org.apache.kafka.clients.consumer
     KafkaConsumer]))

(defn producer
  "Creates a Kafka producer connecting to `port`."
  [port]
  (KafkaProducer. {"bootstrap.servers" (str "localhost:" port)}
                  (StringSerializer.)
                  (StringSerializer.)))

;;

(deftest with-zk-test
  (test-kafka/with-zk [port]
    (with-open [z (zk/connect (str "localhost:" port))]
      (is (not (nil? (:data (zk/data z "/zookeeper/quota"))))))))

(deftest with-broker-test
  (test-kafka/with-broker [kafka-port zk-port topic]
    (let [message "some message"
          producer (producer kafka-port)
          ;;consumer (consumer kafka-port)
          record (ProducerRecord. topic message)]
      (is (deref (.send producer record) 200 nil)))))
