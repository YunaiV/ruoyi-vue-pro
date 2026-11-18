package cn.iocoder.yudao.module.ai.dal.mysql.skill;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.ai.dal.dataobject.skill.AiSkillFileDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

/**
 * AI 技能文件 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface AiSkillFileMapper extends BaseMapperX<AiSkillFileDO> {

    /**
     * 按目录优先、排序号、编号升序返回技能下的全部节点
     */
    default List<AiSkillFileDO> selectListBySkillId(Long skillId) {
        return selectList(new LambdaQueryWrapperX<AiSkillFileDO>()
                .eq(AiSkillFileDO::getSkillId, skillId)
                .orderByAsc(AiSkillFileDO::getType)
                .orderByAsc(AiSkillFileDO::getSort)
                .orderByAsc(AiSkillFileDO::getId));
    }

    default List<AiSkillFileDO> selectListBySkillIds(Collection<Long> skillIds) {
        return selectList(AiSkillFileDO::getSkillId, skillIds);
    }

    default List<AiSkillFileDO> selectListBySkillIdAndParentId(Long skillId, Long parentId) {
        return selectList(AiSkillFileDO::getSkillId, skillId, AiSkillFileDO::getParentId, parentId);
    }

    default AiSkillFileDO selectBySkillIdAndParentIdAndName(Long skillId, Long parentId, String name) {
        return selectOne(new LambdaQueryWrapperX<AiSkillFileDO>()
                .eq(AiSkillFileDO::getSkillId, skillId)
                .eq(AiSkillFileDO::getParentId, parentId)
                .eq(AiSkillFileDO::getName, name));
    }

    default int deleteBySkillId(Long skillId) {
        return delete(new LambdaQueryWrapperX<AiSkillFileDO>().eq(AiSkillFileDO::getSkillId, skillId));
    }

}
