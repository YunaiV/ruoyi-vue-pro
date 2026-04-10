package cn.iocoder.yudao.module.mes.service.wm.returnvendor;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.mes.controller.admin.wm.returnvendor.vo.MesWmReturnVendorPageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.wm.returnvendor.vo.MesWmReturnVendorSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.returnvendor.MesWmReturnVendorDO;
import jakarta.validation.Valid;

/**
 * MES 供应商退货单 Service 接口
 */
public interface MesWmReturnVendorService {

    /**
     * 创建供应商退货单
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createReturnVendor(@Valid MesWmReturnVendorSaveReqVO createReqVO);

    /**
     * 修改供应商退货单
     *
     * @param updateReqVO 修改信息
     */
    void updateReturnVendor(@Valid MesWmReturnVendorSaveReqVO updateReqVO);

    /**
     * 删除供应商退货单
     *
     * @param id 编号
     */
    void deleteReturnVendor(Long id);

    /**
     * 获得供应商退货单
     *
     * @param id 编号
     * @return 供应商退货单
     */
    MesWmReturnVendorDO getReturnVendor(Long id);

    /**
     * 获得供应商退货单分页
     *
     * @param pageReqVO 分页参数
     * @return 供应商退货单分页
     */
    PageResult<MesWmReturnVendorDO> getReturnVendorPage(MesWmReturnVendorPageReqVO pageReqVO);

    /**
     * 校验供应商退货单是否存在
     *
     * @param id 编号
     * @return 供应商退货单
     */
    MesWmReturnVendorDO validateReturnVendorExists(Long id);

    /**
     * 提交供应商退货单（草稿 -> 待拣货）
     *
     * @param id 编号
     */
    void submitReturnVendor(Long id);

    /**
     * 执行拣货（待拣货 -> 待执行退货）
     *
     * @param id 编号
     */
    void stockReturnVendor(Long id);

    /**
     * 执行退货/出库（待执行退货 -> 已完成），更新库存台账
     *
     * @param id 编号
     */
    void finishReturnVendor(Long id);

    /**
     * 取消供应商退货单（任意非已完成/已取消状态 -> 已取消）
     *
     * @param id 编号
     */
    void cancelReturnVendor(Long id);

    /**
     * 校验供应商退货单的数量：每行明细数量之和是否等于行退货数量
     *
     * @param id 编号
     * @return 是否全部一致
     */
    Boolean checkReturnVendorQuantity(Long id);

    /**
     * 查询指定供应商的退货单数量
     *
     * @param vendorId 供应商编号
     * @return 数量
     */
    Long getReturnVendorCountByVendorId(Long vendorId);

    /**
     * 校验供应商退货单存在且为草稿状态
     *
     * @param id 编号
     * @return 供应商退货单
     */
    MesWmReturnVendorDO validateReturnVendorExistsAndPrepare(Long id);

}
