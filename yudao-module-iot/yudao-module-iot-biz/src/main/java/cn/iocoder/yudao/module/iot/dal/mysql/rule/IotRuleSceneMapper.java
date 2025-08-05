package cn.iocoder.yudao.module.iot.dal.mysql.rule;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.controller.admin.rule.vo.scene.IotRuleScenePageReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.rule.IotSceneRuleDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * IoT 场景联动 Mapper
 *
 * @author HUIHUI
 */
@Mapper
public interface IotRuleSceneMapper extends BaseMapperX<IotSceneRuleDO> {

    default PageResult<IotSceneRuleDO> selectPage(IotRuleScenePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<IotSceneRuleDO>()
                .likeIfPresent(IotSceneRuleDO::getName, reqVO.getName())
                .likeIfPresent(IotSceneRuleDO::getDescription, reqVO.getDescription())
                .eqIfPresent(IotSceneRuleDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(IotSceneRuleDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(IotSceneRuleDO::getId));
    }

    default List<IotSceneRuleDO> selectListByStatus(Integer status) {
        return selectList(IotSceneRuleDO::getStatus, status);
    }

}
