package com.feng.mydemo.bean;

/**
 * @author 刘松汉
 * @time 2016/12/20  14:59
 * @desc ${TODD}
 */
public class MsgInfo {

    private String left_text;
    private String right_text;

    public MsgInfo(String left_text, String right_text) {
        this.left_text = left_text;
        this.right_text = right_text;
    }

    public String getLeft_text() {
        return left_text;
    }

    public String getRight_text() {
        return right_text;
    }

}
