(ns circuit.backend.app
  (:require [circuit.ui.main :as main]
            [clojure.edn :as edn]
            [clojure.java.io :as io]
            [hiccup2.core :refer [html raw]]
            [hyperfiddle.electric :as e]
            [reitit.ring :as ring]
            [ring.util.http-response :as resp]))

(defn- js-modules [manifest-path asset-path]
  (when-let [manifest (io/resource manifest-path)]
    (->> (slurp manifest)
         (edn/read-string)
         (reduce (fn [r module]
                   (assoc r (:name module) (str asset-path "/" (:output-name module)))) {}))))

(defn- index-html [manifest-path asset-path
                   & {:keys [title]
                      :or {title "Circuit"}}]
  (html
   (raw "<!DOCTYPE html>")
   [:html
    [:head
     [:meta {:charset "utf-8"}]
     [:title title]
     [:meta {:name "viewport"
             :content "width=device-width, initial-scale=1"}]]
    [:body
     [:script {:type "text/javascript"
               :src (:main (js-modules manifest-path asset-path))}]]]))

(defn- handle-get [_request]
  (-> (index-html "public/assets/js/manifest.edn" "/assets/js")
      str
      (resp/ok)
      (resp/content-type "text/html")
      (resp/header "Cache-Control" "no-store")))

(defn- electric-entrypoint [request]
  (e/boot-server {} main/Main request))

(defn app-route-data
  {:init/inject []
   :init/tags #{:circuit.backend.http/route-data}}
  []
  ["/" {:electric {:entrypoint electric-entrypoint
                   ;; TODO: Get build-time version
                   :user-version nil}
        :get #'handle-get}])

(defn resource-route-data
  {:init/inject []
   :init/tags #{:circuit.backend.http/route-data}}
  []
  ["/assets/*" {:get (ring/create-resource-handler {:root "public/assets"
                                                    :index-files []
                                                    ;; TODO
                                                    #_#_:not-found-handler not-found-handler})}])
