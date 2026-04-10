package cn.iocoder.yudao.module.mes.dal.mysql.wm.productsales;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.mes.controller.admin.wm.productsales.vo.MesWmProductSalesPageReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.productsales.MesWmProductSalesDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * MES 销售出库单 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface MesWmProductSalesMapper extends BaseMapperX<MesWmProductSalesDO> {

    default PageResult<MesWmProductSalesDO> selectPage(MesWmProductSalesPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<MesWmProductSalesDO>()
                .likeIfPresent(MesWmProductSalesDO::getCode, reqVO.getCode())
                .likeIfPresent(MesWmProductSalesDO::getName, reqVO.getName())
                .eqIfPresent(MesWmProductSalesDO::getClientId, reqVO.getClientId())
                .eqIfPresent(MesWmProductSalesDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(MesWmProductSalesDO::getSalesDate, reqVO.getShipmentDate())
                .orderByDesc(MesWmProductSalesDO::getId));
    }

    default MesWmProductSalesDO selectByCode(String code) {
        return selectOne(MesWmProductSalesDO::getCode, code);
    }

    default List<MesWmProductSalesDO> selectListByClientId(Long clientId) {
        return selectList(MesWmProductSalesDO::getClientId, clientId);
    }

}
