package cn.iocoder.yudao.module.promotion.service.decorate;

import cn.iocoder.yudao.module.promotion.controller.admin.decorate.vo.DecorateComponentSaveReqVO;
import cn.iocoder.yudao.module.promotion.dal.dataobject.decorate.DecorateComponentDO;
import cn.iocoder.yudao.module.promotion.enums.decorate.DecoratePageEnum;

import java.util.List;

/**
 * 装修组件 Service 接口
 *
 * @author jason
 */
public interface DecorateComponentService {

    /**
     * 店铺装修页面保存
     *
     * @param reqVO 请求 VO
     */
    void savePageComponents(DecorateComponentSaveReqVO reqVO);

    /**
     * 根据页面 id。获取页面的组件信息
     *
     * @param pageId 页面类型 {@link DecoratePageEnum#getId()}
     */
    List<DecorateComponentDO> getPageComponents(Integer pageId);

}
