package cn.iocoder.yudao.module.system.framework.captcha.core;

import com.anji.captcha.model.common.CaptchaTypeEnum;
import com.anji.captcha.model.common.RepCodeEnum;
import com.anji.captcha.model.common.ResponseModel;
import com.anji.captcha.model.vo.CaptchaVO;
import com.anji.captcha.service.impl.AbstractCaptchaService;
import com.anji.captcha.service.impl.CaptchaServiceFactory;
import com.anji.captcha.util.AESUtil;
import com.anji.captcha.util.ImageUtils;
import com.anji.captcha.util.RandomUtils;
import org.apache.commons.lang3.StringUtils;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.Properties;
import java.util.Random;

/**
 * 图片文字验证码
 *
 * @author Tsui
 * @since 2025/7/23 20:44
 */
public class PictureWordCaptchaServiceImpl extends AbstractCaptchaService {

    private static final int WIDTH = 120;
    private static final int HEIGHT = 40;
    private static final int LINES = 10;
    private static final Random RANDOM = new Random();
    private static final String CHARACTERS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";

    @Override
    public void init(Properties config) {
        super.init(config);
    }

    @Override
    public void destroy(Properties config) {
        logger.info("start-clear-history-data-{}", captchaType());
    }

    @Override
    public String captchaType() {
        return "pictureWord";
//        return CaptchaTypeEnum.PICTURE_WORD.getCodeValue();
    }

    @Override
    public ResponseModel get(CaptchaVO captchaVO) {
        String text = generateRandomText(4);

        CaptchaVO imageData = getImageData(text);

        // pointJson不传到前端,只做后端校验,测试时放开
//        imageData.setPointJson(text);

        return ResponseModel.successData(imageData);
    }

    @Override
    public ResponseModel check(CaptchaVO captchaVO) {
        ResponseModel r = super.check(captchaVO);
        if (!validatedReq(r)) {
            return r;
        }

        // 取出验证码
        String codeKey = String.format(REDIS_CAPTCHA_KEY, captchaVO.getToken());
        if (!CaptchaServiceFactory.getCache(cacheType).exists(codeKey)) {
            return ResponseModel.errorMsg(RepCodeEnum.API_CAPTCHA_INVALID);
        }
        // 正确的验证码
        String codeValue = CaptchaServiceFactory.getCache(cacheType).get(codeKey);
        String code = getCodeByCodeValue(codeValue);
        String secretKey = getSecretKeyByCodeValue(codeValue);
        //验证码只用一次，即刻失效
        CaptchaServiceFactory.getCache(cacheType).delete(codeKey);

        // 用户输入的验证码(CaptchaVO中没有预留字段,暂时用pointJson,无需加解密)
        String userCode = captchaVO.getPointJson();
        if (!StringUtils.equalsIgnoreCase(code, userCode)) {
            afterValidateFail(captchaVO);
            return ResponseModel.errorMsg(RepCodeEnum.API_CAPTCHA_COORDINATE_ERROR);
        }

        //校验成功，将信息存入缓存
        String value;
        try {
            value = AESUtil.aesEncrypt(captchaVO.getToken().concat("---").concat(userCode), secretKey);
        } catch (Exception e) {
            logger.error("AES加密失败", e);
            afterValidateFail(captchaVO);
            return ResponseModel.errorMsg(e.getMessage());
        }
        String secondKey = String.format(REDIS_SECOND_CAPTCHA_KEY, value);
        CaptchaServiceFactory.getCache(cacheType).set(secondKey, captchaVO.getToken(), EXPIRESIN_THREE);
        captchaVO.setResult(true);
        captchaVO.resetClientFlag();
        return ResponseModel.successData(captchaVO);
    }

    @Override
    public ResponseModel verification(CaptchaVO captchaVO) {
        ResponseModel r = super.verification(captchaVO);
        if (!validatedReq(r)) {
            return r;
        }
        try {
            String codeKey = String.format(REDIS_SECOND_CAPTCHA_KEY, captchaVO.getCaptchaVerification());
            if (!CaptchaServiceFactory.getCache(cacheType).exists(codeKey)) {
                return ResponseModel.errorMsg(RepCodeEnum.API_CAPTCHA_INVALID);
            }
            //二次校验取值后，即刻失效
            CaptchaServiceFactory.getCache(cacheType).delete(codeKey);
        } catch (Exception e) {
            logger.error("验证码解析失败", e);
            return ResponseModel.errorMsg(e.getMessage());
        }
        return ResponseModel.success();
    }


    private CaptchaVO getImageData(String text) {
        CaptchaVO dataVO = new CaptchaVO();
        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();

        // 设置背景色
        g.setColor(getRandomColor(200, 250));
        g.fillRect(0, 0, WIDTH, HEIGHT);

        // 绘制干扰线
        for (int i = 0; i < LINES; i++) {
            g.setColor(getRandomColor(100, 200));
            int x1 = RANDOM.nextInt(WIDTH);
            int y1 = RANDOM.nextInt(HEIGHT);
            int x2 = RANDOM.nextInt(WIDTH);
            int y2 = RANDOM.nextInt(HEIGHT);
            g.drawLine(x1, y1, x2, y2);
        }

        // 设置字体
        g.setFont(new Font("Arial", Font.BOLD, 24));

        // 绘制验证码文本
        for (int i = 0; i < text.length(); i++) {
            g.setColor(getRandomColor(20, 130));

            // 文字旋转
            AffineTransform affineTransform = new AffineTransform();
            int x = 20 + i * 20;
            int y = 24 + RANDOM.nextInt(8);
            // 旋转范围 -45 ~ 45
            affineTransform.setToRotation(Math.toRadians(RandomUtils.getRandomInt(-45, 45)), x, y);
            g.setTransform(affineTransform);

            g.drawString(text.charAt(i) + "", x, y);
        }

        // 添加噪点
        for (int i = 0; i < 100; i++) {
            int x = RANDOM.nextInt(WIDTH);
            int y = RANDOM.nextInt(HEIGHT);
            image.setRGB(x, y, getRandomColor(0, 255).getRGB());
        }

        g.dispose();

        String secretKey = null;
        if (captchaAesStatus) {
            secretKey = AESUtil.getKey();
        }
        dataVO.setSecretKey(secretKey);

        dataVO.setOriginalImageBase64(ImageUtils.getImageToBase64Str(image).replaceAll("\r|\n", ""));
        dataVO.setToken(RandomUtils.getUUID());
//        dataVO.setSecretKey(secretKey);
        //将坐标信息存入redis中
        String codeKey = String.format(REDIS_CAPTCHA_KEY, dataVO.getToken());
        CaptchaServiceFactory.getCache(cacheType).set(codeKey, getCodeValue(text, secretKey), EXPIRESIN_SECONDS);
        return dataVO;
    }

    private String getCodeValue(String text, String secretKey) {
        return text + "," + secretKey;
    }

    private String getCodeByCodeValue(String codeValue) {
        return codeValue.split(",")[0];
    }

    private String getSecretKeyByCodeValue(String codeValue) {
        return codeValue.split(",")[1];
    }

    private Color getRandomColor(int min, int max) {
        if (min > max) {
            int temp = min;
            min = max;
            max = temp;
        }
        int r = min + RANDOM.nextInt(max - min);
        int g = min + RANDOM.nextInt(max - min);
        int b = min + RANDOM.nextInt(max - min);
        return new Color(r, g, b);
    }

    /**
     * 生成指定长度的随机字符串
     *
     * @param length 长度
     * @return {@link String}
     * @author Rex
     * @since 2025/6/26 15:20
     */
    public static String generateRandomText(int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int index = RANDOM.nextInt(CHARACTERS.length());
            sb.append(CHARACTERS.charAt(index));
        }
        return sb.toString();
    }

}