package cn.iocoder.yudao.module.wms.dal.mysql.outbound;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.wms.dal.dataobject.outbound.WmsOutboundDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.wms.controller.admin.outbound.vo.*;

/**
 * 出库单 Mapper
 *
 * @author 李方捷
 */
@Mapper
public interface WmsOutboundMapper extends BaseMapperX<WmsOutboundDO> {

    default PageResult<WmsOutboundDO> selectPage(WmsOutboundPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<WmsOutboundDO>()
                .eqIfPresent(WmsOutboundDO::getNo, reqVO.getNo())
                .eqIfPresent(WmsOutboundDO::getWarehouseId, reqVO.getWarehouseId())
                .eqIfPresent(WmsOutboundDO::getType, reqVO.getType())
                .eqIfPresent(WmsOutboundDO::getStatus, reqVO.getStatus())
                .eqIfPresent(WmsOutboundDO::getAuditStatus, reqVO.getAuditStatus())
                .eqIfPresent(WmsOutboundDO::getSourceBillId, reqVO.getSourceBillId())
                .eqIfPresent(WmsOutboundDO::getSourceBillNo, reqVO.getSourceBillNo())
                .eqIfPresent(WmsOutboundDO::getSourceBillType, reqVO.getSourceBillType())
                .eqIfPresent(WmsOutboundDO::getCreatorComment, reqVO.getCreatorComment())
                .betweenIfPresent(WmsOutboundDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(WmsOutboundDO::getId));
    }

}