package cn.iocoder.yudao.module.iot.dal.mysql.rule;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.controller.admin.rule.vo.scene.IotRuleScenePageReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.rule.IotRuleSceneDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * IoT 场景联动 Mapper
 *
 * @author HUIHUI
 */
@Mapper
public interface IotRuleSceneMapper extends BaseMapperX<IotRuleSceneDO> {

    default PageResult<IotRuleSceneDO> selectPage(IotRuleScenePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<IotRuleSceneDO>()
                .likeIfPresent(IotRuleSceneDO::getName, reqVO.getName())
                .likeIfPresent(IotRuleSceneDO::getDescription, reqVO.getDescription())
                .eqIfPresent(IotRuleSceneDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(IotRuleSceneDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(IotRuleSceneDO::getId));
    }

    default List<IotRuleSceneDO> selectListByStatus(Integer status) {
        return selectList(IotRuleSceneDO::getStatus, status);
    }

}
