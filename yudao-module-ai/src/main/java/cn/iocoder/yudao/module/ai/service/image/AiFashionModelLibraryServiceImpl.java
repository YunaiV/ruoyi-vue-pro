package cn.iocoder.yudao.module.ai.service.image;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.ai.controller.admin.image.vo.fashion.*;
import cn.iocoder.yudao.module.ai.dal.dataobject.image.*;
import cn.iocoder.yudao.module.ai.dal.mysql.image.*;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * AI 服装素材库 Service 实现
 *
 * <p>整合模特库管理（Python ModelLibraryManager）、
 * 增强采集器（Python EnhancedImageCollector）以及
 * AI 悬浮对话管理器（Python AIConversationManager）三大模块。</p>
 *
 * @author deepay
 */
@Service
@Slf4j
public class AiFashionModelLibraryServiceImpl implements AiFashionModelLibraryService {

    @Resource
    private AiFashionModelLibraryMapper libraryMapper;
    @Resource
    private AiFashionModelFeaturesMapper featuresMapper;
    @Resource
    private AiFashionCollectionSourceMapper sourceMapper;
    @Resource
    private AiFashionAssistantStateMapper assistantStateMapper;

    // ---------- 页面 → 默认窗口坐标 ----------
    private static final Map<String, int[]> PAGE_DEFAULT_POSITION = new LinkedHashMap<>();
    static {
        PAGE_DEFAULT_POSITION.put("home",            new int[]{50,  100});
        PAGE_DEFAULT_POSITION.put("model_library",   new int[]{30,  150});
        PAGE_DEFAULT_POSITION.put("design_studio",   new int[]{100,  80});
        PAGE_DEFAULT_POSITION.put("image_library",   new int[]{20,  120});
        PAGE_DEFAULT_POSITION.put("3d_viewer",       new int[]{20,   20});
    }

    // ---------- 页面 → 回复模板 ----------
    private static final Map<String, List<String>> PAGE_REPLY_TEMPLATES = new LinkedHashMap<>();
    static {
        PAGE_REPLY_TEMPLATES.put("home", Arrays.asList(
                "您好！我是服装设计助手。我可以：1) 浏览模特库 2) 设计服装 3) 搜索时尚图片 4) 生成3D模型。需要我做什么？",
                "收到您的请求！我可以带您去模特库挑选模特，或者开始设计新服装。您想先做什么？"
        ));
        PAGE_REPLY_TEMPLATES.put("model_library", Arrays.asList(
                "在模特库页面，我可以帮您：1) 按身高/体型筛选模特 2) 查看模特详情 3) 选择模特进行试衣。",
                "模特库助手在线！我们有各种体型、身高的模特可供选择。告诉我您想要什么样的模特，我来帮您筛选。"
        ));
        PAGE_REPLY_TEMPLATES.put("design_studio", Arrays.asList(
                "在设计工作室，我可以：1) 根据描述生成设计 2) 修改现有设计 3) 调整颜色和材质 4) 生成3D预览。",
                "设计助手就绪！您可以描述想要的服装，或者直接发指令（如"改成红色"、"出3款简约风"）。"
        ));
        PAGE_REPLY_TEMPLATES.put("image_library", Arrays.asList(
                "在图库页面，我可以：1) 按风格/颜色搜索图片 2) 查找相似款式 3) 一键发送到设计工作室。",
                "图库助手收到！我们有时装秀、品牌和街拍图片，说出您想找的风格，我立刻帮您搜索。"
        ));
        PAGE_REPLY_TEMPLATES.put("3d_viewer", Arrays.asList(
                "在3D预览页面，我可以：1) 旋转3D模型 2) 调整材质颜色 3) 导出OBJ/GLTF文件。",
                "3D助手在线！您想调整视角、材质还是颜色？说一句话我帮您搞定。"
        ));
    }

    // ---------- 页面 → 行动指令前缀（供前端解析） ----------
    private static final Map<String, List<String>> PAGE_ACTION_HINTS = new LinkedHashMap<>();
    static {
        PAGE_ACTION_HINTS.put("model_library",  Arrays.asList("FILTER_MODEL_BODY_TYPE", "FILTER_MODEL_HEIGHT_MIN", "OPEN_MODEL_DETAIL"));
        PAGE_ACTION_HINTS.put("design_studio",  Arrays.asList("START_DESIGN_TASK", "MODIFY_COLOR", "MODIFY_STYLE", "GENERATE_3D"));
        PAGE_ACTION_HINTS.put("image_library",  Arrays.asList("SEARCH_BY_STYLE", "SEARCH_BY_COLOR", "SEND_TO_STUDIO"));
        PAGE_ACTION_HINTS.put("3d_viewer",      Arrays.asList("ROTATE_MODEL", "CHANGE_COLOR", "EXPORT_OBJ"));
        PAGE_ACTION_HINTS.put("home",           Arrays.asList("GOTO_MODEL_LIBRARY", "GOTO_DESIGN_STUDIO", "GOTO_IMAGE_LIBRARY", "GOTO_3D_VIEWER"));
    }

    // =====================================================================
    // 素材库
    // =====================================================================

    @Override
    public PageResult<AiFashionModelLibraryRespVO> getLibraryPage(AiFashionModelLibraryPageReqVO pageReqVO) {
        PageResult<AiFashionModelLibraryDO> doPage = libraryMapper.selectPage(pageReqVO);
        List<AiFashionModelLibraryRespVO> voList = doPage.getList().stream()
                .map(this::toRespVO)
                .collect(Collectors.toList());
        return new PageResult<>(voList, doPage.getTotal());
    }

    @Override
    public AiFashionModelLibraryRespVO getLibraryDetail(Long id) {
        AiFashionModelLibraryDO lib = libraryMapper.selectById(id);
        if (lib == null) {
            return null;
        }
        AiFashionModelLibraryRespVO vo = toRespVO(lib);
        // 补充模特特征
        if (Boolean.TRUE.equals(lib.getIsModel())) {
            AiFashionModelFeaturesDO feat = featuresMapper.selectByLibraryImageId(id);
            if (feat != null) {
                fillFeatures(vo, feat);
            }
        }
        return vo;
    }

    @Override
    public AiFashionModelStatsRespVO getStats() {
        AiFashionModelStatsRespVO stats = new AiFashionModelStatsRespVO();

        stats.setTotalImages(libraryMapper.selectCount(null));
        stats.setModelImages(libraryMapper.selectCount(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<AiFashionModelLibraryDO>()
                        .eq(AiFashionModelLibraryDO::getIsModel, true)));
        stats.setFashionShowImages(libraryMapper.selectCount(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<AiFashionModelLibraryDO>()
                        .eq(AiFashionModelLibraryDO::getSourceType, "fashion_show")));
        stats.setBrandImages(libraryMapper.selectCount(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<AiFashionModelLibraryDO>()
                        .eq(AiFashionModelLibraryDO::getSourceType, "brand")));
        stats.setModelAgencyImages(libraryMapper.selectCount(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<AiFashionModelLibraryDO>()
                        .eq(AiFashionModelLibraryDO::getSourceType, "model_agency")));
        stats.setActiveSources(sourceMapper.selectCount(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<AiFashionCollectionSourceDO>()
                        .eq(AiFashionCollectionSourceDO::getStatus, "active")));

        // 品类分布
        stats.setCategoryDistribution(groupCount("category"));
        // 体型分布
        stats.setBodyTypeDistribution(groupCount("model_body_type"));
        // 品牌分布（Top 20）
        stats.setBrandDistribution(groupCount("brand"));

        // 姿势 & 肤色分布（来自 model_features 表）
        stats.setPoseDistribution(featureGroupCount("pose_type"));
        stats.setSkinToneDistribution(featureGroupCount("skin_tone"));

        stats.setStatsAt(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        return stats;
    }

    // =====================================================================
    // 采集源
    // =====================================================================

    @Override
    public List<AiFashionCollectionSourceDO> listSources(String sourceType) {
        if (sourceType != null && !sourceType.isEmpty()) {
            return sourceMapper.selectBySourceType(sourceType);
        }
        return sourceMapper.selectAllActive();
    }

    @Override
    public AiFashionCollectRespVO triggerCollect(AiFashionCollectReqVO reqVO) {
        AiFashionCollectRespVO resp = new AiFashionCollectRespVO();
        resp.setAsync(reqVO.getAsync());

        // 确定要采集的源
        List<AiFashionCollectionSourceDO> sources;
        if (reqVO.getSourceId() != null && !reqVO.getSourceId().isEmpty()) {
            AiFashionCollectionSourceDO single = sourceMapper.selectById(reqVO.getSourceId());
            sources = single != null ? Collections.singletonList(single) : Collections.emptyList();
        } else {
            sources = listSources(reqVO.getSourceType());
        }

        resp.setSourcesTriggered(sources.size());
        resp.setSourceIds(sources.stream().map(AiFashionCollectionSourceDO::getId).collect(Collectors.toList()));

        if (Boolean.TRUE.equals(reqVO.getAsync())) {
            // 异步：生成 jobId 并派发后台任务
            String jobId = "CJOB_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
            resp.setCollectJobId(jobId);
            doCollectAsync(sources, reqVO.getLimitPerSource(), jobId);
        } else {
            // 同步：执行并收集结果
            List<AiFashionCollectRespVO.SourceResult> results = sources.stream()
                    .map(src -> doCollectOne(src, reqVO.getLimitPerSource()))
                    .collect(Collectors.toList());
            resp.setResults(results);
        }

        return resp;
    }

    // =====================================================================
    // AI 悬浮对话框
    // =====================================================================

    @Override
    public AiFashionPageChatRespVO pageChat(Long userId, AiFashionPageChatReqVO reqVO) {
        String pageName = reqVO.getPageName();
        String message  = reqVO.getMessage();

        // 持久化窗口状态（位置、最小化）
        Boolean minimized = reqVO.getMinimized();
        Integer x = reqVO.getPositionX();
        Integer y = reqVO.getPositionY();
        updateAssistantState(userId, pageName, minimized, x, y);

        // 读取最新状态（含默认值回填）
        AiFashionAssistantStateDO state = getOrInitState(userId, pageName);

        // 生成页面感知回复
        String reply = buildPageReply(pageName, message);

        // 推导行动指令（根据消息中的关键词）
        List<String> actions = inferActions(pageName, message);

        // 尝试关联相关素材（简单关键词检索前5条）
        List<Long> relatedIds = searchRelatedImages(pageName, message);

        AiFashionPageChatRespVO respVO = new AiFashionPageChatRespVO();
        respVO.setSessionToken(reqVO.getSessionToken() != null ? reqVO.getSessionToken()
                : UUID.randomUUID().toString());
        respVO.setPageName(pageName);
        respVO.setReply(reply);
        respVO.setActions(actions);
        respVO.setRelatedImageIds(relatedIds);
        respVO.setMinimized(state.getMinimized() != null && state.getMinimized());
        respVO.setPositionX(state.getPositionX());
        respVO.setPositionY(state.getPositionY());
        return respVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateAssistantState(Long userId, String pageName,
                                     Boolean minimized, Integer x, Integer y) {
        if (pageName == null || pageName.isEmpty()) {
            return;
        }
        AiFashionAssistantStateDO state = assistantStateMapper.selectByUserIdAndPage(userId, pageName);
        if (state == null) {
            state = new AiFashionAssistantStateDO();
            state.setUserId(userId);
            state.setPageName(pageName);
            int[] def = PAGE_DEFAULT_POSITION.getOrDefault(pageName, new int[]{20, 20});
            state.setPositionX(def[0]);
            state.setPositionY(def[1]);
            state.setMinimized(false);
            state.setLastActiveTime(LocalDateTime.now());
            assistantStateMapper.insert(state);
        }

        if (minimized != null) {
            state.setMinimized(minimized);
        }
        if (x != null) {
            state.setPositionX(x);
        }
        if (y != null) {
            state.setPositionY(y);
        }
        state.setLastActiveTime(LocalDateTime.now());
        assistantStateMapper.updateById(state);
    }

    // =====================================================================
    // 私有辅助方法
    // =====================================================================

    /** 根据 DO 转 VO（不填模特特征，由调用方按需补充） */
    private AiFashionModelLibraryRespVO toRespVO(AiFashionModelLibraryDO d) {
        AiFashionModelLibraryRespVO vo = new AiFashionModelLibraryRespVO();
        vo.setId(d.getId());
        vo.setTitle(d.getTitle());
        vo.setDescription(d.getDescription());
        vo.setCategory(d.getCategory());
        vo.setBrand(d.getBrand());
        vo.setSeason(d.getSeason());
        vo.setSourceType(d.getSourceType());
        vo.setCollectionSourceId(d.getCollectionSourceId());
        vo.setWidth(d.getWidth());
        vo.setHeight(d.getHeight());
        vo.setFileFormat(d.getFileFormat());
        vo.setIsModel(d.getIsModel());
        vo.setModelPose(d.getModelPose());
        vo.setModelBodyType(d.getModelBodyType());
        vo.setLocalPath(d.getLocalPath());
        vo.setIndexedAt(d.getIndexedAt());
        vo.setCreateTime(d.getCreateTime());
        // JSON tags
        vo.setStyleTags(parseTags(d.getStyleTags()));
        vo.setColorTags(parseTags(d.getColorTags()));
        return vo;
    }

    private void fillFeatures(AiFashionModelLibraryRespVO vo, AiFashionModelFeaturesDO f) {
        vo.setHeightCm(f.getHeightCm());
        vo.setBustCm(f.getBustCm());
        vo.setWaistCm(f.getWaistCm());
        vo.setHipsCm(f.getHipsCm());
        vo.setSkinTone(f.getSkinTone());
        vo.setHairColor(f.getHairColor());
        vo.setHairLength(f.getHairLength());
        vo.setPoseType(f.getPoseType());
    }

    /** 简单解析 JSON 数组字符串为 List<String> */
    @SuppressWarnings("unchecked")
    private List<String> parseTags(String json) {
        if (json == null || json.isBlank()) {
            return Collections.emptyList();
        }
        try {
            // 轻量解析：["a","b"] → [a, b]
            String cleaned = json.trim();
            if (cleaned.startsWith("[")) {
                cleaned = cleaned.substring(1, cleaned.length() - 1);
            }
            if (cleaned.isBlank()) return Collections.emptyList();
            return Arrays.stream(cleaned.split(","))
                    .map(s -> s.trim().replaceAll("\"", ""))
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    /** 简单分组统计（MyBatis-Plus 原生 groupBy 暂用内存聚合方式） */
    private Map<String, Long> groupCount(String field) {
        List<AiFashionModelLibraryDO> all = libraryMapper.selectList(null);
        Map<String, Long> result = new LinkedHashMap<>();
        for (AiFashionModelLibraryDO d : all) {
            String key = switch (field) {
                case "category"        -> d.getCategory();
                case "model_body_type" -> d.getModelBodyType();
                case "brand"           -> d.getBrand();
                default -> null;
            };
            if (key != null && !key.isBlank()) {
                result.merge(key, 1L, Long::sum);
            }
        }
        return result.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(20)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (a, b) -> a, LinkedHashMap::new));
    }

    private Map<String, Long> featureGroupCount(String field) {
        List<AiFashionModelFeaturesDO> all = featuresMapper.selectList(null);
        Map<String, Long> result = new LinkedHashMap<>();
        for (AiFashionModelFeaturesDO f : all) {
            String key = switch (field) {
                case "pose_type"  -> f.getPoseType();
                case "skin_tone"  -> f.getSkinTone();
                default -> null;
            };
            if (key != null && !key.isBlank()) {
                result.merge(key, 1L, Long::sum);
            }
        }
        return result;
    }

    /** 生成页面感知 AI 回复 */
    private String buildPageReply(String pageName, String message) {
        List<String> templates = PAGE_REPLY_TEMPLATES.getOrDefault(pageName,
                Collections.singletonList("我收到您的消息：'" + message + "'，正在为您处理。"));
        // 将消息嵌入第一个模板
        String base = templates.get(0);
        return "【" + pageName + "】" + base;
    }

    /** 根据消息关键词推导行动指令 */
    private List<String> inferActions(String pageName, String message) {
        if (message == null) return Collections.emptyList();
        List<String> hints = PAGE_ACTION_HINTS.getOrDefault(pageName, Collections.emptyList());
        List<String> actions = new ArrayList<>();
        String msgLower = message.toLowerCase();

        if (pageName.equals("model_library")) {
            if (msgLower.contains("苗条") || msgLower.contains("slim")) actions.add("FILTER_MODEL_BODY_TYPE:slim");
            if (msgLower.contains("高挑") || msgLower.contains("tall"))  actions.add("FILTER_MODEL_HEIGHT_MIN:175");
            if (msgLower.contains("圆润") || msgLower.contains("curvy")) actions.add("FILTER_MODEL_BODY_TYPE:curvy");
        } else if (pageName.equals("design_studio")) {
            if (msgLower.contains("红") || msgLower.contains("red"))     actions.add("MODIFY_COLOR:red");
            if (msgLower.contains("蓝") || msgLower.contains("blue"))    actions.add("MODIFY_COLOR:blue");
            if (msgLower.contains("3d") || msgLower.contains("三维"))     actions.add("GENERATE_3D");
        } else if (pageName.equals("image_library")) {
            if (msgLower.contains("简约") || msgLower.contains("minimal")) actions.add("SEARCH_BY_STYLE:minimal");
            if (msgLower.contains("奢华") || msgLower.contains("luxury"))  actions.add("SEARCH_BY_STYLE:luxury");
        } else if (pageName.equals("3d_viewer")) {
            if (msgLower.contains("旋转") || msgLower.contains("rotate")) actions.add("ROTATE_MODEL:360");
            if (msgLower.contains("导出") || msgLower.contains("export")) actions.add("EXPORT_OBJ");
        } else if (pageName.equals("home")) {
            if (msgLower.contains("模特"))  actions.add("GOTO_MODEL_LIBRARY");
            if (msgLower.contains("设计"))  actions.add("GOTO_DESIGN_STUDIO");
            if (msgLower.contains("图库"))  actions.add("GOTO_IMAGE_LIBRARY");
            if (msgLower.contains("3d"))    actions.add("GOTO_3D_VIEWER");
        }

        return actions;
    }

    /** 关键词搜索相关素材（最多5条 ID） */
    private List<Long> searchRelatedImages(String pageName, String message) {
        if (message == null || message.isBlank()) return Collections.emptyList();
        try {
            AiFashionModelLibraryPageReqVO req = new AiFashionModelLibraryPageReqVO();
            req.setKeyword(message.length() > 20 ? message.substring(0, 20) : message);
            req.setPageNo(1);
            req.setPageSize(5);
            if ("model_library".equals(pageName)) {
                req.setIsModel(true);
            }
            PageResult<AiFashionModelLibraryRespVO> page = getLibraryPage(req);
            return page.getList().stream().map(AiFashionModelLibraryRespVO::getId).collect(Collectors.toList());
        } catch (Exception e) {
            log.warn("searchRelatedImages error: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    /** 获取或初始化 AssistantState（不写 DB，仅返回对象） */
    private AiFashionAssistantStateDO getOrInitState(Long userId, String pageName) {
        AiFashionAssistantStateDO state = assistantStateMapper.selectByUserIdAndPage(userId, pageName);
        if (state == null) {
            int[] def = PAGE_DEFAULT_POSITION.getOrDefault(pageName, new int[]{20, 20});
            state = new AiFashionAssistantStateDO();
            state.setUserId(userId);
            state.setPageName(pageName);
            state.setMinimized(false);
            state.setPositionX(def[0]);
            state.setPositionY(def[1]);
        }
        return state;
    }

    /** 异步触发采集（模拟：记录 last_collected 时间并更新 collect_count） */
    @Async
    protected void doCollectAsync(List<AiFashionCollectionSourceDO> sources, int limitPerSource, String jobId) {
        log.info("[采集任务 {}] 开始异步采集，共 {} 个来源", jobId, sources.size());
        for (AiFashionCollectionSourceDO src : sources) {
            try {
                AiFashionCollectRespVO.SourceResult r = doCollectOne(src, limitPerSource);
                log.info("[采集任务 {}] 来源 {} 完成：新增 {}", jobId, src.getId(), r.getNewCount());
            } catch (Exception e) {
                log.error("[采集任务 {}] 来源 {} 失败: {}", jobId, src.getId(), e.getMessage());
            }
        }
        log.info("[采集任务 {}] 全部采集完成", jobId);
    }

    /**
     * 执行单个采集源的采集逻辑
     *
     * <p>当前实现：更新 last_collected 时间戳并累加 collect_count。
     * 实际项目中此处替换为真正的 HTTP 采集 + 图片入库逻辑。</p>
     */
    private AiFashionCollectRespVO.SourceResult doCollectOne(AiFashionCollectionSourceDO src, int limit) {
        AiFashionCollectRespVO.SourceResult result = new AiFashionCollectRespVO.SourceResult();
        result.setSourceId(src.getId());
        result.setSourceName(src.getName());
        result.setSourceType(src.getSourceType());
        try {
            // 模拟：新增条数 = min(limit, 随机 1~limit)
            int newCount = new Random().nextInt(limit) + 1;
            int skipCount = new Random().nextInt(3);

            // 更新采集源统计
            src.setLastCollected(LocalDateTime.now());
            src.setCollectCount((src.getCollectCount() == null ? 0 : src.getCollectCount()) + newCount);
            sourceMapper.updateById(src);

            result.setNewCount(newCount);
            result.setSkipCount(skipCount);
            result.setSuccess(true);
        } catch (Exception e) {
            log.error("采集源 [{}] 执行失败: {}", src.getId(), e.getMessage());
            result.setNewCount(0);
            result.setSkipCount(0);
            result.setSuccess(false);
            result.setErrorMessage(e.getMessage());
        }
        return result;
    }

}
