package cn.iocoder.yudao.module.wms.dal.mysql.warehouse;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.wms.dal.dataobject.warehouse.WmsWarehouseDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.wms.controller.admin.warehouse.vo.*;

/**
 * 仓库 Mapper
 *
 * @author 李方捷
 */
@Mapper
public interface WmsWarehouseMapper extends BaseMapperX<WmsWarehouseDO> {

    default PageResult<WmsWarehouseDO> selectPage(WmsWarehousePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<WmsWarehouseDO>()
                .eqIfPresent(WmsWarehouseDO::getMode, reqVO.getMode())
                .eqIfPresent(WmsWarehouseDO::getCode, reqVO.getCode())
                .likeIfPresent(WmsWarehouseDO::getName, reqVO.getName())
                .eqIfPresent(WmsWarehouseDO::getExternalStorageId, reqVO.getExternalStorageId())
                .eqIfPresent(WmsWarehouseDO::getExternalStorageCode, reqVO.getExternalStorageCode())
                .likeIfPresent(WmsWarehouseDO::getCompanyName, reqVO.getCompanyName())
                .eqIfPresent(WmsWarehouseDO::getCountry, reqVO.getCountry())
                .eqIfPresent(WmsWarehouseDO::getProvince, reqVO.getProvince())
                .eqIfPresent(WmsWarehouseDO::getCity, reqVO.getCity())
                .eqIfPresent(WmsWarehouseDO::getAddressLine1, reqVO.getAddressLine1())
                .eqIfPresent(WmsWarehouseDO::getAddressLine2, reqVO.getAddressLine2())
                .eqIfPresent(WmsWarehouseDO::getPostcode, reqVO.getPostcode())
                .eqIfPresent(WmsWarehouseDO::getContactPerson, reqVO.getContactPerson())
                .eqIfPresent(WmsWarehouseDO::getContactPhone, reqVO.getContactPhone())
                .eqIfPresent(WmsWarehouseDO::getIsSync, reqVO.getIsSync())
                .betweenIfPresent(WmsWarehouseDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(WmsWarehouseDO::getId));
    }

}