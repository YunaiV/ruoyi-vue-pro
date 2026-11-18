package cn.iocoder.yudao.module.ai.util;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileTypeUtil;
import cn.hutool.core.lang.Singleton;
import cn.hutool.core.lang.func.Func0;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.iocoder.yudao.module.ai.dal.dataobject.model.AiApiKeyDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.video.AiVideoDO;
import cn.iocoder.yudao.module.ai.enums.video.AiVideoInputRoleEnum;
import cn.iocoder.yudao.module.ai.enums.video.AiVideoModeEnum;
import cn.iocoder.yudao.module.ai.enums.video.AiVideoTypeEnum;
import cn.iocoder.yudao.module.ai.framework.ai.core.video.doubao.DouBaoSeedanceVideoApi;
import cn.iocoder.yudao.module.ai.framework.ai.core.video.jimeng.JimengVisualApi;
import cn.iocoder.yudao.module.ai.framework.ai.core.video.kling.KlingCredentials;
import cn.iocoder.yudao.module.ai.framework.ai.core.video.kling.KlingVideoApi;
import cn.iocoder.yudao.module.ai.framework.ai.core.video.minimax.MiniMaxH3VideoApi;
import cn.iocoder.yudao.module.ai.framework.ai.core.video.minimax.MiniMaxHailuoVideoApi;
import cn.iocoder.yudao.module.ai.framework.ai.core.video.tongyi.HappyHorseVideoApi;
import cn.iocoder.yudao.module.ai.framework.ai.core.video.tongyi.TongYiWanVideoApi;
import cn.iocoder.yudao.module.ai.framework.ai.core.video.veo.VertexVeoVideoApi;
import cn.iocoder.yudao.module.ai.framework.ai.core.video.veo.VertexVeoVideoRequest;

import java.io.ByteArrayInputStream;
import java.util.*;

/**
 * AI 视频的工具类：输入文件、服务商路由、客户端
 *
 * 服务商的请求校验在各客户端（framework/ai/core/video）里，这里只做按平台、模型的选择
 *
 * @author 芋道源码
 */
public class AiVideoUtils {

    // ========== 输入文件 ==========

    /**
     * 过滤输入文件：只保留类型、方式使用的角色，去掉空地址
     *
     * @param type 创作类型
     * @param mode 生成方式，只有视频生成使用
     * @param inputUrls 输入文件：键为角色，值为文件地址
     * @return 过滤后的输入文件；没有时为 null
     */
    public static Map<String, List<String>> filterInputUrls(Integer type, String mode, Map<String, List<String>> inputUrls) {
        if (inputUrls == null) {
            return null;
        }
        // 1. 类型、方式使用的角色：数字人口播为人物图片 + 驱动音频，动作模仿为人物图片 + 动作视频，视频生成按方式
        List<AiVideoInputRoleEnum> roles;
        if (Objects.equals(type, AiVideoTypeEnum.AVATAR.getType())) {
            roles = Arrays.asList(AiVideoInputRoleEnum.IMAGE, AiVideoInputRoleEnum.AUDIO);
        } else if (Objects.equals(type, AiVideoTypeEnum.MOTION.getType())) {
            roles = Arrays.asList(AiVideoInputRoleEnum.IMAGE, AiVideoInputRoleEnum.VIDEO);
        } else {
            roles = AiVideoModeEnum.of(mode).getInputRoles();
        }
        // 2. 逐个角色去掉空地址，没有地址的角色不保留
        Map<String, List<String>> result = new LinkedHashMap<>();
        for (AiVideoInputRoleEnum role : roles) {
            List<String> urls = CollUtil.removeBlank(CollUtil.newArrayList(
                    inputUrls.getOrDefault(role.getRole(), Collections.emptyList())));
            if (CollUtil.isNotEmpty(urls)) {
                result.put(role.getRole(), urls);
            }
        }
        return result.isEmpty() ? null : result;
    }

    // ========== 服务商路由 ==========

    /**
     * 是否走即梦 Visual：数字人口播、动作模仿，或 jimeng_ 开头的视频模型；字节跳动的其余模型走方舟 Seedance
     */
    public static boolean isJimeng(AiVideoDO video) {
        return ObjUtil.notEqual(video.getType(), AiVideoTypeEnum.GENERATE.getType())
                || StrUtil.startWith(video.getModel(), "jimeng_");
    }

    /**
     * 是否可灵旧版接口：kling-v 开头的模型
     */
    public static boolean isKlingLegacy(String model) {
        return model != null && model.matches("kling-v\\d.*");
    }

    /**
     * 是否 MiniMax-H3：走 v2 接口，其余走海螺 v1 接口
     */
    public static boolean isMiniMaxH3(String model) {
        return StrUtil.startWith(model, MiniMaxH3VideoApi.MODEL_H3);
    }

    /**
     * 是否阿里 HappyHorse：其余走通义万相
     */
    public static boolean isHappyHorse(String model) {
        return StrUtil.startWith(model, "happyhorse");
    }

    /**
     * 校验 Gemini 平台的视频模型：只支持 Veo
     */
    public static void validateVeoModel(String model) {
        if (!StrUtil.startWith(model, "veo-")) {
            throw new IllegalArgumentException("Gemini 平台的视频生成只支持 Veo 模型（veo- 开头）");
        }
    }

    /**
     * 即梦视频 3.0 的帧数：5 秒为 121 帧，10 秒为 241 帧
     */
    public static Integer getJimengFrames(Integer duration) {
        if (duration == null) {
            return null;
        }
        return duration >= 10 ? 241 : 121;
    }

    /**
     * 构建 Veo 的图片：下载后以 base64 提交，只支持 PNG、JPEG
     */
    public static VertexVeoVideoRequest.Image buildVeoImage(String url) {
        if (StrUtil.isBlank(url)) {
            return null;
        }
        byte[] content = HttpUtil.downloadBytes(url);
        String type = FileTypeUtil.getType(new ByteArrayInputStream(content));
        String mimeType = "png".equals(type) ? "image/png" : StrUtil.equalsAny(type, "jpg", "jpeg") ? "image/jpeg" : null;
        if (mimeType == null) {
            throw new IllegalArgumentException("Veo 只支持 PNG、JPEG 图片");
        }
        return new VertexVeoVideoRequest.Image(Base64.encode(content), null, mimeType);
    }

    // ========== 客户端：按密钥、地址缓存，密钥修改后自动使用新的客户端 ==========

    public static DouBaoSeedanceVideoApi getSeedanceApi(AiApiKeyDO apiKey) {
        return getClient(DouBaoSeedanceVideoApi.class, apiKey,
                () -> new DouBaoSeedanceVideoApi(apiKey.getUrl(), apiKey.getApiKey()));
    }

    /**
     * 获得即梦客户端：密钥为 AccessKeyId|SecretAccessKey 时使用签名方式，否则为 IAM API Key
     */
    public static JimengVisualApi getJimengApi(AiApiKeyDO apiKey) {
        List<String> parts = StrUtil.split(apiKey.getApiKey(), '|', true, true);
        return getClient(JimengVisualApi.class, apiKey, () -> parts.size() == 2
                ? JimengVisualApi.ofAccessKey(parts.get(0), parts.get(1), apiKey.getUrl(), null)
                : JimengVisualApi.ofApiKey(apiKey.getApiKey(), apiKey.getUrl(), null));
    }

    /**
     * 获得可灵客户端：密钥为 AccessKey|SecretKey 时使用 JWT 签名，否则为 API Key
     */
    public static KlingVideoApi getKlingApi(AiApiKeyDO apiKey) {
        List<String> parts = StrUtil.split(apiKey.getApiKey(), '|', true, true);
        return getClient(KlingVideoApi.class, apiKey, () -> new KlingVideoApi(apiKey.getUrl(), parts.size() == 2
                ? KlingCredentials.ofAccessKey(parts.get(0), parts.get(1))
                : KlingCredentials.ofApiKey(apiKey.getApiKey())));
    }

    public static MiniMaxH3VideoApi getMiniMaxH3Api(AiApiKeyDO apiKey) {
        return getClient(MiniMaxH3VideoApi.class, apiKey,
                () -> new MiniMaxH3VideoApi(apiKey.getUrl(), apiKey.getApiKey()));
    }

    public static MiniMaxHailuoVideoApi getHailuoApi(AiApiKeyDO apiKey) {
        return getClient(MiniMaxHailuoVideoApi.class, apiKey,
                () -> new MiniMaxHailuoVideoApi(apiKey.getUrl(), apiKey.getApiKey()));
    }

    public static HappyHorseVideoApi getHappyHorseApi(AiApiKeyDO apiKey) {
        return getClient(HappyHorseVideoApi.class, apiKey,
                () -> new HappyHorseVideoApi(apiKey.getUrl(), apiKey.getApiKey()));
    }

    public static TongYiWanVideoApi getTongYiWanApi(AiApiKeyDO apiKey) {
        return getClient(TongYiWanVideoApi.class, apiKey,
                () -> new TongYiWanVideoApi(apiKey.getUrl(), apiKey.getApiKey()));
    }

    /**
     * 获得 Veo 客户端：密钥的地址为 Vertex AI 地址（含项目、地域），API 密钥为 OAuth 访问令牌
     */
    public static VertexVeoVideoApi getVeoApi(AiApiKeyDO apiKey) {
        return getClient(VertexVeoVideoApi.class, apiKey,
                () -> VertexVeoVideoApi.ofVertexUrl(apiKey.getUrl(), apiKey::getApiKey));
    }

    private static <T> T getClient(Class<T> clazz, AiApiKeyDO apiKey, Func0<T> supplier) {
        String cacheKey = StrUtil.join(":", clazz.getName(), apiKey.getApiKey(), StrUtil.nullToEmpty(apiKey.getUrl()));
        return Singleton.get(cacheKey, supplier);
    }

}
