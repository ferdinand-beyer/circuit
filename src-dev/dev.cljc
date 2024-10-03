(ns dev
  #?(:clj (:require [dev.server]
                    [clojure.tools.namespace.repl :as namespace])))

#_{:clj-kondo/ignore [:unresolved-namespace]}
(comment
  (namespace/refresh)

  (dev.server/reset)
  (dev.server/stop)

  ;;
  )
