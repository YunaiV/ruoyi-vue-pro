package cn.iocoder.yudao.module.fms.service.config;

import cn.iocoder.yudao.module.fms.controller.admin.config.vo.digest.FmsDigestSaveReqVO;
import cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsDigestDO;

import java.util.List;

/**
 * FMS 常用摘要 Service 接口
 *
 * @author 芋道源码
 */
public interface FmsDigestService {

    /**
     * 创建常用摘要
     *
     * @param createReqVO 创建信息
     * @param userId 当前用户编号
     * @return 常用摘要编号
     */
    Long createDigest(FmsDigestSaveReqVO createReqVO, Long userId);

    /**
     * 修改常用摘要
     *
     * @param updateReqVO 修改信息
     * @param userId 当前用户编号
     */
    void updateDigest(FmsDigestSaveReqVO updateReqVO, Long userId);

    /**
     * 删除常用摘要
     *
     * @param accountSetId 账套编号
     * @param id 常用摘要编号
     * @param userId 当前用户编号
     */
    void deleteDigest(Long accountSetId, Long id, Long userId);

    /**
     * 获得账套常用摘要列表
     *
     * @param accountSetId 账套编号
     * @param userId 当前用户编号
     * @return 常用摘要列表
     */
    List<FmsDigestDO> getDigestList(Long accountSetId, Long userId);

    /**
     * 校验并获得常用摘要
     *
     * @param accountSetId 账套编号
     * @param id 常用摘要编号
     * @return 常用摘要
     */
    FmsDigestDO validateDigestExists(Long accountSetId, Long id);

}
