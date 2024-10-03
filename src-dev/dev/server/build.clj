(ns dev.server.build
  (:require [shadow.cljs.devtools.api :as shadow]
            [shadow.cljs.devtools.server :as shadow-server]))

(defn shadow-server
  {:init/inject []
   :init/stop-fn (fn [_] (shadow-server/stop!))}
  []
  (shadow-server/start!))

(defn shadow-watch
  {:init/inject [#'shadow-server]}
  [_]
  (shadow/watch :dev))
