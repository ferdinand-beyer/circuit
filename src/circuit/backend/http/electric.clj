(ns circuit.backend.http.electric
  (:require [hyperfiddle.electric-httpkit-adapter :as electric-httpkit]
            [hyperfiddle.electric-ring-adapter :as electric-ring]))

(def websocket-middleware
  {:name ::websocket
   :compile (fn [route-data _opts]
              (when-let [boot-fn (or (get-in route-data [:electric :boot-fn])
                                     (get-in route-data [:electric :entrypoint]))]
                (let [wrap-fn (case (:server route-data)
                                ;; httpkit is still not 100% ring compatible?
                                :httpkit electric-httpkit/wrap-electric-websocket
                                electric-ring/wrap-electric-websocket)]
                  #(wrap-fn % boot-fn))))})

(def reject-stale-client-middleware
  {:name ::reject-stale-client
   :compile (fn compile [route-data _opts]
              (when-let [user-version (get-in route-data [:electric :user-version])]
                #(electric-ring/wrap-reject-stale-client % {:hyperfiddle.electric/user-version user-version})))})
