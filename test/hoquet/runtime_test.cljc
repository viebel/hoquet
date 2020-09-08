(ns hoquet.runtime-test
  (:require [clojure.test :refer [deftest is]]
            [hoquet.runtime :refer [escape-html render-html]]))

(deftest runtime-escaped-chars
  (is (= (escape-html "\"") "&quot;"))
  (is (= (escape-html "<") "&lt;"))
  (is (= (escape-html ">") "&gt;"))
  (is (= (escape-html "&") "&amp;"))
  (is (= (escape-html "foo") "foo")))

(deftest tag-names
  (is (= (render-html [:div]) "<div></div>"))
  (is (= (render-html ["div"]) "<div></div>"))
  (is (= (render-html ['div]) "<div></div>"))
  (is (= (render-html [:div#foo]) "<div id=\"foo\"></div>"))
  (is (= (render-html [:form#sign-in]) "<form id=\"sign-in\"></form>"))
  (is (= (render-html [:input#sign-in-email]) "<input id=\"sign-in-email\" />"))
  (is (= (render-html [:div.foo]) "<div class=\"foo\"></div>"))
  (is (= (render-html [:div.foo (str "bar" "baz")])
           "<div class=\"foo\">barbaz</div>"))
  (is (= (render-html [:div.a.b]) "<div class=\"a b\"></div>"))
  (is (= (render-html [:div.a.b.c]) "<div class=\"a b c\"></div>"))
  (is (= (render-html [:div#foo.bar.baz])
           "<div class=\"bar baz\" id=\"foo\"></div>")))

(deftest tag-contents
 ; empty tags
 (is (= (render-html [:div]) "<div></div>"))
 (is (= (render-html [:h1]) "<h1></h1>"))
 (is (= (render-html [:script]) "<script></script>"))
 (is (= (render-html [:text]) "<text />"))
 (is (= (render-html [:a]) "<a></a>"))
 (is (= (render-html [:iframe]) "<iframe></iframe>"))
 ; tags containing text
 (is (= (render-html [:text "Lorem Ipsum"]) "<text>Lorem Ipsum</text>"))
 ; contents are concatenated
 (is (= (render-html [:body "foo" "bar"]) "<body>foobar</body>"))
 (is (= (render-html [:body [:p] [:br]]) "<body><p /><br /></body>"))
 ; seqs are expanded
 (is (= (render-html [:body (list "foo" "bar")]) "<body>foobar</body>"))
 (is (= (render-html (list [:p "a"] [:p "b"])) "<p>a</p><p>b</p>"))
 ; tags can contain tags
 (is (= (render-html [:div [:p]]) "<div><p /></div>"))
 (is (= (render-html [:div [:b]]) "<div><b></b></div>"))
 (is (= (render-html [:p [:span [:a "foo"]]])
          "<p><span><a>foo</a></span></p>")))

(deftest tag-attributes
  ; tag with blank attribute map
  (is (= (render-html [:xml {}]) "<xml />"))
  ; tag with populated attribute map
  (is (= (render-html [:xml {:a "1", :b "2"}]) "<xml a=\"1\" b=\"2\" />"))
  (is (= (render-html [:img {"id" "foo"}]) "<img id=\"foo\" />"))
  (is (= (render-html [:img {'id "foo"}]) "<img id=\"foo\" />"))
  (is (= (render-html [:xml {:a "1", 'b "2", "c" "3"}])
           "<xml a=\"1\" b=\"2\" c=\"3\" />"))
  ; attribute values are escaped
  (is (= (render-html [:div {:id "\""}]) "<div id=\"&quot;\"></div>"))
  ; boolean attributes
  (is (= (render-html [:input {:type "checkbox" :checked true}])
           "<input checked=\"checked\" type=\"checkbox\" />"))
  (is (= (render-html [:input {:type "checkbox" :checked false}])
           "<input type=\"checkbox\" />"))
  ; nil attributes
  (is (= (render-html [:span {:class nil} "foo"])
           "<span>foo</span>")))

(deftest compiled-tags
  ; tag content can be vars
  (is (= (let [x "foo"] (render-html [:span x])) "<span>foo</span>"))
  ; tag content can be forms
  (is (= (render-html [:span (str (+ 1 1))]) "<span>2</span>"))
  (is (= (render-html [:span ({:foo "bar"} :foo)]) "<span>bar</span>"))
  ; attributes can contain vars
  (let [x "foo"]
    (is (= (render-html [:xml {:x x}]) "<xml x=\"foo\" />"))
    (is (= (render-html [:xml {x "x"}]) "<xml foo=\"x\" />"))
    (is (= (render-html [:xml {:x x} "bar"]) "<xml x=\"foo\">bar</xml>")))
  ; attributes are evaluated
    (is (= (render-html [:img {:src (str "/foo" "/bar")}])
             "<img src=\"/foo/bar\" />"))
    (is (= (render-html [:div {:id (str "a" "b")} (str "foo")])
             "<div id=\"ab\">foo</div>"))
  ; optimized forms
    (is (= (render-html [:ul (for [n (range 3)]
                                  [:li n])])
             "<ul><li>0</li><li>1</li><li>2</li></ul>"))
    (is (= (render-html [:div (if true
                                   [:span "foo"]
                                   [:span "bar"])])
             "<div><span>foo</span></div>"))
  ; values are evaluated only once
  (let [times-called (atom 0)
        foo #(do (swap! times-called inc) "foo")]
    (render-html [:div (foo)])
    (is (= 1 @times-called))))


(deftest render-modes
  ; "closed tag"
  (is (= (render-html [:br]) "<br />"))
  (is (= (render-html [:br]) "<br />"))
  ; boolean attributes
  (is (= (render-html [:input {:type "checkbox" :checked true}])
           "<input checked=\"checked\" type=\"checkbox\" />")))

(comment
  (clojure.test/run-tests))
