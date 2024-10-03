(ns dev.client
  (:require [circuit.ui.main :as main]
            [hyperfiddle.electric :as e]))

(def electric-entrypoint (e/boot-client {} main/Main nil))

(defonce reactor nil)

(defn ^:dev/after-load ^:export start! []
  (set! reactor (electric-entrypoint
                 #(js/console.log "Reactor success:" %)
                 #(js/console.error "Reactor failure:" %))))

(defn ^:dev/before-load stop! []
  (when reactor (reactor)) ; stop the reactor
  (set! reactor nil))
