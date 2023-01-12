package com.anji.captcha.properties;

import com.anji.captcha.model.common.CaptchaTypeEnum;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.awt.*;

import static com.anji.captcha.properties.AjCaptchaProperties.PREFIX;
import static com.anji.captcha.properties.AjCaptchaProperties.StorageType.local;

@Data
@ConfigurationProperties(PREFIX)
public class AjCaptchaProperties {
    public static final String PREFIX = "aj.captcha";

    /**
     * 验证码类型.
     */
    private CaptchaTypeEnum type = CaptchaTypeEnum.DEFAULT;

    /**
     * 滑动拼图底图路径.
     */
    private String jigsaw = "";

    /**
     * 点选文字底图路径.
     */
    private String picClick = "";


    /**
     * 右下角水印文字(我的水印).
     */
    private String waterMark = "我的水印";

    /**
     * 右下角水印字体(文泉驿正黑).
     */
    private String waterFont = "WenQuanZhengHei.ttf";

    /**
     * 点选文字验证码的文字字体(文泉驿正黑).
     */
    private String fontType = "WenQuanZhengHei.ttf";

    /**
     * 校验滑动拼图允许误差偏移量(默认5像素).
     */
    private String slipOffset = "5";

    /**
     * aes加密坐标开启或者禁用(true|false).
     */
    private Boolean aesStatus = true;

    /**
     * 滑块干扰项(0/1/2)
     */
    private String interferenceOptions = "0";

    /**
     * local缓存的阈值
     */
    private String cacheNumber = "1000";

    /**
     * 定时清理过期local缓存(单位秒)
     */
    private String timingClear = "180";

    /**
     * 缓存类型redis/local/....
     */
    private StorageType cacheType = local;
    /**
     * 历史数据清除开关
     */
    private boolean historyDataClearEnable = false;

    /**
     * 一分钟内接口请求次数限制 开关
     */
    private boolean reqFrequencyLimitEnable = false;

    /***
     * 一分钟内check接口失败次数
     */
    private int reqGetLockLimit = 5;
    /**
     *
     */
    private int reqGetLockSeconds = 300;

    /***
     * get接口一分钟内限制访问数
     */
    private int reqGetMinuteLimit = 100;
    private int reqCheckMinuteLimit = 100;
    private int reqVerifyMinuteLimit = 100;

    /**
     * 点选字体样式
     */
    private int fontStyle = Font.BOLD;

    /**
     * 点选字体大小
     */
    private int fontSize = 25;

    /**
     * 点选文字个数，存在问题，暂不要使用
     */
    private int clickWordCount = 4;

    public boolean getReqFrequencyLimitEnable() {
        return reqFrequencyLimitEnable;
    }

    public enum StorageType {
        /**
         * 内存.
         */
        local,
        /**
         * redis.
         */
        redis,
        /**
         * 其他.
         */
        other,
    }

    public static String getPrefix() {
        return PREFIX;
    }

}
