package cn.iocoder.yudao.module.fms.service.finance.subject;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.fms.controller.admin.finance.subject.vo.FmsFinanceSubjectPageReqVO;
import cn.iocoder.yudao.module.fms.controller.admin.finance.subject.vo.FmsFinanceSubjectSaveReqVO;
import cn.iocoder.yudao.module.fms.controller.admin.finance.subject.vo.FmsFinanceSubjectSimpleRespVO;
import cn.iocoder.yudao.module.fms.dal.dataobject.finance.subject.FmsCompanyDO;
import jakarta.validation.Valid;

import java.util.List;

/**
 * Erp财务主体 Service 接口
 *
 * @author 王岽宇
 */
public interface FmsFinanceSubjectService {

    /**
     * 创建Erp财务主体
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createFinanceSubject(@Valid FmsFinanceSubjectSaveReqVO createReqVO);

    /**
     * 更新Erp财务主体
     *
     * @param updateReqVO 更新信息
     */
    void updateFinanceSubject(@Valid FmsFinanceSubjectSaveReqVO updateReqVO);

    /**
     * 删除Erp财务主体
     *
     * @param id 编号
     */
    void deleteFinanceSubject(Long id);

    /**
     * 获得Erp财务主体
     *
     * @param id 编号
     * @return Erp财务主体
     */
    FmsCompanyDO getFinanceSubject(Long id);


    /**
     * 获得Erp财务主体分页
     *
     * @param pageReqVO 分页查询
     * @return Erp财务主体分页
     */
    PageResult<FmsCompanyDO> getFinanceSubjectPage(FmsFinanceSubjectPageReqVO pageReqVO);

    List<FmsFinanceSubjectSimpleRespVO> ListFinanceSubjectSimple();

    /**
     * 校验财务主体集合
     *
     * @param ids ids
     * @return FmsFinanceSubjectDO
     */
    List<FmsCompanyDO> listFinanceSubject(List<Long> ids);
}