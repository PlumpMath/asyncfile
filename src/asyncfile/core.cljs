(ns asyncfile.core
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require [goog.dom :as dom]
            [goog.events :as events]
            [cljs.core.async :refer [put! chan <! close!]]))


(defn walk-dirent [dirent read-entries-err]
  "Given a DirectoryEntry dirent, returns a channel of FileEntry
  objects representing the recursively walked contents of dirent."
  (let [reader (.createReader dirent)
        dirs (atom #{dirent})
        dir-chan (chan)
        file-chan (chan)]
    (put! dir-chan dirent)
    (go-loop []
             (if-let [dirent (<! dir-chan)]
               (do
                 (.readEntries (.createReader dirent)
                               (fn [entries-arr]
                                 (doseq [entry (array-seq entries-arr)]
                                   (if (.-isDirectory entry)
                                     (do
                                       (swap! dirs conj entry)
                                       (put! dir-chan entry))
                                     ;; TODO: watch out for closed channel
                                     (put! file-chan entry)))
                                 (when (empty? (swap! dirs disj dirent))
                                   (close! dir-chan)))
                               read-entries-err)
                 (recur))
               (close! file-chan)))
    file-chan))
