/*
 *Copyright © 2018 anji-plus
 *安吉加加信息技术有限公司
 *http://www.anji-plus.com
 *All rights reserved.
 */
package com.anji.captcha.service.impl;

import cn.hutool.core.util.StrUtil;
import com.anji.captcha.model.common.CaptchaTypeEnum;
import com.anji.captcha.model.common.Const;
import com.anji.captcha.model.common.RepCodeEnum;
import com.anji.captcha.model.common.ResponseModel;
import com.anji.captcha.model.vo.CaptchaVO;
import com.anji.captcha.model.vo.PointVO;
import com.anji.captcha.util.*;
import lombok.extern.slf4j.Slf4j;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.*;

/**
 * 点选文字验证码
 * <p>
 * Created by raodeming on 2019/12/25.
 */
@Slf4j
public class ClickWordCaptchaServiceImpl extends AbstractCaptchaService {

    public static String HAN_ZI = "\u7684\u4e00\u4e86\u662f\u6211\u4e0d\u5728\u4eba\u4eec\u6709\u6765\u4ed6\u8fd9\u4e0a\u7740\u4e2a\u5730\u5230\u5927\u91cc\u8bf4\u5c31\u53bb\u5b50\u5f97\u4e5f\u548c\u90a3\u8981\u4e0b\u770b\u5929\u65f6\u8fc7\u51fa\u5c0f\u4e48\u8d77\u4f60\u90fd\u628a\u597d\u8fd8\u591a\u6ca1\u4e3a\u53c8\u53ef\u5bb6\u5b66\u53ea\u4ee5\u4e3b\u4f1a\u6837\u5e74\u60f3\u751f\u540c\u8001\u4e2d\u5341\u4ece\u81ea\u9762\u524d\u5934\u9053\u5b83\u540e\u7136\u8d70\u5f88\u50cf\u89c1\u4e24\u7528\u5979\u56fd\u52a8\u8fdb\u6210\u56de\u4ec0\u8fb9\u4f5c\u5bf9\u5f00\u800c\u5df1\u4e9b\u73b0\u5c71\u6c11\u5019\u7ecf\u53d1\u5de5\u5411\u4e8b\u547d\u7ed9\u957f\u6c34\u51e0\u4e49\u4e09\u58f0\u4e8e\u9ad8\u624b\u77e5\u7406\u773c\u5fd7\u70b9\u5fc3\u6218\u4e8c\u95ee\u4f46\u8eab\u65b9\u5b9e\u5403\u505a\u53eb\u5f53\u4f4f\u542c\u9769\u6253\u5462\u771f\u5168\u624d\u56db\u5df2\u6240\u654c\u4e4b\u6700\u5149\u4ea7\u60c5\u8def\u5206\u603b\u6761\u767d\u8bdd\u4e1c\u5e2d\u6b21\u4eb2\u5982\u88ab\u82b1\u53e3\u653e\u513f\u5e38\u6c14\u4e94\u7b2c\u4f7f\u5199\u519b\u5427\u6587\u8fd0\u518d\u679c\u600e\u5b9a\u8bb8\u5feb\u660e\u884c\u56e0\u522b\u98de\u5916\u6811\u7269\u6d3b\u90e8\u95e8\u65e0\u5f80\u8239\u671b\u65b0\u5e26\u961f\u5148\u529b\u5b8c\u5374\u7ad9\u4ee3\u5458\u673a\u66f4\u4e5d\u60a8\u6bcf\u98ce\u7ea7\u8ddf\u7b11\u554a\u5b69\u4e07\u5c11\u76f4\u610f\u591c\u6bd4\u9636\u8fde\u8f66\u91cd\u4fbf\u6597\u9a6c\u54ea\u5316\u592a\u6307\u53d8\u793e\u4f3c\u58eb\u8005\u5e72\u77f3\u6ee1\u65e5\u51b3\u767e\u539f\u62ff\u7fa4\u7a76\u5404\u516d\u672c\u601d\u89e3\u7acb\u6cb3\u6751\u516b\u96be\u65e9\u8bba\u5417\u6839\u5171\u8ba9\u76f8\u7814\u4eca\u5176\u4e66\u5750\u63a5\u5e94\u5173\u4fe1\u89c9\u6b65\u53cd\u5904\u8bb0\u5c06\u5343\u627e\u4e89\u9886\u6216\u5e08\u7ed3\u5757\u8dd1\u8c01\u8349\u8d8a\u5b57\u52a0\u811a\u7d27\u7231\u7b49\u4e60\u9635\u6015\u6708\u9752\u534a\u706b\u6cd5\u9898\u5efa\u8d76\u4f4d\u5531\u6d77\u4e03\u5973\u4efb\u4ef6\u611f\u51c6\u5f20\u56e2\u5c4b\u79bb\u8272\u8138\u7247\u79d1\u5012\u775b\u5229\u4e16\u521a\u4e14\u7531\u9001\u5207\u661f\u5bfc\u665a\u8868\u591f\u6574\u8ba4\u54cd\u96ea\u6d41\u672a\u573a\u8be5\u5e76\u5e95\u6df1\u523b\u5e73\u4f1f\u5fd9\u63d0\u786e\u8fd1\u4eae\u8f7b\u8bb2\u519c\u53e4\u9ed1\u544a\u754c\u62c9\u540d\u5440\u571f\u6e05\u9633\u7167\u529e\u53f2\u6539\u5386\u8f6c\u753b\u9020\u5634\u6b64\u6cbb\u5317\u5fc5\u670d\u96e8\u7a7f\u5185\u8bc6\u9a8c\u4f20\u4e1a\u83dc\u722c\u7761\u5174\u5f62\u91cf\u54b1\u89c2\u82e6\u4f53\u4f17\u901a\u51b2\u5408\u7834\u53cb\u5ea6\u672f\u996d\u516c\u65c1\u623f\u6781\u5357\u67aa\u8bfb\u6c99\u5c81\u7ebf\u91ce\u575a\u7a7a\u6536\u7b97\u81f3\u653f\u57ce\u52b3\u843d\u94b1\u7279\u56f4\u5f1f\u80dc\u6559\u70ed\u5c55\u5305\u6b4c\u7c7b\u6e10\u5f3a\u6570\u4e61\u547c\u6027\u97f3\u7b54\u54e5\u9645\u65e7\u795e\u5ea7\u7ae0\u5e2e\u5566\u53d7\u7cfb\u4ee4\u8df3\u975e\u4f55\u725b\u53d6\u5165\u5cb8\u6562\u6389\u5ffd\u79cd\u88c5\u9876\u6025\u6797\u505c\u606f\u53e5\u533a\u8863\u822c\u62a5\u53f6\u538b\u6162\u53d4\u80cc\u7ec6";

    protected static String clickWordFontStr = "NotoSerif-Light.ttf";

    protected Font clickWordFont;//点选文字字体

    @Override
    public String captchaType() {
        return CaptchaTypeEnum.CLICKWORD.getCodeValue();
    }

    @Override
    public void init(Properties config) {
        super.init(config);
        clickWordFontStr = config.getProperty(Const.CAPTCHA_FONT_TYPE, "SourceHanSansCN-Normal.otf");
        try {
            int size = Integer.parseInt(config.getProperty(Const.CAPTCHA_FONT_SIZE,HAN_ZI_SIZE+""));

            if (clickWordFontStr.toLowerCase().endsWith(".ttf")
                    || clickWordFontStr.toLowerCase().endsWith(".ttc")
                    || clickWordFontStr.toLowerCase().endsWith(".otf")) {
                this.clickWordFont = Font.createFont(Font.TRUETYPE_FONT,
                                Objects.requireNonNull(getClass().getResourceAsStream("/fonts/" + clickWordFontStr)))
                        .deriveFont(Font.BOLD, size);
            } else {
                int style = Integer.parseInt(config.getProperty(Const.CAPTCHA_FONT_STYLE,Font.BOLD+""));
                this.clickWordFont = new Font(clickWordFontStr, style, size);
            }
        } catch (Exception ex) {
            log.error("load font error:{}", ex);
        }
        this.wordTotalCount = Integer.parseInt(config.getProperty(Const.CAPTCHA_WORD_COUNT,"4"));
    }

    @Override
    public void destroy(Properties config) {
        log.info("start-clear-history-data-", captchaType());
    }

    @Override
    public ResponseModel get(CaptchaVO captchaVO) {
        ResponseModel r = super.get(captchaVO);
        if (!validatedReq(r)) {
            return r;
        }
        BufferedImage bufferedImage = ImageUtils.getPicClick();
        if (null == bufferedImage) {
            log.error("滑动底图未初始化成功，请检查路径");
            return ResponseModel.errorMsg(RepCodeEnum.API_CAPTCHA_BASEMAP_NULL);
        }
        CaptchaVO imageData = getImageData(bufferedImage);
        if (imageData == null
                || StrUtil.isBlank(imageData.getOriginalImageBase64())) {
            return ResponseModel.errorMsg(RepCodeEnum.API_CAPTCHA_ERROR);
        }
        return ResponseModel.successData(imageData);
    }

    @Override
    public ResponseModel check(CaptchaVO captchaVO) {
        ResponseModel r = super.check(captchaVO);
        if (!validatedReq(r)) {
            return r;
        }
        //取坐标信息
        String codeKey = String.format(REDIS_CAPTCHA_KEY, captchaVO.getToken());
        if (!CaptchaServiceFactory.getCache(cacheType).exists(codeKey)) {
            return ResponseModel.errorMsg(RepCodeEnum.API_CAPTCHA_INVALID);
        }
        String s = CaptchaServiceFactory.getCache(cacheType).get(codeKey);
        //验证码只用一次，即刻失效
        CaptchaServiceFactory.getCache(cacheType).delete(codeKey);
        List<PointVO> point = null;
        List<PointVO> point1 = null;
        String pointJson = null;
        /**
         * [
         *             {
         *                 "x": 85.0,
         *                 "y": 34.0
         *             },
         *             {
         *                 "x": 129.0,
         *                 "y": 56.0
         *             },
         *             {
         *                 "x": 233.0,
         *                 "y": 27.0
         *             }
         * ]
         */
        try {
            point = JsonUtil.parseArray(s, PointVO.class);
            //aes解密
            pointJson = decrypt(captchaVO.getPointJson(), point.get(0).getSecretKey());
            point1 = JsonUtil.parseArray(pointJson, PointVO.class);
        } catch (Exception e) {
            log.error("验证码坐标解析失败", e);
            afterValidateFail(captchaVO);
            return ResponseModel.errorMsg(e.getMessage());
        }
        for (int i = 0; i < point.size(); i++) {
            if (point.get(i).x - HAN_ZI_SIZE > point1.get(i).x
                    || point1.get(i).x > point.get(i).x + HAN_ZI_SIZE
                    || point.get(i).y - HAN_ZI_SIZE > point1.get(i).y
                    || point1.get(i).y > point.get(i).y + HAN_ZI_SIZE) {
                afterValidateFail(captchaVO);
                return ResponseModel.errorMsg(RepCodeEnum.API_CAPTCHA_COORDINATE_ERROR);
            }
        }
        //校验成功，将信息存入缓存
        String secretKey = point.get(0).getSecretKey();
        String value = null;
        try {
            value = AESUtil.aesEncrypt(captchaVO.getToken().concat("---").concat(pointJson), secretKey);
        } catch (Exception e) {
            log.error("AES加密失败", e);
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
        /*if (captchaVO == null) {
            return RepCodeEnum.NULL_ERROR.parseError("captchaVO");
        }
        if (StrUtil.isEmpty(captchaVO.getCaptchaVerification())) {
            return RepCodeEnum.NULL_ERROR.parseError("captchaVerification");
        }*/
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
            log.error("验证码坐标解析失败", e);
            return ResponseModel.errorMsg(e.getMessage());
        }
        return ResponseModel.success();
    }

    public int getWordTotalCount() {
        return wordTotalCount;
    }

    public void setWordTotalCount(int wordTotalCount) {
        this.wordTotalCount = wordTotalCount;
    }

    public boolean isFontColorRandom() {
        return fontColorRandom;
    }

    public void setFontColorRandom(boolean fontColorRandom) {
        this.fontColorRandom = fontColorRandom;
    }

    /**
     * 点选文字 字体总个数
     */
    private int wordTotalCount = 4;
    /**
     * 点选文字 字体颜色是否随机
     */
    private boolean fontColorRandom = Boolean.TRUE;

    private CaptchaVO getImageData(BufferedImage backgroundImage) {
        CaptchaVO dataVO = new CaptchaVO();
        List<String> wordList = new ArrayList<String>();
        List<PointVO> pointList = new ArrayList();

        Graphics backgroundGraphics = backgroundImage.getGraphics();
        int width = backgroundImage.getWidth();
        int height = backgroundImage.getHeight();

        int wordCount = getWordTotalCount();
        //定义随机1到arr.length某一个字不参与校验
        int num = RandomUtils.getRandomInt(1, wordCount);
        Set<String> currentWords = getRandomWords(wordCount);
        String secretKey = null;
        if (captchaAesStatus) {
            secretKey = AESUtil.getKey();
        }
        /*for (int i = 0; i < wordCount; i++) {
            String word;
            do {
                word = RandomUtils.getRandomHan(HAN_ZI);
                currentWords.add(word);
            } while (!currentWords.contains(word));*/
        int i = 0;
        for (String word : currentWords) {
            //随机字体坐标
            PointVO point = randomWordPoint(width, height, i, wordCount);
            point.setSecretKey(secretKey);
            //随机字体颜色
            if (isFontColorRandom()) {
                backgroundGraphics.setColor(new Color(RandomUtils.getRandomInt(1, 255),
                        RandomUtils.getRandomInt(1, 255), RandomUtils.getRandomInt(1, 255)));
            } else {
                backgroundGraphics.setColor(Color.BLACK);
            }
            //设置角度
            AffineTransform affineTransform = new AffineTransform();
            affineTransform.rotate(Math.toRadians(RandomUtils.getRandomInt(-45, 45)), 0, 0);
            Font rotatedFont = clickWordFont.deriveFont(affineTransform);
            backgroundGraphics.setFont(rotatedFont);
            backgroundGraphics.drawString(word, point.getX(), point.getY());

            if ((num - 1) != i) {
                wordList.add(word);
                pointList.add(point);
            }
            i++;
        }

        backgroundGraphics.setFont(waterMarkFont);
        backgroundGraphics.setColor(Color.white);
        backgroundGraphics.drawString(waterMark, width - getEnOrChLength(waterMark), height - (HAN_ZI_SIZE / 2) + 7);

        //创建合并图片
        BufferedImage combinedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics combinedGraphics = combinedImage.getGraphics();
        combinedGraphics.drawImage(backgroundImage, 0, 0, null);

        dataVO.setOriginalImageBase64(ImageUtils.getImageToBase64Str(backgroundImage).replaceAll("\r|\n", ""));
        //pointList信息不传到前端，只做后端check校验
        //dataVO.setPointList(pointList);
        dataVO.setWordList(wordList);
        dataVO.setToken(RandomUtils.getUUID());
        dataVO.setSecretKey(secretKey);
        //将坐标信息存入redis中
        String codeKey = String.format(REDIS_CAPTCHA_KEY, dataVO.getToken());
        CaptchaServiceFactory.getCache(cacheType).set(codeKey, JsonUtil.toJSONString(pointList), EXPIRESIN_SECONDS);
//        base64StrToImage(getImageToBase64Str(backgroundImage), "D:\\点击.png");
        return dataVO;
    }

    private Set<String> getRandomWords(int wordCount) {
        Set<String> words = new HashSet<>();
        int size = HAN_ZI.length();
        for (; ; ) {
            String t = HAN_ZI.charAt(RandomUtils.getRandomInt(size)) + "";
            words.add(t);
            if (words.size() >= wordCount) {
                break;
            }
        }
        return words;
    }

    /**
     * 随机字体循环排序下标
     *
     * @param imageWidth    图片宽度
     * @param imageHeight   图片高度
     * @param wordSortIndex 字体循环排序下标(i)
     * @param wordCount     字数量
     * @return
     */
    private static PointVO randomWordPoint(int imageWidth, int imageHeight, int wordSortIndex, int wordCount) {
        int avgWidth = imageWidth / (wordCount + 1);
        int x, y;
        if (avgWidth < HAN_ZI_SIZE_HALF) {
            x = RandomUtils.getRandomInt(1 + HAN_ZI_SIZE_HALF, imageWidth);
        } else {
            if (wordSortIndex == 0) {
                x = RandomUtils.getRandomInt(1 + HAN_ZI_SIZE_HALF, avgWidth * (wordSortIndex + 1) - HAN_ZI_SIZE_HALF);
            } else {
                x = RandomUtils.getRandomInt(avgWidth * wordSortIndex + HAN_ZI_SIZE_HALF, avgWidth * (wordSortIndex + 1) - HAN_ZI_SIZE_HALF);
            }
        }
        y = RandomUtils.getRandomInt(HAN_ZI_SIZE, imageHeight - HAN_ZI_SIZE);
        return new PointVO(x, y, null);
    }


}
