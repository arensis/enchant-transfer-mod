package net.alfonsormadrid.enchanttransfer.utils;

public class TestExample {

    private String message;

    public void helloMessage() {
        this.message = "Hello world";
    }

    public void customMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
