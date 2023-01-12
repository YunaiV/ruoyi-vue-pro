/*
 *Copyright © 2018 anji-plus
 *安吉加加信息技术有限公司
 *http://www.anji-plus.com
 *All rights reserved.
 */
package com.anji.captcha.service.impl;

import com.anji.captcha.model.common.Const;
import com.anji.captcha.model.common.RepCodeEnum;
import com.anji.captcha.model.common.ResponseModel;
import com.anji.captcha.model.vo.CaptchaVO;
import com.anji.captcha.service.CaptchaCacheService;
import com.anji.captcha.service.CaptchaService;
import com.anji.captcha.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Properties;

/**
 * Created by raodeming on 2019/12/25.
 */
public abstract class AbstractCaptchaService implements CaptchaService {

    protected Logger logger = LoggerFactory.getLogger(getClass());

    protected static final String IMAGE_TYPE_PNG = "png";

	protected static int HAN_ZI_SIZE = 25;

	protected static int HAN_ZI_SIZE_HALF = HAN_ZI_SIZE / 2;
    //check校验坐标
    protected static String REDIS_CAPTCHA_KEY = "RUNNING:CAPTCHA:%s";

    //后台二次校验坐标
    protected static String REDIS_SECOND_CAPTCHA_KEY = "RUNNING:CAPTCHA:second-%s";

    protected static Long EXPIRESIN_SECONDS = 2 * 60L;

    protected static Long EXPIRESIN_THREE = 3 * 60L;

    protected static String waterMark = "我的水印";

    protected static String waterMarkFontStr = "WenQuanZhengHei.ttf";

    protected Font waterMarkFont;//水印字体

    protected static String slipOffset = "5";

    protected static Boolean captchaAesStatus = true;

    protected static String clickWordFontStr = "WenQuanZhengHei.ttf";

    protected Font clickWordFont;//点选文字字体

    protected static String cacheType = "local";

    protected static int captchaInterferenceOptions = 0;

    //判断应用是否实现了自定义缓存，没有就使用内存
    @Override
    public void init(final Properties config) {
        //初始化底图
        boolean aBoolean = Boolean.parseBoolean(config.getProperty(Const.CAPTCHA_INIT_ORIGINAL));
        if (!aBoolean) {
            ImageUtils.cacheImage(config.getProperty(Const.ORIGINAL_PATH_JIGSAW),
                    config.getProperty(Const.ORIGINAL_PATH_PIC_CLICK));
        }
        logger.info("--->>>初始化验证码底图<<<---" + captchaType());
        waterMark = config.getProperty(Const.CAPTCHA_WATER_MARK, "我的水印");
        slipOffset = config.getProperty(Const.CAPTCHA_SLIP_OFFSET, "5");
        waterMarkFontStr = config.getProperty(Const.CAPTCHA_WATER_FONT, "WenQuanZhengHei.ttf");
        captchaAesStatus = Boolean.parseBoolean(config.getProperty(Const.CAPTCHA_AES_STATUS, "true"));
        clickWordFontStr = config.getProperty(Const.CAPTCHA_FONT_TYPE, "WenQuanZhengHei.ttf");
        //clickWordFontStr = config.getProperty(Const.CAPTCHA_FONT_TYPE, "SourceHanSansCN-Normal.otf");
        cacheType = config.getProperty(Const.CAPTCHA_CACHETYPE, "local");
        captchaInterferenceOptions = Integer.parseInt(
                config.getProperty(Const.CAPTCHA_INTERFERENCE_OPTIONS, "0"));

        // 部署在linux中，如果没有安装中文字段，水印和点选文字，中文无法显示，
        // 通过加载resources下的font字体解决，无需在linux中安装字体
        loadWaterMarkFont();

        if (cacheType.equals("local")) {
            logger.info("初始化local缓存...");
            CacheUtil.init(Integer.parseInt(config.getProperty(Const.CAPTCHA_CACAHE_MAX_NUMBER, "1000")),
                    Long.parseLong(config.getProperty(Const.CAPTCHA_TIMING_CLEAR_SECOND, "180")));
        }
        if (config.getProperty(Const.HISTORY_DATA_CLEAR_ENABLE, "0").equals("1")) {
            logger.info("历史资源清除开关...开启..." + captchaType());
            Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
                @Override
                public void run() {
                    destroy(config);
                }
            }));
        }
        if (config.getProperty(Const.REQ_FREQUENCY_LIMIT_ENABLE, "0").equals("1")) {
            if (limitHandler == null) {
                logger.info("接口分钟内限流开关...开启...");
                limitHandler = new FrequencyLimitHandler.DefaultLimitHandler(config, getCacheService(cacheType));
            }
        }
    }

    protected CaptchaCacheService getCacheService(String cacheType) {
        return CaptchaServiceFactory.getCache(cacheType);
    }

    @Override
    public void destroy(Properties config) {

    }

    private static FrequencyLimitHandler limitHandler;

    @Override
    public ResponseModel get(CaptchaVO captchaVO) {
        if (limitHandler != null) {
            captchaVO.setClientUid(getValidateClientId(captchaVO));
            return limitHandler.validateGet(captchaVO);
        }
        return null;
    }

    @Override
    public ResponseModel check(CaptchaVO captchaVO) {
        if (limitHandler != null) {
            // 验证客户端
           /* ResponseModel ret = limitHandler.validateCheck(captchaVO);
            if(!validatedReq(ret)){
                return ret;
            }
            // 服务端参数验证*/
            captchaVO.setClientUid(getValidateClientId(captchaVO));
            return limitHandler.validateCheck(captchaVO);
        }
        return null;
    }

    @Override
    public ResponseModel verification(CaptchaVO captchaVO) {
        if (captchaVO == null) {
            return RepCodeEnum.NULL_ERROR.parseError("captchaVO");
        }
        if (StringUtils.isEmpty(captchaVO.getCaptchaVerification())) {
            return RepCodeEnum.NULL_ERROR.parseError("captchaVerification");
        }
        if (limitHandler != null) {
            return limitHandler.validateVerify(captchaVO);
        }
        return null;
    }

    protected boolean validatedReq(ResponseModel resp) {
        return resp == null || resp.isSuccess();
    }

	protected String getValidateClientId(CaptchaVO req){
    	// 以服务端获取的客户端标识 做识别标志
		if(StringUtils.isNotEmpty(req.getBrowserInfo())){
			return MD5Util.md5(req.getBrowserInfo());
		}
		// 以客户端Ui组件id做识别标志
		if(StringUtils.isNotEmpty(req.getClientUid())){
			return req.getClientUid();
		}
    	return null;
	}

    protected void afterValidateFail(CaptchaVO data) {
        if (limitHandler != null) {
            // 验证失败 分钟内计数
            String fails = String.format(FrequencyLimitHandler.LIMIT_KEY, "FAIL", data.getClientUid());
            CaptchaCacheService cs = getCacheService(cacheType);
            if (!cs.exists(fails)) {
                cs.set(fails, "1", 60);
            }
            cs.increment(fails, 1);
        }
    }

    /**
     * 加载resources下的font字体，add by lide1202@hotmail.com
     * 部署在linux中，如果没有安装中文字段，水印和点选文字，中文无法显示，
     * 通过加载resources下的font字体解决，无需在linux中安装字体
     */
    private void loadWaterMarkFont() {
        try {
            if (waterMarkFontStr.toLowerCase().endsWith(".ttf") || waterMarkFontStr.toLowerCase().endsWith(".ttc")
                    || waterMarkFontStr.toLowerCase().endsWith(".otf")) {
                this.waterMarkFont = Font.createFont(Font.TRUETYPE_FONT,
                        getClass().getResourceAsStream("/fonts/" + waterMarkFontStr))
                        .deriveFont(Font.BOLD, HAN_ZI_SIZE / 2);
            } else {
                this.waterMarkFont = new Font(waterMarkFontStr, Font.BOLD, HAN_ZI_SIZE / 2);
            }

        } catch (Exception e) {
            logger.error("load font error:{}", e);
        }
    }

    public static boolean base64StrToImage(String imgStr, String path) {
        if (imgStr == null) {
            return false;
        }

        Base64.Decoder decoder = Base64.getDecoder();
        try {
            // 解密
            byte[] b = decoder.decode(imgStr);
            // 处理数据
            for (int i = 0; i < b.length; ++i) {
                if (b[i] < 0) {
                    b[i] += 256;
                }
            }
            //文件夹不存在则自动创建
            File tempFile = new File(path);
            if (!tempFile.getParentFile().exists()) {
                tempFile.getParentFile().mkdirs();
            }
            OutputStream out = new FileOutputStream(tempFile);
            out.write(b);
            out.flush();
            out.close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 解密前端坐标aes加密
     *
     * @param point
     * @return
     * @throws Exception
     */
    public static String decrypt(String point, String key) throws Exception {
        return AESUtil.aesDecrypt(point, key);
    }

    protected static int getEnOrChLength(String s) {
        int enCount = 0;
        int chCount = 0;
        for (int i = 0; i < s.length(); i++) {
            int length = String.valueOf(s.charAt(i)).getBytes(StandardCharsets.UTF_8).length;
            if (length > 1) {
                chCount++;
            } else {
                enCount++;
            }
        }
        int chOffset = (HAN_ZI_SIZE / 2) * chCount + 5;
        int enOffset = enCount * 8;
        return chOffset + enOffset;
    }


}
