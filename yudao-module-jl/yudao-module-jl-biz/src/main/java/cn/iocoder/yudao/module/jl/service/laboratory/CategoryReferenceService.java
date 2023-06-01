package cn.iocoder.yudao.module.jl.service.laboratory;

import java.util.*;
import javax.validation.*;
import cn.iocoder.yudao.module.jl.controller.admin.laboratory.vo.*;
import cn.iocoder.yudao.module.jl.entity.laboratory.CategoryReference;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

/**
 * 实验名目的参考资料 Service 接口
 *
 */
public interface CategoryReferenceService {

    /**
     * 创建实验名目的参考资料
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createCategoryReference(@Valid CategoryReferenceCreateReqVO createReqVO);

    /**
     * 更新实验名目的参考资料
     *
     * @param updateReqVO 更新信息
     */
    void updateCategoryReference(@Valid CategoryReferenceUpdateReqVO updateReqVO);

    /**
     * 删除实验名目的参考资料
     *
     * @param id 编号
     */
    void deleteCategoryReference(Long id);

    /**
     * 获得实验名目的参考资料
     *
     * @param id 编号
     * @return 实验名目的参考资料
     */
    Optional<CategoryReference> getCategoryReference(Long id);

    /**
     * 获得实验名目的参考资料列表
     *
     * @param ids 编号
     * @return 实验名目的参考资料列表
     */
    List<CategoryReference> getCategoryReferenceList(Collection<Long> ids);

    /**
     * 获得实验名目的参考资料分页
     *
     * @param pageReqVO 分页查询
     * @return 实验名目的参考资料分页
     */
    PageResult<CategoryReference> getCategoryReferencePage(CategoryReferencePageReqVO pageReqVO, CategoryReferencePageOrder orderV0);

    /**
     * 获得实验名目的参考资料列表, 用于 Excel 导出
     *
     * @param exportReqVO 查询条件
     * @return 实验名目的参考资料列表
     */
    List<CategoryReference> getCategoryReferenceList(CategoryReferenceExportReqVO exportReqVO);

}
