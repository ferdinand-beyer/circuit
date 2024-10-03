(ns circuit.backend.http
  (:require [circuit.backend.http.electric :as electric]
            [muuntaja.core]
            [org.httpkit.server :as server]
            [reitit.ring :as ring]
            [reitit.ring.middleware.exception :as exception]
            [reitit.ring.middleware.parameters :as parameters]))

(defn exception-middleware
  {:init/inject []}
  []
  ;; TODO: Custom handlers
  (exception/create-exception-middleware))

(defn middleware
  {:init/inject [#'exception-middleware]}
  [exception-middleware]
  [exception-middleware
   parameters/parameters-middleware
   electric/reject-stale-client-middleware
   electric/websocket-middleware])

(defn router
  {:init/inject [#{::route-data} #'middleware]}
  [route-data middleware]
  (ring/router route-data {:data {:server :httpkit
                                  :middleware middleware}}))

(defn default-handler
  {:init/inject []}
  []
  (ring/routes (ring/redirect-trailing-slash-handler)
               ;; TODO: Custom handlers
               (ring/create-default-handler)))

(defn root-handler
  {:init/inject [#'router #'default-handler]}
  [router default-handler]
  (ring/ring-handler router default-handler))

(defn server
  {:init/inject [#'root-handler]}
  [handler]
  (let [opts {:ip "0.0.0.0"
              :port 8080
              :legacy-return-value? false
              :server-header "circuit"}]
    (server/run-server handler opts)))

(defn stop-server!
  {:init/stops #'server}
  [server]
  (server/server-stop! server {:timeout 500}))
