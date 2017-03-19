(ns us.edwardstx.common.spec
  (:require [clojure.spec :as s]))

(s/def ::non-empty-string (s/and string? not-empty))
(def base64-regex #"^[A-Za-z0-9+/]+[=]{0,2}$")
(s/def ::base64 (s/and ::non-empty-string #(re-matches base64-regex %)))

