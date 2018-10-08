(ns mail
    (:require [postal.core :refer [send-message]])
    (:gen-class))

(defn send-file-alert [user_email diff]
      (def message_body (str "Hey there,\nIt's the team from Magehash alterting you, we noticed \"filename\" was recently updated, here is the code that changed.\nCheck \"filename\" for these changes!"))
      (def mail_info {:host "smtp.mailtrap.io"
                      :user "12341d55f5c1ed"
                      :pass "2fd6cbf3ccbf1e"})

      (def no-reply-email "canreply@magehash.com")

      (send-message mail_info {:from no-reply-email
                               :to user_email
                               :subject "Magehash Alert!"
                               :body message_body}))
