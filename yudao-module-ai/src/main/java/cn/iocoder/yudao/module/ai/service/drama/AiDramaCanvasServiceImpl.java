package cn.iocoder.yudao.module.ai.service.drama;

import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.ai.controller.admin.drama.vo.canvas.AiDramaCanvasSaveReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.drama.vo.canvas.AiDramaDirectorSaveReqVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.drama.AiDramaCanvasDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.drama.AiDramaDirectorDO;
import cn.iocoder.yudao.module.ai.dal.mysql.drama.AiDramaCanvasMapper;
import cn.iocoder.yudao.module.ai.dal.mysql.drama.AiDramaDirectorMapper;
import cn.iocoder.yudao.module.ai.enums.drama.AiDramaAspectRatioEnum;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.annotations.VisibleForTesting;
import jakarta.annotation.Resource;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.*;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.SetUtils.asSet;
import static cn.iocoder.yudao.module.ai.enums.ErrorCodeConstants.*;

/**
 * AI 短剧画布 Service 实现类
 *
 * 1. 画布每个项目一份；3D 导演工程按画布的预演节点各一份。文档整体保存为 JSON，节点、连线、场景对象等不拆表
 * 2. 保存时比对修订号：读取后被其它窗口保存过的，拒绝保存，避免互相覆盖
 * 3. 保存只校验文档结构与上限；节点执行（生成剧本、出图、生成视频等）调用各自的业务接口，不在这里提交任何任务
 *
 * @author 芋道源码
 */
@Service
@Validated
public class AiDramaCanvasServiceImpl implements AiDramaCanvasService {

    /**
     * 画布节点类型
     */
    private static final Set<String> CANVAS_NODE_KINDS = asSet("text", "script", "image", "video", "audio", "group", "previs");
    /**
     * 画布文档的上限：大小（字节）、节点数、连线数
     */
    private static final int CANVAS_MAX_BYTES = 2 * 1024 * 1024;
    private static final int CANVAS_MAX_NODES = 500;
    private static final int CANVAS_MAX_EDGES = 1500;

    /**
     * 导演工程：对象类型、白模动作、关节、插值方式
     */
    private static final Set<String> DIRECTOR_OBJECT_KINDS = asSet("actor", "box", "wall", "door", "castle", "cannon");
    private static final Set<String> DIRECTOR_ACTIONS = asSet("idle", "walk", "run", "wave", "sit", "reach", "point",
            "nod", "shakeHead", "crouch", "bow", "talk");
    private static final Set<String> DIRECTOR_JOINTS = asSet("pelvis", "torso", "head", "leftShoulder", "rightShoulder",
            "leftArm", "leftElbow", "rightArm", "rightElbow", "leftHand", "rightHand", "leftLeg", "leftKnee", "rightLeg",
            "rightKnee", "leftFoot", "rightFoot");
    private static final Set<String> DIRECTOR_EASES = asSet("linear", "smooth", "hold");
    /**
     * 导演工程的上限：大小（字节）、对象数；帧率固定 24
     */
    private static final int DIRECTOR_MAX_BYTES = 1024 * 1024;
    private static final int DIRECTOR_MAX_OBJECTS = 80;
    private static final int DIRECTOR_FPS = 24;
    private static final String ID_PATTERN = "[a-zA-Z0-9_-]{1,100}";

    @Resource
    private AiDramaCanvasMapper canvasMapper;
    @Resource
    private AiDramaDirectorMapper directorMapper;

    @Resource
    private AiDramaProjectService projectService;

    // ========== 画布 ==========

    @Override
    public AiDramaCanvasDO getCanvasMy(Long userId, Long projectId) {
        projectService.validateProjectMy(projectId, userId);
        return canvasMapper.selectByProjectId(projectId);
    }

    @Override
    public Long saveCanvasMy(Long userId, AiDramaCanvasSaveReqVO reqVO) {
        // 1.1 校验项目存在，且属于当前用户
        projectService.validateProjectMy(reqVO.getProjectId(), userId);
        // 1.2 校验文档：结构、上限、节点、连线、分组
        validateCanvasDocument(JsonUtils.parseTree(JsonUtils.toJsonString(reqVO.getDocument())));

        // 2. 比对修订号：未保存过时为 0；与读取时的不一致，说明期间被其它窗口保存过
        AiDramaCanvasDO canvas = canvasMapper.selectByProjectId(reqVO.getProjectId());
        long revision = canvas != null ? canvas.getRevision() : 0L;
        if (ObjUtil.notEqual(revision, reqVO.getRevision())) {
            throw exception(DRAMA_CANVAS_REVISION_CONFLICT);
        }
        // 3.1 首次保存：新增，修订号为 1
        if (canvas == null) {
            try {
                canvasMapper.insert(new AiDramaCanvasDO().setProjectId(reqVO.getProjectId()).setRevision(1L)
                        .setDocument(reqVO.getDocument()));
            } catch (DuplicateKeyException e) {
                // 其它窗口同时首次保存
                throw exception(DRAMA_CANVAS_REVISION_CONFLICT);
            }
            return 1L;
        }
        // 3.2 再次保存：以修订号为条件更新，修订号加 1；更新不到，说明期间被其它窗口保存过
        int updated = canvasMapper.updateByIdAndRevision(canvas.getId(), revision,
                new AiDramaCanvasDO().setRevision(revision + 1).setDocument(reqVO.getDocument()));
        if (updated == 0) {
            throw exception(DRAMA_CANVAS_REVISION_CONFLICT);
        }
        return revision + 1;
    }

    /**
     * 校验画布文档：结构、上限、节点编号唯一、连线端点存在且无环、分组关系
     */
    @VisibleForTesting
    static void validateCanvasDocument(JsonNode document) {
        // 1. 结构与上限
        if (document == null || !document.isObject() || document.path("schema").asInt() != 1
                || !document.path("nodes").isArray() || !document.path("edges").isArray()) {
            throw exception(DRAMA_CANVAS_INVALID, "格式不正确");
        }
        if (JsonUtils.toJsonByte(document).length > CANVAS_MAX_BYTES || document.path("nodes").size() > CANVAS_MAX_NODES
                || document.path("edges").size() > CANVAS_MAX_EDGES) {
            throw exception(DRAMA_CANVAS_INVALID, "超过保存上限（2MB、500 个节点、1500 条连线）");
        }
        // 2. 节点：编号唯一、类型有效、坐标为数字
        Map<String, String> kinds = new HashMap<>();
        for (JsonNode node : document.path("nodes")) {
            String id = node.path("id").asText();
            JsonNode position = node.path("position");
            if (StrUtil.isBlank(id) || id.length() > 180 || kinds.containsKey(id) || !CANVAS_NODE_KINDS.contains(node.path("kind").asText())
                    || !node.path("data").isObject() || !isFiniteNumber(position.path("x")) || !isFiniteNumber(position.path("y"))) {
                throw exception(DRAMA_CANVAS_INVALID, "节点格式或编号无效");
            }
            kinds.put(id, node.path("kind").asText());
        }
        // 3. 连线：端点存在、不连自身、不连分组；整体无环
        Map<String, List<String>> targets = new HashMap<>();
        Set<String> edgeIds = new HashSet<>();
        for (JsonNode edge : document.path("edges")) {
            String source = edge.path("source").asText();
            String target = edge.path("target").asText();
            if (!edgeIds.add(edge.path("id").asText()) || !kinds.containsKey(source) || !kinds.containsKey(target)
                    || source.equals(target) || "group".equals(kinds.get(source)) || "group".equals(kinds.get(target))) {
                throw exception(DRAMA_CANVAS_INVALID, "连线端点无效");
            }
            targets.computeIfAbsent(source, key -> new ArrayList<>()).add(target);
        }
        Set<String> visited = new HashSet<>();
        for (String id : kinds.keySet()) {
            visitCanvasNode(id, targets, visited, new HashSet<>());
        }
        // 4. 分组：只能属于分组节点，分组不能嵌套
        for (JsonNode node : document.path("nodes")) {
            String parentId = node.path("parentId").asText();
            if (!parentId.isEmpty() && (!"group".equals(kinds.get(parentId)) || parentId.equals(node.path("id").asText())
                    || "group".equals(node.path("kind").asText()))) {
                throw exception(DRAMA_CANVAS_INVALID, "分组关系无效");
            }
        }
    }

    /**
     * 深度优先遍历连线，校验从该节点出发不形成循环
     *
     * @param id      当前节点编号
     * @param targets 每个节点连出的目标节点编号
     * @param visited 已遍历过的节点：从它们出发不会形成循环，不再重复遍历
     * @param active  当前遍历路径上的节点
     */
    private static void visitCanvasNode(String id, Map<String, List<String>> targets, Set<String> visited, Set<String> active) {
        // 1. 在当前路径上再次遇到，说明连线形成循环
        if (active.contains(id)) {
            throw exception(DRAMA_CANVAS_INVALID, "连线不能形成循环");
        }
        // 2. 已遍历过，跳过
        if (!visited.add(id)) {
            return;
        }
        // 3. 加入当前路径，遍历连出的节点；遍历完后移出路径
        active.add(id);
        for (String next : targets.getOrDefault(id, Collections.emptyList())) {
            visitCanvasNode(next, targets, visited, active);
        }
        active.remove(id);
    }

    // ========== 3D 导演工程 ==========

    @Override
    public AiDramaDirectorDO getDirectorMy(Long userId, Long projectId, String nodeId) {
        projectService.validateProjectMy(projectId, userId);
        return directorMapper.selectByProjectIdAndNodeId(projectId, nodeId);
    }

    @Override
    public Long saveDirectorMy(Long userId, AiDramaDirectorSaveReqVO reqVO) {
        // 1.1 校验项目；预演节点需已保存在画布中
        projectService.validateProjectMy(reqVO.getProjectId(), userId);
        AiDramaCanvasDO canvas = canvasMapper.selectByProjectId(reqVO.getProjectId());
        boolean nodeExists = false;
        if (canvas != null && canvas.getDocument() != null) {
            for (JsonNode node : JsonUtils.parseTree(JsonUtils.toJsonString(canvas.getDocument())).path("nodes")) {
                nodeExists |= reqVO.getNodeId().equals(node.path("id").asText()) && "previs".equals(node.path("kind").asText());
            }
        }
        if (!nodeExists) {
            throw exception(DRAMA_DIRECTOR_INVALID, "预演节点不在画布中，请先保存画布");
        }
        // 1.2 校验文档
        validateDirectorDocument(JsonUtils.parseTree(JsonUtils.toJsonString(reqVO.getDocument())));

        // 2. 比对修订号：未保存过时为 0；与读取时的不一致，说明期间被其它窗口保存过
        AiDramaDirectorDO director = directorMapper.selectByProjectIdAndNodeId(reqVO.getProjectId(), reqVO.getNodeId());
        long revision = director != null ? director.getRevision() : 0L;
        if (ObjUtil.notEqual(revision, reqVO.getRevision())) {
            throw exception(DRAMA_DIRECTOR_REVISION_CONFLICT);
        }
        // 3.1 首次保存：新增，修订号为 1
        if (director == null) {
            try {
                directorMapper.insert(new AiDramaDirectorDO().setProjectId(reqVO.getProjectId()).setNodeId(reqVO.getNodeId())
                        .setRevision(1L).setDocument(reqVO.getDocument()));
            } catch (DuplicateKeyException e) {
                // 其它窗口同时首次保存
                throw exception(DRAMA_DIRECTOR_REVISION_CONFLICT);
            }
            return 1L;
        }
        // 3.2 再次保存：以修订号为条件更新，修订号加 1；更新不到，说明期间被其它窗口保存过
        int updated = directorMapper.updateByIdAndRevision(director.getId(), revision,
                new AiDramaDirectorDO().setRevision(revision + 1).setDocument(reqVO.getDocument()));
        if (updated == 0) {
            throw exception(DRAMA_DIRECTOR_REVISION_CONFLICT);
        }
        return revision + 1;
    }

    /**
     * 校验导演工程文档：帧率 24、时长 2 ~ 60 秒、画幅 16:9 或 9:16；对象、关键帧、动作片段、相机在范围内
     */
    @VisibleForTesting
    static void validateDirectorDocument(JsonNode document) {
        // 1. 整体结构与上限
        if (document == null || !document.isObject() || document.path("schema").asInt() != 1
                || document.path("fps").asInt() != DIRECTOR_FPS || !document.path("duration").isIntegralNumber()
                || document.path("duration").asInt() < 2 || document.path("duration").asInt() > 60
                || !StrUtil.equalsAny(document.path("aspect").asText(), AiDramaAspectRatioEnum.RATIO_16_9.getRatio(),
                        AiDramaAspectRatioEnum.RATIO_9_16.getRatio()) || !document.path("objects").isArray()
                || document.path("objects").size() > DIRECTOR_MAX_OBJECTS || JsonUtils.toJsonByte(document).length > DIRECTOR_MAX_BYTES) {
            throw exception(DRAMA_DIRECTOR_INVALID, "格式无效或超过上限（1MB、80 个对象、2 ~ 60 秒）");
        }
        int frames = document.path("duration").asInt() * DIRECTOR_FPS;
        // 2. 场景对象
        Set<String> ids = new HashSet<>();
        Set<String> castleIds = new HashSet<>();
        for (JsonNode object : document.path("objects")) {
            String kind = object.path("kind").asText();
            if (!object.path("id").asText().matches(ID_PATTERN) || !ids.add(object.path("id").asText())
                    || !DIRECTOR_OBJECT_KINDS.contains(kind) || StrUtil.isBlank(object.path("name").asText())
                    || object.path("name").asText().length() > 100 || !object.path("color").asText().matches("#[a-fA-F0-9]{6}")
                    || !object.path("visible").isBoolean() || !object.path("locked").isBoolean()) {
                throw exception(DRAMA_DIRECTOR_INVALID, "场景对象无效");
            }
            if ("castle".equals(kind)) {
                castleIds.add(object.path("id").asText());
            }
            validateVector(object.path("scale"), true);
            validateKeys(object.path("keys"), frames);
            if (object.has("fireFrame") && (!"cannon".equals(kind) || !object.path("fireFrame").isIntegralNumber()
                    || object.path("fireFrame").asInt() < 0 || object.path("fireFrame").asInt() >= frames)) {
                throw exception(DRAMA_DIRECTOR_INVALID, "开炮时刻超出时间轴范围");
            }
            for (JsonNode key : object.path("keys")) {
                validateVector(key.path("position"), false);
                validateNumber(key.path("yaw"), -360, 360);
                if (!DIRECTOR_ACTIONS.contains(key.path("action").asText())) {
                    throw exception(DRAMA_DIRECTOR_INVALID, "白模动作无效");
                }
                validateEase(key);
                validatePose(key.path("pose"));
            }
            validateClips(object, kind, frames);
        }
        // 3. 对象之间的引用：注视对象、大炮的目标
        for (JsonNode object : document.path("objects")) {
            if (object.has("lookAtId") && (!ids.contains(object.path("lookAtId").asText())
                    || object.path("lookAtId").asText().equals(object.path("id").asText()))) {
                throw exception(DRAMA_DIRECTOR_INVALID, "注视对象无效");
            }
            if (object.has("targetId") && (!"cannon".equals(object.path("kind").asText())
                    || !castleIds.contains(object.path("targetId").asText()))) {
                throw exception(DRAMA_DIRECTOR_INVALID, "大炮的目标必须是当前场景的城门城墙");
            }
        }
        // 4. 相机与动作标记
        JsonNode camera = document.path("camera");
        validateKeys(camera.path("keys"), frames);
        for (JsonNode key : camera.path("keys")) {
            validateVector(key.path("position"), false);
            validateVector(key.path("target"), false);
            validateNumber(key.path("fov"), 15, 100);
            validateNumber(key.path("roll"), -180, 180);
            validateEase(key);
            boolean same = true;
            for (int i = 0; i < 3; i++) {
                if (Math.abs(key.path("position").get(i).asDouble() - key.path("target").get(i).asDouble()) >= 0.001) {
                    same = false;
                }
            }
            if (same) {
                throw exception(DRAMA_DIRECTOR_INVALID, "机位与拍摄目标不能重合");
            }
        }
        if (camera.has("follow")) {
            JsonNode follow = camera.path("follow");
            if (!ids.contains(follow.path("objectId").asText()) || !StrUtil.equalsAny(follow.path("mode").asText(), "aim", "follow")) {
                throw exception(DRAMA_DIRECTOR_INVALID, "相机跟随对象无效");
            }
            validateVector(follow.path("offset"), false);
            validateNumber(follow.path("height"), 0, 3);
        }
        if (document.has("markers")) {
            JsonNode markers = document.path("markers");
            if (!markers.isArray() || markers.size() > 100) {
                throw exception(DRAMA_DIRECTOR_INVALID, "动作标记无效");
            }
            for (JsonNode marker : markers) {
                int frame = marker.path("frame").asInt(-1);
                String label = marker.path("label").asText();
                if (frame < 0 || frame >= frames || StrUtil.isBlank(label) || label.length() > 60) {
                    throw exception(DRAMA_DIRECTOR_INVALID, "动作标记无效");
                }
            }
        }
    }

    /**
     * 校验关节姿态：height 为身高偏移，其它为关节的三轴角度
     */
    private static void validatePose(JsonNode pose) {
        if (pose.isMissingNode()) {
            return;
        }
        if (!pose.isObject()) {
            throw exception(DRAMA_DIRECTOR_INVALID, "关节姿态无效");
        }
        pose.fields().forEachRemaining(entry -> {
            if ("height".equals(entry.getKey())) {
                validateNumber(entry.getValue(), -0.9, 1);
                return;
            }
            if (!DIRECTOR_JOINTS.contains(entry.getKey()) || !entry.getValue().isArray() || entry.getValue().size() != 3) {
                throw exception(DRAMA_DIRECTOR_INVALID, "关节名称或角度无效");
            }
            entry.getValue().forEach(angle -> validateNumber(angle, -180, 180));
        });
    }

    /**
     * 校验人物的动作片段：按时间排列、不重叠，过渡不超过片段的一半
     */
    private static void validateClips(JsonNode object, String kind, int frames) {
        if (!object.has("clips")) {
            return;
        }
        JsonNode clips = object.path("clips");
        if (!"actor".equals(kind) || !clips.isArray() || clips.size() > 100) {
            throw exception(DRAMA_DIRECTOR_INVALID, "动作片段无效");
        }
        int previousEnd = 0;
        Set<String> clipIds = new HashSet<>();
        for (JsonNode clip : clips) {
            int start = clip.path("start").asInt(-1);
            int end = clip.path("end").asInt(-1);
            int blend = clip.path("blend").asInt(-1);
            if (!clip.path("id").asText().matches(ID_PATTERN) || !clipIds.add(clip.path("id").asText())
                    || !DIRECTOR_ACTIONS.contains(clip.path("action").asText()) || start < previousEnd || end <= start
                    || end > frames || blend < 0 || blend > (end - start) / 2) {
                throw exception(DRAMA_DIRECTOR_INVALID, "动作片段时间无效、重叠或过渡过长");
            }
            validateNumber(clip.path("speed"), 0.25, 3);
            validateNumber(clip.path("strength"), 0, 1.5);
            previousEnd = end;
        }
    }

    private static void validateEase(JsonNode key) {
        if (key.has("ease") && !DIRECTOR_EASES.contains(key.path("ease").asText())) {
            throw exception(DRAMA_DIRECTOR_INVALID, "插值方式无效");
        }
    }

    private static void validateVector(JsonNode vector, boolean scale) {
        if (!vector.isArray() || vector.size() != 3) {
            throw exception(DRAMA_DIRECTOR_INVALID, "坐标格式无效");
        }
        vector.forEach(value -> validateNumber(value, scale ? 0.1 : -100, scale ? 20 : 100));
    }

    private static void validateNumber(JsonNode value, double min, double max) {
        if (!isFiniteNumber(value) || value.asDouble() < min || value.asDouble() > max) {
            throw exception(DRAMA_DIRECTOR_INVALID, "数值超出范围");
        }
    }

    /**
     * 校验关键帧：从第 0 帧开始，严格递增，且在时间轴内
     */
    private static void validateKeys(JsonNode keys, int frames) {
        if (!keys.isArray() || keys.isEmpty() || keys.size() > 1440 || keys.get(0).path("frame").asInt(-1) != 0) {
            throw exception(DRAMA_DIRECTOR_INVALID, "关键帧必须从第 0 帧开始");
        }
        int previous = -1;
        for (JsonNode key : keys) {
            int frame = key.path("frame").asInt(-1);
            if (!key.path("frame").isIntegralNumber() || frame <= previous || frame >= frames) {
                throw exception(DRAMA_DIRECTOR_INVALID, "关键帧顺序或时间无效");
            }
            previous = frame;
        }
    }

    private static boolean isFiniteNumber(JsonNode value) {
        return value.isNumber() && Double.isFinite(value.asDouble());
    }

    // ========== 删除 ==========

    @Override
    public void deleteCanvasByProjectId(Long projectId) {
        canvasMapper.deleteByProjectId(projectId);
        directorMapper.deleteByProjectId(projectId);
    }

}
