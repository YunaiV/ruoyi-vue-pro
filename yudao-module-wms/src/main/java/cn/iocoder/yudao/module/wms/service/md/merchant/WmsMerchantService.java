package cn.iocoder.yudao.module.wms.service.md.merchant;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.wms.controller.admin.md.merchant.vo.WmsMerchantListReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.md.merchant.vo.WmsMerchantPageReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.md.merchant.vo.WmsMerchantSaveReqVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.md.merchant.WmsMerchantDO;
import jakarta.validation.Valid;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;

/**
 * WMS 往来企业 Service 接口
 *
 * @author 芋道源码
 */
public interface WmsMerchantService {

    /**
     * 创建往来企业
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createMerchant(@Valid WmsMerchantSaveReqVO createReqVO);

    /**
     * 更新往来企业
     *
     * @param updateReqVO 更新信息
     */
    void updateMerchant(@Valid WmsMerchantSaveReqVO updateReqVO);

    /**
     * 删除往来企业
     *
     * @param id 编号
     */
    void deleteMerchant(Long id);

    /**
     * 校验往来企业存在
     *
     * @param id 编号
     * @return 往来企业
     */
    WmsMerchantDO validateMerchantExists(Long id);

    /**
     * 校验供应商存在
     *
     * @param id 编号
     * @return 往来企业
     */
    @SuppressWarnings("UnusedReturnValue")
    WmsMerchantDO validateSupplierMerchantExists(Long id);

    /**
     * 校验客户存在
     *
     * @param id 编号
     * @return 往来企业
     */
    @SuppressWarnings("UnusedReturnValue")
    WmsMerchantDO validateCustomerMerchantExists(Long id);

    /**
     * 获得往来企业
     *
     * @param id 编号
     * @return 往来企业
     */
    WmsMerchantDO getMerchant(Long id);

    /**
     * 获得往来企业分页
     *
     * @param pageReqVO 分页查询
     * @return 往来企业分页
     */
    PageResult<WmsMerchantDO> getMerchantPage(WmsMerchantPageReqVO pageReqVO);

    /**
     * 获得往来企业列表
     *
     * @param listReqVO 查询条件
     * @return 往来企业列表
     */
    List<WmsMerchantDO> getMerchantList(WmsMerchantListReqVO listReqVO);

    /**
     * 按编号集合获得往来企业列表
     *
     * @param ids 编号集合
     * @return 往来企业列表
     */
    List<WmsMerchantDO> getMerchantList(Collection<Long> ids);

    /**
     * 按编号集合获得往来企业 Map
     *
     * @param ids 编号集合
     * @return 往来企业 Map
     */
    default Map<Long, WmsMerchantDO> getMerchantMap(Collection<Long> ids) {
        return convertMap(getMerchantList(ids), WmsMerchantDO::getId);
    }

}
