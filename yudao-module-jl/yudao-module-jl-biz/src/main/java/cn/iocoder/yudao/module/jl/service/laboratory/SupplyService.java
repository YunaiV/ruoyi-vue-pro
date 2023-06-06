package cn.iocoder.yudao.module.jl.service.laboratory;

import java.util.*;
import javax.validation.*;
import cn.iocoder.yudao.module.jl.controller.admin.laboratory.vo.*;
import cn.iocoder.yudao.module.jl.entity.laboratory.Supply;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

/**
 * 实验物资 Service 接口
 *
 */
public interface SupplyService {

    /**
     * 创建实验物资
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createSupply(@Valid SupplyCreateReqVO createReqVO);

    /**
     * 更新实验物资
     *
     * @param updateReqVO 更新信息
     */
    void updateSupply(@Valid SupplyUpdateReqVO updateReqVO);

    /**
     * 删除实验物资
     *
     * @param id 编号
     */
    void deleteSupply(Long id);

    /**
     * 获得实验物资
     *
     * @param id 编号
     * @return 实验物资
     */
    Optional<Supply> getSupply(Long id);

    /**
     * 获得实验物资列表
     *
     * @param ids 编号
     * @return 实验物资列表
     */
    List<Supply> getSupplyList(Collection<Long> ids);

    /**
     * 获得实验物资分页
     *
     * @param pageReqVO 分页查询
     * @return 实验物资分页
     */
    PageResult<Supply> getSupplyPage(SupplyPageReqVO pageReqVO, SupplyPageOrder orderV0);

    /**
     * 获得实验物资列表, 用于 Excel 导出
     *
     * @param exportReqVO 查询条件
     * @return 实验物资列表
     */
    List<Supply> getSupplyList(SupplyExportReqVO exportReqVO);

}
