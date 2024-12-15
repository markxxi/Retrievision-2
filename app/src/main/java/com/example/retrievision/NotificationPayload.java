package com.example.retrievision;

public class NotificationPayload {
    private Message message;

    public NotificationPayload() {
    }

    public NotificationPayload(String to, String title, String body) {
        this.message = new Message(to, title, body);
    }

    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }

    public static class Message {
        private String token;
        private Notification notification;

        public Message() {
        }

        public Message(String token, String title, String body) {
            this.token = token;
            this.notification = new Notification(title, body);
        }

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }

        public Notification getNotification() {
            return notification;
        }

        public void setNotification(Notification notification) {
            this.notification = notification;
        }
    }

    public static class Notification {
        private String title;
        private String body;

        public Notification() {
        }

        public Notification(String title, String body) {
            this.title = title;
            this.body = body;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getBody() {
            return body;
        }

        public void setBody(String body) {
            this.body = body;
        }
    }
}
