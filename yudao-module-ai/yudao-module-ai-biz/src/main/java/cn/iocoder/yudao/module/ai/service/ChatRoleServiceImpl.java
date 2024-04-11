package cn.iocoder.yudao.module.ai.service;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.ai.controller.app.vo.*;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * chat 角色
 *
 * @fansili
 * @since v1.0
 */
@Service
@AllArgsConstructor
@Slf4j
public class ChatRoleServiceImpl implements ChatRoleService {


    @Override
    public CommonResult<ChatRoleListRes> list(ChatRoleListReq req) {
        return null;
    }

    @Override
    public ChatRoleListRes add(ChatRoleAddReq req) {
        return null;
    }

    @Override
    public void update(ChatRoleUpdateReq req) {

    }

    @Override
    public void updateVisibility(ChatRoleUpdateVisibilityReq req) {

    }

    @Override
    public void delete(Long chatRoleId) {

    }
}
