package cn.iocoder.yudao.module.jl.service.crm;

import java.util.*;
import javax.validation.*;
import cn.iocoder.yudao.module.jl.controller.admin.crm.vo.*;
import cn.iocoder.yudao.module.jl.entity.crm.Institution;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

/**
 * 机构/公司 Service 接口
 *
 */
public interface InstitutionService {

    /**
     * 创建机构/公司
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createInstitution(@Valid InstitutionCreateReqVO createReqVO);

    /**
     * 更新机构/公司
     *
     * @param updateReqVO 更新信息
     */
    void updateInstitution(@Valid InstitutionUpdateReqVO updateReqVO);

    /**
     * 删除机构/公司
     *
     * @param id 编号
     */
    void deleteInstitution(Long id);

    /**
     * 获得机构/公司
     *
     * @param id 编号
     * @return 机构/公司
     */
    Optional<Institution> getInstitution(Long id);

    /**
     * 获得机构/公司列表
     *
     * @param ids 编号
     * @return 机构/公司列表
     */
    List<Institution> getInstitutionList(Collection<Long> ids);

    /**
     * 获得机构/公司分页
     *
     * @param pageReqVO 分页查询
     * @return 机构/公司分页
     */
    PageResult<Institution> getInstitutionPage(InstitutionPageReqVO pageReqVO, InstitutionPageOrder orderV0);

    /**
     * 获得机构/公司列表, 用于 Excel 导出
     *
     * @param exportReqVO 查询条件
     * @return 机构/公司列表
     */
    List<Institution> getInstitutionList(InstitutionExportReqVO exportReqVO);

}
