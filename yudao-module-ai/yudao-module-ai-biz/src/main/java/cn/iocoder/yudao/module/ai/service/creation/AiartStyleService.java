package cn.iocoder.yudao.module.ai.service.creation;

import cn.iocoder.yudao.module.ai.controller.admin.creation.vo.style.BackgroundTemplateReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.creation.vo.style.BackgroundTemplateResVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.creation.AiartStyleBackgroundTemplateDO;

import java.util.List;

/**
 * AI风格Service 接口
 *
 * @author zzt
 */
public interface AiartStyleService {


    /**
     * 查询商品背景模版列表
     * @param backgroundTemplateReqVO 商品背景模版请求对象
     * @return 结果
     */
    List<AiartStyleBackgroundTemplateDO> queryBackgroundStyle(BackgroundTemplateReqVO backgroundTemplateReqVO);
}