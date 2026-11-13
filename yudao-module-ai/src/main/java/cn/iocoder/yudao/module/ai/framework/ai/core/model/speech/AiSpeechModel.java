package cn.iocoder.yudao.module.ai.framework.ai.core.model.speech;

import java.util.function.Consumer;

/**
 * AI 语音模型：业务统一按「调用一次，拿到完整音频」使用
 *
 * 1. 同步合成的平台（通义、MiniMax、豆包、百度）：{@link AbstractAiSpeechModel}，一次请求拿到音频
 * 2. 原生为异步任务的平台（腾讯云长文本）：提交一次，交出任务编号后，在时限内等待结果；
 *    协议细节由各自的实现处理，业务不感知
 *
 * @author 芋道源码
 */
public interface AiSpeechModel {

    /**
     * 校验合成参数：不发请求。业务在保存记录、付费调用之前提前拒绝
     *
     * @param text 朗读文本
     * @param voice 音色
     * @param format 音频格式，为空时为默认格式
     * @param speed 语速倍数，为空时为 1
     * @throws IllegalArgumentException 参数不合法、不支持
     */
    void validateOptions(String text, String voice, String format, Double speed);

    /**
     * 合成：只调用服务商一次，不自动重试；网络异常、超时时服务端是否已处理未知，调用方也不能重试
     *
     * @param text 朗读文本
     * @param voice 音色
     * @param format 音频格式，为空时为默认格式
     * @param speed 语速倍数，为空时为 1
     * @param taskIdConsumer 原生为异步任务时，提交成功后、等待结果前，回调任务编号，由调用方立即持久化；同步合成不回调
     * @return 完整音频；异步任务在时限内未完成时为 null，之后由调用方按任务编号继续查询，不能重新提交
     * @throws RuntimeException 合成失败（含任务失败）
     */
    byte[] synthesize(String text, String voice, String format, Double speed, Consumer<String> taskIdConsumer);

}
