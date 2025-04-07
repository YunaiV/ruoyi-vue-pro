package cn.iocoder.yudao.module.iot.dal.mysql.rule;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.controller.admin.rule.vo.databridge.IotDataBridgePageReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.rule.IotDataBridgeDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * IoT 数据桥梁 Mapper
 *
 * @author HUIHUI
 */
@Mapper
public interface IotDataBridgeMapper extends BaseMapperX<IotDataBridgeDO> {

    default PageResult<IotDataBridgeDO> selectPage(IotDataBridgePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<IotDataBridgeDO>()
                .likeIfPresent(IotDataBridgeDO::getName, reqVO.getName())
                .eqIfPresent(IotDataBridgeDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(IotDataBridgeDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(IotDataBridgeDO::getId));
    }

}