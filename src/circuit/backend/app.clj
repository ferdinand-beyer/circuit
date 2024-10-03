(ns circuit.backend.app
  (:require [circuit.ui.main :as main]
            [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [hiccup2.core :refer [html raw]]
            [hyperfiddle.electric :as e]
            [reitit.ring :as ring]
            [ring.util.http-response :as resp]))

(defn- js-modules [manifest-path]
  (when-let [manifest (io/resource manifest-path)]
    (let [dir-url (-> manifest-path
                      (str/split #"\/")
                      vec
                      pop
                      (conj "")
                      next
                      (->> (cons "") (str/join "/")))]
      (->> (slurp manifest)
           (edn/read-string)
           (reduce (fn [r module]
                     (assoc r (:name module) (str dir-url (:output-name module)))) {})))))

(defn- index-html [manifest-path & {:keys [title]
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
               :src (:main (js-modules manifest-path))}]]]))

(defn- handle-get [_request]
  (-> (index-html "public/assets/js/manifest.edn")
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
                                                    #_#_:not-found-handler not-found-handler})}])
