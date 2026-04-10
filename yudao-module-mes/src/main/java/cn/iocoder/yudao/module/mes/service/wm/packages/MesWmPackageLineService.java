package cn.iocoder.yudao.module.mes.service.wm.packages;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.mes.controller.admin.wm.packages.vo.line.MesWmPackageLinePageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.wm.packages.vo.line.MesWmPackageLineSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.packages.MesWmPackageLineDO;
import jakarta.validation.Valid;

/**
 * MES 装箱明细 Service 接口
 *
 * @author 芋道源码
 */
public interface MesWmPackageLineService {

    /**
     * 创建装箱明细
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createPackageLine(@Valid MesWmPackageLineSaveReqVO createReqVO);

    /**
     * 修改装箱明细
     *
     * @param updateReqVO 修改信息
     */
    void updatePackageLine(@Valid MesWmPackageLineSaveReqVO updateReqVO);

    /**
     * 删除装箱明细
     *
     * @param id 编号
     */
    void deletePackageLine(Long id);

    /**
     * 获得装箱明细
     *
     * @param id 编号
     * @return 装箱明细
     */
    MesWmPackageLineDO getPackageLine(Long id);

    /**
     * 获得装箱明细分页
     *
     * @param pageReqVO 分页查询
     * @return 分页结果
     */
    PageResult<MesWmPackageLineDO> getPackageLinePage(MesWmPackageLinePageReqVO pageReqVO);

    /**
     * 根据装箱单 ID 删除所有明细
     *
     * @param packageId 装箱单 ID
     */
    void deletePackageLineByPackageId(Long packageId);

}
