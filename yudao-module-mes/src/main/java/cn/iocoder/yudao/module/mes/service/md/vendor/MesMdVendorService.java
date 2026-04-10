package cn.iocoder.yudao.module.mes.service.md.vendor;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.mes.controller.admin.md.vendor.vo.MesMdVendorImportExcelVO;
import cn.iocoder.yudao.module.mes.controller.admin.md.vendor.vo.MesMdVendorImportRespVO;
import cn.iocoder.yudao.module.mes.controller.admin.md.vendor.vo.MesMdVendorPageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.md.vendor.vo.MesMdVendorSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.vendor.MesMdVendorDO;
import jakarta.validation.Valid;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;

/**
 * MES 供应商 Service 接口
 *
 * @author 芋道源码
 */
public interface MesMdVendorService {

    /**
     * 创建供应商
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createVendor(@Valid MesMdVendorSaveReqVO createReqVO);

    /**
     * 更新供应商
     *
     * @param updateReqVO 更新信息
     */
    void updateVendor(@Valid MesMdVendorSaveReqVO updateReqVO);

    /**
     * 删除供应商
     *
     * @param id 编号
     */
    void deleteVendor(Long id);

    /**
     * 获得供应商
     *
     * @param id 编号
     * @return 供应商
     */
    MesMdVendorDO getVendor(Long id);

    /**
     * 校验供应商存在
     *
     * @param id 供应商 ID
     * @return 供应商
     */
    MesMdVendorDO validateVendorExists(Long id);

    /**
     * 获得供应商分页
     *
     * @param pageReqVO 分页查询
     * @return 供应商分页
     */
    PageResult<MesMdVendorDO> getVendorPage(MesMdVendorPageReqVO pageReqVO);

    /**
     * 获得供应商列表
     *
     * @param ids 编号列表
     * @return 供应商列表
     */
    List<MesMdVendorDO> getVendorList(Collection<Long> ids);

    /**
     * 获得供应商 Map
     *
     * @param ids 编号列表
     * @return 供应商 Map
     */
    default Map<Long, MesMdVendorDO> getVendorMap(Collection<Long> ids) {
        return convertMap(getVendorList(ids), MesMdVendorDO::getId);
    }

    /**
     * 批量导入供应商
     *
     * @param importVendors 导入供应商列表
     * @param updateSupport 是否支持更新
     * @return 导入结果
     */
    MesMdVendorImportRespVO importVendorList(List<MesMdVendorImportExcelVO> importVendors, boolean updateSupport);

}
