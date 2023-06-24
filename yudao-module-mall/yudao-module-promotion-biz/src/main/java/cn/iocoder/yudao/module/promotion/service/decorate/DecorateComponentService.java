package cn.iocoder.yudao.module.promotion.service.decorate;

import cn.iocoder.yudao.module.promotion.controller.admin.decorate.vo.DecorateComponentReqVO;
import cn.iocoder.yudao.module.promotion.dal.dataobject.decorate.DecorateComponentDO;
import cn.iocoder.yudao.module.promotion.enums.decorate.DecoratePageTypeEnum;

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
    void pageSave(DecorateComponentReqVO reqVO);

    /**
     * 根据页面类型。获取页面的组件信息
     *
     * @param type 页面类型 {@link DecoratePageTypeEnum#getType()}
     */
    List<DecorateComponentDO> getPageComponents(Integer type);

}
