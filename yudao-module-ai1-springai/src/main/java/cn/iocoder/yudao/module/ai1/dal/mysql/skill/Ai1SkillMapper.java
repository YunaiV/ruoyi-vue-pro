package cn.iocoder.yudao.module.ai1.dal.mysql.skill;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.ai1.controller.admin.skill.vo.skill.Ai1SkillPageReqVO;
import cn.iocoder.yudao.module.ai1.dal.dataobject.skill.Ai1SkillDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * AI1 SKILL Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface Ai1SkillMapper extends BaseMapperX<Ai1SkillDO> {

    default PageResult<Ai1SkillDO> selectPage(Ai1SkillPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<Ai1SkillDO>()
                .likeIfPresent(Ai1SkillDO::getName, reqVO.getName())
                .eqIfPresent(Ai1SkillDO::getStatus, reqVO.getStatus())
                .orderByAsc(Ai1SkillDO::getId));
    }

    default Ai1SkillDO selectByName(String name) {
        return selectOne(Ai1SkillDO::getName, name);
    }

    default List<Ai1SkillDO> selectListByStatus(Integer status) {
        return selectList(new LambdaQueryWrapperX<Ai1SkillDO>()
                .eq(Ai1SkillDO::getStatus, status)
                .orderByAsc(Ai1SkillDO::getId));
    }

    // TODO DONE @AI：可以清理掉了；

}
