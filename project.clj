(defproject hello-world "0.1.0-SNAPSHOT"
  :description "Simple console to interact with obdii through serial port!"
  :url "https://github.com/JMRodriguez24/obdii-console"

  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/clojurescript "0.0-2505"]
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]]

  :node-dependencies [[source-map-support "0.2.8"]
                      [prompt "0.2.14"]
                      [serialport "1.4.9"]]

  :plugins [[lein-cljsbuild "1.0.4-SNAPSHOT"]
            [lein-npm "0.4.0"]]

  :source-paths ["src"]

  :cljsbuild {
    :builds [{:id "dev"
              :source-paths ["src"]
              :compiler {
                :output-to "out/obdii-console.js"
                :output-dir "out"
                :target :nodejs
                :optimizations :none
                :source-map true}}]})
