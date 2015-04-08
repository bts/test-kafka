(ns test-kafka-test
  (:use
   [clojure.test])
  (:require
   [clojure.test :refer :all]
   [test-kafka]
   [clj-kafka.producer :as producer]
   [clj-kafka.consumer.simple :as consumer]
   [zookeeper :as zk]))

(deftest with-zk-test
  (test-kafka/with-zk [port]
    (with-open [z (zk/connect (str "localhost:" port))]
      (is (not (nil? (:data (zk/data z "/zookeeper/quota"))))))))

(deftest with-broker-test
  (test-kafka/with-broker [kafka-port zk-port topic]
    (let [payload (.getBytes "test message")
          producer-config {"metadata.broker.list" (str "localhost:" kafka-port)}
          producer (producer/producer producer-config)
          consumer (consumer/consumer "localhost" kafka-port "test-consumer")]
      (producer/send-message producer (producer/message topic payload))
      (is (number? (consumer/latest-topic-offset consumer topic 0))))))
