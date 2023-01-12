package com.anji.captcha.model.vo;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Created by raodeming on 2020/5/16.
 */
public class PointVO {
    private String secretKey;

    public int x;

    public int y;

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public PointVO(int x, int y, String secretKey) {
        this.secretKey = secretKey;
        this.x = x;
        this.y = y;
    }

    public PointVO() {
    }

    public PointVO(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public String toJsonString() {
        return String.format("{\"secretKey\":\"%s\",\"x\":%d,\"y\":%d}", secretKey, x, y);
    }

    public PointVO parse(String jsonStr) {
        Map<String, Object> m = new HashMap();
        Arrays.stream(jsonStr
                .replaceFirst(",\\{", "\\{")
                .replaceFirst("\\{", "")
                .replaceFirst("\\}", "")
                .replaceAll("\"", "")
                .split(",")).forEach(item -> {
            m.put(item.split(":")[0], item.split(":")[1]);
        });
        //PointVO d = new PointVO();
        setX(Double.valueOf("" + m.get("x")).intValue());
        setY(Double.valueOf("" + m.get("y")).intValue());
        setSecretKey(m.getOrDefault("secretKey", "") + "");
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PointVO pointVO = (PointVO) o;
        return x == pointVO.x && y == pointVO.y && Objects.equals(secretKey, pointVO.secretKey);
    }

    @Override
    public int hashCode() {

        return Objects.hash(secretKey, x, y);
    }
}
