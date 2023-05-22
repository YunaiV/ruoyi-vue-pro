package cn.iocoder.yudao.module.jl.service.crm;

import java.util.*;
import javax.validation.*;
import cn.iocoder.yudao.module.jl.controller.admin.crm.vo.*;
import cn.iocoder.yudao.module.jl.dal.dataobject.crm.InstitutionDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

/**
 * CRM 模块的机构/公司 Service 接口
 *
 * @author 芋道源码
 */
public interface InstitutionService {

    /**
     * 创建CRM 模块的机构/公司
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createInstitution(@Valid InstitutionCreateReqVO createReqVO);

    /**
     * 更新CRM 模块的机构/公司
     *
     * @param updateReqVO 更新信息
     */
    void updateInstitution(@Valid InstitutionUpdateReqVO updateReqVO);

    /**
     * 删除CRM 模块的机构/公司
     *
     * @param id 编号
     */
    void deleteInstitution(Long id);

    /**
     * 获得CRM 模块的机构/公司
     *
     * @param id 编号
     * @return CRM 模块的机构/公司
     */
    InstitutionDO getInstitution(Long id);

    /**
     * 获得CRM 模块的机构/公司列表
     *
     * @param ids 编号
     * @return CRM 模块的机构/公司列表
     */
    List<InstitutionDO> getInstitutionList(Collection<Long> ids);

    /**
     * 获得CRM 模块的机构/公司分页
     *
     * @param pageReqVO 分页查询
     * @return CRM 模块的机构/公司分页
     */
    PageResult<InstitutionDO> getInstitutionPage(InstitutionPageReqVO pageReqVO);

    /**
     * 获得CRM 模块的机构/公司列表, 用于 Excel 导出
     *
     * @param exportReqVO 查询条件
     * @return CRM 模块的机构/公司列表
     */
    List<InstitutionDO> getInstitutionList(InstitutionExportReqVO exportReqVO);

}
