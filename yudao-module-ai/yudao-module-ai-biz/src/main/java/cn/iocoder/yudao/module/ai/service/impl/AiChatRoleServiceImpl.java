package cn.iocoder.yudao.module.ai.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.module.ai.ErrorCodeConstants;
import cn.iocoder.yudao.module.ai.controller.admin.model.vo.role.*;
import cn.iocoder.yudao.module.ai.convert.AiChatRoleConvert;
import cn.iocoder.yudao.module.ai.dal.dataobject.model.AiChatRoleDO;
import cn.iocoder.yudao.module.ai.dal.mysql.AiChatRoleMapper;
import cn.iocoder.yudao.module.ai.enums.AiChatRoleCategoryEnum;
import cn.iocoder.yudao.module.ai.service.AiChatModalService;
import cn.iocoder.yudao.module.ai.service.AiChatRoleService;
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
public class AiChatRoleServiceImpl implements AiChatRoleService {

    private final AiChatRoleMapper aiChatRoleMapper;
    private final AiChatModalService aiChatModalService;

    @Override
    public PageResult<AiChatRoleListRespVO> list(AiChatRoleListReqVO req) {
        // 查询条件
        LambdaQueryWrapperX<AiChatRoleDO> queryWrapperX = new LambdaQueryWrapperX<>();
        // search 查询
        if (!StrUtil.isBlank(req.getSearch())) {
            queryWrapperX.eq(AiChatRoleDO::getName, req.getSearch());
        }
        // 默认排序id desc
        queryWrapperX.orderByDesc(AiChatRoleDO::getId);
        //
        PageResult<AiChatRoleDO> aiChatRoleDOPageResult = aiChatRoleMapper.selectPage(req, queryWrapperX);
        Long total = aiChatRoleDOPageResult.getTotal();
        List<AiChatRoleDO> roleList = aiChatRoleDOPageResult.getList();
        // 换货res
        List<AiChatRoleListRespVO> chatRoleListResList = AiChatRoleConvert.INSTANCE.convertChatRoleListRes(roleList);
        return new PageResult<>(chatRoleListResList, total);
    }

    @Override
    public void add(AiChatRoleAddReqVO req) {
        // 转换enum，并校验enum
        AiChatRoleCategoryEnum.valueOfCategory(req.getCategory());
        // 校验模型是否存在
        aiChatModalService.validateExists(req.getModelId());
        // 转换do
        AiChatRoleDO insertAiChatRoleDO = AiChatRoleConvert.INSTANCE.convertAiChatRoleDO(req);
        insertAiChatRoleDO.setUserId(SecurityFrameworkUtils.getLoginUserId());
        insertAiChatRoleDO.setStatus(CommonStatusEnum.ENABLE.getStatus());
        // 保存
        aiChatRoleMapper.insert(insertAiChatRoleDO);
    }

    @Override
    public void update(AiChatRoleUpdateReqVO req) {
        // 检查角色是否存在
        validateExists(req.getId());
        // 转换enum，并校验enum
        AiChatRoleCategoryEnum.valueOfCategory(req.getCategory());
        // 校验模型是否存在
        aiChatModalService.validateExists(req.getModelId());
        // 转换do
        AiChatRoleDO updateChatRole = AiChatRoleConvert.INSTANCE.convertAiChatRoleDO(req);
        updateChatRole.setId(req.getId());
        aiChatRoleMapper.updateById(updateChatRole);
    }


    @Override
    public void updatePublicStatus(AiChatRoleUpdatePublicStatusReqVO req) {
        // 检查角色是否存在
        validateExists(req.getId());
        // 更新
        aiChatRoleMapper.updateById(
                new AiChatRoleDO()
                        .setId(req.getId())
                        .setPublicStatus(req.getPublicStatus())
        );
    }

    @Override
    public void delete(Long chatRoleId) {
        // 检查角色是否存在
        validateExists(chatRoleId);
        // 删除
        aiChatRoleMapper.deleteById(chatRoleId);
    }

    @Override
    public AiChatRoleRespVO getChatRole(Long roleId) {
        // 检查角色是否存在
        AiChatRoleDO aiChatRoleDO = validateExists(roleId);
        return AiChatRoleConvert.INSTANCE.convertAiChatRoleRes(aiChatRoleDO);
    }

    public AiChatRoleDO validateExists(Long id) {
        AiChatRoleDO aiChatRoleDO = aiChatRoleMapper.selectById(id);
        if (aiChatRoleDO == null) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.AI_CHAT_ROLE_NOT_EXIST);
        }
        return aiChatRoleDO;
    }

    public void validateIsPublic(AiChatRoleDO aiChatRoleDO) {
        if (aiChatRoleDO == null) {
            return;
        }
        if (!aiChatRoleDO.getPublicStatus()) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.AI_CHAT_ROLE_NOT_PUBLIC);
        }
    }
}

