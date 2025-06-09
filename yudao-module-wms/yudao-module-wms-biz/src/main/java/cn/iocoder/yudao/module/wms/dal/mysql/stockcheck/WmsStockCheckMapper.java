package cn.iocoder.yudao.module.wms.dal.mysql.stockcheck;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.wms.controller.admin.stockcheck.vo.WmsStockCheckPageReqVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.stockcheck.WmsStockCheckDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 盘点 Mapper
 *
 * @author 李方捷
 */
@Mapper
public interface WmsStockCheckMapper extends BaseMapperX<WmsStockCheckDO> {

    default PageResult<WmsStockCheckDO> selectPage(WmsStockCheckPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<WmsStockCheckDO>()
            .eqIfPresent(WmsStockCheckDO::getCode, reqVO.getCode())
            .eqIfPresent(WmsStockCheckDO::getWarehouseId, reqVO.getWarehouseId())
            .eqIfPresent(WmsStockCheckDO::getAuditStatus, reqVO.getAuditStatus())
            .eqIfPresent(WmsStockCheckDO::getRemark, reqVO.getRemark())
            .betweenIfPresent(WmsStockCheckDO::getCreateTime, reqVO.getCreateTime())
            .orderByDesc(WmsStockCheckDO::getId));
    }

    /**
     * 按 no 查询唯一的 WmsStockCheckDO
     */
    default WmsStockCheckDO getByNo(String no) {
        LambdaQueryWrapperX<WmsStockCheckDO> wrapper = new LambdaQueryWrapperX<>();
        wrapper.eq(WmsStockCheckDO::getCode, no);
        return selectOne(wrapper);
    }

    /**
     * 按 warehouse_id 查询 WmsStockCheckDO 清单
     */
    default List<WmsStockCheckDO> selectByWarehouseId(Long warehouseId) {
        return selectList(new LambdaQueryWrapperX<WmsStockCheckDO>().eq(WmsStockCheckDO::getWarehouseId, warehouseId));
    }

    /**
     * 按 code 查询唯一的 WmsStockCheckDO
     */
    default WmsStockCheckDO getByCode(String code) {
        LambdaQueryWrapperX<WmsStockCheckDO> wrapper = new LambdaQueryWrapperX<>();
        wrapper.eq(WmsStockCheckDO::getCode, code);
        return selectOne(wrapper);
    }
}
