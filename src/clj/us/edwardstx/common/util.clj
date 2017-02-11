(ns us.edwardstx.common.util
  (:import java.net.URLEncoder
            java.net.URLDecoder
            java.util.UUID))

(defn uuid [] (str (UUID/randomUUID)))

(def utf8 "UTF8")

(defn encode-utf8 [& strs]
  (URLEncoder/encode (apply str strs) utf8))

(defn decode-utf8 [s]
  (URLDecoder/decode s utf8))
