package cn.iocoder.yudao.module.wms.dal.mysql.warehouse;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.wms.controller.admin.warehouse.vo.WmsWarehousePageReqVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.warehouse.WmsWarehouseDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

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
				.likeIfPresent(WmsWarehouseDO::getCode, reqVO.getCode())
                .eqIfPresent(WmsWarehouseDO::getStatus, reqVO.getStatus())
				.likeIfPresent(WmsWarehouseDO::getName, reqVO.getName())
				.eqIfPresent(WmsWarehouseDO::getExternalStorageId, reqVO.getExternalStorageId())
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

    /**
     * 按 externalStorageId 查询 WmsWarehouseDO
     */
    default List<WmsWarehouseDO> selectByExternalStorageId(Long externalStorageId, int limit) {
        WmsWarehousePageReqVO reqVO = new WmsWarehousePageReqVO();
        reqVO.setPageSize(limit);
        reqVO.setPageNo(1);
        LambdaQueryWrapperX<WmsWarehouseDO> wrapper = new LambdaQueryWrapperX<>();
        wrapper.eq(WmsWarehouseDO::getExternalStorageId, externalStorageId);
        return selectPage(reqVO, wrapper).getList();
    }

    /**
     * 按 name 查询唯一的 WmsWarehouseDO
     */
    default WmsWarehouseDO getByName(String name) {
        LambdaQueryWrapperX<WmsWarehouseDO> wrapper = new LambdaQueryWrapperX<>();
        wrapper.eq(WmsWarehouseDO::getName, name);
        return selectOne(wrapper);
    }

    /**
     * 按 code 查询唯一的 WmsWarehouseDO
     */
    default WmsWarehouseDO getByCode(String code) {
        LambdaQueryWrapperX<WmsWarehouseDO> wrapper = new LambdaQueryWrapperX<>();
        wrapper.eq(WmsWarehouseDO::getCode, code);
        return selectOne(wrapper);
    }

    default List<WmsWarehouseDO> getSimpleList(WmsWarehousePageReqVO pageReqVO) {
        LambdaQueryWrapperX<WmsWarehouseDO> wrapper = new LambdaQueryWrapperX<>();
        wrapper.likeIfPresent(WmsWarehouseDO::getCode, pageReqVO.getCode());
        wrapper.likeIfPresent(WmsWarehouseDO::getName, pageReqVO.getName());
        return selectList(wrapper);
    }
}
