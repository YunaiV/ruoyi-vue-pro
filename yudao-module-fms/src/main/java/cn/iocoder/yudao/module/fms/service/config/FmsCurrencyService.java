package cn.iocoder.yudao.module.fms.service.config;

import cn.iocoder.yudao.module.fms.controller.admin.config.vo.currency.FmsCurrencySaveReqVO;
import cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsCurrencyDO;
import cn.iocoder.yudao.module.fms.enums.config.FmsCurrencyPresetEnum;

import java.util.List;

/**
 * FMS 币别 Service 接口
 *
 * @author 芋道源码
 */
public interface FmsCurrencyService {

    /**
     * 初始化本位币
     *
     * @param accountSetId 账套编号
     * @param presetCurrency 预置币别
     * @return 本位币
     */
    FmsCurrencyDO initializeStandardCurrency(Long accountSetId, FmsCurrencyPresetEnum presetCurrency);

    /**
     * 创建币别
     *
     * @param createReqVO 创建信息
     * @param userId 用户编号
     * @return 币别编号
     */
    Long createCurrency(FmsCurrencySaveReqVO createReqVO, Long userId);

    /**
     * 更新币别
     *
     * @param updateReqVO 更新信息
     * @param userId 用户编号
     */
    void updateCurrency(FmsCurrencySaveReqVO updateReqVO, Long userId);

    /**
     * 删除币别
     *
     * @param accountSetId 账套编号
     * @param id 币别编号
     * @param userId 用户编号
     */
    void deleteCurrency(Long accountSetId, Long id, Long userId);

    /**
     * 获得币别列表
     *
     * @param accountSetId 账套编号
     * @param userId 用户编号
     * @return 币别列表
     */
    List<FmsCurrencyDO> getCurrencyList(Long accountSetId, Long userId);

    /**
     * 获得币别
     *
     * @param accountSetId 账套编号
     * @param id 币别编号
     * @param userId 用户编号
     * @return 币别
     */
    FmsCurrencyDO getCurrency(Long accountSetId, Long id, Long userId);

    /**
     * 校验并获得币别列表
     *
     * @param accountSetId 账套编号
     * @param ids 币别编号数组
     * @return 币别列表
     */
    List<FmsCurrencyDO> validateCurrencyList(Long accountSetId, List<Long> ids);

}
