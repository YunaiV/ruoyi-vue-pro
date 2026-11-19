package cn.iocoder.yudao.module.ai.service.skill;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.ai.controller.admin.skill.vo.file.AiSkillFileContentReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.skill.vo.file.AiSkillFileCreateReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.skill.vo.file.AiSkillFileMoveReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.skill.vo.file.AiSkillFileRenameReqVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.skill.AiSkillDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.skill.AiSkillFileDO;
import cn.iocoder.yudao.module.ai.dal.mysql.skill.AiSkillFileMapper;
import cn.iocoder.yudao.module.ai.enums.skill.AiSkillFileTypeEnum;
import javax.annotation.Resource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.nio.charset.StandardCharsets;
import java.util.*;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.*;
import static cn.iocoder.yudao.module.ai.enums.ErrorCodeConstants.*;

/**
 * AI 技能文件 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class AiSkillFileServiceImpl implements AiSkillFileService {

    /**
     * 固定目录：scripts，只作为文本保存，不执行
     */
    static final String DIRECTORY_NAME_SCRIPTS = "scripts";
    /**
     * 固定目录：reference，存放参考文档
     */
    static final String DIRECTORY_NAME_REFERENCE = "reference";

    /**
     * 允许的文件类型：只保存文本文件
     */
    static final List<String> FILE_TYPES = Arrays.asList("md", "txt", "json", "yaml", "yml", "csv");
    /**
     * 单个文件的最大字节数：256 KB
     */
    static final int FILE_SIZE_MAX = 256 * 1024;
    /**
     * 单个技能文件合计的最大字节数：2 MB
     */
    static final int TOTAL_SIZE_MAX = 2 * 1024 * 1024;
    /**
     * 名称的最大长度
     */
    static final int NAME_MAX_LENGTH = 100;
    /**
     * SKILL.md 头信息 description 的最大长度
     */
    static final int DESCRIPTION_MAX_LENGTH = 1024;

    /**
     * SKILL.md 默认内容模板：占位符依次为名称、描述、名称
     */
    private static final String SKILL_FILE_CONTENT_TEMPLATE = "---\n"
            + "name: {}\n"
            + "description: {}\n"
            + "---\n\n"
            + "# {}\n\n"
            + "在这里编写技能说明。模型先只看到上面的 description，需要时才会读取本文件正文。\n\n"
            + "- `reference/`：存放参考文档（md / txt / json / yaml / csv），正文中用相对路径引用，例如 reference/guide.md\n"
            + "- `scripts/`：只作为文本保存，不会被执行\n";
    /**
     * 新建 Markdown 文件的默认内容
     */
    private static final String MARKDOWN_FILE_CONTENT_TEMPLATE = "# 新文档\n\n请在此编写内容（Markdown）。\n";

    @Resource
    private AiSkillFileMapper skillFileMapper;

    @Resource
    @Lazy // 延迟加载，避免循环依赖
    private AiSkillService skillService;

    @Override
    public void createDefaultSkillFileList(AiSkillDO skill) {
        // 1. 用创建时的名称、描述生成 SKILL.md；之后描述以 SKILL.md 为准
        String content = StrUtil.format(SKILL_FILE_CONTENT_TEMPLATE, skill.getName(),
                toFrontmatterValue(skill.getDescription()), skill.getName());
        // 与保存 SKILL.md 时的校验一致，避免生成之后无法原样保存的头信息（例如空白描述）
        validateSkillFrontmatter(skill, content);

        // 2. 插入固定节点：SKILL.md + scripts/ + reference/
        AiSkillFileDO skillFile = AiSkillFileDO.builder().skillId(skill.getId()).parentId(AiSkillFileDO.PARENT_ID_ROOT)
                .name(AiSkillFileDO.NAME_SKILL).type(AiSkillFileTypeEnum.FILE.getType()).fileType("md")
                .content(content).size(StrUtil.byteLength(content, StandardCharsets.UTF_8)).locked(true).sort(1).build();
        AiSkillFileDO scriptsDirectory = AiSkillFileDO.builder().skillId(skill.getId()).parentId(AiSkillFileDO.PARENT_ID_ROOT)
                .name(DIRECTORY_NAME_SCRIPTS).type(AiSkillFileTypeEnum.DIRECTORY.getType()).size(0).locked(true).sort(2).build();
        AiSkillFileDO referenceDirectory = AiSkillFileDO.builder().skillId(skill.getId()).parentId(AiSkillFileDO.PARENT_ID_ROOT)
                .name(DIRECTORY_NAME_REFERENCE).type(AiSkillFileTypeEnum.DIRECTORY.getType()).size(0).locked(true).sort(3).build();
        skillFileMapper.insertBatch(Arrays.asList(skillFile, scriptsDirectory, referenceDirectory));
    }

    /**
     * 头信息的值保持单行，避免换行被解析成额外的字段
     */
    static String toFrontmatterValue(String value) {
        return StrUtil.trim(StrUtil.replaceChars(StrUtil.nullToEmpty(value), new char[]{'\r', '\n'}, " "));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createSkillFile(AiSkillFileCreateReqVO createReqVO) {
        // 1.1 校验技能存在、名称、类型
        skillService.validateSkillExists(createReqVO.getSkillId());
        String name = validateSkillFileName(createReqVO.getName());
        boolean file = AiSkillFileTypeEnum.isFile(createReqVO.getType());
        String fileType = file ? validateFileType(name) : null;
        // 1.2 校验父目录、同级唯一
        validateParentDirectory(createReqVO.getSkillId(), createReqVO.getParentId());
        validateSkillFileNameUnique(null, createReqVO.getSkillId(), createReqVO.getParentId(), name);
        // 1.3 校验大小：Markdown 文件使用默认模板内容，其它文件为空内容
        String content = file ? ("md".equals(fileType) ? MARKDOWN_FILE_CONTENT_TEMPLATE : "") : null;
        int size = StrUtil.byteLength(content, StandardCharsets.UTF_8);
        validateSkillFileSize(createReqVO.getSkillId(), null, size);

        // 2. 插入：排序号追加到同级末尾
        List<AiSkillFileDO> siblings = skillFileMapper.selectListBySkillIdAndParentId(
                createReqVO.getSkillId(), createReqVO.getParentId());
        Integer maxSort = getMaxValue(siblings, AiSkillFileDO::getSort);
        AiSkillFileDO skillFile = AiSkillFileDO.builder().skillId(createReqVO.getSkillId())
                .parentId(createReqVO.getParentId()).name(name).type(createReqVO.getType())
                .fileType(fileType).content(content).size(size)
                .locked(false).sort(maxSort != null ? maxSort + 1 : 1).build();
        skillFileMapper.insert(skillFile);
        return skillFile.getId();
    }

    @Override
    public void renameSkillFile(AiSkillFileRenameReqVO renameReqVO) {
        // 1. 校验存在、非固定、名称、类型、同级唯一
        AiSkillFileDO skillFile = validateSkillFileExists(renameReqVO.getId());
        validateSkillFileNotLocked(skillFile, "重命名");
        String name = validateSkillFileName(renameReqVO.getName());
        String fileType = AiSkillFileTypeEnum.isFile(skillFile.getType()) ? validateFileType(name) : null;
        validateSkillFileNameUnique(skillFile.getId(), skillFile.getSkillId(), skillFile.getParentId(), name);

        // 2. 更新：文件同步刷新扩展名
        skillFileMapper.updateById(new AiSkillFileDO().setId(skillFile.getId()).setName(name).setFileType(fileType));
    }

    @Override
    public void moveSkillFile(AiSkillFileMoveReqVO moveReqVO) {
        // 1.1 校验存在、非固定
        AiSkillFileDO skillFile = validateSkillFileExists(moveReqVO.getId());
        validateSkillFileNotLocked(skillFile, "移动");
        // 1.2 校验目标目录：不能是自身或其后代，且必须是同一技能下的目录
        Long parentId = moveReqVO.getParentId();
        if (ObjUtil.equal(parentId, skillFile.getId())) {
            throw exception(SKILL_FILE_MOVE_TO_SELF);
        }
        if (isDescendant(skillFile.getSkillId(), skillFile.getId(), parentId)) {
            throw exception(SKILL_FILE_MOVE_TO_DESCENDANT);
        }
        validateParentDirectory(skillFile.getSkillId(), parentId);
        // 1.3 校验目标目录下同名唯一
        validateSkillFileNameUnique(skillFile.getId(), skillFile.getSkillId(), parentId, skillFile.getName());

        // 2. 更新
        skillFileMapper.updateById(new AiSkillFileDO().setId(skillFile.getId()).setParentId(parentId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSkillFileContent(AiSkillFileContentReqVO contentReqVO) {
        // 1.1 校验存在，且为文件
        AiSkillFileDO skillFile = validateSkillFileExists(contentReqVO.getId());
        if (!AiSkillFileTypeEnum.isFile(skillFile.getType())) {
            throw exception(SKILL_FILE_NOT_FILE);
        }
        // 1.2 校验大小：单个文件、技能合计
        String content = contentReqVO.getContent();
        int size = StrUtil.byteLength(content, StandardCharsets.UTF_8);
        validateSkillFileSize(skillFile.getSkillId(), skillFile.getId(), size);
        // 1.3 SKILL.md：校验头信息，name 必须与技能名称一致
        String description = null;
        if (isSkillFile(skillFile)) {
            AiSkillDO skill = skillService.validateSkillExists(skillFile.getSkillId());
            description = validateSkillFrontmatter(skill, content);
        }

        // 2. 更新内容
        skillFileMapper.updateById(new AiSkillFileDO().setId(skillFile.getId()).setContent(content).setSize(size));

        // 3. SKILL.md：同步描述到技能，以 SKILL.md 为唯一来源
        if (description != null) {
            skillService.updateSkillDescription(skillFile.getSkillId(), description);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteSkillFile(Long id) {
        // 1. 校验存在、非固定
        AiSkillFileDO skillFile = validateSkillFileExists(id);
        validateSkillFileNotLocked(skillFile, "删除");

        // 2.1 收集自身及全部后代
        List<AiSkillFileDO> skillFiles = skillFileMapper.selectListBySkillId(skillFile.getSkillId());
        Map<Long, List<Long>> childIdsMap = convertMultiMap(skillFiles, AiSkillFileDO::getParentId, AiSkillFileDO::getId);
        List<Long> ids = new ArrayList<>();
        ids.add(skillFile.getId());
        Deque<Long> queue = new ArrayDeque<>(ids);
        // 按层级向下广度遍历，最多 Short.MAX_VALUE 次，避免异常数据中的环导致死循环
        for (int i = 0; i < Short.MAX_VALUE && !queue.isEmpty(); i++) {
            List<Long> childIds = childIdsMap.get(queue.poll());
            if (CollUtil.isNotEmpty(childIds)) {
                ids.addAll(childIds);
                queue.addAll(childIds);
            }
        }
        // 2.2 删除
        skillFileMapper.deleteByIds(ids);
    }

    @Override
    public void deleteSkillFileListBySkillId(Long skillId) {
        skillFileMapper.deleteBySkillId(skillId);
    }

    @Override
    public AiSkillFileDO getSkillFile(Long id) {
        return skillFileMapper.selectById(id);
    }

    @Override
    public List<AiSkillFileDO> getSkillFileListBySkillId(Long skillId) {
        return skillFileMapper.selectListBySkillId(skillId);
    }

    @Override
    public Map<Long, Integer> getSkillFileCountMap(Collection<Long> skillIds) {
        if (CollUtil.isEmpty(skillIds)) {
            return Collections.emptyMap();
        }
        List<AiSkillFileDO> files = filterList(skillFileMapper.selectListBySkillIds(skillIds),
                skillFile -> AiSkillFileTypeEnum.isFile(skillFile.getType()));
        return convertMap(files, AiSkillFileDO::getSkillId, skillFile -> 1, Integer::sum);
    }

    @Override
    public List<String> getSkillFilePathList(Long skillId) {
        List<AiSkillFileDO> skillFiles = skillFileMapper.selectListBySkillId(skillId);
        Map<Long, AiSkillFileDO> fileMap = convertMap(skillFiles, AiSkillFileDO::getId);
        List<String> paths = new ArrayList<>();
        for (AiSkillFileDO skillFile : skillFiles) {
            if (!AiSkillFileTypeEnum.isFile(skillFile.getType())) {
                continue;
            }
            // 沿父链向上拼接路径，最多遍历节点总数次，防御异常数据中的环
            Deque<String> names = new ArrayDeque<>();
            AiSkillFileDO current = skillFile;
            for (int i = 0; i <= fileMap.size() && current != null; i++) {
                names.addFirst(current.getName());
                current = fileMap.get(current.getParentId());
            }
            paths.add(String.join("/", names));
        }
        return paths;
    }

    @Override
    public String readSkillFile(AiSkillDO skill, String path) {
        // 1. 校验路径：只允许技能内的相对路径
        List<String> names = parsePath(path);
        if (names == null) {
            return StrUtil.format("路径({})不合法：只能使用技能内的相对路径，例如 reference/guide.md，"
                    + "不能以 / 开头，不能包含 . 或 ..", path);
        }

        // 2. 逐级查找节点
        Map<Long, List<AiSkillFileDO>> childrenMap = convertMultiMap(
                skillFileMapper.selectListBySkillId(skill.getId()), AiSkillFileDO::getParentId);
        AiSkillFileDO node = null;
        Long parentId = AiSkillFileDO.PARENT_ID_ROOT;
        for (String name : names) {
            node = CollUtil.findOne(childrenMap.get(parentId), item -> name.equals(item.getName()));
            if (node == null) {
                return StrUtil.format("技能({})中不存在文件({})", skill.getName(), path);
            }
            parentId = node.getId();
        }

        // 3.1 目录：返回子节点名称，便于模型继续读取
        if (AiSkillFileTypeEnum.isDirectory(node.getType())) {
            List<String> childNames = convertList(childrenMap.get(node.getId()), child ->
                    AiSkillFileTypeEnum.isDirectory(child.getType()) ? child.getName() + "/" : child.getName());
            return StrUtil.format("路径({})是目录，包含：{}", path,
                    CollUtil.isEmpty(childNames) ? "（空）" : String.join("、", childNames));
        }
        // 3.2 文件：返回内容
        return StrUtil.nullToEmpty(node.getContent());
    }

    // ========== 校验 ==========

    private AiSkillFileDO validateSkillFileExists(Long id) {
        AiSkillFileDO skillFile = skillFileMapper.selectById(id);
        if (skillFile == null) {
            throw exception(SKILL_FILE_NOT_EXISTS);
        }
        return skillFile;
    }

    private static void validateSkillFileNotLocked(AiSkillFileDO skillFile, String operation) {
        if (Boolean.TRUE.equals(skillFile.getLocked())) {
            throw exception(SKILL_FILE_LOCKED, operation);
        }
    }

    /**
     * 校验名称：去除首尾空白，长度不超过 {@link #NAME_MAX_LENGTH}，不包含路径分隔符、控制字符，不能为 . 或 ..
     *
     * @return 去除首尾空白后的名称
     */
    private static String validateSkillFileName(String name) {
        String trimmed = StrUtil.trim(name);
        if (StrUtil.isEmpty(trimmed) || trimmed.length() > NAME_MAX_LENGTH || StrUtil.equalsAny(trimmed, ".", "..")
                || StrUtil.containsAny(trimmed, '/', '\\') || trimmed.chars().anyMatch(Character::isISOControl)) {
            throw exception(SKILL_FILE_NAME_INVALID, NAME_MAX_LENGTH);
        }
        return trimmed;
    }

    /**
     * 校验文件类型：只允许 {@link #FILE_TYPES} 文本文件
     *
     * @return 小写的扩展名
     */
    private static String validateFileType(String name) {
        String fileType = StrUtil.nullToEmpty(FileUtil.getSuffix(name)).toLowerCase(Locale.ROOT);
        if (!FILE_TYPES.contains(fileType)) {
            throw exception(SKILL_FILE_TYPE_NOT_SUPPORTED, name, String.join("、", FILE_TYPES));
        }
        return fileType;
    }

    /**
     * 校验父目录：根级直接通过；否则必须是同一技能下的目录
     */
    private void validateParentDirectory(Long skillId, Long parentId) {
        if (ObjUtil.equal(parentId, AiSkillFileDO.PARENT_ID_ROOT)) {
            return;
        }
        AiSkillFileDO parent = skillFileMapper.selectById(parentId);
        if (parent == null) {
            throw exception(SKILL_FILE_PARENT_NOT_EXISTS);
        }
        if (ObjUtil.notEqual(parent.getSkillId(), skillId) || !AiSkillFileTypeEnum.isDirectory(parent.getType())) {
            throw exception(SKILL_FILE_PARENT_INVALID);
        }
    }

    private void validateSkillFileNameUnique(Long id, Long skillId, Long parentId, String name) {
        AiSkillFileDO skillFile = skillFileMapper.selectBySkillIdAndParentIdAndName(skillId, parentId, name);
        if (skillFile != null && ObjUtil.notEqual(skillFile.getId(), id)) {
            throw exception(SKILL_FILE_NAME_DUPLICATE, name);
        }
    }

    /**
     * 校验大小：单个文件不超过 {@link #FILE_SIZE_MAX}，技能合计不超过 {@link #TOTAL_SIZE_MAX}
     *
     * @param skillId   技能编号
     * @param excludeId 被替换内容的文件编号；新建时为空
     * @param size      新内容的字节数
     */
    private void validateSkillFileSize(Long skillId, Long excludeId, int size) {
        if (size > FILE_SIZE_MAX) {
            throw exception(SKILL_FILE_SIZE_EXCEED, FILE_SIZE_MAX / 1024, (size + 1023) / 1024);
        }
        List<AiSkillFileDO> skillFiles = skillFileMapper.selectListBySkillId(skillId);
        long totalSize = size + sum(filterList(skillFiles, skillFile -> ObjUtil.notEqual(skillFile.getId(), excludeId)),
                skillFile -> ObjUtil.defaultIfNull(skillFile.getSize(), 0));
        if (totalSize > TOTAL_SIZE_MAX) {
            throw exception(SKILL_FILE_TOTAL_SIZE_EXCEED, TOTAL_SIZE_MAX / 1024 / 1024, (totalSize + 1023) / 1024);
        }
    }

    /**
     * 校验 SKILL.md 头信息：name、description 齐全，name 与技能名称一致
     *
     * @return 头信息中的 description
     */
    static String validateSkillFrontmatter(AiSkillDO skill, String content) {
        Map<String, String> frontmatter = parseFrontmatter(content);
        String name = frontmatter.get("name");
        if (StrUtil.isBlank(name)) {
            throw exception(SKILL_FILE_SKILL_MD_INVALID, "缺少 name");
        }
        if (ObjUtil.notEqual(name, skill.getName())) {
            throw exception(SKILL_FILE_SKILL_MD_INVALID,
                    StrUtil.format("name({}) 必须与技能名称({})一致", name, skill.getName()));
        }
        String description = frontmatter.get("description");
        if (StrUtil.isBlank(description)) {
            throw exception(SKILL_FILE_SKILL_MD_INVALID, "缺少 description");
        }
        if (StrUtil.startWithAny(description, ">", "|")) {
            throw exception(SKILL_FILE_SKILL_MD_INVALID, "description 需要写在一行");
        }
        if (description.length() > DESCRIPTION_MAX_LENGTH) {
            throw exception(SKILL_FILE_SKILL_MD_INVALID,
                    StrUtil.format("description 不能超过 {} 个字符", DESCRIPTION_MAX_LENGTH));
        }
        return description;
    }

    /**
     * 解析 SKILL.md 头信息：以 --- 行开始、以 --- 行结束，逐行解析顶层的 key: value（只支持单行的值）
     */
    private static Map<String, String> parseFrontmatter(String content) {
        String text = StrUtil.removePrefix(StrUtil.nullToEmpty(content), "\uFEFF").replace("\r\n", "\n");
        if (!text.startsWith("---\n")) {
            throw exception(SKILL_FILE_SKILL_MD_INVALID, "必须以 --- 开头");
        }
        int end = text.indexOf("\n---", 3);
        if (end < 0) {
            throw exception(SKILL_FILE_SKILL_MD_INVALID, "缺少结束的 ---");
        }
        Map<String, String> frontmatter = new HashMap<>();
        String block = end >= 4 ? text.substring(4, end) : "";
        for (String line : block.split("\n")) {
            int index = line.indexOf(':');
            if (index <= 0 || Character.isWhitespace(line.charAt(0)) || line.startsWith("#")) {
                continue;
            }
            frontmatter.put(line.substring(0, index).trim(), unquote(line.substring(index + 1).trim()));
        }
        return frontmatter;
    }

    private static String unquote(String value) {
        if (value.length() >= 2 && ((value.startsWith("\"") && value.endsWith("\""))
                || (value.startsWith("'") && value.endsWith("'")))) {
            return value.substring(1, value.length() - 1).trim();
        }
        return value;
    }

    /**
     * 解析相对路径：拒绝空路径、绝对路径、反斜杠，以及 .、.. 和空的路径段
     *
     * 只在技能的节点树中逐级按名称查找，不访问文件系统，所以不需要拒绝 a:guide.md 这类盘符形式（与命名规则一致）
     *
     * @return 路径段；不合法时返回 null
     */
    static List<String> parsePath(String path) {
        String trimmed = StrUtil.trim(path);
        if (StrUtil.isEmpty(trimmed) || trimmed.startsWith("/") || trimmed.contains("\\")) {
            return null;
        }
        List<String> names = StrUtil.split(trimmed, '/');
        for (String name : names) {
            if (StrUtil.isEmpty(name) || StrUtil.equalsAny(name, ".", "..")
                    || name.chars().anyMatch(Character::isISOControl)) {
                return null;
            }
        }
        return names;
    }

    private static boolean isSkillFile(AiSkillFileDO skillFile) {
        return ObjUtil.equal(skillFile.getParentId(), AiSkillFileDO.PARENT_ID_ROOT)
                && AiSkillFileDO.NAME_SKILL.equals(skillFile.getName());
    }

    /**
     * 判断 candidateId 是否为 nodeId 的后代（移动时的成环保护）
     */
    private boolean isDescendant(Long skillId, Long nodeId, Long candidateId) {
        // 1. 查询技能下全部节点，构建编号映射
        Map<Long, AiSkillFileDO> fileMap = convertMap(skillFileMapper.selectListBySkillId(skillId), AiSkillFileDO::getId);

        // 2. 沿父链向上查找，最多遍历节点总数次，防御异常数据中的环
        Long currentId = candidateId;
        for (int i = 0; i <= fileMap.size() && ObjUtil.notEqual(currentId, AiSkillFileDO.PARENT_ID_ROOT); i++) {
            AiSkillFileDO current = fileMap.get(currentId);
            if (current == null) {
                return false;
            }
            if (ObjUtil.equal(current.getParentId(), nodeId)) {
                return true;
            }
            currentId = current.getParentId();
        }
        return false;
    }

}
