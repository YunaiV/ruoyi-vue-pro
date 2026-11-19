package cn.iocoder.yudao.module.ai.service.drama;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.ObjectUtils;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.framework.tenant.core.util.TenantUtils;
import cn.iocoder.yudao.module.ai.controller.admin.drama.vo.script.AiDramaChatReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.drama.vo.project.AiDramaProjectSaveReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.drama.vo.script.AiDramaChatStreamReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.drama.vo.script.AiDramaScriptCreateReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.drama.vo.script.AiDramaScriptCreateRespVO;
import cn.iocoder.yudao.module.ai.controller.admin.drama.vo.script.AiDramaScriptPolishReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.drama.vo.script.AiDramaScriptUpdateReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.drama.vo.storyboard.AiDramaStoryboardImportReqVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.drama.*;
import cn.iocoder.yudao.module.ai.dal.dataobject.model.AiModelDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.skill.AiSkillDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.skill.AiSkillFileDO;
import cn.iocoder.yudao.module.ai.dal.mysql.drama.AiDramaScriptMapper;
import cn.iocoder.yudao.module.ai.enums.drama.*;
import cn.iocoder.yudao.module.ai.enums.model.AiModelTypeEnum;
import cn.iocoder.yudao.module.ai.enums.model.AiPlatformEnum;
import cn.iocoder.yudao.module.ai.service.drama.bo.AiDramaStoryboardPlanRespBO;
import cn.iocoder.yudao.module.ai.service.model.AiModelService;
import cn.iocoder.yudao.module.ai.service.skill.AiSkillFileService;
import cn.iocoder.yudao.module.ai.service.skill.AiSkillService;
import cn.iocoder.yudao.module.ai.util.AiChatUtils;
import cn.iocoder.yudao.module.ai.util.AiReasoningUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.annotations.VisibleForTesting;
import jakarta.annotation.Resource;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.deepseek.DeepSeekChatOptions;
import org.springframework.ai.deepseek.api.ResponseFormat;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.validation.annotation.Validated;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.BufferedReader;
import java.io.StringReader;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.invalidParamException;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.error;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.*;
import static cn.iocoder.yudao.module.ai.enums.ErrorCodeConstants.*;

/**
 * AI 短剧剧本 Service 实现类
 *
 * 1. 剧本版本：同一项目可有多条剧本，生成、打磨各新增一条，最新一条为当前剧本；手动保存只更新当前剧本。
 *    拆分分镜只替换当前剧本的分镜，旧版本的分镜保留，可按版本浏览
 * 2. 生成、打磨、分析、拆分可同步调用，也可流式调用。流式生成在后台独立订阅：前端断开后继续，结束后解析入库；
 *    进行中的生成只保存在本实例的内存中，接续、同一项目的互斥都只在本实例内有效：多实例部署时，同一项目的请求需固定到同一实例；服务重启会中断未完成的生成（不入库）
 * 3. 项目绑定了技能时，技能入口文件 SKILL.md 的内容加入对应的提示词
 *
 * @author 芋道源码
 */
@Service
@Validated
@Slf4j
public class AiDramaScriptServiceImpl implements AiDramaScriptService {

    private static final String SCRIPT_JSON_FORMAT = "只输出一个 JSON 对象，不要输出其它内容，格式：\n"
            + "{\"title\":\"剧名\",\"tone\":\"叙事基调，例如悬疑、温情\",\"outline\":\"剧情大纲，300 字以内\",\"content\":\"剧本正文：按场次书写，每场写明场景、人物动作与台词\"}";

    private static final String GENERATE_SCRIPT_PROMPT = "你是一名短剧编剧。请根据下面的故事想法，创作一部 1 到 3 分钟的短剧剧本。\n"
            + "画风：{}\n"
            + "故事想法：\n"
            + "{}\n"
            + "\n" + SCRIPT_JSON_FORMAT;

    private static final String POLISH_SCRIPT_PROMPT = "你是一名短剧编剧。请按修改意见打磨下面的剧本，人物与主线保持不变。\n"
            + "修改意见：{}\n"
            + "剧名：{}\n"
            + "叙事基调：{}\n"
            + "剧情大纲：{}\n"
            + "剧本正文：\n"
            + "{}\n"
            + "\n" + SCRIPT_JSON_FORMAT;

    private static final String ANALYZE_ASSETS_PROMPT = "请从下面的短剧剧本中，提取需要保持形象统一的角色、场景与关键道具，用于后续出图。画风：{}\n"
            + "剧本：\n"
            + "{}\n"
            + "\n"
            + "只输出一个 JSON 对象，不要输出其它内容，格式：\n"
            + "{\"characters\":[{\"name\":\"角色名\",\"aliases\":[\"别名、称呼\"],\"roleLevel\":\"角色层级：S 绝对主角、A 核心配角、B 重要配角、C 次要角色、D 群众演员\","
            + "\"ageRange\":\"年龄段\",\"personalityTags\":[\"性格标签\"],\"description\":\"年龄、外貌、服装等设定\",\"prompt\":\"用于出图的人物画面描述\","
            + "\"voiceDescription\":\"声音描述\"}],"
            + "\"locations\":[{\"name\":\"场景名\",\"description\":\"用于出图的场景画面描述\",\"crowd\":是否有群众演员,\"crowdDescription\":\"群众演员描述\","
            + "\"availableSlots\":[\"可用站位，例如 左侧窗边\"]}],"
            + "\"props\":[{\"title\":\"道具名\",\"prompt\":\"用于出图的道具画面描述\",\"shotNumbers\":[出现的场次数字]}]}";

    private static final String GENERATE_STORYBOARDS_PROMPT = "你是一名短剧分镜师。请把下面的剧本拆分为镜头分镜，每个镜头 3 到 10 秒，总时长 1 到 3 分钟。\n"
            + "画风：{}\n"
            + "可用角色：{}\n"
            + "可用场景：{}\n"
            + "可用道具：{}\n"
            + "剧本：\n"
            + "{}\n"
            + "\n" + "{}";

    /**
     * 逐场拆分分镜：只拆当前一场，带本场角色、时长预算与上一场结尾；输出格式与整部剧本拆分一致
     */
    private static final String GENERATE_SCENE_STORYBOARDS_PROMPT = "你是一名短剧分镜师。剧本共 {} 场，本次只拆第 {} 场为镜头分镜，每个镜头 3 到 10 秒；不要拆其它场次，也不要重复上一场的内容。\n"
            + "本场时长预算：约 {} 秒，按原文实际的对白与动作估算，不为凑时长新增对白或情节。\n"
            + "画风：{}\n"
            + "本场角色：{}\n"
            + "可用场景：{}\n"
            + "可用道具：{}\n"
            + "剧情大纲（只作因果上下文）：{}\n"
            + "上一场结尾（只作承接参考，不重复生成）：\n"
            + "{}\n"
            + "本场原文：\n"
            + "{}\n"
            + "\n"
            + "本场出镜与发声的角色，只以本场原文及上一场结尾的明确承接为据，不添加本场没有安排的角色。sceneNumber 固定为 {}。\n" + "{}";

    /**
     * 拆分分镜的连续性要求与输出格式：整部剧本拆分、逐场拆分共用
     */
    private static final String STORYBOARDS_JSON_FORMAT = "连续性要求：同一场景且同一连续时刻内，下一镜的 startState 承接上一镜的 endState；narrativeCause 承接上一镜的 "
            + "storyResult；场景切换必须有剧情原因，startState 写明“新场景建立”。imagePrompt 只描述 startState 的静态首帧，"
            + "不提前画出结束动作。\n"
            + "只输出一个 JSON 数组，不要输出其它内容，每个元素的格式：\n"
            + "{\"sceneNo\":镜头序号，从 1 连续递增不重复,\"sceneNumber\":原文场次数字，同一场内不因地点、时间变化递增,"
            + "\"sourceSceneContext\":\"所属原文场的背景摘要\",\"segmentNumber\":场内片段序号,\"segmentGoal\":\"当前片段唯一的剧情任务\","
            + "\"segmentResult\":\"片段结束时的剧情变化\",\"bridgeIn\":\"如何承接上一片段，首片段写开场建立\","
            + "\"bridgeOut\":\"留给下一片段承接的动作、情绪、视线或声音\","
            + "\"beatType\":\"setup、trigger、reaction、decision、action、result、transition 之一\","
            + "\"narrativeCause\":\"本镜发生的直接原因\",\"characterGoal\":\"焦点角色此刻的具体目标\","
            + "\"storyAction\":\"为实现目标采取的可见行动或关键台词\",\"storyResult\":\"行动造成的新信息、阻碍、位置或关系变化\","
            + "\"nextHook\":\"后续镜头承接的问题、动作或后果，本场结尾写收束\","
            + "\"title\":\"镜头标题\",\"sourceText\":\"对应的剧本原文\",\"sceneText\":\"镜头内容描述\","
            + "\"shotType\":\"景别\",\"cameraMove\":\"运镜\",\"duration\":秒数,\"imagePrompt\":\"用于关键帧出图的画面描述\","
            + "\"videoPrompt\":\"用于视频生成的画面描述，包含人物动作与环境\",\"dialogues\":[{\"character\":\"角色名，旁白为空\",\"line\":\"台词\"}],"
            + "\"characters\":[{\"name\":\"出场角色名\",\"slot\":\"站位，取场景的可用站位\","
            + "\"appearance\":\"角色换了形象时填形象序号或变化原因，取可用角色中列出的形象；初始形象不填\"}],\"location\":\"场景名\",\"props\":[\"道具名\"],"
            + "\"startState\":\"本镜 0 秒的静态状态\",\"endState\":\"本镜最后一帧的人物位置、朝向、姿态、道具与动作结果\","
            + "\"continuityAction\":\"与下一镜衔接的未完成动作、视线方向或运动趋势\","
            + "\"spatialAnchor\":\"空间锚点，明确左右、前后、远近与人物相对位置\","
            + "\"presentCharacters\":[\"0 秒画面中的角色名，后续入场与画外声不列入\"],"
            + "\"backgroundExtras\":\"0 秒画面中的匿名群演：人数、服饰、位置、姿态，没有为空\","
            + "\"shotDesign\":{\"focus\":\"焦点\",\"framing\":\"景别与构图\",\"axis\":\"轴线\",\"movement\":\"运动\",\"transition\":\"转场\"},"
            + "\"performanceBeats\":[{\"name\":\"出镜角色名\",\"acting\":\"本镜的触发、意图与可见回应\"}],"
            + "\"timing\":{\"spokenText\":\"实际发音台词\",\"speechRate\":语速,\"actionSeconds\":动作秒数,\"pauseSeconds\":停顿秒数}}";

    private static final String PHOTOGRAPHY_RULES_PROMPT = "你是一名短剧摄影指导。请为下面每个镜头制定摄影规则：光线的方向与质感、人物在画面中的位置、姿态与朝向、景深、色调；"
            + "景深随景别变化（远景大景深，近景、特写与多人对话浅景深），同一场次内的光线与色调保持连贯。画风：{}\n"
            + "镜头：\n"
            + "{}\n"
            + "\n"
            + "只输出一个 JSON 数组，不要输出其它内容，每个镜头一个元素，格式：\n"
            + "{\"panelNumber\":镜头序号,\"lighting\":{\"direction\":\"光线方向\",\"quality\":\"光线质感\"},"
            + "\"characters\":[{\"name\":\"角色名\",\"screenPosition\":\"画面位置\",\"posture\":\"姿态\",\"facing\":\"朝向\"}],"
            + "\"depthOfField\":\"景深\",\"colorTone\":\"色调\"}";

    private static final String ACTING_NOTES_PROMPT = "你是一名短剧表演指导。请为下面每个镜头的每个出场角色写一句表演指导：情绪、表情、肢体与细微动作，符合镜头的场景类型；"
            + "写看得见的表演，不要只写“开心”“悲伤”等抽象情绪词，称呼使用角色名。画风：{}\n"
            + "镜头：\n"
            + "{}\n"
            + "\n"
            + "只输出一个 JSON 数组，不要输出其它内容，每个镜头一个元素，格式：\n"
            + "{\"panelNumber\":镜头序号,\"characters\":[{\"name\":\"角色名\",\"acting\":\"表演指导\"}]}";

    private static final String FRAME_PLAN_PROMPT = "你是一名短剧分镜画师。请为下面每个镜头规划关键帧：选取最能代表镜头的一瞬间，写出用于出图的画面描述"
            + "（人物位置、动作、表情、构图、光线与环境），以及画面焦点；不要包含台词文字。画风：{}\n"
            + "镜头：\n"
            + "{}\n"
            + "\n"
            + "只输出一个 JSON 数组，不要输出其它内容，每个镜头一个元素，格式：\n"
            + "{\"panelNumber\":镜头序号,\"imagePrompt\":\"关键帧画面描述\",\"frameFocus\":\"画面焦点\"}";

    /**
     * 技能提示词：放在任务提示词之前，输出格式的要求保持在最后
     */
    private static final String SKILL_PROMPT = "请遵循下面的创作技能「{}」完成任务：\n"
            + "{}\n"
            + "\n";
    /**
     * 从故事想法创建项目时：项目名称的最大长度，与项目创建的校验一致；未填写且没有剧名时的默认名称
     */
    private static final int PROJECT_NAME_MAX_LENGTH = 64;
    private static final String DEFAULT_PROJECT_NAME = "未命名短剧";

    /**
     * 场次标题：「第一场」「第一场（预计 20 秒）」「【第一场】车厢」「场景 1」「内景 / 外景」开头，或「1-2 日 内 车厢」、
     * Markdown 的「## 一、车厢」；「第一场雪」等正文不算，序号后需为结尾、空白、标点或括号
     */
    private static final Pattern SCENE_HEADING_PATTERN = Pattern.compile(
            "^(?:#{1,6}\\s*)?(?:\\*\\*)?[【\\[（(]?\\s*(?:"
                    + "(?:第\\s*[一二三四五六七八九十百零〇两0-9]+\\s*场|场景\\s*[一二三四五六七八九十百零〇0-9]+)"
                    + "(?:$|[\\s　：:、.．】\\]）)（(【\\[\\-—*].*)"
                    + "|(?:[0-9]+\\s*[.、．]\\s*)?(?:内景|外景|内外景|INT\\.|EXT\\.).*"
                    + "|[0-9]+[-－–][0-9]+[\\s　]+(?:日|夜|晨|清晨|早晨|白天|午后|傍晚|黄昏|黎明|深夜|接上)[\\s　]+(?:内外景|内景|外景|内外|内|外)(?:[\\s　].*)?"
                    + ")$"
                    + "|^#{2,6}\\s+[一二三四五六七八九十百0-9]+[\\s　、.．]+\\S.*$");
    /**
     * 场次标题中的预计时长，例如「第一场（预计 20 秒）」
     */
    private static final Pattern SCENE_ESTIMATE_PATTERN = Pattern.compile("预计\\s*(\\d+(?:\\.\\d+)?)\\s*秒");
    /**
     * 逐场拆分：未写明预计时长时，按原文长度分配的总时长（秒），与整部剧本拆分的 1 到 3 分钟一致；每场至少的时长（秒）
     */
    private static final int SCRIPT_TOTAL_SECONDS = 120;
    private static final int SCENE_MIN_SECONDS = 5;
    /**
     * 逐场拆分：带给下一场的上一场结尾的长度（字符）；同时调用的场数
     */
    private static final int PREVIOUS_SCENE_ENDING_LENGTH = 1000;
    private static final int SCENE_PLAN_CONCURRENCY = 3;

    private static final String WORLDBUILDING_PROMPT = "当前项目的世界观设定，人物、场景与剧情须遵守：\n"
            + "{}\n"
            + "\n";

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
    @Resource
    private AiSkillService skillService;
    @Resource
    private AiSkillFileService skillFileService;
    @Resource
    private TransactionTemplate transactionTemplate;

    /**
     * 进行中的流式生成：key 为项目编号；同一项目同时只有一个
     */
    private final Map<Long, ChatTask> chatTasks = new ConcurrentHashMap<>();
    /**
     * 进行中的从故事想法创建项目：用户编号；同一用户同时只有一个
     */
    private final Set<Long> createTasks = ConcurrentHashMap.newKeySet();

    // ========== 剧本 ==========

    @Override
    public AiDramaScriptDO getScriptByProjectId(Long projectId) {
        return scriptMapper.selectLatestByProjectId(projectId);
    }

    @Override
    public List<AiDramaScriptDO> getScriptListMy(Long userId, Long projectId) {
        projectService.validateProjectMy(projectId, userId);
        return scriptMapper.selectListByProjectId(projectId);
    }

    @Override
    public Map<Long, Long> getLatestScriptIdMap(Collection<Long> projectIds) {
        if (CollUtil.isEmpty(projectIds)) {
            return Collections.emptyMap();
        }
        Map<Long, Long> result = new HashMap<>();
        scriptMapper.selectIdListByProjectIds(projectIds).forEach(script ->
                result.merge(script.getProjectId(), script.getId(), Math::max));
        return result;
    }

    @Override
    public Long updateScriptMy(Long userId, AiDramaScriptUpdateReqVO updateReqVO) {
        // 1.1 校验项目
        projectService.validateProjectMy(updateReqVO.getProjectId(), userId);
        // 1.2 校验剧本：指定编号时，需属于该项目
        AiDramaScriptDO current = null;
        if (updateReqVO.getId() != null) {
            current = scriptMapper.selectById(updateReqVO.getId());
            if (current == null || ObjUtil.notEqual(current.getProjectId(), updateReqVO.getProjectId())) {
                throw exception(DRAMA_SCRIPT_NOT_EXISTS);
            }
        }

        // 2. 保存：指定编号时更新该版本；未指定时新增版本，来源为手动编写
        AiDramaScriptDO script = BeanUtils.toBean(updateReqVO, AiDramaScriptDO.class);
        if (current == null) {
            scriptMapper.insert(script.setSource(AiDramaScriptSourceEnum.MANUAL.getSource())
                    .setCreationMode(ObjUtil.defaultIfNull(script.getCreationMode(), AiDramaScriptCreationModeEnum.STANDARD.getMode())));
        } else {
            scriptMapper.updateById(script);
        }

        // 3. 当前剧本的内容变化时，项目置为剧本已变更：需要重新拆分分镜；修改历史版本不影响当前的分镜
        //    正文、大纲、基调、世界观未传时视为不修改
        AiDramaScriptDO latest = scriptMapper.selectLatestByProjectId(updateReqVO.getProjectId());
        if (latest != null && Objects.equals(latest.getId(), script.getId()) && (current == null
                || (script.getContent() != null && ObjUtil.notEqual(current.getContent(), script.getContent()))
                || (script.getOutline() != null && ObjUtil.notEqual(current.getOutline(), script.getOutline()))
                || (script.getTone() != null && ObjUtil.notEqual(current.getTone(), script.getTone()))
                || (script.getWorldbuilding() != null && ObjUtil.notEqual(current.getWorldbuilding(), script.getWorldbuilding())))) {
            projectService.updateProjectStatus(updateReqVO.getProjectId(), AiDramaProjectStatusEnum.SCRIPT_CHANGED.getStatus());
        }
        return script.getId();
    }

    @Override
    public AiDramaScriptDO getScript(Long id) {
        return scriptMapper.selectById(id);
    }

    @Override
    public void deleteScriptByProjectId(Long projectId) {
        scriptMapper.deleteByProjectId(projectId);
    }

    // ========== 同步生成 ==========

    @Override
    public AiDramaScriptDO generateScriptMy(Long userId, AiDramaChatReqVO reqVO) {
        AiDramaProjectDO project = projectService.validateProjectMy(reqVO.getProjectId(), userId);
        return executeChat(AiDramaChatTypeEnum.SCRIPT_GENERATE, project, reqVO.getModelId(), null);
    }

    @Override
    public AiDramaScriptDO polishScriptMy(Long userId, AiDramaScriptPolishReqVO reqVO) {
        AiDramaProjectDO project = projectService.validateProjectMy(reqVO.getProjectId(), userId);
        return executeChat(AiDramaChatTypeEnum.SCRIPT_POLISH, project, reqVO.getModelId(), reqVO.getNote());
    }

    @Override
    public void analyzeAssetListMy(Long userId, AiDramaChatReqVO reqVO) {
        AiDramaProjectDO project = projectService.validateProjectMy(reqVO.getProjectId(), userId);
        executeChat(AiDramaChatTypeEnum.ASSET_ANALYZE, project, reqVO.getModelId(), null);
    }

    @Override
    public void generateStoryboardListMy(Long userId, AiDramaChatReqVO reqVO) {
        // 1. 校验：没有进行中的流式生成；当前剧本存在，其分镜没有镜头视频在生成中
        AiDramaProjectDO project = projectService.validateProjectMy(reqVO.getProjectId(), userId);
        AiDramaChatTypeEnum type = AiDramaChatTypeEnum.STORYBOARD_GENERATE;
        ChatTask running = chatTasks.get(project.getId());
        if (running != null) {
            throw exception(DRAMA_CHAT_RUNNING, running.getType().getName());
        }
        AiDramaScriptDO script = validateChatPrecondition(type, project, null);

        // 2. 与规划分镜相同的逐场拆分，直接替换该剧本的分镜（画布执行使用）
        saveStoryboardList(project, script, planStoryboards(project, script, reqVO.getModelId()));
    }

    @Override
    public void generateStoryboardDetailMy(Long userId, AiDramaChatReqVO reqVO, AiDramaChatTypeEnum type) {
        AiDramaProjectDO project = projectService.validateProjectMy(reqVO.getProjectId(), userId);
        executeChat(type, project, reqVO.getModelId(), null);
    }

    @Override
    public List<AiDramaStoryboardPlanRespBO> planStoryboardListMy(Long userId, AiDramaChatReqVO reqVO) {
        // 1. 校验：没有进行中的流式生成；当前剧本存在
        AiDramaProjectDO project = projectService.validateProjectMy(reqVO.getProjectId(), userId);
        ChatTask running = chatTasks.get(project.getId());
        if (running != null) {
            throw exception(DRAMA_CHAT_RUNNING, running.getType().getName());
        }
        AiDramaScriptDO script = validateScriptExists(project.getId());

        // 2. 逐场拆分为分镜：不保存，供审阅后导入
        return buildStoryboardList(project, script, planStoryboards(project, script, reqVO.getModelId()));
    }

    /**
     * 由剧本拆分分镜：规划分镜（审阅后导入）、直接拆分分镜（画布执行）共用
     *
     * 1. 无法识别出多个场次时：整部剧本一次拆分（原有用法）
     * 2. 逐场拆分：每场带本场角色、时长预算与上一场结尾，并行（最多 SCENE_PLAN_CONCURRENCY 场）调用，按原场次顺序合并；
     *    任一场失败时整体失败，不保存跨任务的场级检查点，重试时重新规划
     *
     * @return 模型返回的分镜 JSON 数组；逐场时原文场次 sceneNumber 以切分为准
     */
    private JsonNode planStoryboards(AiDramaProjectDO project, AiDramaScriptDO script, Long modelId) {
        // 1. 整部剧本一次拆分
        AiDramaChatTypeEnum type = AiDramaChatTypeEnum.STORYBOARD_GENERATE;
        List<String> scenes = splitScriptScenes(script.getContent());
        if (scenes.size() <= 1) {
            String content = chat(type.getName(), modelId, type.isJsonObject(), buildChatPrompt(type, project, script, null));
            return parseJson(type.getName(), content);
        }
        // 2.1 逐场调用模型并解析：在弹性线程中并行（最多 SCENE_PLAN_CONCURRENCY 场），带上当前租户；结果按原场次顺序返回。
        //     任一场调用、解析失败时整体失败，其它进行中的场次随之取消
        List<String> prompts = buildScenePlanPrompts(project, script, scenes);
        Long tenantId = TenantContextHolder.getTenantId();
        List<JsonNode> results = Flux.range(0, prompts.size())
                .flatMapSequential(i -> Mono.fromCallable(() -> executeInTenant(tenantId, () -> {
                            String content = chat(type.getName(), modelId, type.isJsonObject(), prompts.get(i));
                            return parseJson(type.getName() + "（第 " + (i + 1) + " 场）", content);
                        }))
                        .subscribeOn(Schedulers.boundedElastic()), SCENE_PLAN_CONCURRENCY)
                .collectList().block();
        // 2.2 按原场次顺序合并：任一场没有返回分镜时整体失败
        ArrayNode merged = JsonNodeFactory.instance.arrayNode();
        for (int i = 0; i < results.size(); i++) {
            int sceneNumber = i + 1;
            JsonNode result = results.get(i);
            if (!result.isArray() || result.isEmpty()) {
                throw exception(DRAMA_CHAT_FAIL, type.getName(), "模型没有返回第 " + sceneNumber + " 场的分镜");
            }
            for (JsonNode node : result) {
                // 原文场次以切分的场次为准：模型返回的不可靠
                if (node instanceof ObjectNode) {
                    ((ObjectNode) node).put("sceneNumber", sceneNumber);
                }
                merged.add(node);
            }
        }
        return merged;
    }

    /**
     * 拼接逐场拆分的提示词：技能、世界观、资产等对每场相同的部分只构建一次
     */
    private List<String> buildScenePlanPrompts(AiDramaProjectDO project, AiDramaScriptDO script, List<String> scenes) {
        // 1. 每场相同的部分
        String prefix = buildStoryboardSkillPrompt(project) + buildWorldbuildingPrompt(script);
        String style = getStyleName(project, script);
        List<AiDramaCharacterDO> characters = assetService.getCharacterList(project.getId());
        String locationNames = buildLocationNames(project.getId());
        String propNames = joinNames(convertList(assetService.getPropList(project.getId()), AiDramaVisualAssetDO::getTitle));
        String outline = StrUtil.blankToDefault(StrUtil.trim(script.getOutline()), "无");
        int[] budgets = estimateSceneSeconds(scenes);

        // 2. 逐场拼接：同时用到本场、上一场、场次编号与时长预算，按下标遍历
        List<String> prompts = new ArrayList<>();
        for (int i = 0; i < scenes.size(); i++) {
            int sceneNumber = i + 1;
            String scene = scenes.get(i);
            String previousEnding = i > 0 ? StrUtil.subSufByLength(scenes.get(i - 1), PREVIOUS_SCENE_ENDING_LENGTH) : "无（本场为第一场）";
            // 本场角色：本场原文、上一场结尾中出现的角色（名称或别名）；都没有出现时提供全部角色
            List<AiDramaCharacterDO> sceneCharacters = CollUtil.defaultIfEmpty(filterList(characters,
                    character -> isCharacterMentioned(character, scene) || isCharacterMentioned(character, previousEnding)), characters);
            prompts.add(prefix + StrUtil.format(GENERATE_SCENE_STORYBOARDS_PROMPT, scenes.size(), sceneNumber, budgets[i], style,
                    buildCharacterNames(sceneCharacters), locationNames, propNames,
                    outline, previousEnding, scene, sceneNumber, STORYBOARDS_JSON_FORMAT));
        }
        return prompts;
    }

    /**
     * 角色是否在文本中出现：按名称或别名
     */
    private static boolean isCharacterMentioned(AiDramaCharacterDO character, String text) {
        if (StrUtil.isNotBlank(character.getName()) && text.contains(character.getName())) {
            return true;
        }
        return StrUtil.split(StrUtil.nullToEmpty(character.getAliases()), ',', true, true).stream().anyMatch(text::contains);
    }

    /**
     * 按原文场次切分剧本：识别「第一场」「场景 1」「内景 / 外景」等场次标题；第一个标题之前的内容并入第一场
     *
     * @return 场次原文；无法识别出场次时只有一项（整部剧本）
     */
    @VisibleForTesting
    static List<String> splitScriptScenes(String content) {
        List<String> scenes = new ArrayList<>();
        StringBuilder current = new StringBuilder(); // 当前场次的原文
        boolean seenHeading = false; // 是否已遇到过场次标题
        for (String line : StrUtil.nullToEmpty(content).split("\\R")) {
            // 1. 遇到场次标题：结束上一场，开始新的一场；第一个标题之前没有已结束的场次，前言留在 current 并入第一场
            boolean heading = SCENE_HEADING_PATTERN.matcher(StrUtil.trim(line, 0, Character::isWhitespace)).matches();
            if (heading && seenHeading && StrUtil.isNotBlank(current)) {
                scenes.add(StrUtil.trim(current.toString(), 0, Character::isWhitespace));
                current.setLength(0);
            }
            seenHeading |= heading;
            // 2. 标题行、正文行都计入当前场次
            current.append(line).append('\n');
        }
        // 3. 最后一场；没有识别出标题时即整部剧本
        if (StrUtil.isNotBlank(current)) {
            scenes.add(StrUtil.trim(current.toString(), 0, Character::isWhitespace));
        }
        return scenes;
    }

    /**
     * 估算每场的时长预算（秒）：场次标题写明「预计 N 秒」时按它，其余按原文长度分配总时长
     *
     * 场次标题取本场第一个标题行：第一场可能并入了标题之前的前言，不能取首行
     */
    @VisibleForTesting
    static int[] estimateSceneSeconds(List<String> scenes) {
        int totalLength = scenes.stream().mapToInt(String::length).sum();
        int[] budgets = new int[scenes.size()];
        for (int i = 0; i < scenes.size(); i++) {
            // 1. 本场的第一个标题行
            String scene = scenes.get(i);
            String heading = new BufferedReader(new StringReader(scene)).lines()
                    .filter(line -> SCENE_HEADING_PATTERN.matcher(StrUtil.trim(line, 0, Character::isWhitespace)).matches())
                    .findFirst().orElse("");
            // 2. 标题写明「预计 N 秒」时按它；否则按本场原文占全部原文的比例，分配剧本总时长
            Matcher matcher = SCENE_ESTIMATE_PATTERN.matcher(heading);
            int budget = matcher.find() ? (int) Math.round(Double.parseDouble(matcher.group(1)))
                    : (int) Math.round((double) SCRIPT_TOTAL_SECONDS * scene.length() / Math.max(totalLength, 1));
            // 3. 不低于每场的最短时长
            budgets[i] = Math.max(budget, SCENE_MIN_SECONDS);
        }
        return budgets;
    }

    @Override
    public void importStoryboardListMy(Long userId, AiDramaStoryboardImportReqVO reqVO) {
        // 1.1 校验项目；规划时的剧本仍是当前剧本
        AiDramaProjectDO project = projectService.validateProjectMy(reqVO.getProjectId(), userId);
        AiDramaScriptDO script = validateScriptExists(project.getId());
        if (ObjUtil.notEqual(script.getId(), reqVO.getScriptId())) {
            throw invalidParamException("剧本已有新版本，请重新规划分镜");
        }
        // 1.2 校验该剧本的分镜没有镜头视频在生成中
        validateStoryboardNotRunning(project.getId(), script.getId());

        // 2. 替换该剧本的分镜，项目置为分镜就绪：未填写场次的按顺序编号；镜头序号不能重复
        List<AiDramaStoryboardDO> storyboards = new ArrayList<>();
        Set<Integer> sceneNos = new HashSet<>();
        for (int i = 0; i < reqVO.getStoryboards().size(); i++) {
            AiDramaStoryboardDO storyboard = BeanUtils.toBean(reqVO.getStoryboards().get(i), AiDramaStoryboardDO.class)
                    .setId(null).setProjectId(project.getId()).setScriptId(script.getId());
            storyboards.add(storyboard.setSceneNo(ObjUtil.defaultIfNull(storyboard.getSceneNo(), i + 1)));
            if (!sceneNos.add(storyboard.getSceneNo())) {
                throw invalidParamException("镜头序号 {} 重复，请调整后再导入", storyboard.getSceneNo());
            }
        }
        storyboardService.replaceStoryboardList(project.getId(), script.getId(), storyboards);
        projectService.updateProjectStatus(project.getId(), AiDramaProjectStatusEnum.STORYBOARD_READY.getStatus());
    }

    /**
     * 同步调用对话模型，解析并保存
     *
     * @return 生成、打磨剧本时为新增的剧本，其它为空
     */
    private AiDramaScriptDO executeChat(AiDramaChatTypeEnum type, AiDramaProjectDO project, Long modelId, String note) {
        // 1. 校验：没有进行中的流式生成，且满足前置条件
        ChatTask running = chatTasks.get(project.getId());
        if (running != null) {
            throw exception(DRAMA_CHAT_RUNNING, running.getType().getName());
        }
        AiDramaScriptDO script = validateChatPrecondition(type, project, note);

        // 2. 调用对话模型，解析并保存
        String content = chat(type.getName(), modelId, type.isJsonObject(), buildChatPrompt(type, project, script, note));
        AiDramaScriptDO newScript = saveChatResult(type, project, script, note, content);
        return newScript != null ? scriptMapper.selectById(newScript.getId()) : null;
    }

    // ========== 流式生成 ==========

    @Override
    public Flux<CommonResult<String>> streamChatMy(Long userId, AiDramaChatStreamReqVO reqVO) {
        AiDramaChatTypeEnum type = AiDramaChatTypeEnum.valueOfType(reqVO.getType());
        // 1. 校验并构建对话模型的流：不通过时直接返回错误
        AiDramaProjectDO project;
        AiDramaScriptDO script;
        Flux<String> stream;
        try {
            project = projectService.validateProjectMy(reqVO.getProjectId(), userId);
            script = validateChatPrecondition(type, project, reqVO.getNote());
            stream = chatStream(reqVO.getModelId(), type.isJsonObject(), buildChatPrompt(type, project, script, reqVO.getNote()));
        } catch (ServiceException ex) {
            return Flux.just(error(ex.getCode(), ex.getMessage()));
        }

        // 2. 构建任务：逐段返回增量文本；结束后在请求时的租户下解析入库，入库失败时追加一条错误；cache 供断线后重放
        Long tenantId = TenantContextHolder.getTenantId();
        StringBuilder content = new StringBuilder();
        Flux<CommonResult<String>> flux = stream.map(text -> {
                    content.append(text);
                    return success(text);
                })
                .concatWith(Mono.fromRunnable(() -> TenantUtils.execute(tenantId,
                        () -> saveChatResult(type, project, script, reqVO.getNote(), content.toString()))))
                .onErrorResume(e -> Flux.just(buildChatError(type, project.getId(), e)))
                .doFinally(signal -> chatTasks.remove(project.getId()))
                .cache();

        // 3. 登记任务，并在后台订阅：前端断开不影响生成；同一项目同时只允许一个
        ChatTask running = chatTasks.putIfAbsent(project.getId(), new ChatTask(type, flux));
        if (running != null) {
            ServiceException ex = exception(DRAMA_CHAT_RUNNING, running.getType().getName());
            return Flux.just(error(ex.getCode(), ex.getMessage()));
        }
        flux.subscribe();
        return flux;
    }

    // DONE @AI：方法内注释；
    @Override
    public Flux<CommonResult<String>> resumeChatStreamMy(Long userId, Long projectId) {
        // 1. 校验项目：不通过时直接返回错误
        try {
            projectService.validateProjectMy(projectId, userId);
        } catch (ServiceException ex) {
            return Flux.just(error(ex.getCode(), ex.getMessage()));
        }
        // 2. 重放已生成的内容并接续；没有进行中的生成时直接结束
        ChatTask task = chatTasks.get(projectId);
        return task != null ? task.getFlux() : Flux.empty();
    }

    @Override
    public Flux<CommonResult<AiDramaScriptCreateRespVO>> createProjectStreamMy(Long userId, AiDramaScriptCreateReqVO reqVO) {
        // 1. 同一用户同时只允许一个：前端刷新、重复点击时不重复创建项目
        if (!createTasks.add(userId)) {
            ServiceException ex = exception(DRAMA_CREATE_RUNNING);
            return Flux.just(error(ex.getCode(), ex.getMessage()));
        }
        // 2. 校验并构建对话模型的流：项目尚未创建，按请求中的项目设置拼接提示词；不通过时（包括非业务异常）释放占位，直接返回错误
        AiDramaChatTypeEnum type = AiDramaChatTypeEnum.SCRIPT_GENERATE;
        AiDramaProjectSaveReqVO projectReqVO;
        Flux<String> stream;
        try {
            projectReqVO = BeanUtils.toBean(reqVO, AiDramaProjectSaveReqVO.class);
            projectReqVO.setName(StrUtil.trimToNull(reqVO.getName()));
            AiDramaProjectDO project = new AiDramaProjectDO().setName(projectReqVO.getName()).setIdea(reqVO.getIdea())
                    .setStyle(ObjUtil.defaultIfNull(reqVO.getStyle(), AiDramaStyleEnum.SCRIPT_TONE.getStyle()));
            stream = chatStream(reqVO.getModelId(), type.isJsonObject(), buildChatPrompt(type, project, null, null));
        } catch (RuntimeException e) {
            createTasks.remove(userId);
            return Flux.just(buildChatError(type, null, e));
        }

        // 3. 构建任务：逐段返回增量文本；结束后在请求时的租户、登录用户下（创建人、更新人与请求一致）解析，
        //    成功时创建项目、保存剧本，最后返回项目编号
        Long tenantId = TenantContextHolder.getTenantId();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        StringBuilder content = new StringBuilder();
        Flux<CommonResult<AiDramaScriptCreateRespVO>> flux = stream.map(text -> {
                    content.append(text);
                    return success(new AiDramaScriptCreateRespVO().setContent(text));
                })
                .concatWith(Mono.fromCallable(() -> executeInTenant(tenantId, () -> executeAs(authentication, () -> success(
                        new AiDramaScriptCreateRespVO().setProjectId(createProjectByScript(userId, projectReqVO, content.toString())))))))
                .onErrorResume(e -> Flux.just(buildChatError(type, null, e)))
                .doFinally(signal -> createTasks.remove(userId))
                .cache();

        // 4. 在后台订阅：前端断开不影响生成、入库
        flux.subscribe();
        return flux;
    }

    /**
     * 在指定的租户下执行：异常原样抛出（{@link TenantUtils#execute(Long, java.util.concurrent.Callable)} 会把业务异常包装为
     * RuntimeException，丢失错误码与提示）
     */
    @SuppressWarnings("unchecked")
    private static <T> T executeInTenant(Long tenantId, Supplier<T> supplier) {
        Object[] result = new Object[1];
        TenantUtils.execute(tenantId, () -> {
            result[0] = supplier.get();
        });
        return (T) result[0];
    }

    /**
     * 在指定的登录用户下执行：流式生成结束后的入库运行在其它线程，没有请求的登录用户，创建人、更新人会为空；
     * 执行后恢复原来的上下文
     */
    private static <T> T executeAs(Authentication authentication, Supplier<T> supplier) {
        SecurityContext original = SecurityContextHolder.getContext();
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        try {
            return supplier.get();
        } finally {
            SecurityContextHolder.setContext(original);
        }
    }

    /**
     * 解析生成的剧本，在同一事务内创建项目、保存剧本：解析失败时不创建项目
     *
     * @return 项目编号
     */
    private Long createProjectByScript(Long userId, AiDramaProjectSaveReqVO projectReqVO, String content) {
        // 1. 解析剧本
        AiDramaScriptDO script = parseScript(AiDramaChatTypeEnum.SCRIPT_GENERATE.getName(), content)
                .setSource(AiDramaScriptSourceEnum.LLM.getSource())
                .setCreationMode(AiDramaScriptCreationModeEnum.STANDARD.getMode());
        // 2. 项目名称未填写时，使用剧名
        if (StrUtil.isBlank(projectReqVO.getName())) {
            projectReqVO.setName(StrUtil.sub(StrUtil.blankToDefault(StrUtil.trim(script.getTitle()), DEFAULT_PROJECT_NAME),
                    0, PROJECT_NAME_MAX_LENGTH));
        }
        // 3. 同一事务内创建项目、保存剧本
        return transactionTemplate.execute(status -> {
            Long projectId = projectService.createProject(userId, projectReqVO);
            scriptMapper.insert(script.setProjectId(projectId));
            return projectId;
        });
    }

    @Override
    public Integer getRunningChatType(Long projectId) {
        ChatTask task = chatTasks.get(projectId);
        return task != null ? task.getType().getType() : null;
    }

    /**
     * 构建流式生成的错误：业务异常（例如解析失败）直接返回，其它异常记为调用失败
     */
    private static <T> CommonResult<T> buildChatError(AiDramaChatTypeEnum type, Long projectId, Throwable e) {
        log.error("[buildChatError][project({}) type({}) 流式生成失败]", projectId, type.getType(), e);
        ServiceException ex = e instanceof ServiceException ? (ServiceException) e
                : exception(DRAMA_CHAT_FAIL, type.getName(), StrUtil.blankToDefault(e.getMessage(), "调用对话模型失败"));
        return error(ex.getCode(), ex.getMessage());
    }

    // ========== 生成：前置条件、提示词、保存 ==========

    /**
     * 校验生成的前置条件
     *
     * @return 当前剧本；生成剧本时为空
     */
    private AiDramaScriptDO validateChatPrecondition(AiDramaChatTypeEnum type, AiDramaProjectDO project, String note) {
        // 1. 生成剧本：无前置条件
        if (type == AiDramaChatTypeEnum.SCRIPT_GENERATE) {
            return null;
        }
        // 2. 打磨剧本：需要打磨意见
        if (type == AiDramaChatTypeEnum.SCRIPT_POLISH && StrUtil.isBlank(note)) {
            throw invalidParamException("打磨意见不能为空");
        }
        // 3. 打磨、分析、拆分：当前剧本需要存在且有正文
        AiDramaScriptDO script = validateScriptExists(project.getId());
        // 4. 拆分分镜：该剧本的分镜没有镜头视频在生成中
        if (type == AiDramaChatTypeEnum.STORYBOARD_GENERATE) {
            validateStoryboardNotRunning(project.getId(), script.getId());
        }
        // 5. 摄影规则、表演指导、关键帧规划：该剧本已有分镜，且没有镜头视频在生成中
        if (isStoryboardDetail(type)) {
            validateStoryboardNotRunning(project.getId(), script.getId());
            if (getScriptStoryboardList(project.getId(), script.getId()).isEmpty()) {
                throw exception(DRAMA_STORYBOARD_EMPTY);
            }
        }
        return script;
    }

    /**
     * 校验剧本的分镜没有镜头视频在生成中：拆分分镜会替换它们
     */
    private void validateStoryboardNotRunning(Long projectId, Long scriptId) {
        List<AiDramaStoryboardDO> storyboards = storyboardService.getStoryboardList(projectId);
        if (CollUtil.contains(storyboards, storyboard ->
                Objects.equals(storyboard.getScriptId(), scriptId) && AiDramaStatusEnum.isRunning(storyboard.getVideoStatus()))) {
            throw exception(DRAMA_TASK_RUNNING);
        }
    }

    /**
     * 拼接提示词：项目绑定了对应的技能时，技能内容放在最前
     */
    private String buildChatPrompt(AiDramaChatTypeEnum type, AiDramaProjectDO project, AiDramaScriptDO script, String note) {
        String style = getStyleName(project, script);
        // 世界观：生成剧本时取当前剧本的（新版本沿用；从故事想法创建项目时项目尚未创建，没有），其它取所用剧本的
        String worldbuilding = buildWorldbuildingPrompt(type == AiDramaChatTypeEnum.SCRIPT_GENERATE
                ? (project.getId() != null ? scriptMapper.selectLatestByProjectId(project.getId()) : null) : script);
        switch (type) {
            case SCRIPT_GENERATE:
                // 故事想法为空时使用项目名称
                return worldbuilding + StrUtil.format(GENERATE_SCRIPT_PROMPT, style,
                        StrUtil.blankToDefault(project.getIdea(), project.getName()));
            case SCRIPT_POLISH:
                return worldbuilding + StrUtil.format(POLISH_SCRIPT_PROMPT, note,
                        StrUtil.nullToEmpty(script.getTitle()), StrUtil.nullToEmpty(script.getTone()),
                        StrUtil.nullToEmpty(script.getOutline()), script.getContent());
            case ASSET_ANALYZE:
                return buildSkillPrompt(project.getAestheticSkillId()) + worldbuilding + StrUtil.format(ANALYZE_ASSETS_PROMPT,
                        style, script.getContent());
            case PHOTOGRAPHY_RULES:
                return buildSkillPrompt(project.getDirectorSkillId()) + worldbuilding + StrUtil.format(PHOTOGRAPHY_RULES_PROMPT,
                        style, buildPanelText(project, script));
            case ACTING_NOTES:
                return buildSkillPrompt(project.getDirectorSkillId()) + worldbuilding + StrUtil.format(ACTING_NOTES_PROMPT,
                        style, buildPanelText(project, script));
            case FRAME_PLAN:
                return buildSkillPrompt(project.getAestheticSkillId()) + worldbuilding + StrUtil.format(FRAME_PLAN_PROMPT,
                        style, buildPanelText(project, script));
            default: {
                // 拆分分镜：技能在前；提供可用的资产名称（角色带可换的形象，场景带站位），便于按名称关联
                return buildStoryboardSkillPrompt(project) + worldbuilding + StrUtil.format(GENERATE_STORYBOARDS_PROMPT, style,
                        buildCharacterNames(assetService.getCharacterList(project.getId())), buildLocationNames(project.getId()),
                        joinNames(convertList(assetService.getPropList(project.getId()), AiDramaVisualAssetDO::getTitle)),
                        script.getContent(), STORYBOARDS_JSON_FORMAT);
            }
        }
    }

    /**
     * 拼接拆分分镜的技能提示词：美学、导演技能在前，分镜技能按顺序追加
     */
    private String buildStoryboardSkillPrompt(AiDramaProjectDO project) {
        StringBuilder skillPrompt = new StringBuilder(buildSkillPrompt(project.getAestheticSkillId()))
                .append(buildSkillPrompt(project.getDirectorSkillId()));
        CollUtil.emptyIfNull(project.getStoryboardSkillIds()).forEach(skillId -> skillPrompt.append(buildSkillPrompt(skillId)));
        return skillPrompt.toString();
    }

    /**
     * 拼接角色名称：有变化形象的，列出可换的形象（序号与变化原因），供分镜按序号或变化原因指定
     *
     * 例如说：林夏（可换形象：1 受伤、2 换装）、周远
     */
    private String buildCharacterNames(List<AiDramaCharacterDO> characters) {
        Map<Long, List<AiDramaCharacterAppearanceDO>> appearanceMap = convertMultiMap(
                assetService.getAppearanceListByCharacterIds(convertList(characters, AiDramaCharacterDO::getId)),
                AiDramaCharacterAppearanceDO::getCharacterId);
        return joinNames(convertList(characters, character -> {
            List<String> changes = new ArrayList<>();
            appearanceMap.getOrDefault(character.getId(), Collections.emptyList()).forEach(appearance -> {
                if (appearance.getAppearanceIndex() != null && appearance.getAppearanceIndex() > 0) {
                    changes.add(appearance.getAppearanceIndex() + " " + StrUtil.blankToDefault(StrUtil.trim(
                            appearance.getChangeReason()), StrUtil.sub(StrUtil.nullToEmpty(appearance.getDescription()), 0, 20)));
                }
            });
            return character.getName() + (changes.isEmpty() ? "" : "（可换形象：" + String.join("、", changes) + "）");
        }));
    }

    /**
     * 拼接场景名称：带可用站位
     */
    private String buildLocationNames(Long projectId) {
        return joinNames(convertList(assetService.getLocationList(projectId), location -> location.getName()
                + (CollUtil.isNotEmpty(location.getAvailableSlots()) ? "（站位：" + CollUtil.join(location.getAvailableSlots(), "、") + "）" : "")));
    }

    private static boolean isStoryboardDetail(AiDramaChatTypeEnum type) {
        return ObjectUtils.equalsAny(type, AiDramaChatTypeEnum.PHOTOGRAPHY_RULES, AiDramaChatTypeEnum.ACTING_NOTES,
                AiDramaChatTypeEnum.FRAME_PLAN);
    }

    /**
     * 获得剧本的分镜：按场次排序
     */
    private List<AiDramaStoryboardDO> getScriptStoryboardList(Long projectId, Long scriptId) {
        return filterList(storyboardService.getStoryboardList(projectId), item -> Objects.equals(item.getScriptId(), scriptId));
    }

    /**
     * 拼接剧本分镜的镜头说明：JSON 数组，镜头序号为分镜的顺序（从 1 开始），供摄影规则、表演指导、关键帧规划按序号对应
     */
    private String buildPanelText(AiDramaProjectDO project, AiDramaScriptDO script) {
        // 1. 资产名称：出场角色按形象找到角色
        List<AiDramaStoryboardDO> storyboards = getScriptStoryboardList(project.getId(), script.getId());
        Map<Long, AiDramaCharacterDO> characterMap = convertMap(assetService.getCharacterList(project.getId()),
                AiDramaCharacterDO::getId);
        Map<Long, Long> appearanceCharacterMap = convertMap(assetService.getAppearanceListByCharacterIds(characterMap.keySet()),
                AiDramaCharacterAppearanceDO::getId, AiDramaCharacterAppearanceDO::getCharacterId);
        Map<Long, AiDramaLocationDO> locationMap = convertMap(assetService.getLocationList(project.getId()), AiDramaLocationDO::getId);

        // 2. 逐个镜头拼接
        List<Map<String, Object>> panels = new ArrayList<>();
        for (int i = 0; i < storyboards.size(); i++) {
            AiDramaStoryboardDO storyboard = storyboards.get(i);
            AiDramaSceneTypeEnum sceneType = ArrayUtil.firstMatch(item -> item.getType().equals(storyboard.getSceneType()),
                    AiDramaSceneTypeEnum.values());
            AiDramaLocationDO location = storyboard.getLocationId() != null ? locationMap.get(storyboard.getLocationId()) : null;
            Map<String, Object> panel = new LinkedHashMap<>();
            panel.put("panelNumber", i + 1);
            panel.put("sceneNo", storyboard.getSceneNo());
            panel.put("title", storyboard.getTitle());
            panel.put("sceneType", sceneType != null ? sceneType.getName() : null);
            panel.put("shotType", storyboard.getShotType());
            panel.put("cameraMove", storyboard.getCameraMove());
            panel.put("duration", storyboard.getDuration());
            panel.put("sceneText", storyboard.getSceneText());
            panel.put("location", location != null ? location.getName() : null);
            panel.put("characters", convertList(storyboard.getCharacters(), ref -> {
                AiDramaCharacterDO character = characterMap.get(appearanceCharacterMap.get(ref.getAppearanceId()));
                return character != null ? character.getName() : null;
            }));
            panel.put("dialogues", convertList(storyboard.getDialogues(), dialogue -> {
                AiDramaCharacterDO speaker = dialogue.getCharacterId() != null ? characterMap.get(dialogue.getCharacterId()) : null;
                return (speaker != null ? speaker.getName() : "旁白") + "：" + dialogue.getLine();
            }));
            panels.add(panel);
        }
        return JsonUtils.toJsonString(panels);
    }

    /**
     * 获得画风名称：随剧本基调时，取剧本的叙事基调；剧本未填基调（例如生成剧本时）由模型按剧本决定
     */
    private static String getStyleName(AiDramaProjectDO project, AiDramaScriptDO script) {
        AiDramaStyleEnum style = ArrayUtil.firstMatch(item -> item.getStyle().equals(project.getStyle()), AiDramaStyleEnum.values());
        if (style != null && style != AiDramaStyleEnum.SCRIPT_TONE) {
            return style.getName();
        }
        return script != null && StrUtil.isNotBlank(script.getTone())
                ? AiDramaStyleEnum.SCRIPT_TONE.getName() + "：" + StrUtil.trim(script.getTone())
                : AiDramaStyleEnum.SCRIPT_TONE.getName();
    }

    /**
     * 拼接世界观提示词：剧本未填写世界观时为空串
     */
    private static String buildWorldbuildingPrompt(AiDramaScriptDO script) {
        if (script == null || StrUtil.isBlank(script.getWorldbuilding())) {
            return "";
        }
        return StrUtil.format(WORLDBUILDING_PROMPT, StrUtil.trim(script.getWorldbuilding()));
    }

    /**
     * 拼接技能提示词：取技能入口文件 SKILL.md 的内容；未绑定、内容为空时为空串
     *
     * 绑定的技能已删除、已停用时报错，不静默忽略
     */
    private String buildSkillPrompt(Long skillId) {
        // 1. 未绑定技能
        if (skillId == null) {
            return "";
        }
        // 2. 校验技能存在、已启用
        AiSkillDO skill = skillService.getSkill(skillId);
        if (skill == null) {
            throw exception(SKILL_NOT_EXISTS);
        }
        if (CommonStatusEnum.isDisable(skill.getStatus())) {
            throw exception(SKILL_DISABLE, skill.getName());
        }
        // 3. 取根目录的入口文件 SKILL.md，拼接提示词
        AiSkillFileDO file = CollUtil.findOne(skillFileService.getSkillFileListBySkillId(skillId), item ->
                Objects.equals(item.getParentId(), AiSkillFileDO.PARENT_ID_ROOT) && AiSkillFileDO.NAME_SKILL.equals(item.getName()));
        if (file == null || StrUtil.isBlank(file.getContent())) {
            return "";
        }
        return StrUtil.format(SKILL_PROMPT, skill.getName(), StrUtil.trim(file.getContent()));
    }

    /**
     * 解析模型的回复并保存
     *
     * @return 生成、打磨剧本时为新增的剧本，其它为空
     */
    private AiDramaScriptDO saveChatResult(AiDramaChatTypeEnum type, AiDramaProjectDO project, AiDramaScriptDO script,
                                           String note, String content) {
        switch (type) {
            case SCRIPT_GENERATE: {
                AiDramaScriptDO current = scriptMapper.selectLatestByProjectId(project.getId());
                AiDramaScriptDO newScript = parseScript(type.getName(), content).setProjectId(project.getId())
                        .setSource(AiDramaScriptSourceEnum.LLM.getSource())
                        .setCreationMode(AiDramaScriptCreationModeEnum.STANDARD.getMode());
                if (current != null) {
                    newScript.setSourceMaterial(current.getSourceMaterial()).setWorldbuilding(current.getWorldbuilding());
                }
                scriptMapper.insert(newScript);
                if (current != null) {
                    projectService.updateProjectStatus(project.getId(), AiDramaProjectStatusEnum.SCRIPT_CHANGED.getStatus());
                }
                return newScript;
            }
            case SCRIPT_POLISH: {
                AiDramaScriptDO newScript = parseScript(type.getName(), content).setProjectId(project.getId())
                        .setSource(AiDramaScriptSourceEnum.LLM.getSource())
                        .setCreationMode(ObjUtil.defaultIfNull(script.getCreationMode(), AiDramaScriptCreationModeEnum.STANDARD.getMode()))
                        .setSourceMaterial(script.getSourceMaterial()).setWorldbuilding(script.getWorldbuilding())
                        .setRevisionNote(note);
                scriptMapper.insert(newScript);
                projectService.updateProjectStatus(project.getId(), AiDramaProjectStatusEnum.SCRIPT_CHANGED.getStatus());
                return newScript;
            }
            case ASSET_ANALYZE:
                saveAssetList(project, parseJson(type.getName(), content));
                return null;
            case STORYBOARD_GENERATE:
                saveStoryboardList(project, script, parseJson(type.getName(), content));
                return null;
            default:
                saveStoryboardDetail(type, project, script, parseJson(type.getName(), content));
                return null;
        }
    }

    /**
     * 保存分析出的角色、场景、道具：只新增名称不重复的
     */
    private void saveAssetList(AiDramaProjectDO project, JsonNode root) {
        // 1. 角色：同时创建主形象
        Set<String> characterNames = new HashSet<>(convertSet(assetService.getCharacterList(project.getId()),
                AiDramaCharacterDO::getName));
        for (JsonNode node : root.path("characters")) {
            String name = StrUtil.trim(node.path("name").asText());
            if (StrUtil.isEmpty(name) || !characterNames.add(name)) {
                continue;
            }
            String roleLevel = textOrNull(node, "roleLevel");
            assetService.createCharacter(new AiDramaCharacterDO().setProjectId(project.getId()).setName(name)
                    .setAliases(StrUtil.emptyToNull(CollUtil.join(textList(node, "aliases"), ",")))
                    .setRoleLevel(ArrayUtil.contains(AiDramaCharacterRoleLevelEnum.ARRAYS, roleLevel) ? roleLevel : null)
                    .setAgeRange(textOrNull(node, "ageRange")).setPersonalityTags(textList(node, "personalityTags"))
                    .setDescription(textOrNull(node, "description")).setPrompt(textOrNull(node, "prompt"))
                    .setVoiceDescription(textOrNull(node, "voiceDescription")));
        }
        // 2. 场景
        Set<String> locationNames = new HashSet<>(convertSet(assetService.getLocationList(project.getId()),
                AiDramaLocationDO::getName));
        for (JsonNode node : root.path("locations")) {
            String name = StrUtil.trim(node.path("name").asText());
            if (StrUtil.isEmpty(name) || !locationNames.add(name)) {
                continue;
            }
            assetService.createLocation(new AiDramaLocationDO().setProjectId(project.getId()).setName(name)
                    .setDescription(textOrNull(node, "description")).setCrowd(node.path("crowd").asBoolean(false))
                    .setCrowdDescription(textOrNull(node, "crowdDescription"))
                    .setAvailableSlots(textList(node, "availableSlots"))
                    .setImageUrls(new ArrayList<>()).setImageDescriptions(new ArrayList<>()));
        }
        // 3. 道具：出现的场次记在参考图中，分镜未单独指定道具时按它匹配
        Set<String> propTitles = new HashSet<>(convertSet(assetService.getPropList(project.getId()),
                AiDramaVisualAssetDO::getTitle));
        for (JsonNode node : root.path("props")) {
            String title = StrUtil.trim(node.path("title").asText());
            if (StrUtil.isEmpty(title) || !propTitles.add(title)) {
                continue;
            }
            List<Integer> shotNumbers = new ArrayList<>();
            node.path("shotNumbers").forEach(item -> {
                if (item.canConvertToInt()) {
                    shotNumbers.add(item.asInt());
                }
            });
            assetService.createVisualAsset(new AiDramaVisualAssetDO().setProjectId(project.getId())
                    .setKind(AiDramaVisualAssetKindEnum.PROP.getType()).setTitle(title)
                    .setPrompt(textOrNull(node, "prompt"))
                    .setReferenceImages(new AiDramaVisualAssetDO.ReferenceImages().setShotNumbers(shotNumbers)));
        }
    }

    /**
     * 读取字符串数组：去掉空白项
     */
    private static List<String> textList(JsonNode node, String field) {
        List<String> result = new ArrayList<>();
        node.path(field).forEach(item -> {
            String text = StrUtil.trim(item.asText());
            if (StrUtil.isNotEmpty(text)) {
                result.add(text);
            }
        });
        return result;
    }

    /**
     * 保存拆分出的分镜：替换该剧本的分镜，其它版本的保留；完成后项目置为分镜就绪
     */
    private void saveStoryboardList(AiDramaProjectDO project, AiDramaScriptDO script, JsonNode root) {
        // 1. 构建分镜（规划提示不保存）；校验生成期间没有开始生成镜头视频
        List<AiDramaStoryboardDO> storyboards = convertList(buildStoryboardList(project, script, root),
                AiDramaStoryboardPlanRespBO::getStoryboard);
        validateStoryboardNotRunning(project.getId(), script.getId());

        // 2. 替换该剧本的分镜，项目置为分镜就绪
        storyboardService.replaceStoryboardList(project.getId(), script.getId(), storyboards);
        projectService.updateProjectStatus(project.getId(), AiDramaProjectStatusEnum.STORYBOARD_READY.getStatus());
    }

    /**
     * 把模型返回的分镜构建为分镜（不保存）：资产按名称关联，角色取主形象
     *
     * @return 分镜，及其规划提示（规划分镜时返回供审阅，直接拆分时不使用）
     */
    private List<AiDramaStoryboardPlanRespBO> buildStoryboardList(AiDramaProjectDO project, AiDramaScriptDO script, JsonNode root) {
        // 1. 校验：模型返回了分镜
        if (!root.isArray() || root.isEmpty()) {
            throw exception(DRAMA_CHAT_FAIL, AiDramaChatTypeEnum.STORYBOARD_GENERATE.getName(), "模型没有返回分镜");
        }

        // 2. 构建分镜：资产按名称关联
        List<AiDramaCharacterDO> characters = assetService.getCharacterList(project.getId());
        Map<String, AiDramaCharacterDO> characterMap = convertMap(characters, AiDramaCharacterDO::getName);
        Map<Long, List<AiDramaCharacterAppearanceDO>> appearanceMap = convertMultiMap(
                assetService.getAppearanceListByCharacterIds(convertList(characters, AiDramaCharacterDO::getId)),
                AiDramaCharacterAppearanceDO::getCharacterId);
        Map<String, AiDramaLocationDO> locationMap = convertMap(assetService.getLocationList(project.getId()),
                AiDramaLocationDO::getName);
        Map<String, AiDramaVisualAssetDO> visualAssetMap = convertMap(assetService.getPropList(project.getId()),
                AiDramaVisualAssetDO::getTitle);
        List<AiDramaStoryboardPlanRespBO> storyboards = new ArrayList<>();
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
            // 出场角色：兼容只返回角色名的；形象按序号或变化原因匹配，未指定时用初始形象，指定了但无法唯一匹配时用初始形象并提示检查
            List<AiDramaStoryboardDO.CharacterRef> characterRefs = new ArrayList<>();
            List<String> planWarnings = new ArrayList<>();
            for (JsonNode item : node.path("characters")) {
                String name = StrUtil.trim(item.isObject() ? item.path("name").asText() : item.asText());
                AiDramaCharacterDO character = characterMap.get(name);
                if (character == null) {
                    continue;
                }
                String appearanceSpec = item.isObject() ? textOrNull(item, "appearance") : null;
                AiDramaCharacterAppearanceDO appearance = matchAppearance(appearanceMap.get(character.getId()), appearanceSpec);
                if (appearance == null && appearanceSpec != null) {
                    appearance = matchAppearance(appearanceMap.get(character.getId()), null);
                    planWarnings.add(StrUtil.format("{}的形象「{}」无法唯一匹配，已使用初始形象，请检查", name, appearanceSpec));
                }
                Long appearanceId = appearance != null ? appearance.getId() : null;
                if (appearanceId != null && !CollUtil.contains(characterRefs, ref -> appearanceId.equals(ref.getAppearanceId()))) {
                    characterRefs.add(new AiDramaStoryboardDO.CharacterRef().setAppearanceId(appearanceId)
                            .setSlot(item.isObject() ? textOrNull(item, "slot") : null));
                }
            }
            // 道具：返回了道具时明确指定（空数组表示不使用道具）；未返回时为空，按道具的场次匹配
            List<Long> visualAssetIds = null;
            if (node.path("props").isArray()) {
                visualAssetIds = new ArrayList<>();
                for (JsonNode title : node.path("props")) {
                    AiDramaVisualAssetDO visualAsset = visualAssetMap.get(StrUtil.trim(title.asText()));
                    if (visualAsset != null && !visualAssetIds.contains(visualAsset.getId())) {
                        visualAssetIds.add(visualAsset.getId());
                    }
                }
            }
            AiDramaLocationDO location = locationMap.get(StrUtil.trim(node.path("location").asText()));
            // 镜头序号：按返回顺序编号，保证同一剧本内不重复；原文场次记录在连续性设置中
            AiDramaStoryboardDO storyboard = new AiDramaStoryboardDO().setProjectId(project.getId()).setScriptId(script.getId())
                    .setSceneNo(index).setContinuity(buildPlanContinuity(node))
                    .setTitle(textOrNull(node, "title")).setSourceText(textOrNull(node, "sourceText"))
                    .setSceneText(textOrNull(node, "sceneText")).setShotType(textOrNull(node, "shotType"))
                    .setCameraMove(textOrNull(node, "cameraMove"))
                    .setDuration(node.path("duration").asInt() > 0 ? node.path("duration").asInt() : 5)
                    .setImagePrompt(textOrNull(node, "imagePrompt"))
                    .setVideoPrompt(textOrNull(node, "videoPrompt")).setDialogues(dialogues)
                    .setCharacters(characterRefs).setLocationId(location != null ? location.getId() : null)
                    .setVisualAssetIds(visualAssetIds);
            storyboards.add(new AiDramaStoryboardPlanRespBO().setStoryboard(storyboard)
                    .setWarnings(planWarnings.isEmpty() ? null : planWarnings));
        }
        return storyboards;
    }

    /**
     * 匹配角色的形象
     *
     * 1. 未指定时：初始形象（形象序号为 0，没有时取序号最小的）
     * 2. 指定为数字（可带「形象」前缀）时：按形象序号
     * 3. 其它按变化原因：先完全相同，再互相包含，最后按形象设定包含；须唯一
     *
     * @param appearances 角色的形象
     * @param spec 模型指定的形象，为空表示未指定
     * @return 形象；指定了但无法唯一匹配、角色没有形象时为空
     */
    @VisibleForTesting
    static AiDramaCharacterAppearanceDO matchAppearance(List<AiDramaCharacterAppearanceDO> appearances, String spec) {
        if (CollUtil.isEmpty(appearances)) {
            return null;
        }
        // 1. 未指定：初始形象
        String text = StrUtil.trimToNull(spec);
        if (text == null) {
            return appearances.stream().min(Comparator.comparing(AiDramaCharacterAppearanceDO::getAppearanceIndex,
                    Comparator.nullsLast(Comparator.naturalOrder()))).orElse(null);
        }
        // 2. 按形象序号
        String index = StrUtil.removePrefix(text, "形象").trim();
        if (StrUtil.isNotEmpty(index) && StrUtil.isNumeric(index)) {
            return findUnique(appearances, appearance -> Objects.equals(appearance.getAppearanceIndex(), Integer.valueOf(index)));
        }
        // 3. 按变化原因、形象设定
        AiDramaCharacterAppearanceDO appearance = findUnique(appearances, item -> StrUtil.equalsIgnoreCase(StrUtil.trim(item.getChangeReason()), text));
        if (appearance == null) {
            appearance = findUnique(appearances, item -> StrUtil.isNotBlank(item.getChangeReason())
                    && (item.getChangeReason().contains(text) || text.contains(StrUtil.trim(item.getChangeReason()))));
        }
        if (appearance == null) {
            appearance = findUnique(appearances, item -> StrUtil.contains(item.getDescription(), text));
        }
        return appearance;
    }

    private static <T> T findUnique(List<T> list, java.util.function.Predicate<T> predicate) {
        List<T> matched = filterList(list, predicate::test);
        return matched.size() == 1 ? matched.get(0) : null;
    }

    /**
     * 保存摄影规则、表演指导、关键帧规划：按镜头序号（该剧本分镜的顺序）对应，模型需要返回每个镜头
     */
    private void saveStoryboardDetail(AiDramaChatTypeEnum type, AiDramaProjectDO project, AiDramaScriptDO script, JsonNode root) {
        // 1.1 校验生成期间没有开始生成镜头视频
        validateStoryboardNotRunning(project.getId(), script.getId());
        // 1.2 按镜头序号读取模型的返回
        Map<Integer, JsonNode> panelMap = new HashMap<>();
        root.forEach(node -> {
            if (node.path("panelNumber").canConvertToInt()) {
                panelMap.put(node.path("panelNumber").asInt(), node);
            }
        });

        // 2. 逐个镜头更新对应的字段：缺少任一镜头时不保存
        List<AiDramaStoryboardDO> storyboards = getScriptStoryboardList(project.getId(), script.getId());
        List<AiDramaStoryboardDO> updateList = new ArrayList<>();
        for (int i = 0; i < storyboards.size(); i++) {
            AiDramaStoryboardDO storyboard = storyboards.get(i);
            JsonNode node = panelMap.get(i + 1);
            if (node == null) {
                throw exception(DRAMA_CHAT_FAIL, type.getName(), "模型没有返回第 " + (i + 1) + " 个镜头");
            }
            AiDramaStoryboardDO updateObj = new AiDramaStoryboardDO().setId(storyboard.getId());
            if (type == AiDramaChatTypeEnum.PHOTOGRAPHY_RULES) {
                Map<String, Object> rules = JsonUtils.parseObject(node.toString(), new TypeReference<LinkedHashMap<String, Object>>() {});
                rules.remove("panelNumber");
                updateObj.setPhotographyRules(rules);
            } else if (type == AiDramaChatTypeEnum.ACTING_NOTES) {
                updateObj.setActingNotes(buildActingNoteList(node.path("characters")));
            } else {
                AiDramaStoryboardDO.Continuity continuity = ObjUtil.defaultIfNull(storyboard.getContinuity(),
                        AiDramaStoryboardDO.Continuity::new);
                updateObj.setImagePrompt(textOrNull(node, "imagePrompt"))
                        .setContinuity(continuity.setFrameFocus(textOrNull(node, "frameFocus")));
            }
            updateList.add(updateObj);
        }
        storyboardService.updateStoryboardList(updateList);
    }

    // ========== 私有方法 ==========

    /**
     * 校验项目的当前剧本存在，且有正文
     */
    private AiDramaScriptDO validateScriptExists(Long projectId) {
        AiDramaScriptDO script = scriptMapper.selectLatestByProjectId(projectId);
        if (script == null || StrUtil.isBlank(script.getContent())) {
            throw exception(DRAMA_SCRIPT_NOT_EXISTS);
        }
        return script;
    }

    /**
     * 同步调用对话模型：未指定模型时，使用默认的对话模型
     *
     * @param scene 场景名，用于错误提示
     * @param modelId 对话模型编号，可为空
     * @param jsonObject 是否要求返回 JSON 对象：提示词要求输出 JSON 对象时为 true；输出 JSON 数组（拆分分镜）时为 false
     * @param content 提示词
     * @return 模型的回复
     */
    private String chat(String scene, Long modelId, boolean jsonObject, String content) {
        // 1. 获得模型：使用模型的默认思考档位，未配置思考时为 null，与原有行为一致
        AiModelDO model = validateChatModel(modelId);
        Map<String, Object> reasoningOptions = AiReasoningUtils.getReasoningOptions(model,
                AiReasoningUtils.resolveReasoningLevel(model, null));
        ChatModel chatModel = modelService.getChatModel(model.getId(), reasoningOptions);

        // 2. 同步调用
        String text;
        try {
            ChatResponse response = chatModel.call(buildPrompt(model, jsonObject, content, reasoningOptions));
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
     * 流式调用对话模型：返回增量文本，订阅时才发起调用
     */
    private Flux<String> chatStream(Long modelId, boolean jsonObject, String content) {
        // 1. 获得模型：使用模型的默认思考档位，未配置思考时为 null，与原有行为一致
        AiModelDO model = validateChatModel(modelId);
        Map<String, Object> reasoningOptions = AiReasoningUtils.getReasoningOptions(model,
                AiReasoningUtils.resolveReasoningLevel(model, null));
        ChatModel chatModel = modelService.getChatModel(model.getId(), reasoningOptions);

        // 2. 流式调用：只保留非空的增量文本
        return chatModel.stream(buildPrompt(model, jsonObject, content, reasoningOptions))
                .map(response -> response != null && response.getResult() != null && response.getResult().getOutput() != null
                        ? StrUtil.nullToEmpty(response.getResult().getOutput().getText()) : "")
                .filter(StrUtil::isNotEmpty);
    }

    /**
     * 校验对话模型：未指定时使用默认的对话模型
     */
    private AiModelDO validateChatModel(Long modelId) {
        if (modelId == null) {
            return modelService.getRequiredDefaultModel(AiModelTypeEnum.CHAT.getType());
        }
        AiModelDO model = modelService.validateModel(modelId);
        if (ObjUtil.notEqual(model.getType(), AiModelTypeEnum.CHAT.getType())) {
            throw exception(MODEL_USE_TYPE_ERROR);
        }
        return model;
    }

    /**
     * 构建对话请求
     *
     * @param reasoningOptions 思考参数，与客户端是同一份；未配置思考时为 null
     */
    private static Prompt buildPrompt(AiModelDO model, boolean jsonObject, String content,
                                      Map<String, Object> reasoningOptions) {
        AiPlatformEnum platform = AiPlatformEnum.validatePlatform(model.getPlatform());
        ChatOptions options = AiChatUtils.buildChatOptions(platform, model.getModel(), model.getTemperature(), model.getMaxTokens(),
                null, null, false, reasoningOptions);
        // JSON 输出：DeepSeek 开启 json_object，保证返回合法的 JSON 对象（提示词已包含“JSON”及格式示例，符合其要求）；
        // 它只约束顶层为对象，所以输出数组的拆分分镜不开启。其它平台的参数不统一，仍只靠提示词约束，由 parseJson 容错解析
        if (jsonObject && platform == AiPlatformEnum.DEEPSEEK) {
            options = ((DeepSeekChatOptions) options).mutate()
                    .responseFormat(ResponseFormat.builder().type(ResponseFormat.Type.JSON_OBJECT).build()).build();
        }
        return new Prompt(Collections.singletonList(new UserMessage(content)), options);
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

    private static Integer intOrNull(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return value != null && value.canConvertToInt() && value.asInt() > 0 ? value.asInt() : null;
    }

    private static Map<String, Object> mapOrNull(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return value != null && value.isObject()
                ? JsonUtils.parseObject(value.toString(), new TypeReference<LinkedHashMap<String, Object>>() {}) : null;
    }

    /**
     * 构建表演指导：缺少角色名或表演的跳过
     */
    private static List<AiDramaStoryboardDO.ActingNote> buildActingNoteList(JsonNode items) {
        List<AiDramaStoryboardDO.ActingNote> notes = new ArrayList<>();
        for (JsonNode item : items) {
            String name = textOrNull(item, "name");
            String acting = textOrNull(item, "acting");
            if (name != null && acting != null) {
                notes.add(new AiDramaStoryboardDO.ActingNote().setName(name).setActing(acting));
            }
        }
        return notes;
    }

    /**
     * 构建分镜规划的连续性设置：模型返回的规划字段，供关键帧与视频提示词使用
     */
    private static AiDramaStoryboardDO.Continuity buildPlanContinuity(JsonNode node) {
        // 0 秒出镜角色：未返回时为空，表示不按出镜角色过滤
        List<String> presentCharacters = null;
        if (node.path("presentCharacters").isArray()) {
            presentCharacters = new ArrayList<>();
            for (JsonNode name : node.path("presentCharacters")) {
                String text = StrUtil.trimToNull(name.asText());
                if (text != null && !presentCharacters.contains(text)) {
                    presentCharacters.add(text);
                }
            }
        }
        return new AiDramaStoryboardDO.Continuity().setSceneNumber(intOrNull(node, "sceneNumber"))
                .setSourceSceneContext(textOrNull(node, "sourceSceneContext"))
                .setSegmentNumber(intOrNull(node, "segmentNumber"))
                .setSegmentGoal(textOrNull(node, "segmentGoal")).setSegmentResult(textOrNull(node, "segmentResult"))
                .setBridgeIn(textOrNull(node, "bridgeIn")).setBridgeOut(textOrNull(node, "bridgeOut"))
                .setBeatType(textOrNull(node, "beatType")).setNarrativeCause(textOrNull(node, "narrativeCause"))
                .setCharacterGoal(textOrNull(node, "characterGoal")).setStoryAction(textOrNull(node, "storyAction"))
                .setStoryResult(textOrNull(node, "storyResult")).setNextHook(textOrNull(node, "nextHook"))
                .setStartState(textOrNull(node, "startState")).setEndState(textOrNull(node, "endState"))
                .setContinuityAction(textOrNull(node, "continuityAction"))
                .setSpatialAnchor(textOrNull(node, "spatialAnchor")).setPresentCharacters(presentCharacters)
                .setBackgroundExtras(textOrNull(node, "backgroundExtras"))
                .setShotDesign(mapOrNull(node, "shotDesign"))
                .setPerformanceBeats(node.path("performanceBeats").isArray()
                        ? buildActingNoteList(node.path("performanceBeats")) : null)
                .setTiming(mapOrNull(node, "timing"));
    }

    /**
     * 拼接名称，用于提示词；没有时为“无”
     */
    private static String joinNames(List<String> names) {
        return CollUtil.isEmpty(names) ? "无" : String.join("、", names);
    }

    /**
     * 进行中的流式生成
     */
    @Getter
    @AllArgsConstructor
    private static class ChatTask {

        /**
         * 生成类型
         */
        private final AiDramaChatTypeEnum type;
        /**
         * 已缓存的流：重新订阅时先重放已生成的内容
         */
        private final Flux<CommonResult<String>> flux;

    }

}
