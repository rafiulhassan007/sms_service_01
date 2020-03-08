package com.rafiul.sms_sender_service;

public class SmsContents {
    private String text;
    private String number;

    public SmsContents(String text, String number) {
        this.text = text;
        this.number = number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getText() {
        return text;
    }

    public String getNumber() {
        return number;
    }
}
