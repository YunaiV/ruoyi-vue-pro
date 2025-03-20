package cn.iocoder.yudao.module.ai.service.creation;

import cn.iocoder.yudao.module.ai.controller.admin.creation.vo.style.AiartReplaceBackgroundReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.creation.vo.style.AiartReplaceBackgroundResVO;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

/**
 * AI创作Service 实现类
 *
 * @author zzt
 */
@Service
@Validated
public class AiartCreationServiceImpl implements AiartCreationService {


    @Override
    public AiartReplaceBackgroundResVO replaceBackground(Long loginUserId, AiartReplaceBackgroundReqVO replaceBackgroundReqVO) {
        return null;
    }
}