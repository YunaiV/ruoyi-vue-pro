package cn.iocoder.yudao.module.mes.service.wm.returnvendor;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.mes.controller.admin.wm.returnvendor.vo.line.MesWmReturnVendorLinePageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.wm.returnvendor.vo.line.MesWmReturnVendorLineSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.returnvendor.MesWmReturnVendorLineDO;
import jakarta.validation.Valid;

import java.util.List;

/**
 * MES 供应商退货单行 Service 接口
 */
public interface MesWmReturnVendorLineService {

    /**
     * 创建供应商退货单行
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createReturnVendorLine(@Valid MesWmReturnVendorLineSaveReqVO createReqVO);

    /**
     * 更新供应商退货单行
     *
     * @param updateReqVO 更新信息
     */
    void updateReturnVendorLine(@Valid MesWmReturnVendorLineSaveReqVO updateReqVO);

    /**
     * 删除供应商退货单行
     *
     * @param id 编号
     */
    void deleteReturnVendorLine(Long id);

    /**
     * 获得供应商退货单行
     *
     * @param id 编号
     * @return 供应商退货单行
     */
    MesWmReturnVendorLineDO getReturnVendorLine(Long id);

    /**
     * 获得供应商退货单行分页
     *
     * @param pageReqVO 分页查询
     * @return 供应商退货单行分页
     */
    PageResult<MesWmReturnVendorLineDO> getReturnVendorLinePage(MesWmReturnVendorLinePageReqVO pageReqVO);

    /**
     * 根据退货单ID获取行列表
     *
     * @param returnId 退货单ID
     * @return 行列表
     */
    List<MesWmReturnVendorLineDO> getReturnVendorLineListByReturnId(Long returnId);

    /**
     * 根据退货单ID删除行
     *
     * @param returnId 退货单ID
     */
    void deleteReturnVendorLineByReturnId(Long returnId);

    /**
     * 校验供应商退货单行是否存在
     *
     * @param id 编号
     * @return 供应商退货单行
     */
    MesWmReturnVendorLineDO validateReturnVendorLineExists(Long id);

}
