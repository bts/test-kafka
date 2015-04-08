(ns test-kafka
  (:require
   [clojure.java.io :as io])
  (:import
   [org.apache.zookeeper.server
    ZooKeeperServer
    NIOServerCnxnFactory]
   [kafka.server
    KafkaConfig
    KafkaServer]
   [kafka.admin
    AdminUtils]
   [kafka.utils
    Time]
   [org.I0Itec.zkclient
    ZkClient]
   [org.I0Itec.zkclient.serialize
    ZkSerializer]
   [org.apache.commons.io
    FileUtils]
   [java.util
    Properties]
   [scala
    Option]))

(defn as-properties
  "Returns a Properties instance populated from the provided map."
  [m]
  (let [props (Properties.)]
    (doseq [[n v] m] (.setProperty props n v))
    props))

(defn tmp-dir
  "Returns a java.io.File for a temp directory with named by optional
  dir-names."
  [& dir-names]
  (let [tmp-root (System/getProperty "java.io.tmpdir")
        directory (apply io/file tmp-root "test-kafka" dir-names)
        path (.getPath directory)]
    (io/file path)))

(defn zookeeper
  "Starts an in-process ZooKeeper server."
  [port]
  (let [snapshot-dir (tmp-dir "zookeeper-snapshot")
        log-dir (tmp-dir "zookeeper-log")
        tick-time 500
        server (ZooKeeperServer. snapshot-dir log-dir tick-time)
        max-conns 10
        conn-factory (NIOServerCnxnFactory/createFactory port max-conns)]
    (doto conn-factory
      (.startup server))))

(def system-time
  (proxy [Time] []
    (milliseconds [] (System/currentTimeMillis))
    (nanoseconds [] (System/nanoTime))
    (sleep [ms] (Thread/sleep ms))))

(defn broker
  [kafka-port zookeeper-port]
  (let [log-path (.getAbsolutePath (tmp-dir "kafka-log"))
        config-map {"broker.id" "0"
                    "port" (str kafka-port)
                    "host.name" "localhost"
                    "zookeeper.connect" (str "127.0.0.1:" zookeeper-port)
                    "log.flush.interval.messages" "1"
                    "auto.create.topics.enable" "true"
                    "log.dir" log-path}
        config (KafkaConfig. (as-properties config-map))]
    (KafkaServer. config system-time)))

(defn create-topic
  [zk-client name & {:keys [partitions replicas]
                     :or   {partitions 1
                            replicas 1}}]
  (AdminUtils/createTopic zk-client name partitions replicas (Properties.)))

(defn option-get
  "Gets a value or nil from a Scala `Option`."
  [^Option option]
  (when-not (.isEmpty option)
    (.get option)))

(defn wait-until-initialized
  [^KafkaServer kafka-server topic]
  (let [apis (.apis kafka-server)
        cache (.metadataCache apis)]
    (while (not (option-get (.getPartitionInfo cache topic 0)))
      (Thread/sleep 100))))

(def string-serializer
  (proxy [ZkSerializer] []
    (serialize [data]
      (.getBytes data "UTF-8"))
    (deserialize [bytes]
      (when bytes
        (String. bytes "UTF-8")))))

;; public api

(defmacro with-zk
  "Sets up and tears down an in-process ZK server for testing."
  [[port'] & body]
  (let [port 2182]
    `(do
       (FileUtils/deleteDirectory (tmp-dir))
       (let [zk# (zookeeper ~port)
             ~port' ~port]
         (try
           ~@body
           (finally
             (do
               (.shutdown zk#)
               (FileUtils/deleteDirectory (tmp-dir)))))))))

(defmacro with-broker
  "Sets up and tears down an in-process Kafka broker for testing."
  [[kafka-port' zk-port' topic-name'] & body]
  (let [zk-port 2182
        kafka-port 9999
        topic "test"]
    `(do
       (FileUtils/deleteDirectory (tmp-dir))
       (let [zk# (zookeeper ~zk-port)
             kafka# (broker ~kafka-port ~zk-port)
             ~kafka-port' ~kafka-port
             ~zk-port' ~zk-port
             ~topic-name' ~topic]
         (try
           (.startup kafka#)
           (let [zk-client# (ZkClient. (str "127.0.0.1:" ~zk-port)
                                       500
                                       500
                                       string-serializer)]
             (create-topic zk-client# ~topic)
             (wait-until-initialized kafka# ~topic))
           ~@body
           (finally
             (do
               (.shutdown kafka#)
               (.awaitShutdown kafka#)
               (.shutdown zk#)
               (FileUtils/deleteDirectory (tmp-dir)))))))))
