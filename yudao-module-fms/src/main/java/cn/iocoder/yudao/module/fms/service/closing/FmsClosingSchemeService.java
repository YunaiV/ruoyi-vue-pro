package cn.iocoder.yudao.module.fms.service.closing;

import cn.iocoder.yudao.module.fms.controller.admin.closing.vo.FmsClosingQueryReqVO;
import cn.iocoder.yudao.module.fms.dal.dataobject.closing.FmsClosingSchemeDO;
import cn.iocoder.yudao.module.fms.controller.admin.closing.vo.FmsClosingSchemeRespVO;
import cn.iocoder.yudao.module.fms.controller.admin.closing.vo.FmsClosingSchemeSaveReqVO;
import cn.iocoder.yudao.module.fms.controller.admin.closing.vo.FmsProfitLossSettingsSaveReqVO;
import cn.iocoder.yudao.module.fms.controller.admin.closing.vo.FmsSpecialClosingSettingsSaveReqVO;

import java.util.Collection;
import java.util.List;

/**
 * FMS 结账方案 Service 接口
 *
 * @author 芋道源码
 */
public interface FmsClosingSchemeService {

    /**
     * 初始化账套默认结账方案
     *
     * @param accountSetId 账套编号
     * @param userId 用户编号
     */
    void initializeDefaultClosingSchemes(Long accountSetId, Long userId);

    /**
     * 获得账套下指定类型的结账方案
     *
     * @param accountSetId 账套编号
     * @param type 结账业务类型
     * @return 结账方案
     */
    FmsClosingSchemeDO getClosingSchemeByAccountSetIdAndType(Long accountSetId, Integer type);

    /**
     * 校验账套下的结账方案存在
     *
     * @param accountSetId 账套编号
     * @param id 方案编号
     * @return 结账方案
     */
    FmsClosingSchemeDO validateClosingSchemeExists(Long accountSetId, Long id);

    /**
     * 获得引用指定科目的结账方案数量
     *
     * @param accountSetId 账套编号
     * @param subjectIds 科目编号数组
     * @return 结账方案数量
     */
    Long getClosingSchemeCountBySubjectIds(Long accountSetId, Collection<Long> subjectIds);

    /**
     * 获得引用指定凭证字的结账方案数量
     *
     * @param accountSetId 账套编号
     * @param voucherWordId 凭证字编号
     * @return 结账方案数量
     */
    Long getClosingSchemeCountByVoucherWordId(Long accountSetId, Long voucherWordId);

    /**
     * 保存结转损益设置
     *
     * @param saveReqVO 保存参数
     * @param userId 用户编号
     * @return 方案编号
     */
    Long saveProfitLossSettings(FmsProfitLossSettingsSaveReqVO saveReqVO, Long userId);

    /**
     * 获得结账方案列表
     *
     * @param queryReqVO 查询参数
     * @param userId 用户编号
     * @return 结账方案列表
     */
    List<FmsClosingSchemeRespVO> getClosingSchemeList(FmsClosingQueryReqVO queryReqVO, Long userId);

    /**
     * 创建结账方案
     *
     * @param createReqVO 创建参数
     * @param userId 用户编号
     * @return 方案编号
     */
    Long createClosingScheme(FmsClosingSchemeSaveReqVO createReqVO, Long userId);

    /**
     * 更新结账方案
     *
     * @param updateReqVO 更新参数
     * @param userId 用户编号
     */
    void updateClosingScheme(FmsClosingSchemeSaveReqVO updateReqVO, Long userId);

    /**
     * 更新专用结转设置
     *
     * @param updateReqVO 更新参数
     * @param userId 用户编号
     */
    void updateSpecialClosingSettings(FmsSpecialClosingSettingsSaveReqVO updateReqVO, Long userId);

    /**
     * 删除结账方案
     *
     * @param accountSetId 账套编号
     * @param id 方案编号
     * @param userId 用户编号
     */
    void deleteClosingScheme(Long accountSetId, Long id, Long userId);

}
