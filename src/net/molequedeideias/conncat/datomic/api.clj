(ns net.molequedeideias.conncat.datomic.api
  (:require [net.molequedeideias.conncat :as conncat])
  (:import (datomic Connection)))

(extend-protocol conncat/IWithTx
  Connection
  (with-tx [conn tx]
    (reify Connection
      (transact [_ tx-data]
        (.transact conn (concat tx-data tx)))
      (db [_]
        (.db conn))
      (log [_]
        (.log conn))
      (release [_]
        (.release conn))
      (removeTxReportQueue [_]
        (.removeTxReportQueue conn))
      (requestIndex [_]
        (.requestIndex conn))
      (sync [_]
        (.sync conn))
      (sync [_ t]
        (.sync conn t))
      (gcStorage [_ inst]
        (.gcStorage conn inst)))))
