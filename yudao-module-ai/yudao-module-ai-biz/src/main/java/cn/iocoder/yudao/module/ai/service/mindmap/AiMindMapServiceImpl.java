package cn.iocoder.yudao.module.ai.service.mindmap;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.ai.core.enums.AiPlatformEnum;
import cn.iocoder.yudao.framework.ai.core.util.AiUtils;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.tenant.core.util.TenantUtils;
import cn.iocoder.yudao.module.ai.controller.admin.mindmap.vo.AiMindMapGenerateReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.model.vo.chatRole.AiChatRolePageReqVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.mindmap.AiMindMapDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.model.AiChatModelDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.model.AiChatRoleDO;
import cn.iocoder.yudao.module.ai.dal.mysql.mindmap.AiMindMapMapper;
import cn.iocoder.yudao.module.ai.enums.ErrorCodeConstants;
import cn.iocoder.yudao.module.ai.service.model.AiApiKeyService;
import cn.iocoder.yudao.module.ai.service.model.AiChatModelService;
import cn.iocoder.yudao.module.ai.service.model.AiChatRoleService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.error;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * AI 写作 Service 实现类
 *
 * @author xiaoxin
 */
@Service
@Slf4j
public class AiMindMapServiceImpl implements AiMindMapService {

    @Resource
    private AiApiKeyService apiKeyService;
    @Resource
    private AiChatModelService chatModalService;
    @Resource
    private AiChatRoleService chatRoleService;

    @Resource
    private AiMindMapMapper mindMapMapper;

    private static final String DEFAULT_SYSTEM_MESSAGE = """
             你是一位非常优秀的思维导图助手，你会把用户的所有提问都总结成思维导图，然后以 Markdown 格式输出。markdown 只需要输出一级标题，二级标题，三级标题，四级标题，最多输出四级，除此之外不要输出任何其他 markdown 标记。下面是一个合格的例子：
             # Geek-AI 助手
             
             ## 完整的开源系统
             ### 前端开源
             ### 后端开源
                      
             ## 支持各种大模型
             ### OpenAI
             ### Azure
             ### 文心一言
             ### 通义千问
                        
             ## 集成多种收费方式
             ### 支付宝
             ### 微信
                       
             另外，除此之外不要任何解释性语句。
            """;

    @Override
    public Flux<CommonResult<String>> generateMindMap(AiMindMapGenerateReqVO generateReqVO, Long userId) {
        // 1.1 获取脑图模型 尝试获取思维导图助手角色，如果没有则使用默认模型
        AiChatRoleDO mindMapRole = selectOneMindMapRole();
        AiChatModelDO model;
        String systemMessage;
        if (Objects.nonNull(mindMapRole)) {
            model = chatModalService.getChatModel(mindMapRole.getModelId());
            systemMessage = mindMapRole.getSystemMessage();
        } else {
            model = chatModalService.getRequiredDefaultChatModel();
            systemMessage = DEFAULT_SYSTEM_MESSAGE;
        }

        AiPlatformEnum platform = AiPlatformEnum.validatePlatform(model.getPlatform());
        ChatModel chatModel = apiKeyService.getChatModel(model.getKeyId());

        // 1.2 插入思维导图信息
        AiMindMapDO mindMapDO = BeanUtils.toBean(generateReqVO, AiMindMapDO.class, e -> e.setUserId(userId).setModel(model.getModel()).setPlatform(platform.getPlatform()));
        mindMapMapper.insert(mindMapDO);

        ChatOptions chatOptions = AiUtils.buildChatOptions(platform, model.getModel(), model.getTemperature(), model.getMaxTokens());
        // 2.1 角色设定
        List<Message> chatMessages = new ArrayList<>();
        if (StrUtil.isNotBlank(systemMessage)) {
            chatMessages.add(new SystemMessage(systemMessage));
        }
        // 2.2 用户输入
        chatMessages.add(new UserMessage(generateReqVO.getPrompt()));
        // 2.3 构建提示词
        Prompt prompt = new Prompt(chatMessages, chatOptions);

        Flux<ChatResponse> streamResponse = chatModel.stream(prompt);
        // 2.4 流式返回
        StringBuffer contentBuffer = new StringBuffer();
        return streamResponse.map(chunk -> {
            String newContent = chunk.getResult() != null ? chunk.getResult().getOutput().getContent() : null;
            newContent = StrUtil.nullToDefault(newContent, ""); // 避免 null 的 情况
            contentBuffer.append(newContent);
            // 响应结果
            return success(newContent);
        }).doOnComplete(() -> {
            // 忽略租户，因为 Flux 异步无法透传租户
            TenantUtils.executeIgnore(() ->
                    mindMapMapper.updateById(new AiMindMapDO().setId(mindMapDO.getId()).setGeneratedContent(contentBuffer.toString())));
        }).doOnError(throwable -> {
            log.error("[generateWriteContent][generateReqVO({}) 发生异常]", generateReqVO, throwable);
            // 忽略租户，因为 Flux 异步无法透传租户
            TenantUtils.executeIgnore(() ->
                    mindMapMapper.updateById(new AiMindMapDO().setId(mindMapDO.getId()).setErrorMessage(throwable.getMessage())));
        }).onErrorResume(error -> Flux.just(error(ErrorCodeConstants.WRITE_STREAM_ERROR)));

    }

    private AiChatRoleDO selectOneMindMapRole() {
        AiChatRoleDO chatRoleDO = null;
        PageResult<AiChatRoleDO> mindMapRolePage = chatRoleService.getChatRolePage(new AiChatRolePageReqVO().setName("思维导图助手"));
        List<AiChatRoleDO> list = mindMapRolePage.getList();
        if (CollUtil.isNotEmpty(list)) {
            chatRoleDO = list.get(0);
        }
        return chatRoleDO;
    }
}
