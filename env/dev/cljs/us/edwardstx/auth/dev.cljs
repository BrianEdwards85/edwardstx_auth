(ns ^:figwheel-no-load us.edwardstx.auth.dev
  (:require [us.edwardstx.auth.core :as core]
            [re-frisk.core :as re-frisk]
            ))

(enable-console-print!)
(re-frisk/enable-re-frisk!)
(core/init!)
