package cn.iocoder.yudao.module.promotion.service.bargain;

import cn.iocoder.yudao.module.promotion.controller.app.bargain.vo.help.AppBargainHelpCreateReqVO;
import cn.iocoder.yudao.module.promotion.dal.dataobject.bargain.BargainHelpDO;

/**
 * 砍价助力 Service 接口
 *
 * @author 芋道源码
 */
public interface BargainHelpService {

    /**
     * 创建砍价助力（帮人砍价）
     *
     * @param userId 用户编号
     * @param reqVO 请求信息
     * @return 砍价助力记录
     */
    BargainHelpDO createBargainHelp(Long userId, AppBargainHelpCreateReqVO reqVO);

}
