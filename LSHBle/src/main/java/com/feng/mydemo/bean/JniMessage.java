package com.feng.mydemo.bean;

public class JniMessage {
     public  String from;
    public  String to="qt_jni";
    public JniMessage(String program_id, String command, String data1, String data2) {
        this.from = program_id;
        this.command = command;
        this.data1 = data1;
        this.data2 = data2;
    }

    public  String command;
     public  String data1;
     public  String data2;
    public  int data3;
    public  int data4;

}
