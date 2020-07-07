# conncat

Con`cat` `tx-data` on every transaction in a `conn`ection.

## Usage

Using `deps.edn`
```clojure
net.molequedeideias/conncat {:git/url "https://github.com/molequedeideias/conncat.git"
                             :sha     "95050f2d1d6ac47a371f31efa5c4e3838c1b4385"}
```

```clojure
(require '[datomic.api :as d]
         '[net.molequedeideias.conncat :as conncat]
         ;; extend conncat for datomic.api
         '[net.molequedeideias.conncat.datomic.api])

(def db-uri (doto "datomic:mem://conncat"
               (d/create-database)))

(def conn (d/connect db-uri))

(def conn-with-audit
  (conncat/with-tx audited-conn [[:db/add "datomic.tx" :db/doc "i have audit"]]))

;; now you can transact data on `conn-with-audit`

(d/transact conn-with-audit [{:db/doc "one"}])
(d/transact conn-with-audit [{:db/doc "two"}])


;; now you can see that every transaction done on `conn-with-audit
;; has a `:db/doc "i have audit"` with it!
(d/q '[:find ?e ?ident ?v ?tx ?op
       :in $
       :where
       [?a :db/ident ?ident]
       [?e ?a ?v ?tx ?op]
       [?tx :db/doc]]
     (d/db conn))
=>
#{[134 :db/doc "i have audit" 134 true]
  [132 :db/txInstant #inst"2020-07-07T18:52:27.5" 132 true]
  [132 :db/doc "i have audit" 132 true]
  [134 :db/txInstant #inst"2020-07-07T18:52:27.6" 134 true]
  [179 :db/doc "two" 134 true]
  [177 :db/doc "one" 132 true]}
```

## Applications

0. Append audit data to your transactions
   - Save the token from user that requested a transaction
   - Save all headers from HTTP Request with that transaction
1. Append a unique value attribute, to ensure that your component will make only one transaction
2. Identify with module from your systme done a transaction
3. Append the `COMMIT_SHA` of current code in your transaction, then you will have a lot more information about "how it's done" 
