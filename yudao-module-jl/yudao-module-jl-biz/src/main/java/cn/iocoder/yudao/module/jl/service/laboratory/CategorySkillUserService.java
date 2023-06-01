package cn.iocoder.yudao.module.jl.service.laboratory;

import java.util.*;
import javax.validation.*;
import cn.iocoder.yudao.module.jl.controller.admin.laboratory.vo.*;
import cn.iocoder.yudao.module.jl.entity.laboratory.CategorySkillUser;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

/**
 * 实验名目的擅长人员 Service 接口
 *
 */
public interface CategorySkillUserService {

    /**
     * 创建实验名目的擅长人员
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createCategorySkillUser(@Valid CategorySkillUserCreateReqVO createReqVO);

    /**
     * 更新实验名目的擅长人员
     *
     * @param updateReqVO 更新信息
     */
    void updateCategorySkillUser(@Valid CategorySkillUserUpdateReqVO updateReqVO);

    /**
     * 删除实验名目的擅长人员
     *
     * @param id 编号
     */
    void deleteCategorySkillUser(Long id);

    /**
     * 获得实验名目的擅长人员
     *
     * @param id 编号
     * @return 实验名目的擅长人员
     */
    Optional<CategorySkillUser> getCategorySkillUser(Long id);

    /**
     * 获得实验名目的擅长人员列表
     *
     * @param ids 编号
     * @return 实验名目的擅长人员列表
     */
    List<CategorySkillUser> getCategorySkillUserList(Collection<Long> ids);

    /**
     * 获得实验名目的擅长人员分页
     *
     * @param pageReqVO 分页查询
     * @return 实验名目的擅长人员分页
     */
    PageResult<CategorySkillUser> getCategorySkillUserPage(CategorySkillUserPageReqVO pageReqVO, CategorySkillUserPageOrder orderV0);

    /**
     * 获得实验名目的擅长人员列表, 用于 Excel 导出
     *
     * @param exportReqVO 查询条件
     * @return 实验名目的擅长人员列表
     */
    List<CategorySkillUser> getCategorySkillUserList(CategorySkillUserExportReqVO exportReqVO);

}
