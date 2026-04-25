package cn.iocoder.yudao.module.ai.service.image;

/**
 * AI 服装设计 NLP 意图解析服务接口
 *
 * <p>将用户的任意输入（一个字、短语、完整句子）解析为结构化设计意图，
 * 是智能交互的核心入口。</p>
 *
 * @author deepay
 */
public interface AiFashionNlpParserService {

    /**
     * 解析用户输入，提取意图与实体
     *
     * @param rawInput   用户原始输入（可以是单字"红"，短语"改短一点"，或完整句"给我出5款春夏连衣裙"）
     * @param sessionCtx 当前会话上下文（含上一次完整 prompt、颜色、风格等），可为 null（无会话时）
     * @return 解析结果，包含意图类型、提取的实体、构建好的 SD Prompt
     */
    AiFashionIntentParseResult parse(String rawInput, AiFashionSessionContext sessionCtx);

}
