package cn.iocoder.yudao.module.ai.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.module.ai.ErrorCodeConstants;
import cn.iocoder.yudao.module.ai.convert.ChatRoleConvert;
import cn.iocoder.yudao.module.ai.dal.dataobject.AiChatRoleDO;
import cn.iocoder.yudao.module.ai.enums.ChatRoleClassifyEnum;
import cn.iocoder.yudao.module.ai.enums.ChatRoleSourceEnum;
import cn.iocoder.yudao.module.ai.enums.ChatRoleVisibilityEnum;
import cn.iocoder.yudao.module.ai.mapper.AiChatRoleMapper;
import cn.iocoder.yudao.module.ai.service.ChatRoleService;
import cn.iocoder.yudao.module.ai.vo.*;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

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

    private final AiChatRoleMapper aiChatRoleMapper;

    @Override
    public PageResult<ChatRoleListRes> list(ChatRoleListReq req) {
        // 查询条件
        LambdaQueryWrapperX<AiChatRoleDO> queryWrapperX = new LambdaQueryWrapperX<>();
        // search 查询
        if (!StrUtil.isBlank(req.getSearch())) {
            queryWrapperX.eq(AiChatRoleDO::getRoleName, req.getSearch());
        }
        // 默认排序id desc
        queryWrapperX.orderByDesc(AiChatRoleDO::getId);
        //
        PageResult<AiChatRoleDO> aiChatRoleDOPageResult = aiChatRoleMapper.selectPage(req, queryWrapperX);
        Long total = aiChatRoleDOPageResult.getTotal();
        List<AiChatRoleDO> roleList = aiChatRoleDOPageResult.getList();
        // 换货res
        List<ChatRoleListRes> chatRoleListResList = ChatRoleConvert.INSTANCE.convertChatRoleListRes(roleList);
        return new PageResult<>(chatRoleListResList, total);
    }

    @Override
    public void add(ChatRoleAddReq req) {
        // 转换enum，并校验enum
        ChatRoleClassifyEnum.valueOfClassify(req.getClassify());
        ChatRoleVisibilityEnum.valueOfType(req.getVisibility());
        ChatRoleSourceEnum.valueOfType(req.getRoleSource());
        // 转换do
        AiChatRoleDO insertAiChatRoleDO = ChatRoleConvert.INSTANCE.convertAiChatRoleDO(req);
        insertAiChatRoleDO.setUserId(SecurityFrameworkUtils.getLoginUserId());
        insertAiChatRoleDO.setUseCount(0);
        // 保存
        aiChatRoleMapper.insert(insertAiChatRoleDO);
    }

    @Override
    public void update(ChatRoleUpdateReq req) {
        // 转换enum，并校验enum
        ChatRoleClassifyEnum.valueOfClassify(req.getClassify());
        ChatRoleVisibilityEnum.valueOfType(req.getVisibility());
        ChatRoleSourceEnum.valueOfType(req.getRoleSource());
        // 检查角色是否存在
        AiChatRoleDO aiChatRoleDO = aiChatRoleMapper.selectById(req.getId());
        if (aiChatRoleDO == null) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.AI_CHAT_ROLE_NOT_EXIST);
        }
        // 转换do
        AiChatRoleDO updateChatRole = ChatRoleConvert.INSTANCE.convertAiChatRoleDO(req);
        aiChatRoleMapper.updateById(updateChatRole);
    }

    @Override
    public void updateVisibility(ChatRoleUpdateVisibilityReq req) {

    }

    @Override
    public void delete(Long chatRoleId) {

    }
}
