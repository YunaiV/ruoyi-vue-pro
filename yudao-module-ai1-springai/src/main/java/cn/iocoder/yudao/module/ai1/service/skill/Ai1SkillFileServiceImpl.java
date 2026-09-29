package cn.iocoder.yudao.module.ai1.service.skill;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.ai1.controller.admin.skill.vo.file.Ai1SkillFileContentReqVO;
import cn.iocoder.yudao.module.ai1.controller.admin.skill.vo.file.Ai1SkillFileCreateReqVO;
import cn.iocoder.yudao.module.ai1.controller.admin.skill.vo.file.Ai1SkillFileMoveReqVO;
import cn.iocoder.yudao.module.ai1.controller.admin.skill.vo.file.Ai1SkillFileRenameReqVO;
import cn.iocoder.yudao.module.ai1.dal.dataobject.skill.Ai1SkillDO;
import cn.iocoder.yudao.module.ai1.dal.dataobject.skill.Ai1SkillFileDO;
import cn.iocoder.yudao.module.ai1.dal.mysql.skill.Ai1SkillFileMapper;
import cn.iocoder.yudao.module.ai1.enums.skill.Ai1SkillFileTypeEnum;
import jakarta.annotation.Resource;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.*;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMultiMap;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.getMaxValue;
import static cn.iocoder.yudao.module.ai1.enums.Ai1ErrorCodeConstants.*;

/**
 * AI1 SKILL 内容文件 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class Ai1SkillFileServiceImpl implements Ai1SkillFileService {

    // TODO @AI：这个是不是枚举下噢？放到枚举类里？
    /**
     * 固定文件：SKILL.md
     */
    public static final String FILE_NAME_SKILL = "SKILL.md";
    /**
     * 固定目录：scripts，存放可执行脚本
     */
    private static final String DIRECTORY_NAME_SCRIPTS = "scripts";
    /**
     * 固定目录：reference，存放参考文档
     */
    private static final String DIRECTORY_NAME_REFERENCE = "reference";

    /**
     * 名称最大长度
     */
    private static final int NAME_MAX_LENGTH = 200;

    @Resource
    private Ai1SkillFileMapper skillFileMapper;

    @Resource
    private Ai1SkillService skillService;

    @Override
    public void createSkillSeedFiles(Ai1SkillDO skill) {
        // TODO @AI：这里应该有个 template 变量，然后下面 format 出来噢；
        String content = "---\n"
                + "name: " + skill.getName() + "\n"
                + "description: " + StrUtil.nullToEmpty(skill.getDescription()) + "\n"
                + "---\n\n"
                + "# " + skill.getName() + "\n\n"
                + "本 Skill 文件树符合社区规范：`SKILL.md` 为入口，引用文件用相对路径。\n\n"
                + "- `scripts/`：可存放可执行脚本（.py / .sh / .js 等）\n"
                + "- `reference/`：可存放参考文档（.md / .json / .yaml 等）\n"
                + "- 其他文件/目录可自由新增扩展\n";
        // TODO @AI：先搞出每个变量，然后再 aslist；这样更好维护；
        skillFileMapper.insertBatch(Arrays.asList(
                Ai1SkillFileDO.builder().skillId(skill.getId()).parentId(Ai1SkillFileDO.PARENT_ID_ROOT).name(FILE_NAME_SKILL)
                        .type(Ai1SkillFileTypeEnum.FILE.getType()).fileType("md").content(content).locked(true).sort(1).build(),
                Ai1SkillFileDO.builder().skillId(skill.getId()).parentId(Ai1SkillFileDO.PARENT_ID_ROOT).name(DIRECTORY_NAME_SCRIPTS)
                        .type(Ai1SkillFileTypeEnum.DIRECTORY.getType()).locked(true).sort(2).build(),
                Ai1SkillFileDO.builder().skillId(skill.getId()).parentId(Ai1SkillFileDO.PARENT_ID_ROOT).name(DIRECTORY_NAME_REFERENCE)
                        .type(Ai1SkillFileTypeEnum.DIRECTORY.getType()).locked(true).sort(3).build()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createSkillFile(Ai1SkillFileCreateReqVO createReqVO) {
        // 1.1 校验 SKILL 存在、名称格式、父目录
        skillService.validateSkillExists(createReqVO.getSkillId());
        String name = validateSkillFileName(createReqVO.getName());
        validateParentDirectory(createReqVO.getSkillId(), createReqVO.getParentId());
        // 1.2 校验同级唯一
        validateSkillFileNameUnique(null, createReqVO.getSkillId(), createReqVO.getParentId(), name);

        // 2. 插入：排序号追加到同级末尾；文件按扩展名推断类型并填充默认内容
        List<Ai1SkillFileDO> siblings = skillFileMapper.selectListBySkillIdAndParentId(createReqVO.getSkillId(), createReqVO.getParentId());
        Integer maxSort = getMaxValue(siblings, Ai1SkillFileDO::getSort);
        Ai1SkillFileDO skillFile = Ai1SkillFileDO.builder().skillId(createReqVO.getSkillId()).parentId(createReqVO.getParentId())
                .name(name).type(createReqVO.getType()).locked(false).sort(maxSort != null ? maxSort + 1 : 1).build();
        // TODO @AI："# 新文档\n\n请在此编写内容（Markdown）。\n" 枚举下 tempalte；
        if (Ai1SkillFileTypeEnum.isFile(createReqVO.getType())) {
            skillFile.setFileType(parseFileType(name));
            skillFile.setContent("md".equals(skillFile.getFileType()) ? "# 新文档\n\n请在此编写内容（Markdown）。\n" : "");
        }
        // TODO @AI：不用考虑这个情况。。。该异常就异常，不过度；
        insertOrUpdateSkillFile(skillFile, true);

        // 3. 刷新 SKILL 更新时间
        skillService.touchSkill(createReqVO.getSkillId());
        return skillFile.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void renameSkillFile(Ai1SkillFileRenameReqVO renameReqVO) {
        // 1. 校验存在、非固定、名称格式、同级唯一
        Ai1SkillFileDO skillFile = validateSkillFileExists(renameReqVO.getId());
        validateSkillFileNotLocked(skillFile, "重命名");
        String name = validateSkillFileName(renameReqVO.getName());
        validateSkillFileNameUnique(skillFile.getId(), skillFile.getSkillId(), skillFile.getParentId(), name);

        // 2. 更新：文件同步刷新扩展名
        Ai1SkillFileDO updateObj = new Ai1SkillFileDO().setId(skillFile.getId()).setName(name);
        if (Ai1SkillFileTypeEnum.isFile(skillFile.getType())) {
            updateObj.setFileType(parseFileType(name));
        }
        insertOrUpdateSkillFile(updateObj, false);

        // 3. 刷新 SKILL 更新时间
        skillService.touchSkill(skillFile.getSkillId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void moveSkillFile(Ai1SkillFileMoveReqVO moveReqVO) {
        // 1.1 校验存在、非固定
        Ai1SkillFileDO skillFile = validateSkillFileExists(moveReqVO.getId());
        validateSkillFileNotLocked(skillFile, "移动");
        // 1.2 校验目标目录：不能是自身或其后代，且必须是同一 SKILL 下的目录
        Long parentId = moveReqVO.getParentId();
        if (ObjUtil.equal(parentId, skillFile.getId())
                || isDescendant(skillFile.getSkillId(), skillFile.getId(), parentId)) {
            throw exception(SKILL_FILE_MOVE_TO_SELF);
        }
        validateParentDirectory(skillFile.getSkillId(), parentId);
        // 1.3 校验目标目录下同名唯一
        validateSkillFileNameUnique(skillFile.getId(), skillFile.getSkillId(), parentId, skillFile.getName());

        // 2. 更新
        insertOrUpdateSkillFile(new Ai1SkillFileDO().setId(skillFile.getId()).setName(skillFile.getName()).setParentId(parentId), false);

        // 3. 刷新 SKILL 更新时间
        skillService.touchSkill(skillFile.getSkillId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSkillFileContent(Ai1SkillFileContentReqVO contentReqVO) {
        // 1. 校验存在，且为文件
        Ai1SkillFileDO skillFile = validateSkillFileExists(contentReqVO.getId());
        if (!Ai1SkillFileTypeEnum.isFile(skillFile.getType())) {
            throw exception(SKILL_FILE_NOT_FILE);
        }

        // 2. 更新
        skillFileMapper.updateById(new Ai1SkillFileDO().setId(skillFile.getId()).setContent(contentReqVO.getContent()));

        // 3. 刷新 SKILL 更新时间
        skillService.touchSkill(skillFile.getSkillId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteSkillFile(Long id) {
        // 1. 校验存在、非固定
        Ai1SkillFileDO skillFile = validateSkillFileExists(id);
        validateSkillFileNotLocked(skillFile, "删除");

        // 2. 收集自身及全部后代，写入删除时间后逻辑删除
        List<Long> ids = new ArrayList<>();
        ids.add(skillFile.getId());
        // TODO @AI：先查询出来变量，再 convertMultiMap
        Map<Long, List<Long>> childIdsMap = convertMultiMap(skillFileMapper.selectListBySkillId(skillFile.getSkillId()),
                Ai1SkillFileDO::getParentId, Ai1SkillFileDO::getId);
        // TODO @AI：for 循环，i 《short max》避免死循环；
        Deque<Long> queue = new ArrayDeque<>(ids);
        while (!queue.isEmpty()) {
            List<Long> childIds = childIdsMap.get(queue.poll());
            if (CollUtil.isNotEmpty(childIds)) {
                ids.addAll(childIds);
                queue.addAll(childIds);
            }
        }
        skillFileMapper.updateDeletedAtByIds(ids, LocalDateTime.now());
        skillFileMapper.deleteByIds(ids);

        // 3. 刷新 SKILL 更新时间
        skillService.touchSkill(skillFile.getSkillId());
    }

    @Override
    public void deleteSkillFileListBySkillIds(Collection<Long> skillIds) {
        if (CollUtil.isEmpty(skillIds)) {
            return;
        }
        skillFileMapper.updateDeletedAtBySkillIds(skillIds, LocalDateTime.now());
        skillFileMapper.deleteBySkillIds(skillIds);
    }

    @Override
    public Ai1SkillFileDO getSkillFile(Long id) {
        return skillFileMapper.selectById(id);
    }

    @Override
    public List<Ai1SkillFileDO> getSkillFileListBySkillId(Long skillId) {
        return skillFileMapper.selectListBySkillId(skillId);
    }

    private Ai1SkillFileDO validateSkillFileExists(Long id) {
        Ai1SkillFileDO skillFile = skillFileMapper.selectById(id);
        if (skillFile == null) {
            throw exception(SKILL_FILE_NOT_EXISTS);
        }
        return skillFile;
    }

    private static void validateSkillFileNotLocked(Ai1SkillFileDO skillFile, String operation) {
        if (Boolean.TRUE.equals(skillFile.getLocked())) {
            throw exception(SKILL_FILE_LOCKED, operation);
        }
    }

    /**
     * 校验名称格式：去除首尾空白，长度不超过 200，且不包含路径分隔符、控制字符，不能为 . 或 ..
     *
     * @return 去除首尾空白后的名称
     */
    private static String validateSkillFileName(String name) {
        String trimmed = StrUtil.trim(name);
        // TODO @AI：equals any ".".equals(trimmed) || "..".equals(trimmed)？
        if (StrUtil.isEmpty(trimmed) || trimmed.length() > NAME_MAX_LENGTH || ".".equals(trimmed) || "..".equals(trimmed)
                || StrUtil.containsAny(trimmed, '/', '\\') || trimmed.chars().anyMatch(Character::isISOControl)) {
            throw exception(SKILL_FILE_NAME_INVALID);
        }
        return trimmed;
    }

    /**
     * 校验父目录：根级直接通过；否则必须是同一 SKILL 下的目录
     */
    private void validateParentDirectory(Long skillId, Long parentId) {
        if (ObjUtil.equal(parentId, Ai1SkillFileDO.PARENT_ID_ROOT)) {
            return;
        }
        Ai1SkillFileDO parent = skillFileMapper.selectById(parentId);
        if (parent == null || ObjUtil.notEqual(parent.getSkillId(), skillId)
                || !Ai1SkillFileTypeEnum.isDirectory(parent.getType())) {
            throw exception(SKILL_FILE_PARENT_INVALID);
        }
    }

    private void validateSkillFileNameUnique(Long id, Long skillId, Long parentId, String name) {
        Ai1SkillFileDO skillFile = skillFileMapper.selectBySkillIdAndParentIdAndName(skillId, parentId, name);
        if (skillFile != null && ObjUtil.notEqual(skillFile.getId(), id)) {
            throw exception(SKILL_FILE_NAME_DUPLICATE, name);
        }
    }

    /**
     * 判断 candidateId 是否为 nodeId 的后代（移动时的成环保护）
     */
    private boolean isDescendant(Long skillId, Long nodeId, Long candidateId) {
        // TODO @AI：先查询后。。。代码风格统一；
        Map<Long, Ai1SkillFileDO> fileMap = convertMap(skillFileMapper.selectListBySkillId(skillId), Ai1SkillFileDO::getId);
        Long currentId = candidateId;
        // 沿父链向上查找，最多遍历节点总数次，防御异常数据中的环
        for (int i = 0; i <= fileMap.size() && !ObjUtil.equal(currentId, Ai1SkillFileDO.PARENT_ID_ROOT); i++) {
            Ai1SkillFileDO current = fileMap.get(currentId);
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

    /**
     * 插入或更新节点：并发下由唯一键 (skill_id, parent_id, name, deleted_at) 兜底同级重名
     */
    private void insertOrUpdateSkillFile(Ai1SkillFileDO skillFile, boolean insert) {
        try {
            if (insert) {
                skillFileMapper.insert(skillFile);
            } else {
                skillFileMapper.updateById(skillFile);
            }
        } catch (DuplicateKeyException e) {
            throw exception(SKILL_FILE_NAME_DUPLICATE, skillFile.getName());
        }
    }

    /**
     * 从文件名推断扩展名：小写，无扩展名时默认 txt
     */
    private static String parseFileType(String name) {
        String suffix = FileUtil.getSuffix(name);
        return StrUtil.isEmpty(suffix) ? "txt" : suffix.toLowerCase(Locale.ROOT);
    }

}
