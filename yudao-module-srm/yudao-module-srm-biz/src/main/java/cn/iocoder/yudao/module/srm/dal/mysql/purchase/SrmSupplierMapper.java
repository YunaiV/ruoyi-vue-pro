package cn.iocoder.yudao.module.srm.dal.mysql.purchase;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.srm.controller.admin.purchase.vo.supplier.SrmSupplierPageReqVO;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.SrmSupplierDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * ERP 供应商 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface SrmSupplierMapper extends BaseMapperX<SrmSupplierDO> {

    default PageResult<SrmSupplierDO> selectPage(SrmSupplierPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<SrmSupplierDO>()
                .likeIfPresent(SrmSupplierDO::getName, reqVO.getName())
                .likeIfPresent(SrmSupplierDO::getMobile, reqVO.getMobile())
                .likeIfPresent(SrmSupplierDO::getTelephone, reqVO.getTelephone())
                .orderByDesc(SrmSupplierDO::getId));
    }

    default List<SrmSupplierDO> selectListByStatus(Integer status) {
        return selectList(SrmSupplierDO::getStatus, status);
    }

}