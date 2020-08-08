(ns hoquet.test-runner
  (:require [doo.runner :refer-macros [doo-tests]]
            [hoquet.runtime-test]))

(doo-tests 'hoquet.runtime-test)
