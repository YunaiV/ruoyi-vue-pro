package cn.iocoder.yudao.module.promotion.service.decorate;

import cn.iocoder.yudao.module.promotion.controller.admin.decorate.vo.DecoratePageReqVO;
import cn.iocoder.yudao.module.promotion.controller.admin.decorate.vo.DecoratePageRespVO;
import cn.iocoder.yudao.module.promotion.dal.dataobject.decorate.PageDecorateDO;

import java.util.List;

/**
 * 页面装修 Service 接口
 *
 * @author jason
 */
public interface DecoratePageService {

    /**
     * 测试请求
     */
    List<PageDecorateDO> testReq(DecoratePageReqVO reqVO);

    /**
     * 测试响应
     * @param type 页面类型
     */
    DecoratePageRespVO testResp(Integer type);
}
