package cn.iocoder.yudao.framework.excel.core.util;

import java.util.Date;

@SuppressWarnings({"LombokGetterMayBeUsed", "LombokSetterMayBeUsed"})
public class JavaDataBean {
    private double number;
    private String string;
    private Date date;
    private boolean aBoolean;

    public String getString() {
        return string;
    }

    public void setString(String string) {
        this.string = string;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public boolean isABoolean() {
        return aBoolean;
    }

    public void setABoolean(boolean aBoolean) {
        this.aBoolean = aBoolean;
    }

    public double getNumber() {
        return number;
    }

    public void setNumber(double number) {
        this.number = number;
    }
}
