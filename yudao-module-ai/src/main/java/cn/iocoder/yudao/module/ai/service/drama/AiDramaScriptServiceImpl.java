package cn.iocoder.yudao.module.ai.service.drama;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.ai.controller.admin.drama.vo.script.AiDramaChatReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.drama.vo.script.AiDramaScriptPolishReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.drama.vo.script.AiDramaScriptUpdateReqVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.drama.*;
import cn.iocoder.yudao.module.ai.dal.dataobject.model.AiModelDO;
import cn.iocoder.yudao.module.ai.dal.mysql.drama.AiDramaScriptMapper;
import cn.iocoder.yudao.module.ai.enums.drama.AiDramaStatusEnum;
import cn.iocoder.yudao.module.ai.enums.model.AiModelTypeEnum;
import cn.iocoder.yudao.module.ai.enums.model.AiPlatformEnum;
import cn.iocoder.yudao.module.ai.service.model.AiModelService;
import cn.iocoder.yudao.module.ai.util.AiChatUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.annotations.VisibleForTesting;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.deepseek.DeepSeekChatOptions;
import org.springframework.ai.deepseek.api.ResponseFormat;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.*;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.*;
import static cn.iocoder.yudao.module.ai.enums.ErrorCodeConstants.*;

/**
 * AI 短剧剧本 Service 实现类
 *
 * 每个项目一份剧本；生成、打磨、分析、拆分都同步调用对话模型
 *
 * @author 芋道源码
 */
@Service
@Validated
@Slf4j
public class AiDramaScriptServiceImpl implements AiDramaScriptService {

    private static final String SCRIPT_JSON_FORMAT = """
            只输出一个 JSON 对象，不要输出其它内容，格式：
            {"title":"剧名","tone":"叙事基调，例如悬疑、温情","outline":"剧情大纲，300 字以内","content":"剧本正文：按场次书写，每场写明场景、人物动作与台词"}""";

    private static final String GENERATE_SCRIPT_PROMPT = """
            你是一名短剧编剧。请根据下面的故事想法，创作一部 1 到 3 分钟的短剧剧本。
            画风：{}
            故事想法：
            {}

            """ + SCRIPT_JSON_FORMAT;

    private static final String POLISH_SCRIPT_PROMPT = """
            你是一名短剧编剧。请按修改意见打磨下面的剧本，人物与主线保持不变。
            修改意见：{}
            剧名：{}
            叙事基调：{}
            剧情大纲：{}
            剧本正文：
            {}

            """ + SCRIPT_JSON_FORMAT;

    private static final String ANALYZE_ASSETS_PROMPT = """
            请从下面的短剧剧本中，提取需要保持形象统一的角色、场景与关键道具，用于后续出图。画风：{}
            剧本：
            {}

            只输出一个 JSON 对象，不要输出其它内容，格式：
            {"characters":[{"name":"角色名","description":"年龄、外貌、服装等设定","prompt":"用于出图的人物画面描述"}],\
            "locations":[{"name":"场景名","description":"场景设定","prompt":"用于出图的场景画面描述"}],\
            "props":[{"title":"道具名","prompt":"用于出图的道具画面描述"}]}""";

    private static final String GENERATE_STORYBOARDS_PROMPT = """
            你是一名短剧分镜师。请把下面的剧本拆分为镜头分镜，每个镜头 3 到 10 秒，总时长 1 到 3 分钟。
            画风：{}
            可用角色：{}
            可用场景：{}
            可用道具：{}
            剧本：
            {}

            只输出一个 JSON 数组，不要输出其它内容，每个元素的格式：
            {"sceneNo":场次数字,"title":"镜头标题","shotType":"景别","cameraMove":"运镜","duration":秒数,\
            "videoPrompt":"用于视频生成的画面描述，包含人物动作与环境","dialogues":[{"character":"角色名，旁白为空","line":"台词"}],\
            "characters":["出场角色名"],"location":"场景名","props":["道具名"]}""";

    @Resource
    private AiDramaScriptMapper scriptMapper;

    @Resource
    private AiDramaProjectService projectService;
    @Resource
    private AiDramaAssetService assetService;
    @Resource
    @Lazy // 延迟加载，避免循环依赖
    private AiDramaStoryboardService storyboardService;
    @Resource
    private AiModelService modelService;

    @Override
    public AiDramaScriptDO getScriptByProjectId(Long projectId) {
        return scriptMapper.selectByProjectId(projectId);
    }

    @Override
    public void updateScriptMy(Long userId, AiDramaScriptUpdateReqVO updateReqVO) {
        // 校验项目
        projectService.validateProjectMy(updateReqVO.getProjectId(), userId);
        // 保存：不存在时插入
        saveScript(updateReqVO.getProjectId(), BeanUtils.toBean(updateReqVO, AiDramaScriptDO.class));
    }

    @Override
    public AiDramaScriptDO generateScriptMy(Long userId, AiDramaChatReqVO reqVO) {
        // 1. 校验项目，调用对话模型：故事想法为空时使用项目名称
        AiDramaProjectDO project = projectService.validateProjectMy(reqVO.getProjectId(), userId);
        String content = chat("生成剧本", reqVO.getModelId(), true, StrUtil.format(GENERATE_SCRIPT_PROMPT,
                StrUtil.blankToDefault(project.getStyle(), "不限"), StrUtil.blankToDefault(project.getIdea(), project.getName())));

        // 2. 解析并保存
        return saveScript(project.getId(), parseScript("生成剧本", content));
    }

    @Override
    public AiDramaScriptDO polishScriptMy(Long userId, AiDramaScriptPolishReqVO reqVO) {
        // 1. 校验剧本，调用对话模型
        projectService.validateProjectMy(reqVO.getProjectId(), userId);
        AiDramaScriptDO script = validateScriptExists(reqVO.getProjectId());
        String content = chat("打磨剧本", reqVO.getModelId(), true, StrUtil.format(POLISH_SCRIPT_PROMPT, reqVO.getNote(),
                StrUtil.nullToEmpty(script.getTitle()), StrUtil.nullToEmpty(script.getTone()),
                StrUtil.nullToEmpty(script.getOutline()), script.getContent()));

        // 2. 解析并保存，追加本次的修改意见
        List<AiDramaScriptDO.RevisionNote> revisionNotes = new ArrayList<>(CollUtil.emptyIfNull(script.getRevisionNotes()));
        revisionNotes.add(new AiDramaScriptDO.RevisionNote().setNote(reqVO.getNote()).setTime(LocalDateTime.now()));
        return saveScript(reqVO.getProjectId(), parseScript("打磨剧本", content).setRevisionNotes(revisionNotes));
    }

    @Override
    public void analyzeAssetListMy(Long userId, AiDramaChatReqVO reqVO) {
        // 1. 校验剧本，调用对话模型
        AiDramaProjectDO project = projectService.validateProjectMy(reqVO.getProjectId(), userId);
        AiDramaScriptDO script = validateScriptExists(project.getId());
        JsonNode root = parseJson("分析资产", chat("分析资产", reqVO.getModelId(), true, StrUtil.format(ANALYZE_ASSETS_PROMPT,
                StrUtil.blankToDefault(project.getStyle(), "不限"), script.getContent())));

        // 2.1 角色：同时创建第 1 个形象
        Set<String> characterNames = new HashSet<>(convertSet(assetService.getCharacterList(project.getId()),
                AiDramaCharacterDO::getName));
        for (JsonNode node : root.path("characters")) {
            String name = StrUtil.trim(node.path("name").asText());
            if (StrUtil.isEmpty(name) || !characterNames.add(name)) {
                continue;
            }
            assetService.createCharacter(new AiDramaCharacterDO().setProjectId(project.getId()).setName(name)
                    .setDescription(textOrNull(node, "description")).setPrompt(textOrNull(node, "prompt")));
        }
        // 2.2 场景
        Set<String> locationNames = new HashSet<>(convertSet(assetService.getLocationList(project.getId()),
                AiDramaLocationDO::getName));
        for (JsonNode node : root.path("locations")) {
            String name = StrUtil.trim(node.path("name").asText());
            if (StrUtil.isEmpty(name) || !locationNames.add(name)) {
                continue;
            }
            assetService.createLocation(new AiDramaLocationDO().setProjectId(project.getId()).setName(name)
                    .setDescription(textOrNull(node, "description")).setPrompt(textOrNull(node, "prompt"))
                    .setCandidates(new ArrayList<>()));
        }
        // 2.3 道具
        Set<String> propTitles = new HashSet<>(convertSet(assetService.getVisualAssetList(project.getId()),
                AiDramaVisualAssetDO::getTitle));
        for (JsonNode node : root.path("props")) {
            String title = StrUtil.trim(node.path("title").asText());
            if (StrUtil.isEmpty(title) || !propTitles.add(title)) {
                continue;
            }
            assetService.createVisualAsset(new AiDramaVisualAssetDO().setProjectId(project.getId())
                    .setKind(AiDramaAssetServiceImpl.VISUAL_ASSET_KIND_PROP).setTitle(title)
                    .setPrompt(textOrNull(node, "prompt")));
        }
    }

    @Override
    public void generateStoryboardListMy(Long userId, AiDramaChatReqVO reqVO) {
        // 1. 校验：有镜头视频生成中时不允许替换
        AiDramaProjectDO project = projectService.validateProjectMy(reqVO.getProjectId(), userId);
        AiDramaScriptDO script = validateScriptExists(project.getId());
        List<AiDramaStoryboardDO> oldStoryboards = storyboardService.getStoryboardList(project.getId());
        if (CollUtil.contains(oldStoryboards, storyboard -> AiDramaStatusEnum.isRunning(storyboard.getVideoStatus()))) {
            throw exception(DRAMA_TASK_RUNNING);
        }

        // 2. 调用对话模型：提供可用的资产名称，便于按名称关联
        List<AiDramaCharacterDO> characters = assetService.getCharacterList(project.getId());
        List<AiDramaLocationDO> locations = assetService.getLocationList(project.getId());
        List<AiDramaVisualAssetDO> visualAssets = assetService.getVisualAssetList(project.getId());
        JsonNode root = parseJson("拆分分镜", chat("拆分分镜", reqVO.getModelId(), false, StrUtil.format(GENERATE_STORYBOARDS_PROMPT,
                StrUtil.blankToDefault(project.getStyle(), "不限"),
                joinNames(convertList(characters, AiDramaCharacterDO::getName)),
                joinNames(convertList(locations, AiDramaLocationDO::getName)),
                joinNames(convertList(visualAssets, AiDramaVisualAssetDO::getTitle)),
                script.getContent())));
        if (!root.isArray() || root.isEmpty()) {
            throw exception(DRAMA_CHAT_FAIL, "拆分分镜", "模型没有返回分镜");
        }

        // 3. 替换分镜：资产按名称关联；角色取第 1 个形象
        Map<String, AiDramaCharacterDO> characterMap = convertMap(characters, AiDramaCharacterDO::getName);
        Map<Long, Long> firstAppearanceMap = new HashMap<>();
        assetService.getAppearanceListByCharacterIds(convertList(characters, AiDramaCharacterDO::getId))
                .forEach(appearance -> firstAppearanceMap.putIfAbsent(appearance.getCharacterId(), appearance.getId()));
        Map<String, AiDramaLocationDO> locationMap = convertMap(locations, AiDramaLocationDO::getName);
        Map<String, AiDramaVisualAssetDO> visualAssetMap = convertMap(visualAssets, AiDramaVisualAssetDO::getTitle);
        List<AiDramaStoryboardDO> storyboards = new ArrayList<>();
        int index = 0;
        for (JsonNode node : root) {
            index++;
            List<AiDramaStoryboardDO.Dialogue> dialogues = new ArrayList<>();
            for (JsonNode dialogue : node.path("dialogues")) {
                String line = StrUtil.trim(dialogue.path("line").asText());
                if (StrUtil.isEmpty(line)) {
                    continue;
                }
                AiDramaCharacterDO speaker = characterMap.get(StrUtil.trim(dialogue.path("character").asText()));
                dialogues.add(new AiDramaStoryboardDO.Dialogue()
                        .setCharacterId(speaker != null ? speaker.getId() : null).setLine(line));
            }
            List<Long> appearanceIds = new ArrayList<>();
            for (JsonNode name : node.path("characters")) {
                AiDramaCharacterDO character = characterMap.get(StrUtil.trim(name.asText()));
                Long appearanceId = character != null ? firstAppearanceMap.get(character.getId()) : null;
                if (appearanceId != null && !appearanceIds.contains(appearanceId)) {
                    appearanceIds.add(appearanceId);
                }
            }
            List<Long> visualAssetIds = new ArrayList<>();
            for (JsonNode title : node.path("props")) {
                AiDramaVisualAssetDO visualAsset = visualAssetMap.get(StrUtil.trim(title.asText()));
                if (visualAsset != null && !visualAssetIds.contains(visualAsset.getId())) {
                    visualAssetIds.add(visualAsset.getId());
                }
            }
            AiDramaLocationDO location = locationMap.get(StrUtil.trim(node.path("location").asText()));
            storyboards.add(new AiDramaStoryboardDO().setProjectId(project.getId()).setScriptId(script.getId())
                    .setSceneNo(node.path("sceneNo").canConvertToInt() && node.path("sceneNo").asInt() > 0
                            ? node.path("sceneNo").asInt() : index)
                    .setTitle(textOrNull(node, "title")).setShotType(textOrNull(node, "shotType"))
                    .setCameraMove(textOrNull(node, "cameraMove"))
                    .setDuration(node.path("duration").asInt() > 0 ? node.path("duration").asInt() : 5)
                    .setVideoPrompt(textOrNull(node, "videoPrompt")).setDialogues(dialogues)
                    .setAppearanceIds(appearanceIds).setLocationId(location != null ? location.getId() : null)
                    .setVisualAssetIds(visualAssetIds));
        }
        storyboardService.replaceStoryboardList(project.getId(), storyboards);
    }

    @Override
    public void deleteScriptByProjectId(Long projectId) {
        scriptMapper.deleteByProjectId(projectId);
    }

    // ========== 私有方法 ==========

    /**
     * 校验项目的剧本存在，且有正文
     */
    private AiDramaScriptDO validateScriptExists(Long projectId) {
        AiDramaScriptDO script = scriptMapper.selectByProjectId(projectId);
        if (script == null || StrUtil.isBlank(script.getContent())) {
            throw exception(DRAMA_SCRIPT_NOT_EXISTS);
        }
        return script;
    }

    /**
     * 保存剧本：不存在时创建，存在时更新
     */
    private AiDramaScriptDO saveScript(Long projectId, AiDramaScriptDO script) {
        AiDramaScriptDO dbScript = scriptMapper.selectByProjectId(projectId);
        if (dbScript == null) {
            scriptMapper.insert(script.setProjectId(projectId));
        } else {
            scriptMapper.updateById(script.setId(dbScript.getId()));
        }
        return scriptMapper.selectById(script.getId());
    }

    /**
     * 调用对话模型：未指定模型时，使用默认的对话模型
     *
     * @param scene 场景名，用于错误提示
     * @param modelId 对话模型编号，可为空
     * @param jsonObject 是否要求返回 JSON 对象：提示词要求输出 JSON 对象时为 true；输出 JSON 数组（拆分分镜）时为 false
     * @param content 提示词
     * @return 模型的回复
     */
    private String chat(String scene, Long modelId, boolean jsonObject, String content) {
        // 1. 获得模型
        AiModelDO model;
        if (modelId != null) {
            model = modelService.validateModel(modelId);
            if (ObjUtil.notEqual(model.getType(), AiModelTypeEnum.CHAT.getType())) {
                throw exception(MODEL_USE_TYPE_ERROR);
            }
        } else {
            model = modelService.getRequiredDefaultModel(AiModelTypeEnum.CHAT.getType());
        }
        ChatModel chatModel = modelService.getChatModel(model.getId());
        AiPlatformEnum platform = AiPlatformEnum.validatePlatform(model.getPlatform());
        ChatOptions options = AiChatUtils.buildChatOptions(platform, model.getModel(), model.getTemperature(), model.getMaxTokens());
        // JSON 输出：DeepSeek 开启 json_object，保证返回合法的 JSON 对象（提示词已包含“JSON”及格式示例，符合其要求）；
        // 它只约束顶层为对象，所以输出数组的拆分分镜不开启。其它平台的参数不统一，仍只靠提示词约束，由 parseJson 容错解析
        if (jsonObject && platform == AiPlatformEnum.DEEPSEEK) {
            ((DeepSeekChatOptions) options).setResponseFormat(ResponseFormat.builder().type(ResponseFormat.Type.JSON_OBJECT).build());
        }

        // 2. 同步调用
        String text;
        try {
            ChatResponse response = chatModel.call(new Prompt(Collections.singletonList(new UserMessage(content)), options));
            text = response != null && response.getResult() != null ? response.getResult().getOutput().getText() : null;
        } catch (Exception e) {
            log.error("[chat][scene({}) model({}) 调用失败]", scene, model.getId(), e);
            throw exception(DRAMA_CHAT_FAIL, scene, StrUtil.blankToDefault(e.getMessage(), "调用对话模型失败"));
        }
        if (StrUtil.isBlank(text)) {
            throw exception(DRAMA_CHAT_FAIL, scene, "模型返回为空");
        }
        return text;
    }

    /**
     * 解析模型返回的剧本：正文不能为空
     */
    private static AiDramaScriptDO parseScript(String scene, String content) {
        JsonNode root = parseJson(scene, content);
        String text = textOrNull(root, "content");
        if (StrUtil.isBlank(text)) {
            throw exception(DRAMA_CHAT_FAIL, scene, "模型没有返回剧本正文");
        }
        return new AiDramaScriptDO().setTitle(textOrNull(root, "title")).setTone(textOrNull(root, "tone"))
                .setOutline(textOrNull(root, "outline")).setContent(text);
    }

    /**
     * 解析模型返回的 JSON：去掉 Markdown 代码块等前后内容，取第一个 { 或 [ 到最后一个 } 或 ]
     */
    @VisibleForTesting
    static JsonNode parseJson(String scene, String content) {
        int brace = content.indexOf('{');
        int bracket = content.indexOf('[');
        int start = brace < 0 ? bracket : bracket < 0 ? brace : Math.min(brace, bracket);
        int end = Math.max(content.lastIndexOf('}'), content.lastIndexOf(']'));
        if (start < 0 || end <= start) {
            throw exception(DRAMA_CHAT_FAIL, scene, "模型返回的内容不是 JSON");
        }
        try {
            return JsonUtils.parseTree(content.substring(start, end + 1));
        } catch (Exception e) {
            log.warn("[parseJson][scene({}) 解析失败，内容：{}]", scene, content, e);
            throw exception(DRAMA_CHAT_FAIL, scene, "模型返回的内容不是 JSON");
        }
    }

    /**
     * 读取字段的文本：不存在、为 null、空白时返回 null
     */
    private static String textOrNull(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return value == null || value.isNull() ? null : StrUtil.trimToNull(value.asText());
    }

    /**
     * 拼接名称，用于提示词；没有时为“无”
     */
    private static String joinNames(List<String> names) {
        return CollUtil.isEmpty(names) ? "无" : String.join("、", names);
    }

}
