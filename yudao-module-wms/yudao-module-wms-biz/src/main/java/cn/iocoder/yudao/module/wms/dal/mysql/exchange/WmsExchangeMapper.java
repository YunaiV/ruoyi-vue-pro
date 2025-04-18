package cn.iocoder.yudao.module.wms.dal.mysql.exchange;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.wms.dal.dataobject.exchange.WmsExchangeDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.wms.controller.admin.exchange.vo.*;

/**
 * 换货单 Mapper
 *
 * @author 李方捷
 */
@Mapper
public interface WmsExchangeMapper extends BaseMapperX<WmsExchangeDO> {

    default PageResult<WmsExchangeDO> selectPage(WmsExchangePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<WmsExchangeDO>()
                .eqIfPresent(WmsExchangeDO::getCode, reqVO.getCode())
                .eqIfPresent(WmsExchangeDO::getType, reqVO.getType())
                .eqIfPresent(WmsExchangeDO::getWarehouseId, reqVO.getWarehouseId())
                .eqIfPresent(WmsExchangeDO::getAuditStatus, reqVO.getAuditStatus())
                .eqIfPresent(WmsExchangeDO::getRemark, reqVO.getRemark())
                .betweenIfPresent(WmsExchangeDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(WmsExchangeDO::getId));
    }

}