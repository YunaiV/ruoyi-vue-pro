package cn.iocoder.yudao.module.erp.service.product;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.erp.controller.admin.product.vo.unit.ErpProductUnitPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.product.vo.unit.ErpProductUnitSaveReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.product.ErpProductUnitDO;
import jakarta.validation.Valid;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;

/**
 * ERP 产品单位 Service 接口
 *
 * @author 芋道源码
 */
public interface ErpProductUnitService {

    /**
     * 创建产品单位
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createProductUnit(@Valid ErpProductUnitSaveReqVO createReqVO);

    /**
     * 更新产品单位
     *
     * @param updateReqVO 更新信息
     */
    void updateProductUnit(@Valid ErpProductUnitSaveReqVO updateReqVO);

    /**
     * 删除产品单位
     *
     * @param id 编号
     */
    void deleteProductUnit(Long id);

    /**
     * 获得产品单位
     *
     * @param id 编号
     * @return 产品单位
     */
    ErpProductUnitDO getProductUnit(Long id);

    /**
     * 获得产品单位分页
     *
     * @param pageReqVO 分页查询
     * @return 产品单位分页
     */
    PageResult<ErpProductUnitDO> getProductUnitPage(ErpProductUnitPageReqVO pageReqVO);

    /**
     * 获得指定状态的产品单位列表
     *
     * @param status 状态
     * @return 产品单位列表
     */
    List<ErpProductUnitDO> getProductUnitListByStatus(Integer status);

    /**
     * 获得产品单位列表
     *
     * @param ids 编号数组
     * @return 产品单位列表
     */
    List<ErpProductUnitDO> getProductUnitList(Collection<Long> ids);

    /**
     * 获得产品单位 Map
     *
     * @param ids 编号数组
     * @return 产品单位 Map
     */
    default Map<Long, ErpProductUnitDO> getProductUnitMap(Collection<Long> ids) {
        return convertMap(getProductUnitList(ids), ErpProductUnitDO::getId);
    }

}