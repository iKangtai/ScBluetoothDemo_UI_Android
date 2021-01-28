package com.example.bledemo.event;

/**
 * 自动上传体温事件
 */

public class AutoUploadTemperatureEvent {
    private String content;
    private String subContent;

    public AutoUploadTemperatureEvent(String content) {
        this.content = content;
    }

    public AutoUploadTemperatureEvent(String content, String subContent) {
        this.content = content;
        this.subContent = subContent;
    }

    public String getContent() {
        return content;
    }

    public String getSubContent() {
        return subContent;
    }
}
