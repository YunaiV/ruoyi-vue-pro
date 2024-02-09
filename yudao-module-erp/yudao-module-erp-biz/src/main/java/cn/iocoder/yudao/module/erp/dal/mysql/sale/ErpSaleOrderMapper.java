package cn.iocoder.yudao.module.erp.dal.mysql.sale;


import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.MPJLambdaWrapperX;
import cn.iocoder.yudao.module.erp.controller.admin.sale.vo.order.ErpSaleOrderPageReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.sale.ErpSaleOrderDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.stock.ErpStockOutItemDO;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * ERP 销售订单 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface ErpSaleOrderMapper extends BaseMapperX<ErpSaleOrderDO> {

    default PageResult<ErpSaleOrderDO> selectPage(ErpSaleOrderPageReqVO reqVO) {
        MPJLambdaWrapperX<ErpSaleOrderDO> query = new MPJLambdaWrapperX<ErpSaleOrderDO>()
                .eqIfPresent(ErpSaleOrderDO::getNo, reqVO.getNo())
                .eqIfPresent(ErpSaleOrderDO::getCustomerId, reqVO.getCustomerId())
                .betweenIfPresent(ErpSaleOrderDO::getOrderTime, reqVO.getOrderTime())
                .eqIfPresent(ErpSaleOrderDO::getStatus, reqVO.getStatus())
                .likeIfPresent(ErpSaleOrderDO::getRemark, reqVO.getRemark())
                .eqIfPresent(ErpSaleOrderDO::getCreator, reqVO.getCreator())
                .orderByDesc(ErpSaleOrderDO::getId);
        if (reqVO.getProductId() != null) {
            query.leftJoin(ErpStockOutItemDO.class, ErpStockOutItemDO::getOutId, ErpSaleOrderDO::getId)
                    .eq(reqVO.getProductId() != null, ErpStockOutItemDO::getProductId, reqVO.getProductId())
                    .groupBy(ErpSaleOrderDO::getId); // 避免 1 对多查询，产生相同的 1
        }
        return selectJoinPage(reqVO, ErpSaleOrderDO.class, query);
    }

    default int updateByIdAndStatus(Long id, Integer status, ErpSaleOrderDO updateObj) {
        return update(updateObj, new LambdaUpdateWrapper<ErpSaleOrderDO>()
                .eq(ErpSaleOrderDO::getId, id).eq(ErpSaleOrderDO::getStatus, status));
    }

    default ErpSaleOrderDO selectByNo(String no) {
        return selectOne(ErpSaleOrderDO::getNo, no);
    }

}