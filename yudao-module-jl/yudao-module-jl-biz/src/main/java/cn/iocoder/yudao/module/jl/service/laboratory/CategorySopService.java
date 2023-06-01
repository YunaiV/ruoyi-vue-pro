package cn.iocoder.yudao.module.jl.service.laboratory;

import java.util.*;
import javax.validation.*;
import cn.iocoder.yudao.module.jl.controller.admin.laboratory.vo.*;
import cn.iocoder.yudao.module.jl.entity.laboratory.CategorySop;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

/**
 * 实验名目的操作SOP Service 接口
 *
 */
public interface CategorySopService {

    /**
     * 创建实验名目的操作SOP
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createCategorySop(@Valid CategorySopCreateReqVO createReqVO);

    boolean saveAllCategorySop(@Valid CategorySopSaveReqVO saveReqVO);

    /**
     * 更新实验名目的操作SOP
     *
     * @param updateReqVO 更新信息
     */
    void updateCategorySop(@Valid CategorySopUpdateReqVO updateReqVO);

    /**
     * 删除实验名目的操作SOP
     *
     * @param id 编号
     */
    void deleteCategorySop(Long id);

    /**
     * 获得实验名目的操作SOP
     *
     * @param id 编号
     * @return 实验名目的操作SOP
     */
    Optional<CategorySop> getCategorySop(Long id);

    /**
     * 获得实验名目的操作SOP列表
     *
     * @param ids 编号
     * @return 实验名目的操作SOP列表
     */
    List<CategorySop> getCategorySopList(Collection<Long> ids);

    /**
     * 获得实验名目的操作SOP分页
     *
     * @param pageReqVO 分页查询
     * @return 实验名目的操作SOP分页
     */
    PageResult<CategorySop> getCategorySopPage(CategorySopPageReqVO pageReqVO, CategorySopPageOrder orderV0);

    /**
     * 获得实验名目的操作SOP列表, 用于 Excel 导出
     *
     * @param exportReqVO 查询条件
     * @return 实验名目的操作SOP列表
     */
    List<CategorySop> getCategorySopList(CategorySopExportReqVO exportReqVO);

}
