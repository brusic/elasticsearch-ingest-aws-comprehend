package com.brusic.aws.comprehend.model;

public class Entity {
    private String text;
    private String type;

    public Entity(String text, String type) {
        this.text = text;
        this.type = type;
    }

    public String getText() {
        return text;
    }

    public String getType() {
        return type;
    }

    @Override
    public String toString() {
        return "Entity{" +
                "text='" + text + '\'' +
                ", type='" + type + '\'' +
                '}';
    }
}
