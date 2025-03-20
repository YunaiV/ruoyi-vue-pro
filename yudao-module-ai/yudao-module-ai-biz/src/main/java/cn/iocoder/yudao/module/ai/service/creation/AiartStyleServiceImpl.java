package cn.iocoder.yudao.module.ai.service.creation;

import cn.iocoder.yudao.module.ai.controller.admin.creation.vo.style.BackgroundTemplateReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.creation.vo.style.BackgroundTemplateResVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.creation.AiartStyleBackgroundTemplateDO;
import cn.iocoder.yudao.module.ai.dal.mysql.creation.AiartStyleBackgroundTemplateMapper;
import cn.iocoder.yudao.module.ai.dal.mysql.model.AiApiKeyMapper;
import jakarta.annotation.Resource;

import java.util.List;

/**
 * AI风格Service 接口
 *
 * @author zzt
 */
public class AiartStyleServiceImpl implements AiartStyleService{

    @Resource
    private AiartStyleBackgroundTemplateMapper backgroundTemplateMapper;


    @Override
    public List<AiartStyleBackgroundTemplateDO> queryBackgroundStyle(BackgroundTemplateReqVO backgroundTemplateReqVO) {
        return backgroundTemplateMapper.selectList(backgroundTemplateReqVO);
    }
}