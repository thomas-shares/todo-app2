(ns todo-app2.app
  (:require   [reagent.core :as reagent]
              [re-frame.core :refer [dispatch dispatch-sync subscribe reg-event-db reg-sub]]
              [devtools.core :as devtools]))

(devtools/install!)
(enable-console-print!)

(reg-event-db              ;; sets up initial application state
  :initialise              ;; usage:  (dispatch [:initialize])
  (fn [_ _]                ;; the two parameters are not important here, so use _
    {:todo-list #{}        ;; What it returns becomes the new application state
     :next-todo ""}))      ;; so the application state will initially be a map with two keys

(reg-event-db
  :add-item
  (fn [db [_ new-item]] ;; -db event handlers given 2 parameters:  current application state and event (a vector)
    (let [new-db (update-in db [:todo-list] conj new-item)]
      (assoc new-db :next-todo ""))))

(reg-event-db
  :next-todo
  (fn [db [_ new-letter]]
    (assoc db :next-todo new-letter)))

(reg-event-db
  :delete-item
  (fn [db [_ item]]
    (update-in db [:todo-list] disj item)))

(reg-sub
  :todo-list
  (fn [db _]
    (:todo-list db)))

(reg-sub
  :next-todo
  (fn [db _]
    (:next-todo db)))

(defn todo-page
  []
  (let [list (subscribe [:todo-list])]
    (fn []
      [:form
        [:fieldset
          [:legend "Todo list:"]
          [:ul {:class "todo-list"}
            (for [item @(subscribe [:todo-list])]
              ^{:key item} [:li [:input.toggle
                                   {:type "checkbox"
                                    :id item
                                    :on-change #(dispatch [:delete-item item])}]
                              item])]]
        [:div {:class "add"}
          " "[:input {:class "add-input"
                      :type "text"
                      :id "add-input"
                      :value  @(subscribe [:next-todo]) ;; @next-todo
                      :auto-focus true
                      :placeholder "Your next item..."
                      :on-change  #(dispatch [:next-todo (-> % .-target .-value)])
                      :on-key-press #(when (= 13 (.-which %))
                                       (dispatch [:add-item @(subscribe [:next-todo])])
                                       (.preventDefault %))}]
          [:input {:class "add-submit"
                   :type "button"
                   :value "Add Item"
                   :on-click #(dispatch [:add-item @(subscribe [:next-todo])])}]]])))

(defn init []
  (dispatch-sync [:initialise])
  (reagent/render [todo-page]
            (js/document.getElementById "container")))
