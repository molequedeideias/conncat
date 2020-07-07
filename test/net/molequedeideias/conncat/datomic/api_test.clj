(ns net.molequedeideias.conncat.datomic.api-test
  (:require [clojure.test :refer [deftest testing is use-fixtures]]
            [datomic.api :as d]
            [net.molequedeideias.conncat :as conncat]
            [net.molequedeideias.conncat.datomic.api]))

(def ^:dynamic *db-uri* "datomic:mem://repl")

(comment
  ;; easier REPL dev
  (d/create-database *db-uri*))

(defn with-empty-db-uri
  [f]
  (binding [*db-uri* (str "datomic:mem://" (d/squuid))]
    (try
      (d/create-database *db-uri*)
      (f)
      (finally
        (d/delete-database *db-uri*)))))

(use-fixtures :each with-empty-db-uri)

(deftest simple
  (let [conn (d/connect *db-uri*)
        conn-with-doc (conncat/with-tx conn [[:db/add "datomic.tx"
                                              :db/doc "on-tx"]])
        {:keys [db-after tempids]} @(d/transact conn-with-doc [{:db/id  "entity"
                                                                :db/doc "on-entity"}])
        tx (d/resolve-tempid db-after tempids "datomic.tx")
        e (d/resolve-tempid db-after tempids "entity")]
    (testing
      "I should find both"
      (is (= (vec (d/q '[:find ?v-entity ?v-tx
                         :in $ ?e ?tx
                         :where
                         [?e :db/doc ?v-entity ?tx]
                         [?tx :db/doc ?v-tx ?tx]]
                       db-after e tx))
             [["on-entity" "on-tx"]])))))


(deftest nested
  (let [conn (d/connect *db-uri*)
        conn-with-doc (conncat/with-tx conn [[:db/add "datomic.tx"
                                              :db/doc "on-tx"]])
        conn-with-2-doc (conncat/with-tx conn-with-doc [[:db/add "datomic.tx"
                                                         :db/ident :doc2]])
        {:keys [db-after tempids]} @(d/transact conn-with-2-doc [{:db/id  "entity"
                                                                  :db/doc "on-entity"}])
        tx (d/resolve-tempid db-after tempids "datomic.tx")
        e (d/resolve-tempid db-after tempids "entity")]
    (testing
      "I should find both"
      (is (= (vec (d/q '[:find ?v-entity ?v-tx ?v-tx2
                         :in $ ?e ?tx
                         :where
                         [?e :db/doc ?v-entity ?tx]
                         [?tx :db/doc ?v-tx ?tx]
                         [?tx :db/ident ?v-tx2 ?tx]]
                       db-after e tx))
             [["on-entity" "on-tx" :doc2]])))))
