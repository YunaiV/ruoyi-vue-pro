package cn.iocoder.yudao.module.mes.dal.mysql.wm.returnsales;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.mes.controller.admin.wm.returnsales.vo.MesWmReturnSalesPageReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.returnsales.MesWmReturnSalesDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * MES 销售退货单 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface MesWmReturnSalesMapper extends BaseMapperX<MesWmReturnSalesDO> {

    default PageResult<MesWmReturnSalesDO> selectPage(MesWmReturnSalesPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<MesWmReturnSalesDO>()
                .likeIfPresent(MesWmReturnSalesDO::getCode, reqVO.getCode())
                .likeIfPresent(MesWmReturnSalesDO::getName, reqVO.getName())
                .likeIfPresent(MesWmReturnSalesDO::getSalesOrderCode, reqVO.getSalesOrderCode())
                .eqIfPresent(MesWmReturnSalesDO::getClientId, reqVO.getClientId())
                .eqIfPresent(MesWmReturnSalesDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(MesWmReturnSalesDO::getReturnDate, reqVO.getReturnDate())
                .orderByDesc(MesWmReturnSalesDO::getId));
    }

    default MesWmReturnSalesDO selectByCode(String code) {
        return selectOne(new LambdaQueryWrapperX<MesWmReturnSalesDO>()
                .eq(MesWmReturnSalesDO::getCode, code));
    }

}
