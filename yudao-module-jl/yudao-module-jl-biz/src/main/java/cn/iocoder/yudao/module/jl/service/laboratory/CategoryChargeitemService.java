package cn.iocoder.yudao.module.jl.service.laboratory;

import java.util.*;
import javax.validation.*;
import cn.iocoder.yudao.module.jl.controller.admin.laboratory.vo.*;
import cn.iocoder.yudao.module.jl.entity.laboratory.CategoryChargeitem;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

/**
 * 实验名目的收费项 Service 接口
 *
 */
public interface CategoryChargeitemService {

    /**
     * 创建实验名目的收费项
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createCategoryChargeitem(@Valid CategoryChargeitemCreateReqVO createReqVO);

    /**
     * 更新实验名目的收费项
     *
     * @param updateReqVO 更新信息
     */
    void updateCategoryChargeitem(@Valid CategoryChargeitemUpdateReqVO updateReqVO);

    /**
     * 删除实验名目的收费项
     *
     * @param id 编号
     */
    void deleteCategoryChargeitem(Long id);

    /**
     * 获得实验名目的收费项
     *
     * @param id 编号
     * @return 实验名目的收费项
     */
    Optional<CategoryChargeitem> getCategoryChargeitem(Long id);

    /**
     * 获得实验名目的收费项列表
     *
     * @param ids 编号
     * @return 实验名目的收费项列表
     */
    List<CategoryChargeitem> getCategoryChargeitemList(Collection<Long> ids);

    /**
     * 获得实验名目的收费项分页
     *
     * @param pageReqVO 分页查询
     * @return 实验名目的收费项分页
     */
    PageResult<CategoryChargeitem> getCategoryChargeitemPage(CategoryChargeitemPageReqVO pageReqVO, CategoryChargeitemPageOrder orderV0);

    /**
     * 获得实验名目的收费项列表, 用于 Excel 导出
     *
     * @param exportReqVO 查询条件
     * @return 实验名目的收费项列表
     */
    List<CategoryChargeitem> getCategoryChargeitemList(CategoryChargeitemExportReqVO exportReqVO);

}
