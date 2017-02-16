(ns shathel.metadata.client
  (:gen-class)
  (:require [shathel.metadata.log :as log])
  (:require [qbits.jet.client.http :as client])
  (:require [shathel.metadata.ssl :as ssl])
  (:require [org.httpkit.client :as http-kit])
  (:require [clojure.core.async
             :as a
             :refer [>! <! >!! <!! go chan buffer close! thread go-loop pub sub
                     alts! alts!! timeout]])

  )



(defn callHttpClientForStream [cl baseUrl api]
  (client/get cl (str baseUrl "/v1.26/" api) {:fold-chunked-response? false :as :json}))

(defn callHttpClientForObject [cl baseUrl api]
  (-> (client/get cl (str baseUrl "/v1.26/" api) {:fold-chunked-response? true :as :json})
      <!! :body <!!))

(def supportedTypes ["container", "image", "volume", "network", "daemon"])


(defn subscribeToAll [exentsPublisher subsChannel]
  (doseq [t supportedTypes]
    (sub exentsPublisher  t subsChannel)))

(defn dockerEventPublisher [{:keys [docker-host, docker-cert-path]}]
  (let [cl (client/client {:ssl-context-factory
                           (if docker-cert-path (ssl/sslContextFactory docker-cert-path))})
        url docker-host
        events-channel (chan)
        publising-channel (chan)
        publication (pub publising-channel #(:Type %))
        ]

    (go-loop []
      (try
        (let [stream (<!! (callHttpClientForStream cl url "events"))]
          (log/info "Connected to events endpoint awaiting events...")
          (loop [chunk (<!! (:body stream))]
            (if chunk
              (do
                ;(println "[" chunk "]")
                (log/info "Got Event" (:Type chunk) ":" (:Action chunk) " on " (:id chunk))
                (>! events-channel chunk)
                (recur (<!! (:body stream))))))
          (log/info "End of stream")
          )

        (catch Exception e (log/error e "Something got wrong in main docker client loop, will restart."))
        (finally (log/info "Reconnecting to docker") (Thread/sleep 5000)))
      (recur)
      )
    (go-loop [event (<!! events-channel)]
      (if event
        (let
          [container-list (callHttpClientForObject cl url "containers/json")
           container-details (map #(callHttpClientForObject cl url (str "containers/" (:Id %) "/json")) container-list)
           ]
          (>! publising-channel {:containers container-details, :Type (:Type event) :Event event})
          (recur (<!! events-channel)))))
    publication))



