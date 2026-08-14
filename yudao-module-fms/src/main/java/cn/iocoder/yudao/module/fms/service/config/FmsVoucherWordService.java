package cn.iocoder.yudao.module.fms.service.config;

import cn.iocoder.yudao.module.fms.controller.admin.config.vo.voucherword.FmsVoucherWordSaveReqVO;
import cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsVoucherWordDO;

import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;

/**
 * FMS 凭证字 Service 接口
 *
 * @author 芋道源码
 */
public interface FmsVoucherWordService {

    /**
     * 创建凭证字
     *
     * @param createReqVO 创建信息
     * @param userId 当前用户编号
     * @return 凭证字编号
     */
    Long createVoucherWord(FmsVoucherWordSaveReqVO createReqVO, Long userId);

    /**
     * 修改凭证字
     *
     * @param updateReqVO 修改信息
     * @param userId 当前用户编号
     */
    void updateVoucherWord(FmsVoucherWordSaveReqVO updateReqVO, Long userId);

    /**
     * 删除凭证字
     *
     * @param accountSetId 账套编号
     * @param id 凭证字编号
     * @param userId 当前用户编号
     */
    void deleteVoucherWord(Long accountSetId, Long id, Long userId);

    /**
     * 初始化账套默认凭证字
     *
     * @param accountSetId 账套编号
     */
    void initializeDefaultVoucherWords(Long accountSetId);

    /**
     * 获得账套凭证字列表
     *
     * @param accountSetId 账套编号
     * @return 凭证字列表
     */
    List<FmsVoucherWordDO> getVoucherWordList(Long accountSetId);

    /**
     * 获得账套凭证字 Map
     *
     * @param accountSetId 账套编号
     * @return 凭证字 Map
     */
    default Map<Long, FmsVoucherWordDO> getVoucherWordMap(Long accountSetId) {
        return convertMap(getVoucherWordList(accountSetId), FmsVoucherWordDO::getId);
    }

    /**
     * 获得账套凭证字 Map
     *
     * @param accountSetId 账套编号
     * @return 凭证字 Map，key 为凭证字名称
     */
    default Map<String, FmsVoucherWordDO> getVoucherWordMapByName(Long accountSetId) {
        return convertMap(getVoucherWordList(accountSetId), FmsVoucherWordDO::getName);
    }

    /**
     * 获得账套凭证字列表
     *
     * @param accountSetId 账套编号
     * @param userId 当前用户编号
     * @return 凭证字列表
     */
    List<FmsVoucherWordDO> getVoucherWordList(Long accountSetId, Long userId);

    /**
     * 校验并获得凭证字
     *
     * @param accountSetId 账套编号
     * @param id 凭证字编号
     * @return 凭证字
     */
    FmsVoucherWordDO validateVoucherWordExists(Long accountSetId, Long id);

}
