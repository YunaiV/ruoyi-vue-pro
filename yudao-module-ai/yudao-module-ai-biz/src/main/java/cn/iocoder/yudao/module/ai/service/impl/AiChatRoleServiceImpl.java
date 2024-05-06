package cn.iocoder.yudao.module.ai.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.module.ai.ErrorCodeConstants;
import cn.iocoder.yudao.module.ai.convert.AiChatRoleConvert;
import cn.iocoder.yudao.module.ai.dal.dataobject.model.AiChatRoleDO;
import cn.iocoder.yudao.module.ai.enums.AiChatRoleClassifyEnum;
import cn.iocoder.yudao.module.ai.enums.AiChatRoleEnableEnum;
import cn.iocoder.yudao.module.ai.mapper.AiChatRoleMapper;
import cn.iocoder.yudao.module.ai.service.AiChatRoleService;
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
public class AiChatRoleServiceImpl implements AiChatRoleService {

    private final AiChatRoleMapper aiChatRoleMapper;

    @Override
    public PageResult<AiChatRoleListRes> list(AiChatRoleListReq req) {
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
        List<AiChatRoleListRes> chatRoleListResList = AiChatRoleConvert.INSTANCE.convertChatRoleListRes(roleList);
        return new PageResult<>(chatRoleListResList, total);
    }

    @Override
    public void add(AiChatRoleAddReq req) {
        // 转换enum，并校验enum
        AiChatRoleClassifyEnum.valueOfClassify(req.getClassify());
        AiChatRoleEnableEnum.valueOfType(req.getEnable());
        // 转换do
        AiChatRoleDO insertAiChatRoleDO = AiChatRoleConvert.INSTANCE.convertAiChatRoleDO(req);
        insertAiChatRoleDO.setUserId(SecurityFrameworkUtils.getLoginUserId());
        insertAiChatRoleDO.setUseCount(0);
        // 保存
        aiChatRoleMapper.insert(insertAiChatRoleDO);
    }

    @Override
    public void update(Long id, AiChatRoleUpdateReq req) {
        // 转换enum，并校验enum
        AiChatRoleClassifyEnum.valueOfClassify(req.getClassify());
        AiChatRoleEnableEnum.valueOfType(req.getEnable());
        // 检查角色是否存在
        validateChatRoleExists(id);
        // 转换do
        AiChatRoleDO updateChatRole = AiChatRoleConvert.INSTANCE.convertAiChatRoleDO(req);
        updateChatRole.setId(id);
        aiChatRoleMapper.updateById(updateChatRole);
    }


    @Override
    public void updateEnable(Long id, AiChatRoleUpdateVisibilityReq req) {
        // 转换enum，并校验enum
        AiChatRoleEnableEnum.valueOfType(req.getEnable());
        // 检查角色是否存在
        validateChatRoleExists(id);
        // 更新
        aiChatRoleMapper.updateById(new AiChatRoleDO()
                .setId(id)
                .setEnable(req.getEnable())
        );
    }

    @Override
    public void delete(Long chatRoleId) {
        // 检查角色是否存在
        validateChatRoleExists(chatRoleId);
        // 删除
        aiChatRoleMapper.deleteById(chatRoleId);
    }

    @Override
    public AiChatRoleRes getChatRole(Long roleId) {
        // 检查角色是否存在
        AiChatRoleDO aiChatRoleDO = validateChatRoleExists(roleId);
        return AiChatRoleConvert.INSTANCE.convertAiChatRoleRes(aiChatRoleDO);
    }

    private AiChatRoleDO validateChatRoleExists(Long id) {
        AiChatRoleDO aiChatRoleDO = aiChatRoleMapper.selectById(id);
        if (aiChatRoleDO == null) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.AI_CHAT_ROLE_NOT_EXIST);
        }
        return aiChatRoleDO;
    }
}
