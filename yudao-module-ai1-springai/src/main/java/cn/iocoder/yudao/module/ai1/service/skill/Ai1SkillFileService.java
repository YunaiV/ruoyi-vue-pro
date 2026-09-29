package cn.iocoder.yudao.module.ai1.service.skill;

import cn.iocoder.yudao.module.ai1.controller.admin.skill.vo.file.Ai1SkillFileContentReqVO;
import cn.iocoder.yudao.module.ai1.controller.admin.skill.vo.file.Ai1SkillFileCreateReqVO;
import cn.iocoder.yudao.module.ai1.controller.admin.skill.vo.file.Ai1SkillFileMoveReqVO;
import cn.iocoder.yudao.module.ai1.controller.admin.skill.vo.file.Ai1SkillFileRenameReqVO;
import cn.iocoder.yudao.module.ai1.dal.dataobject.skill.Ai1SkillDO;
import cn.iocoder.yudao.module.ai1.dal.dataobject.skill.Ai1SkillFileDO;
import jakarta.validation.Valid;

import java.util.Collection;
import java.util.List;

/**
 * AI1 SKILL 内容文件 Service 接口
 *
 * 任何内容变更都会刷新所属 SKILL 的更新时间，供本地物化做变更检测 TODO @AI：这个注释是必须的么？还是？
 *
 * @author 芋道源码
 */
public interface Ai1SkillFileService {

    // TODO @AI“seed”会不会有点奇怪？这个是专有名词么？
    // TODO @AI：方法名，不以 s 结尾，使用 List；
    /**
     * 播种 SKILL 的固定文件：SKILL.md（含 frontmatter）、scripts/、reference/，均为锁定节点
     *
     * @param skill SKILL
     */
    void createSkillSeedFiles(Ai1SkillDO skill);

    /**
     * 创建目录或文件；文件按扩展名推断类型，并填充默认内容
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createSkillFile(@Valid Ai1SkillFileCreateReqVO createReqVO);

    /**
     * 重命名节点；固定节点禁止
     *
     * @param renameReqVO 重命名信息
     */
    void renameSkillFile(@Valid Ai1SkillFileRenameReqVO renameReqVO);

    /**
     * 移动节点；固定节点禁止，且不能移入自身或其后代
     *
     * @param moveReqVO 移动信息
     */
    void moveSkillFile(@Valid Ai1SkillFileMoveReqVO moveReqVO);

    /**
     * 保存文件内容；目录不支持
     *
     * @param contentReqVO 内容信息
     */
    void updateSkillFileContent(@Valid Ai1SkillFileContentReqVO contentReqVO);

    /**
     * 删除节点；固定节点禁止，目录递归删除后代
     *
     * @param id 编号
     */
    void deleteSkillFile(Long id);

    /**
     * 删除 SKILL 下的全部节点（SKILL 删除时调用）
     *
     * @param skillIds SKILL 编号集合
     */
    void deleteSkillFileListBySkillIds(Collection<Long> skillIds);

    /**
     * 获得节点
     *
     * @param id 编号
     * @return 节点
     */
    Ai1SkillFileDO getSkillFile(Long id);

    /**
     * 获得 SKILL 下的全部节点，目录优先、按排序号升序
     *
     * @param skillId SKILL 编号
     * @return 节点列表
     */
    List<Ai1SkillFileDO> getSkillFileListBySkillId(Long skillId);

}
