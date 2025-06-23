package cn.iocoder.yudao.module.iot.dal.mysql.rule;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.controller.admin.rule.vo.databridge.IotDataBridgePageReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.rule.IotDataRuleSinkDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * IoT 数据桥梁 Mapper
 *
 * @author HUIHUI
 */
@Mapper
public interface IotDataBridgeMapper extends BaseMapperX<IotDataRuleSinkDO> {

    default PageResult<IotDataRuleSinkDO> selectPage(IotDataBridgePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<IotDataRuleSinkDO>()
                .likeIfPresent(IotDataRuleSinkDO::getName, reqVO.getName())
                .eqIfPresent(IotDataRuleSinkDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(IotDataRuleSinkDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(IotDataRuleSinkDO::getId));
    }

    default List<IotDataRuleSinkDO> selectList(Integer status) {
        return selectList(new LambdaQueryWrapperX<IotDataRuleSinkDO>()
                .eqIfPresent(IotDataRuleSinkDO::getStatus, status)
                .orderByDesc(IotDataRuleSinkDO::getId));
    }

}