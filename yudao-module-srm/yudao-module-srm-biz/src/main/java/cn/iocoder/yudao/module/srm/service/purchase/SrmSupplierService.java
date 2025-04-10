package cn.iocoder.yudao.module.srm.service.purchase;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.srm.controller.admin.purchase.vo.supplier.SrmSupplierPageReqVO;
import cn.iocoder.yudao.module.srm.controller.admin.purchase.vo.supplier.SrmSupplierSaveReqVO;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.SrmSupplierDO;
import jakarta.validation.Valid;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;

/**
 * ERP 供应商 Service 接口
 *
 * @author 芋道源码
 */
public interface SrmSupplierService {

    /**
     * 创建供应商
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createSupplier(@Valid SrmSupplierSaveReqVO createReqVO);

    /**
     * 更新供应商
     *
     * @param updateReqVO 更新信息
     */
    void updateSupplier(@Valid SrmSupplierSaveReqVO updateReqVO);

    /**
     * 删除供应商
     *
     * @param id 编号
     */
    void deleteSupplier(Long id);

    /**
     * 获得供应商
     *
     * @param id 编号
     * @return 供应商
     */
    SrmSupplierDO getSupplier(Long id);

    /**
     * 校验供应商
     *
     * @param id 编号
     * @return 供应商
     */
    SrmSupplierDO validateSupplier(Long id);

    /**
     * 获得供应商列表
     *
     * @param ids 编号列表
     * @return 供应商列表
     */
    List<SrmSupplierDO> getSupplierList(Collection<Long> ids);

    /**
     * 获得供应商 Map
     *
     * @param ids 编号列表
     * @return 供应商 Map
     */
    default Map<Long, SrmSupplierDO> getSupplierMap(Collection<Long> ids) {
        return convertMap(getSupplierList(ids), SrmSupplierDO::getId);
    }

    /**
     * 获得供应商分页
     *
     * @param pageReqVO 分页查询
     * @return 供应商分页
     */
    PageResult<SrmSupplierDO> getSupplierPage(SrmSupplierPageReqVO pageReqVO);

    /**
     * 获得指定状态的供应商列表
     *
     * @param status 状态
     * @return 供应商列表
     */
    List<SrmSupplierDO> getSupplierListByStatus(Integer status);

}