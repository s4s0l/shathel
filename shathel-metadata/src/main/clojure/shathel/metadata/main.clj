(ns shathelmetadata.main
  (:gen-class)
  (:use shathelmetadata.ssl)
  ;(:use org.httpkit.server)
  (:require [shathelmetadata.ssl :as ssl])
  ;  (:require [clojure.data.json :as json])
  ;(:require [dockerclient.core :as docker])
  )










;(defn async-handler [ring-request]
;  ;; unified API for WebSocket and HTTP long polling/streaming
;  (with-channel ring-request channel                        ; get the channel
;                (if (websocket? channel)                    ; if you want to distinguish them
;                  (on-receive channel (fn [data]            ; two way communication
;                                        (send! channel data)))
;                  (send! channel {:status  200
;                                  :headers {"Content-Type" "text/plain"}
;                                  :body    "Long polling?"}))))
;

;(run-server async-handler {:port 8080})
; Ring server
;(def docker-client.core/env  {:docker-host "tcp://111.111.111.99:2376"
;                              :docker-cert-path "/home/sasol/Projects/shathel-swarm/build/playground/tmp/itg/settings/machines/playground-itg-manager-1"
;                              :docker-tls-verify "1"})
(defn -main []
  (println "Started")
  (let [
        {:keys [status headers body error] :as resp} @(http-kit/get "https://111.111.111.99:2376/v1.26/info" {:sslengine nil})
        ]
    (if error
      (println "Failed, exception: " error)
      (println "HTTP GET success: " status))))




(def sslEngine (ssl/sslEngine "/home/sasol/Projects/shathel-swarm/build/playground/tmp/itg/settings/machines/playground-itg-manager-1"))




(let [
      {:keys [status headers body error] :as resp} @(http-kit/get "https://111.111.111.99:2376/v1.26/info" {:sslengine sslEngine})
      ]
  (if error
    (println "Failed, exception: " error)
    (println "HTTP GET success: " status)))