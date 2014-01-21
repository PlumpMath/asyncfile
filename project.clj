(defproject asyncfile "0.1.0-SNAPSHOT"
  :description "HTML5 File API via core.async"
  :url "http://github.com/karchie/asyncfile"

  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/clojurescript "0.0-2138"]
                 [org.clojure/core.async "0.1.267.0-0d7780-alpha"]]

  :plugins [[lein-cljsbuild "1.0.1"]]

  :source-paths ["src"]

  :cljsbuild { 
    :builds [{:id "asyncfile"
              :source-paths ["src"]
              :compiler {
                :output-to "asyncfile.js"
                :output-dir "out"
                :optimizations :none
                :source-map true}}]})
