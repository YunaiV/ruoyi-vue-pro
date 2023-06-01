package cn.iocoder.yudao.module.jl.service.laboratory;

import java.util.*;
import javax.validation.*;
import cn.iocoder.yudao.module.jl.controller.admin.laboratory.vo.*;
import cn.iocoder.yudao.module.jl.entity.laboratory.CategorySupply;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

/**
 * 实验名目的物资 Service 接口
 *
 */
public interface CategorySupplyService {

    /**
     * 创建实验名目的物资
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createCategorySupply(@Valid CategorySupplyCreateReqVO createReqVO);

    /**
     * 更新实验名目的物资
     *
     * @param updateReqVO 更新信息
     */
    void updateCategorySupply(@Valid CategorySupplyUpdateReqVO updateReqVO);

    /**
     * 删除实验名目的物资
     *
     * @param id 编号
     */
    void deleteCategorySupply(Long id);

    /**
     * 获得实验名目的物资
     *
     * @param id 编号
     * @return 实验名目的物资
     */
    Optional<CategorySupply> getCategorySupply(Long id);

    /**
     * 获得实验名目的物资列表
     *
     * @param ids 编号
     * @return 实验名目的物资列表
     */
    List<CategorySupply> getCategorySupplyList(Collection<Long> ids);

    /**
     * 获得实验名目的物资分页
     *
     * @param pageReqVO 分页查询
     * @return 实验名目的物资分页
     */
    PageResult<CategorySupply> getCategorySupplyPage(CategorySupplyPageReqVO pageReqVO, CategorySupplyPageOrder orderV0);

    /**
     * 获得实验名目的物资列表, 用于 Excel 导出
     *
     * @param exportReqVO 查询条件
     * @return 实验名目的物资列表
     */
    List<CategorySupply> getCategorySupplyList(CategorySupplyExportReqVO exportReqVO);

    boolean saveCategorySupply(CategorySupplySaveReqVO saveReqVO);
}
