package cn.iocoder.yudao.module.fms.service.config;

import cn.iocoder.yudao.module.fms.controller.admin.config.vo.accountset.FmsAccountSetInitializeReqVO;
import cn.iocoder.yudao.module.fms.controller.admin.config.vo.financeparameter.FmsFinanceParameterUpdateReqVO;
import cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsFinanceParameterDO;

/**
 * FMS 财务参数 Service 接口
 *
 * @author 芋道源码
 */
public interface FmsFinanceParameterService {

    /**
     * 初始化财务参数
     *
     * @param accountSetId 账套编号
     * @param initializeReqVO 账套初始化信息
     */
    void initializeFinanceParameter(Long accountSetId, FmsAccountSetInitializeReqVO initializeReqVO);

    /**
     * 获得财务参数
     *
     * @param accountSetId 账套编号
     * @param userId 用户编号
     * @return 财务参数
     */
    FmsFinanceParameterDO getFinanceParameter(Long accountSetId, Long userId);

    /**
     * 获得财务参数
     *
     * @param accountSetId 账套编号
     * @return 财务参数
     */
    FmsFinanceParameterDO getFinanceParameter(Long accountSetId);

    /**
     * 将标准科目编码转换为账套实际科目编码
     *
     * @param standardCode 标准科目编码
     * @param subjectCodeRule 科目编码规则
     * @return 账套实际科目编码
     */
    String convertStandardSubjectCode(String standardCode, String subjectCodeRule);

    /**
     * 更新财务参数
     *
     * @param updateReqVO 更新信息
     * @param userId 用户编号
     */
    void updateFinanceParameter(FmsFinanceParameterUpdateReqVO updateReqVO, Long userId);

}
