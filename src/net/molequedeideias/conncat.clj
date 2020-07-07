(ns net.molequedeideias.conncat)

(defprotocol IWithTx
  (with-tx [conn tx]))
