(ns asyncfile.demo
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require [goog.dom :as dom]
            [goog.events :as events]
            [cljs.core.async :refer [put! chan <! close!]]
            [asyncfile.core :as asf]))


(defn accept [event]
  (doto event
    (.stopPropagation)
    (.preventDefault)))

(defn listen-dropped-entries [el]
  (let [out (chan)]
    (events/listen el "dragover"
                   (fn [e]
                     (accept e)
                     (set! (.. e
                               getBrowserEvent
                               -dataTransfer
                               -dropEffect)
                           "copy")))
    (events/listen el "drop"
                   (fn [e]
                     (accept e)
                     (doseq [item (array-seq (.. e
                                                 getBrowserEvent
                                                 -dataTransfer
                                                 -items))]
                       (put! out (.webkitGetAsEntry item)))))
    out))

(defn ls-log [output entry]
  (let [read-entries-err (fn [e] (.log js/console "disaster! " e))
        file-chan (if (.-isDirectory entry)
                    (asf/walk-dirent entry read-entries-err)
                    (let [c (chan)]
                      (put! c entry)
                      (close! c)
                      c))
        log (atom "")]
    (go-loop []
             (if-let [filent (<! file-chan)]
               (do
                 (swap! log str "<li>" (.-fullPath filent) "</li>\n")
                 (set! (.-innerHTML output) @log)
                 (recur))
               (.log js.console "(walk complete)")))))

(let [entries (listen-dropped-entries (dom/getElement "drop_zone"))
      output (dom/getElement "output")]
  (go (while true
        (let [entry (<! entries)]
          (ls-log output entry)))))

;    // files is a FileList of File objects. List some properties.
;    var output = [];
;    for (var i = 0, f; f = files[i]; i++) {
;      output.push('<li><strong>', escape(f.name), '</strong> (', f.type || 'n/a', ') - ',
;                  f.size, ' bytes, last modified: ',
;                  f.lastModifiedDate ? f.lastModifiedDate.toLocaleDateString() : 'n/a',
;                  '</li>');
