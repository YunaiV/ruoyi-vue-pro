package cn.iocoder.yudao.module.ai.framework.ai.core.video.tongyi;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 阿里云百炼 HappyHorse 视频生成的请求
 *
 * 三个模型系列各自只支持一种方式：
 * 1. t2v（happyhorse-1.1-t2v、happyhorse-1.0-t2v）：文生，prompt 必填，可指定 ratio
 * 2. i2v（happyhorse-1.1-i2v、happyhorse-1.0-i2v）：首帧图生，firstFrame 必填，prompt 可选，不支持 ratio（跟随首帧）
 * 3. r2v（happyhorse-1.1-r2v、happyhorse-1.0-r2v）：多图参考，referenceImages 1 ~ 9 张，prompt 必填，用 [Image N] 指代第 N 张
 *
 * @author 芋道源码
 */
@Data
public class HappyHorseVideoRequest {

    private String model;
    /**
     * 提示词：中文最多 2500 字、非中文最多 5000 字符，超出部分会被服务端截断
     */
    private String prompt;
    /**
     * 首帧图片：公网 URL 或 data:{mime};base64,{内容}；只用于 i2v
     */
    private String firstFrame;
    /**
     * 参考图：1 ~ 9 张；只用于 r2v
     */
    private List<String> referenceImages = new ArrayList<>();
    /**
     * 分辨率：480P、720P、1080P（默认）
     */
    private String resolution;
    /**
     * 比例：16:9（默认）、9:16、1:1、4:3、3:4、4:5、5:4、9:21、21:9；i2v 不支持
     */
    private String ratio;
    /**
     * 时长（秒）：3 ~ 15，默认 5
     */
    private Integer duration;
    /**
     * 是否添加水印，服务端默认 true
     */
    private Boolean watermark;
    /**
     * 随机种子：0 ~ 2147483647
     */
    private Long seed;

}
