package cn.iocoder.yudao.module.ai1.service.skill;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.ai1.controller.admin.skill.vo.skill.Ai1SkillPageReqVO;
import cn.iocoder.yudao.module.ai1.controller.admin.skill.vo.skill.Ai1SkillSaveReqVO;
import cn.iocoder.yudao.module.ai1.dal.dataobject.skill.Ai1SkillDO;
import jakarta.validation.Valid;

import java.util.Collection;
import java.util.List;

/**
 * AI1 SKILL Service 接口
 *
 * @author 芋道源码
 */
public interface Ai1SkillService {

    /**
     * 创建 SKILL
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createSkill(@Valid Ai1SkillSaveReqVO createReqVO);

    /**
     * 更新 SKILL
     *
     * @param updateReqVO 更新信息
     */
    void updateSkill(@Valid Ai1SkillSaveReqVO updateReqVO);

    /**
     * 删除 SKILL，级联删除内容文件
     *
     * @param id 编号
     */
    void deleteSkill(Long id);

    /**
     * 批量删除 SKILL，级联删除内容文件
     *
     * @param ids 编号列表
     */
    void deleteSkillListByIds(List<Long> ids);

    /**
     * 获得 SKILL
     *
     * @param id 编号
     * @return SKILL
     */
    Ai1SkillDO getSkill(Long id);

    /**
     * 校验 SKILL 是否存在
     *
     * @param id 编号
     * @return SKILL
     */
    Ai1SkillDO validateSkillExists(Long id);

    /**
     * 获得 SKILL 分页
     *
     * @param pageReqVO 分页查询
     * @return SKILL 分页
     */
    PageResult<Ai1SkillDO> getSkillPage(Ai1SkillPageReqVO pageReqVO);

    /**
     * 获得指定状态的 SKILL 列表
     *
     * @param status 状态
     * @return SKILL 列表
     */
    List<Ai1SkillDO> getSkillListByStatus(Integer status);

    /**
     * 获得 SKILL 列表
     *
     * @param ids 编号集合
     * @return SKILL 列表
     */
    List<Ai1SkillDO> getSkillList(Collection<Long> ids);

    /**
     * 刷新 SKILL 的更新时间：内容文件变更后调用，作为本地物化的变更指纹
     *
     * @param id 编号
     */
    void touchSkill(Long id);

}
