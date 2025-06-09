package cn.iocoder.yudao.module.wms.dal.mysql.exchange.item;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.wms.controller.admin.exchange.item.vo.WmsExchangeItemPageReqVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.exchange.item.WmsExchangeItemDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 良次换货详情 Mapper
 *
 * @author 李方捷
 */
@Mapper
public interface WmsExchangeItemMapper extends BaseMapperX<WmsExchangeItemDO> {

    default PageResult<WmsExchangeItemDO> selectPage(WmsExchangeItemPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<WmsExchangeItemDO>()
            .eqIfPresent(WmsExchangeItemDO::getExchangeId, reqVO.getExchangeId())
            .eqIfPresent(WmsExchangeItemDO::getProductId, reqVO.getProductId())
            .eqIfPresent(WmsExchangeItemDO::getFromBinId, reqVO.getFromBinId())
            .eqIfPresent(WmsExchangeItemDO::getToBinId, reqVO.getToBinId())
            .eqIfPresent(WmsExchangeItemDO::getQty, reqVO.getQty())
            .eqIfPresent(WmsExchangeItemDO::getRemark, reqVO.getRemark())
            .betweenIfPresent(WmsExchangeItemDO::getCreateTime, reqVO.getCreateTime())
            .orderByDesc(WmsExchangeItemDO::getId));
    }

    /**
     * 按 exchange_id 查询 WmsExchangeItemDO 清单
     */
    default List<WmsExchangeItemDO> selectByExchangeId(Long exchangeId) {
        return selectList(new LambdaQueryWrapperX<WmsExchangeItemDO>().eq(WmsExchangeItemDO::getExchangeId, exchangeId));
    }
}
