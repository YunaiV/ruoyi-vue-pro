package cn.iocoder.yudao.module.srm.api.supplier;

import cn.iocoder.yudao.module.srm.api.supplier.dto.SrmSupplierDTO;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 供应商 API 接口
 *
 * @author wdy
 */
public interface SrmSupplierApi {

    /**
     * 获得供应商信息
     *
     * @param id 供应商编号
     * @return 供应商信息
     */
    SrmSupplierDTO getSupplier(Long id);

    /**
     * 获得供应商列表
     *
     * @param ids 供应商编号集合
     * @return 供应商列表
     */
    List<SrmSupplierDTO> getSupplierList(Collection<Long> ids);

    /**
     * 获得供应商 Map
     *
     * @param ids 供应商编号集合
     * @return 供应商 Map
     */
    Map<Long, SrmSupplierDTO> getSupplierMap(Collection<Long> ids);
} 