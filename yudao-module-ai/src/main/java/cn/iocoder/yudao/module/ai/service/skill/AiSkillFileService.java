package cn.iocoder.yudao.module.ai.service.skill;

import cn.iocoder.yudao.module.ai.controller.admin.skill.vo.file.AiSkillFileContentReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.skill.vo.file.AiSkillFileCreateReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.skill.vo.file.AiSkillFileMoveReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.skill.vo.file.AiSkillFileRenameReqVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.skill.AiSkillDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.skill.AiSkillFileDO;
import javax.validation.Valid;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * AI 技能文件 Service 接口
 *
 * @author 芋道源码
 */
public interface AiSkillFileService {

    /**
     * 创建技能的固定文件：SKILL.md（含头信息）、scripts/、reference/
     *
     * @param skill 技能
     */
    void createDefaultSkillFileList(AiSkillDO skill);

    /**
     * 创建目录或文件：文件只允许文本类型，并填充默认内容
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createSkillFile(@Valid AiSkillFileCreateReqVO createReqVO);

    /**
     * 重命名节点；固定节点禁止
     *
     * @param renameReqVO 重命名信息
     */
    void renameSkillFile(@Valid AiSkillFileRenameReqVO renameReqVO);

    /**
     * 移动节点；固定节点禁止，且不能移入自身或其后代
     *
     * @param moveReqVO 移动信息
     */
    void moveSkillFile(@Valid AiSkillFileMoveReqVO moveReqVO);

    /**
     * 保存文件内容：校验大小；SKILL.md 还要校验头信息，并同步描述到技能
     *
     * @param contentReqVO 内容信息
     */
    void updateSkillFileContent(@Valid AiSkillFileContentReqVO contentReqVO);

    /**
     * 删除节点；固定节点禁止，目录递归删除后代
     *
     * @param id 编号
     */
    void deleteSkillFile(Long id);

    /**
     * 删除技能下的全部节点（技能删除时调用）
     *
     * @param skillId 技能编号
     */
    void deleteSkillFileListBySkillId(Long skillId);

    /**
     * 获得节点，包含文件内容
     *
     * @param id 编号
     * @return 节点
     */
    AiSkillFileDO getSkillFile(Long id);

    /**
     * 获得技能下的全部节点，包含文件内容；目录优先、按排序号升序
     *
     * @param skillId 技能编号
     * @return 节点列表
     */
    List<AiSkillFileDO> getSkillFileListBySkillId(Long skillId);

    /**
     * 获得技能的文件数量（不含目录）
     *
     * @param skillIds 技能编号集合
     * @return 文件数量 Map，key 为技能编号
     */
    Map<Long, Integer> getSkillFileCountMap(Collection<Long> skillIds);

    /**
     * 获得技能下全部文件的相对路径，例如 reference/guide.md
     *
     * @param skillId 技能编号
     * @return 相对路径列表
     */
    List<String> getSkillFilePathList(Long skillId);

    /**
     * 按相对路径读取技能的文件，供对话时的技能工具使用
     *
     * @param skill 技能
     * @param path  相对路径，例如 reference/guide.md；不允许绝对路径、. 和 ..
     * @return 文件内容；路径不合法、不存在或是目录时，返回说明文本
     */
    String readSkillFile(AiSkillDO skill, String path);

}
