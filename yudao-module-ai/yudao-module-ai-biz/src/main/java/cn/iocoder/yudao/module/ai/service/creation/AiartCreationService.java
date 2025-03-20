package cn.iocoder.yudao.module.ai.service.creation;

import cn.iocoder.yudao.module.ai.controller.admin.creation.vo.style.AiartReplaceBackgroundReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.creation.vo.style.AiartReplaceBackgroundResVO;

/**
 * AI创作Service 接口
 *
 * @author zzt
 */
public interface AiartCreationService {


    /**
     * 替换商品背景图片
     * @param loginUserId 当前登录用户
     * @param replaceBackgroundReqVO 请求对象
     * @return 结果
     */
    AiartReplaceBackgroundResVO replaceBackground(Long loginUserId, AiartReplaceBackgroundReqVO replaceBackgroundReqVO);
}