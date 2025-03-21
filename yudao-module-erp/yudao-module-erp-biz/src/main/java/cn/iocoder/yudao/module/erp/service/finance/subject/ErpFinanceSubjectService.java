package cn.iocoder.yudao.module.erp.service.finance.subject;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.erp.controller.admin.finance.subject.vo.ErpFinanceSubjectPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.finance.subject.vo.ErpFinanceSubjectSaveReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.finance.subject.vo.ErpFinanceSubjectSimpleRespVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.subject.ErpFinanceSubjectDO;
import jakarta.validation.Valid;

import java.util.List;

/**
 * Erp财务主体 Service 接口
 *
 * @author 王岽宇
 */
public interface ErpFinanceSubjectService {

    /**
     * 创建Erp财务主体
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createFinanceSubject(@Valid ErpFinanceSubjectSaveReqVO createReqVO);

    /**
     * 更新Erp财务主体
     *
     * @param updateReqVO 更新信息
     */
    void updateFinanceSubject(@Valid ErpFinanceSubjectSaveReqVO updateReqVO);

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
    ErpFinanceSubjectDO getFinanceSubject(Long id);

    /**
     * 获得Erp财务主体分页
     *
     * @param pageReqVO 分页查询
     * @return Erp财务主体分页
     */
    PageResult<ErpFinanceSubjectDO> getFinanceSubjectPage(ErpFinanceSubjectPageReqVO pageReqVO);

    List<ErpFinanceSubjectSimpleRespVO> ListFinanceSubjectSimple();
}