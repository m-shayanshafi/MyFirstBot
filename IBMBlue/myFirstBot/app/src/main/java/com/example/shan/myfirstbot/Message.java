package com.example.shan.myfirstbot;

/**
 * Created by shan on 6/15/2017.
 */
import java.io.Serializable;

public class Message {

    String id, message;


    public Message() {
    }

    public Message(String id, String message, String createdAt) {
        this.id = id;
        this.message = message;


    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
