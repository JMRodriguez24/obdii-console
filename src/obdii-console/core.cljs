(ns obdii-console.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs.nodejs :as nodejs]
            [cljs.core.async :refer [close! put! chan <!]]))

(nodejs/enable-util-print!)

(def util (nodejs/require "util"))
(def prompt (nodejs/require "prompt"))
(def serial-port (.-SerialPort (nodejs/require "serialport")))
(def eol (.-EOL (nodejs/require "os")))

(def prompt-props
  #js {:properties 
       #js {:baudrate #js { :description "baud rate" }
            :port #js { :description "serial port name" :required true } } })

(defn- on-error
  [err]
  (println err)
  (print "> ") 
  1)

(defn- print-result 
  [c]
  (go 
    (loop []
      (let [data (<! c)]
        (when data
          (do
            (print (str data))
            (recur)))))))

(defn- write-cmd 
  [c port]
  (go 
    (loop []
      (let [cmd (<! c)]
        (when cmd
        (do 
          (.write 
            port 
            cmd 
            (fn [err result] 
              (when (not (nil? err)) (on-error err))))
          (recur)))))))

(defn- process-input 
  [in-chan cmd-chan data-chan port]
  (go 
    (loop []
      (let [in (<! in-chan)]
        (when in
          (do  
            (cond
              (or (= (str "quit" eol) in) (= (str "exit" eol) in)) (do (print "Bye!") (close! in-chan))

              (= (str "close" eol) in) (.close port)

              (= (str "open" eol) in) (.open port 
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

              :else (put! cmd-chan in))
            (recur)))))
    (close! data-chan)
    (close! cmd-chan)
    (.exit js/process)))

(defn run [p]
  (let [port (new serial-port (.-port p) #js { :baudrate (or (.-baudrate p) 9600) } false)
        stdin (.-stdin js/process)
        in-chan (chan 1)
        cmd-chan (chan 1)
        data-chan (chan 1)]
 
    ;; setup go loops 
    (print-result data-chan)
    (write-cmd cmd-chan port)
    (process-input in-chan cmd-chan data-chan port)

    (.resume stdin)
    (.setEncoding stdin "utf8")
    (print "> ")
  
    ;; Put input into core.async chan
    (.on stdin "data" (fn [text] (put! in-chan text)))))

(defn -main []
  (.start prompt)
  (.get prompt prompt-props (fn [err result]
                              (if (not (nil? err))
                                (on-error err)
                                (run result)))))

(set! *main-cli-fn* -main)
