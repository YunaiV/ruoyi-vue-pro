package cn.iocoder.yudao.module.crm.service.contract;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.crm.controller.admin.contract.vo.*;
import cn.iocoder.yudao.module.crm.dal.dataobject.contract.ContractDO;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;

/**
 * 合同 Service 接口
 *
 * @author dhb52
 */
public interface ContractService {

    /**
     * 创建合同
     *
     * @param createReqVO 创建信息
     * @param userId      用户编号
     * @return 编号
     */
    Long createContract(@Valid ContractCreateReqVO createReqVO, Long userId);

    /**
     * 更新合同
     *
     * @param updateReqVO 更新信息
     */
    void updateContract(@Valid ContractUpdateReqVO updateReqVO);

    /**
     * 删除合同
     *
     * @param id 编号
     */
    void deleteContract(Long id);

    /**
     * 获得合同
     *
     * @param id 编号
     * @return 合同
     */
    ContractDO getContract(Long id);

    /**
     * 获得合同列表
     *
     * @param ids 编号
     * @return 合同列表
     */
    List<ContractDO> getContractList(Collection<Long> ids);

    /**
     * 获得合同分页
     *
     * @param pageReqVO 分页查询
     * @return 合同分页
     */
    PageResult<ContractDO> getContractPage(ContractPageReqVO pageReqVO);

    /**
     * 获得合同列表, 用于 Excel 导出
     *
     * @param exportReqVO 查询条件
     * @return 合同列表
     */
    List<ContractDO> getContractList(ContractExportReqVO exportReqVO);

    /**
     * 合同转移
     *
     * @param reqVO  请求
     * @param userId 用户编号
     */
    void transferContract(CrmTransferContractReqVO reqVO, Long userId);

}
