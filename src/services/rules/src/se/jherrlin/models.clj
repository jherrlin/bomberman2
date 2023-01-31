(ns se.jherrlin.models
  (:require [malli.core :as m]))



(def Timestamp
  [:map
   [:rule/type        :timestamp]
   [:timestamp        inst?]])

(def Player
  [:map
   [:rule/type        :player]
   [:game/id          uuid?]
   [:player/id        uuid?]
   [:board/position   [:tuple int? int?]]
   [:bomb/fire-range  int?]
   [:player/state     keyword?]])

(def Board
  [:map
   [:rule/type        :board]
   [:game/id          uuid?]
   ;; TODO
   ])

(def Stone
  [:map
   [:rule/type        :stone]
   [:game/id          uuid?]
   [:board/position   [:tuple int? int?]]])

(def BoardItem
  [:map
   [:rule/type        :board-item]
   [:game/id          uuid?]
   [:board/position   [:tuple int? int?]]
   [:item/type        keyword?]])

(def FlyingBomb
  [:map
   [:game/id               uuid?]
   [:player/id             uuid?]
   [:board/position        [:tuple int? int?]]
   [:fire/length           int?]
   [:bomb/added-timestamp  inst?]
   [:bomd/flying-direction [:enum :west :east :north :south]]])

(def BombOnBoard
  [:map
   [:game/id               uuid?]
   [:player/id             uuid?]
   [:board/position        [:tuple int? int?]]
   [:fire/length           int?]
   [:bomb/added-timestamp  inst?]])






(def PlaceBombCMD
  [:map
   [:rule/type        :cmd/place-bomb]
   [:game/id          uuid?]
   [:player/id        uuid?]
   [:board/position   [:tuple int? int?]]])

(def PlayerMoveCMD
  [:map
   [:rule/type        :cmd/player-move]
   [:game/id          uuid?]
   [:player/id        uuid?]
   [:direction        [:enum :west :east :north :south]]])

(def PlayerThrowBombCMD
  [:map
   [:rule/type        :cmd/player-throw-bomb]
   [:game/id          uuid?]
   [:player/id        uuid?]
   [:direction        [:enum :west :east :north :south]]])
