package cn.iocoder.yudao.module.ai.service.model;

import cn.iocoder.yudao.module.ai.framework.ai.core.model.midjourney.api.MidjourneyApi;
import cn.iocoder.yudao.module.ai.framework.ai.core.model.suno.api.SunoApi;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.ai.controller.admin.model.vo.model.AiModelPageReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.model.vo.model.AiModelSaveReqVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.model.AiModelDO;
import cn.iocoder.yudao.module.ai.enums.model.AiPlatformEnum;
import dev.tinyflow.core.Tinyflow;
import jakarta.validation.Valid;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.image.ImageModel;
import org.springframework.ai.vectorstore.VectorStore;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

/**
 * AI 模型 Service 接口
 *
 * @author fansili
 * @since 2024/4/24 19:42
 */
public interface AiModelService {

    /**
     * 创建模型
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createModel(@Valid AiModelSaveReqVO createReqVO);

    /**
     * 更新模型
     *
     * @param updateReqVO 更新信息
     */
    void updateModel(@Valid AiModelSaveReqVO updateReqVO);

    /**
     * 删除模型
     *
     * @param id 编号
     */
    void deleteModel(Long id);

    /**
     * 获得模型
     *
     * @param id 编号
     * @return 模型
     */
    AiModelDO getModel(Long id);

    /**
     * 获得默认的模型
     *
     * 如果获取不到，则抛出 {@link cn.iocoder.yudao.framework.common.exception.ServiceException} 业务异常
     *
     * @return 模型
     */
    AiModelDO getRequiredDefaultModel(Integer type);

    /**
     * 获得模型分页
     *
     * @param pageReqVO 分页查询
     * @return 模型分页
     */
    PageResult<AiModelDO> getModelPage(AiModelPageReqVO pageReqVO);

    /**
     * 校验模型是否可使用
     *
     * @param id 编号
     * @return 模型
     */
    AiModelDO validateModel(Long id);

    /**
     * 获得模型列表
     *
     * @param status 状态
     * @param type 类型
     * @param platform 平台，允许空
     * @return 模型列表
     */
    List<AiModelDO> getModelListByStatusAndType(Integer status, Integer type,
                                                @Nullable String platform);

    // ========== 与 Spring AI 集成 ==========

    /**
     * 获得 ChatModel 对象
     *
     * @param id 编号
     * @return ChatModel 对象
     */
    ChatModel getChatModel(Long id);

    /**
     * 获得 ChatModel 对象，并带上本次的思考参数
     *
     * 参数为 null、或平台不走 HTTP 路由时，与 {@link #getChatModel(Long)} 完全一致
     *
     * @param id 编号
     * @param reasoningOptions 思考参数，可为 null
     * @return ChatModel 对象
     */
    ChatModel getChatModel(Long id, @Nullable Map<String, Object> reasoningOptions);

    /**
     * 校验思考配置使用的平台
     *
     * 1. 未配置思考：不查 API 密钥，直接返回模型平台，与现状一致
     * 2. 已配置思考：模型平台必须与 API 密钥平台一致，否则抛出 MODEL_REASONING_PLATFORM_MISMATCH
     *
     * @param model 模型
     * @return 平台；路由、客户端都使用这一个平台
     */
    AiPlatformEnum validateReasoningPlatform(AiModelDO model);

    /**
     * 获得 ImageModel 对象
     *
     * @param id 编号
     * @return ImageModel 对象
     */
    ImageModel getImageModel(Long id);

    /**
     * 获得 MidjourneyApi 对象
     *
     * @param id 编号
     * @return MidjourneyApi 对象
     */
    MidjourneyApi getMidjourneyApi(Long id);

    /**
     * 获得 SunoApi 对象
     *
     * @return SunoApi 对象
     */
    SunoApi getSunoApi();

    /**
     * 获得 VectorStore 对象
     *
     * @param id 编号
     * @param metadataFields 元数据的定义
     * @return VectorStore 对象
     */
    VectorStore getOrCreateVectorStore(Long id, Map<String, Class<?>> metadataFields);

    /**
     * 获取 TinyFlow 所需 LLm Provider
     *
     * @param tinyflow tinyflow
     * @param modelId AI 模型 ID
     */
    void getLLmProvider4Tinyflow(Tinyflow tinyflow, Long modelId);

}
