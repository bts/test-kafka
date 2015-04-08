# test-kafka

An in-process Kafka and ZooKeeper runner for testing in Clojure. Adapted from [clj-kafka](https://github.com/pingles/clj-kafka)'s integration tests.

## Usage

Install with Leiningen:

```clojure
[test-kafka "0.1.0"]
```

Then:

```clojure
(require 'test-kafka)

(test-kafka/with-broker [kafka-port zk-port topic-name]
  (comment "interact with kafka"))

(test-kafka/with-zk [port]
  (comment "interact with zookeeper"))
```

## License

Copyright © 2014 Brian Schroeder

Distributed under the MIT License.

Copyright © 2013 Paul Ingles

Distributed under the Eclipse Public License, the same as Clojure.
