package cn.iocoder.yudao.module.ai.framework.ai.core.video.jimeng;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 即梦 Visual 视频生成（视频生成 3.0 系列）的请求
 *
 * 注意：官方文档已将视频生成 3.0 / 3.0 Pro 标记为「下线中」，新接入优先使用豆包 Seedance（{@code DouBaoSeedanceVideoApi}）
 *
 * 图片数量由 req_key 决定：
 * 1. jimeng_t2v_v30：文生，不能带图
 * 2. jimeng_i2v_first_v30、jimeng_i2v_first_v30_1080：首帧，1 张
 * 3. jimeng_i2v_first_tail_v30：首尾帧，2 张（按首帧、尾帧顺序）
 * 4. jimeng_ti2v_v30_pro：文生或首帧，0 ~ 1 张
 *
 * @author 芋道源码
 */
@Data
public class JimengVideoRequest {

    private String reqKey;
    /**
     * 提示词：建议 400 字以内，不超过 800 字
     */
    private String prompt;
    /**
     * 图片地址，与 binaryDataBase64 二选一
     */
    private List<String> imageUrls = new ArrayList<>();
    /**
     * 图片 base64（不带 data: 前缀），与 imageUrls 二选一
     */
    private List<String> binaryDataBase64 = new ArrayList<>();
    /**
     * 随机种子，默认 -1
     */
    private Long seed;
    /**
     * 帧数：121（5 秒）或 241（10 秒）
     */
    private Integer frames;
    /**
     * 比例：只用于文生（16:9、4:3、1:1、3:4、9:16、21:9）
     */
    private String aspectRatio;

    @Override
    public String toString() {
        return "JimengVideoRequest{reqKey=" + reqKey + ", imageUrls=" + imageUrls.size()
                + ", binaryDataBase64=" + binaryDataBase64.size() + ", frames=" + frames + ", aspectRatio=" + aspectRatio + "}";
    }

}
