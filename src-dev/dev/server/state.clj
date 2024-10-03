(ns dev.server.state
  (:require [clojure.tools.namespace.repl :as namespace]))

(namespace/disable-reload!)

(defonce load-count 0)

(defonce data {})

(defonce config nil)
(defonce system nil)

(defonce build-system nil)
