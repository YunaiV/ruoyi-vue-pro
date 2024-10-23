package cn.iocoder.yudao.module.iot.domain;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.sql.Timestamp;

public class Weather {

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS" , timezone = "GMT+8")
    private Timestamp ts;
    private Float temperature;
    private Float humidity;
    private String location;
    private String note;
    private int groupId;

    public Weather() {
    }

    public Weather(Timestamp ts, float temperature, float humidity) {
        this.ts = ts;
        this.temperature = temperature;
        this.humidity = humidity;
    }

    public Timestamp getTs() {
        return ts;
    }

    public void setTs(Timestamp ts) {
        this.ts = ts;
    }

    public Float getTemperature() {
        return temperature;
    }

    public void setTemperature(Float temperature) {
        this.temperature = temperature;
    }

    public Float getHumidity() {
        return humidity;
    }

    public void setHumidity(Float humidity) {
        this.humidity = humidity;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public int getGroupId() {
        return groupId;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
