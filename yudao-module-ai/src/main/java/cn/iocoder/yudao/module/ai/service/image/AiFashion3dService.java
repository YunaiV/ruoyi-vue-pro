package cn.iocoder.yudao.module.ai.service.image;

import cn.iocoder.yudao.module.ai.controller.admin.image.vo.fashion.AiFashion3dConvertReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.image.vo.fashion.AiFashion3dResultRespVO;

/**
 * AI 服装 3D 转换服务接口
 *
 * @author deepay
 */
public interface AiFashion3dService {

    /**
     * 触发 2D → 3D 异步转换，立即返回
     *
     * @param userId 当前用户ID
     * @param reqVO  请求参数
     * @return 资产信息（status=PROCESSING）
     */
    AiFashion3dResultRespVO convert(Long userId, AiFashion3dConvertReqVO reqVO);

    /**
     * 查询 3D 资产结果
     *
     * @param assetId 资产编号
     * @return 资产结果
     */
    AiFashion3dResultRespVO getResult(Long assetId);

    /**
     * 对已有资产重新应用颜色变换
     *
     * @param assetId  资产编号
     * @param colorHex 目标颜色 Hex（如 "#FF0000"）
     * @return 新资产结果
     */
    AiFashion3dResultRespVO changeColor(Long assetId, String colorHex);

}
