(ns hello-world.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs.nodejs :as nodejs]
            [cljs.core.async :refer [put! chan <!]]))

(nodejs/enable-util-print!)

(def util (nodejs/require "util"))
(def prompt (nodejs/require "prompt"))
(def serial-port (.-SerialPort (nodejs/require "serialport")))

(def prompt-props
  #js {:properties 
       #js {:baudrate #js { :description "baud rate" }
            :port #js { :description "serial port name" :required true } } })

(defn- on-error
  [err]
  (println err)
  (print "> ") 
  1)

(defn- print-data 
  [c]
    (go (while true
          (let [data (<! c)]
            (print (str data))
            (print " ")))))

(defn- write-cmd 
  [c port]
  (go (while true
        (let [cmd (<! c)]
          (.write 
            port 
            cmd 
            (fn [err result] 
              (when (not (nil? err)) (on-error err))))))))

(defn- process-input 
  [in-chan cmd-chan data-chan port]
  (go (while true
        (let [in (<! in-chan)]
          (cond
            (= "quit\r\n" in) (do (print "Bye!") (.exit js/process))

            (= "close\r\n" in) (.close port)

            (= "open\r\n" in) (.open port 
                                (fn [err] 
                                  (if (not (nil? err)) 
                                    (on-error err)
                                    (do 
                                      (print "> ") 
                                      (.on 
                                        port 
                                        "data" 
                                        (fn [data] (put! data-chan data))))
                                    )))

            :else (put! cmd-chan in))))))

(defn run [p]
  (let [port (new serial-port (.-port p) #js { :baudrate (or (.-baudrate p) 9600) } false)
        stdin (.-stdin js/process)
        in-chan (chan 1)
        cmd-chan (chan 1)
        data-chan (chan 1)]
 
    ;; start go loops
    (print-data data-chan)
    (write-cmd cmd-chan port)
    (process-input in-chan cmd-chan data-chan port)

    ;; start capturing stdin
    (.resume stdin)
    (.setEncoding stdin "utf8")
    (print "> ")
  
    ;; Put stdin data into chan
    (.on stdin "data" (fn [text] (put! in-chan text)))))

(defn -main []
  (.start prompt)
  (.get prompt prompt-props (fn [err result]
                              (if (not (nil? err))
                                (on-error err)
                                (run result)))))

(set! *main-cli-fn* -main)
