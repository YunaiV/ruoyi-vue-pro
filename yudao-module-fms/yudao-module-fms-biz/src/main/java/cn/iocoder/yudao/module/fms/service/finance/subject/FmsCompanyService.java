package cn.iocoder.yudao.module.fms.service.finance.subject;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.fms.controller.admin.finance.subject.vo.FmsCompanyPageReqVO;
import cn.iocoder.yudao.module.fms.controller.admin.finance.subject.vo.FmsCompanySaveReqVO;
import cn.iocoder.yudao.module.fms.controller.admin.finance.subject.vo.FmsCompanySimpleRespVO;
import cn.iocoder.yudao.module.fms.dal.dataobject.finance.subject.FmsCompanyDO;
import jakarta.validation.Valid;

import java.util.List;

/**
 * Fms财务公司 Service 接口
 *
 * @author 王岽宇
 */
public interface FmsCompanyService {

    /**
     * 创建Fms财务公司
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createCompany(@Valid FmsCompanySaveReqVO createReqVO);

    /**
     * 更新Fms财务公司
     *
     * @param updateReqVO 更新信息
     */
    void updateCompany(@Valid FmsCompanySaveReqVO updateReqVO);

    /**
     * 删除Fms财务公司
     *
     * @param id 编号
     */
    void deleteCompany(Long id);

    /**
     * 获得Fms财务公司
     *
     * @param id 编号
     * @return Fms财务公司
     */
    FmsCompanyDO getCompany(Long id);


    /**
     * 获得Fms财务公司分页
     *
     * @param pageReqVO 分页查询
     * @return Fms财务公司分页
     */
    PageResult<FmsCompanyDO> getCompanyPage(FmsCompanyPageReqVO pageReqVO);

    List<FmsCompanySimpleRespVO> ListCompanySimple();

    /**
     * 校验财务主体集合
     *
     * @param ids ids
     * @return FmsCompanyDO
     */
    List<FmsCompanyDO> listCompany(List<Long> ids);
}