package cn.iocoder.yudao.module.iot.domain;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.sql.Timestamp;

public class IotSequential extends BaseEntity {
    private static final long serialVersionUID = 1L;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS" , timezone = "GMT+8")
    private Timestamp statetime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS" , timezone = "GMT+8")
    private Timestamp endtime;

    private String deviceid;

    private String eventtime;

    private String serviceid;

    private String devices;

    public String getDeviceid() {
        return deviceid;
    }

    public void setDeviceid(String deviceid) {
        this.deviceid = deviceid;
    }

    public String getEventtime() {
        return eventtime;
    }

    public void setEventtime(String eventtime) {
        this.eventtime = eventtime;
    }

    public String getServiceid() {
        return serviceid;
    }

    public void setServiceid(String serviceid) {
        this.serviceid = serviceid;
    }

    public String getDevices() {
        return devices;
    }

    public void setDevices(String devices) {
        this.devices = devices;
    }

    public Timestamp getStatetime() {
        return statetime;
    }

    public void setStatetime(Timestamp statetime) {
        this.statetime = statetime;
    }

    public Timestamp getEndtime() {
        return endtime;
    }

    public void setEndtime(Timestamp endtime) {
        this.endtime = endtime;
    }
}
