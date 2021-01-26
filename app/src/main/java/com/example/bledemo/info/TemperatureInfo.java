package com.example.bledemo.info;

/**
 * desc
 *
 * @author xiongyl 2021/1/26 11:19
 */
public class TemperatureInfo {
    /**
     * 统一转换成ºC存储;
     */
    private double temperature;
    private long measureTime;

    public TemperatureInfo() {
    }

    public TemperatureInfo(double temperature, long measureTime) {
        this.temperature = temperature;
        this.measureTime = measureTime;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public long getMeasureTime() {
        return measureTime;
    }

    public void setMeasureTime(long measureTime) {
        this.measureTime = measureTime;
    }
}
