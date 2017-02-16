(ns shathel.metadata.main
  (:gen-class)
  (:require [shathel.metadata.client :as client])
  (:require [clojure.core.async
             :as a
             :refer [>! <! >!! <!! go chan buffer close! thread pub sub go-loop
                     alts! alts!! timeout]])

  )

;(def env {:docker-host       "https://111.111.111.99:2376"
;          :docker-cert-path  "/home/sasol/Projects/shathel-swarm/build/playground/tmp/itg/settings/machines/playground-itg-manager-1"
;          :docker-tls-verify "1"})

(def env {:docker-host       "unix:///var/run/docker.sock"
          :docker-cert-path  nil
          :docker-tls-verify "1"})

(let [events (client/dockerEventPublisher env)
      subssample (chan)]
  (client/subscribeToAll events  subssample)
  ;(sub events  "network" subssample)
  (go-loop [x (<! subssample)]
    (println x)
    (recur (<! subssample))
    )
  )

