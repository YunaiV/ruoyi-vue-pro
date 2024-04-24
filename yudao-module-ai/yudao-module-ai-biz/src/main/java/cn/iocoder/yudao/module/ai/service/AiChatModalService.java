package cn.iocoder.yudao.module.ai.service;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.ai.vo.AiChatModalAddReq;
import cn.iocoder.yudao.module.ai.vo.AiChatModalListReq;
import cn.iocoder.yudao.module.ai.vo.AiChatModalListRes;

/**
 * ai modal
 *
 * @author fansili
 * @time 2024/4/24 19:42
 * @since 1.0
 */
public interface AiChatModalService {

    PageResult<AiChatModalListRes> list(AiChatModalListReq req);

    void add(AiChatModalAddReq req);
}
