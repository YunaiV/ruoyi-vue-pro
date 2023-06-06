package cn.iocoder.yudao.module.jl.service.laboratory;

import java.util.*;
import javax.validation.*;
import cn.iocoder.yudao.module.jl.controller.admin.laboratory.vo.*;
import cn.iocoder.yudao.module.jl.entity.laboratory.Category;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

/**
 * 实验名目 Service 接口
 *
 */
public interface CategoryService {

    /**
     * 创建实验名目
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createCategory(@Valid CategoryCreateReqVO createReqVO);

    /**
     * 更新实验名目
     *
     * @param updateReqVO 更新信息
     */
    void updateCategory(@Valid CategoryUpdateReqVO updateReqVO);

    /**
     * 删除实验名目
     *
     * @param id 编号
     */
    void deleteCategory(Long id);

    /**
     * 获得实验名目
     *
     * @param id 编号
     * @return 实验名目
     */
    Optional<Category> getCategory(Long id);

    /**
     * 获得实验名目列表
     *
     * @param ids 编号
     * @return 实验名目列表
     */
    List<Category> getCategoryList(Collection<Long> ids);

    /**
     * 获得实验名目分页
     *
     * @param pageReqVO 分页查询
     * @return 实验名目分页
     */
    PageResult<Category> getCategoryPage(CategoryPageReqVO pageReqVO, CategoryPageOrder orderV0);

    /**
     * 获得实验名目列表, 用于 Excel 导出
     *
     * @param exportReqVO 查询条件
     * @return 实验名目列表
     */
    List<Category> getCategoryList(CategoryExportReqVO exportReqVO);

}
