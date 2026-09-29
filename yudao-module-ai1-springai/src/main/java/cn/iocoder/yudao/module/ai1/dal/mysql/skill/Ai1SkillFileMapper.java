package cn.iocoder.yudao.module.ai1.dal.mysql.skill;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.ai1.dal.dataobject.skill.Ai1SkillFileDO;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

/**
 * AI1 SKILL 内容文件 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface Ai1SkillFileMapper extends BaseMapperX<Ai1SkillFileDO> {

    /**
     * 按目录优先、排序号、编号升序返回 SKILL 下的全部节点
     */
    default List<Ai1SkillFileDO> selectListBySkillId(Long skillId) {
        return selectList(new LambdaQueryWrapperX<Ai1SkillFileDO>()
                .eq(Ai1SkillFileDO::getSkillId, skillId)
                .orderByAsc(Ai1SkillFileDO::getType)
                .orderByAsc(Ai1SkillFileDO::getSort)
                .orderByAsc(Ai1SkillFileDO::getId));
    }

    default List<Ai1SkillFileDO> selectListBySkillIdAndParentId(Long skillId, Long parentId) {
        return selectList(new LambdaQueryWrapperX<Ai1SkillFileDO>()
                .eq(Ai1SkillFileDO::getSkillId, skillId)
                .eq(Ai1SkillFileDO::getParentId, parentId));
    }

    default Ai1SkillFileDO selectBySkillIdAndParentIdAndName(Long skillId, Long parentId, String name) {
        return selectOne(new LambdaQueryWrapperX<Ai1SkillFileDO>()
                .eq(Ai1SkillFileDO::getSkillId, skillId)
                .eq(Ai1SkillFileDO::getParentId, parentId)
                .eq(Ai1SkillFileDO::getName, name));
    }

    // TODO @AI：可以清理掉了；
    /**
     * 逻辑删除前写入删除时间，使其退出 (skill_id, parent_id, name, deleted_at) 唯一键的未删除区间
     */
    default void updateDeletedAtByIds(Collection<Long> ids, LocalDateTime deletedAt) {
        update(new Ai1SkillFileDO().setDeletedAt(deletedAt),
                new LambdaQueryWrapperX<Ai1SkillFileDO>().in(Ai1SkillFileDO::getId, ids));
    }

    default void updateDeletedAtBySkillIds(Collection<Long> skillIds, LocalDateTime deletedAt) {
        update(new Ai1SkillFileDO().setDeletedAt(deletedAt),
                new LambdaQueryWrapperX<Ai1SkillFileDO>().in(Ai1SkillFileDO::getSkillId, skillIds));
    }

    default int deleteBySkillIds(Collection<Long> skillIds) {
        return delete(new LambdaQueryWrapperX<Ai1SkillFileDO>().in(Ai1SkillFileDO::getSkillId, skillIds));
    }

}
