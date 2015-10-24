package com.example.nanochat;

/**
 * Created by Ava on 10/24/2015.
 */
public class ChatMessage {

    private String name;
    private String text;

    public ChatMessage() {
        //For firebase
    }

    public ChatMessage(final String name, final String text) {
        this.name = name;
        this.text = text;
    }

    public String getName() {
        return name;
    }

    public String getText() {
        return text;
    }
}
