(ns dev.server
  (:require [clojure.tools.namespace.repl :as namespace]
            [dev.server.build]
            [dev.server.state :as state]
            [init.core :as init]
            [init.discovery :as discovery]))

(defn- stop-system [system]
  (when system
    (init/stop system)
    (prn ::stopped (keys system))))

(defn build-config []
  (discovery/from-namespaces [(the-ns 'dev.server.build)]))

(defn start-build []
  (alter-var-root #'state/build-system
                  (fn [system]
                    (when (nil? system)
                      (init/start (build-config)))))
  (prn ::started (keys state/build-system))
  :ok)

(defn stop-build []
  (try
    (stop-system state/build-system)
    (finally
      (alter-var-root #'state/build-system (constantly nil))))
  :ok)

(defn dev-config []
  (-> (discovery/scan '[circuit.backend])
      ;; Start (but not stop) the build system.
      (assoc ::build-system {:name ::build-system
                             :start-fn (fn [_] (start-build))})))

(defn start []
  (let [config (dev-config)]
    (alter-var-root #'state/config (constantly config))
    (alter-var-root #'state/system (fn [system]
                                     (when (nil? system)
                                       (init/start config)))))
  (prn ::started (keys state/system))
  :ok)

(defn stop []
  (try
    (stop-system state/system)
    (finally
      (alter-var-root #'state/system (constantly nil))))
  :ok)

(defn reset []
  (stop)
  (namespace/refresh :after `start))

(defn shutdown []
  (doseq [f [stop stop-build shutdown-agents]]
    (try
      (f)
      (catch Throwable e
        (.printStackTrace e))))
  (prn ::shutdown)
  :ok)

(defn init []
  (.addShutdownHook (Runtime/getRuntime) (Thread. #'shutdown))
  (alter-var-root #'state/data assoc ::portal-launcher :auto)
  ;(open-portal)
  ;(add-tap #'portal/submit)
  (prn ::initialized)
  :ok)

(when (= 1 (alter-var-root #'state/load-count inc))
  (init))
