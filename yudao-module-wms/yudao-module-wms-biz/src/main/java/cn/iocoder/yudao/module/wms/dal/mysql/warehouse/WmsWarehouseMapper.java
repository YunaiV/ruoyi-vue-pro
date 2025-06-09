package cn.iocoder.yudao.module.wms.dal.mysql.warehouse;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.MPJLambdaWrapperX;
import cn.iocoder.yudao.module.wms.api.warehouse.dto.vo.WmsWarehouseListReqDTO;
import cn.iocoder.yudao.module.wms.controller.admin.warehouse.vo.WmsWarehousePageReqVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.warehouse.WmsWarehouseDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.warehouse.zone.WmsWarehouseZoneDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Set;

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
            .likeIfPresent(WmsWarehouseDO::getAddressLine3, reqVO.getAddressLine3())
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

    default List<WmsWarehouseDO> selectByCodes(Set<String> codes) {
        LambdaQueryWrapperX<WmsWarehouseDO> wrapper = new LambdaQueryWrapperX<>();
        wrapper.in(WmsWarehouseDO::getCode, codes);
        return selectList(wrapper);
    }

    /**
     * 根据仓库DO查询仓库列表
     *
     * @param warehouse 仓库查询条件
     * @return 仓库列表
     */
    default List<WmsWarehouseDO> selectListByWarehouse(WmsWarehouseDO warehouse) {
        LambdaQueryWrapperX<WmsWarehouseDO> queryWrapper = new LambdaQueryWrapperX<>();
        if (warehouse != null) {
            queryWrapper.eq(warehouse.getId() != null, WmsWarehouseDO::getId, warehouse.getId())
                    .eq(warehouse.getCode() != null, WmsWarehouseDO::getCode, warehouse.getCode())
                    .eq(warehouse.getName() != null, WmsWarehouseDO::getName, warehouse.getName())
                    .eq(warehouse.getExternalStorageId() != null, WmsWarehouseDO::getExternalStorageId, warehouse.getExternalStorageId());
        }
        return selectList(queryWrapper);
    }

    /**
     * 根据条件查询仓库列表
     *
     * @param reqDTO 查询条件
     * @return 仓库列表
     */
    default List<WmsWarehouseDO> selectList(WmsWarehouseListReqDTO reqDTO) {
        LambdaQueryWrapperX<WmsWarehouseDO> queryWrapper = new LambdaQueryWrapperX<>();
        if (reqDTO != null) {
            queryWrapper.eqIfPresent(WmsWarehouseDO::getId, reqDTO.getId())
                    .eqIfPresent(WmsWarehouseDO::getCode, reqDTO.getCode())
                    .eqIfPresent(WmsWarehouseDO::getName, reqDTO.getName())
                    .eqIfPresent(WmsWarehouseDO::getExternalStorageId, reqDTO.getExternalStorageId())
                    .eqIfPresent(WmsWarehouseDO::getCountry, reqDTO.getCountry())
                    .eqIfPresent(WmsWarehouseDO::getProvince, reqDTO.getProvince())
                    .eqIfPresent(WmsWarehouseDO::getCity, reqDTO.getCity())
                    .eqIfPresent(WmsWarehouseDO::getPostcode, reqDTO.getPostcode())
                .eqIfPresent(WmsWarehouseDO::getAddressLine3, reqDTO.getAddressLine3());
        }
        return selectList(queryWrapper);
    }

    /**
     * 获取拥有良品/次品库区的仓库列表
     *
     * @param exchange 分区类型: 1-标准品 , 2-不良品
     * @return 库区列表
     */
    default List<WmsWarehouseDO> getSimpleListForExchange(Integer exchange) {
        MPJLambdaWrapperX<WmsWarehouseDO> queryWrapper = new MPJLambdaWrapperX<>();
        queryWrapper.selectAll(WmsWarehouseDO.class);
        queryWrapper.leftJoin(WmsWarehouseZoneDO.class, on ->
                on.eq(WmsWarehouseZoneDO::getWarehouseId, WmsWarehouseDO::getId))
            .eq(WmsWarehouseZoneDO::getPartitionType, exchange);
        queryWrapper.groupBy(WmsWarehouseDO::getId);
        return selectList(queryWrapper);
    }
}
