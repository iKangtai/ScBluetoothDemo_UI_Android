package com.ikangtai.bluetoothui.event;

/**
 * Automatically upload body temperature events
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
