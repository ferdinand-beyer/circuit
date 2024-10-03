(ns circuit.ui.main
  (:require [hyperfiddle.electric :as e]
            [hyperfiddle.electric-dom2 :as dom]))

(e/defn Main [_request]
  (e/client
   (binding [dom/node js/document.body]
     (dom/h1 (dom/text "Hello, World!"))
     (dom/p (dom/text "Clojure Version: " (e/server (clojure-version)))))))
