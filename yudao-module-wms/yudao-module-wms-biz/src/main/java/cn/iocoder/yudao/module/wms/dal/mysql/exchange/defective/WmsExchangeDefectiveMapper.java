package cn.iocoder.yudao.module.wms.dal.mysql.exchange.defective;

import java.util.*;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.wms.dal.dataobject.exchange.defective.WmsExchangeDefectiveDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.wms.controller.admin.exchange.defective.vo.*;

/**
 * 良次换货详情 Mapper
 *
 * @author 李方捷
 */
@Mapper
public interface WmsExchangeDefectiveMapper extends BaseMapperX<WmsExchangeDefectiveDO> {

    default PageResult<WmsExchangeDefectiveDO> selectPage(WmsExchangeDefectivePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<WmsExchangeDefectiveDO>()
				.eqIfPresent(WmsExchangeDefectiveDO::getExchangeId, reqVO.getExchangeId())
				.eqIfPresent(WmsExchangeDefectiveDO::getProductId, reqVO.getProductId())
				.eqIfPresent(WmsExchangeDefectiveDO::getFromBinId, reqVO.getFromBinId())
				.eqIfPresent(WmsExchangeDefectiveDO::getToBinId, reqVO.getToBinId())
				.eqIfPresent(WmsExchangeDefectiveDO::getQty, reqVO.getQty())
				.eqIfPresent(WmsExchangeDefectiveDO::getRemark, reqVO.getRemark())
				.betweenIfPresent(WmsExchangeDefectiveDO::getCreateTime, reqVO.getCreateTime())
				.orderByDesc(WmsExchangeDefectiveDO::getId));
    }

    /**
     * 按 exchange_id 查询 WmsExchangeDefectiveDO 清单
     */
    default List<WmsExchangeDefectiveDO> selectByExchangeId(Long exchangeId) {
        return selectList(new LambdaQueryWrapperX<WmsExchangeDefectiveDO>().eq(WmsExchangeDefectiveDO::getExchangeId, exchangeId));
    }
}