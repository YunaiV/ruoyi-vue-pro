package cn.iocoder.yudao.module.ai.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.validation.ValidationUtil;
import cn.iocoder.yudao.framework.ai.AiPlatformEnum;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.ai.ErrorCodeConstants;
import cn.iocoder.yudao.module.ai.controller.admin.model.vo.model.*;
import cn.iocoder.yudao.module.ai.convert.AiChatModelConvert;
import cn.iocoder.yudao.module.ai.dal.dataobject.model.AiChatModelDO;
import cn.iocoder.yudao.module.ai.dal.mysql.AiChatModelMapper;
import cn.iocoder.yudao.module.ai.dal.vo.AiChatModalConfigVO;
import cn.iocoder.yudao.module.ai.service.AiChatModelService;
import jakarta.validation.ConstraintViolation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScannerRegistrar;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

/**
 * ai 模型
 *
 * @author fansili
 * @time 2024/4/24 19:42
 * @since 1.0
 */
@AllArgsConstructor
@Service
@Slf4j
public class AiChatModalServiceImpl implements AiChatModelService {

    private final AiChatModelMapper aiChatModelMapper;
    private final MapperScannerRegistrar mapperScannerRegistrar;

    @Override
    public PageResult<AiChatModelListRespVO> list(AiChatModelListReqVO req) {
        LambdaQueryWrapperX<AiChatModelDO> queryWrapperX = new LambdaQueryWrapperX<>();
        // 查询的都是未禁用的模型
        queryWrapperX.eq(AiChatModelDO::getStatus, CommonStatusEnum.ENABLE.getStatus());
        // search
        if (!StrUtil.isBlank(req.getSearch())) {
            queryWrapperX.like(AiChatModelDO::getName, req.getSearch().trim());
        }
        // 默认排序
        queryWrapperX.orderByAsc(AiChatModelDO::getSort);
        // 查询
        PageResult<AiChatModelDO> aiChatModalDOPageResult = aiChatModelMapper.selectPage(req, queryWrapperX);
        // 转换 res
        List<AiChatModelListRespVO> resList = AiChatModelConvert.INSTANCE.convertAiChatModalListRes(aiChatModalDOPageResult.getList());
        return new PageResult<>(resList, aiChatModalDOPageResult.getTotal());
    }

    @Override
    public void add(AiChatModelAddReqVO req) {
        // 校验 platform、type
        validatePlatform(req.getPlatform());
        // 转换 do
        AiChatModelDO insertChatModalDO = AiChatModelConvert.INSTANCE.convertAiChatModalDO(req);
        // 设置默认属性
        insertChatModalDO.setStatus(CommonStatusEnum.ENABLE.getStatus());
        // 保存数据库
        aiChatModelMapper.insert(insertChatModalDO);
    }

    @Override
    public void update(AiChatModelUpdateReqVO req) {
        // 校验 platform
        validatePlatform(req.getPlatform());
        // 校验模型是否存在
        validateExists(req.getId());
        // 转换 updateChatModalDO
        AiChatModelDO updateChatModalDO = AiChatModelConvert.INSTANCE.convertAiChatModalDO(req);
        updateChatModalDO.setId(req.getId());
        // 更新数据库
        aiChatModelMapper.updateById(updateChatModalDO);
    }

    @Override
    public void delete(Long id) {
        // 检查 modal 是否存在
        validateExists(id);
        // 删除 delete
        aiChatModelMapper.deleteById(id);
    }

    @Override
    public AiChatModalRespVO getChatModalOfValidate(Long modalId) {
        // 检查 modal 是否存在
        AiChatModelDO aiChatModalDO = validateExists(modalId);
        return AiChatModelConvert.INSTANCE.convertAiChatModalRes(aiChatModalDO);
    }

    @Override
    public void validateAvailable(AiChatModalRespVO chatModal) {
        // 对话模型是否可用
        if (!CommonStatusEnum.ENABLE.getStatus().equals(chatModal.getStatus())) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.AI_MODAL_DISABLE_NOT_USED);
        }
    }

    @Override
    public List<AiChatModelDO> getModalByIds(Set<Long> modalIds) {
        return aiChatModelMapper.selectByIds(modalIds);
    }

    public AiChatModelDO validateExists(Long id) {
        AiChatModelDO aiChatModalDO = aiChatModelMapper.selectById(id);
        if (aiChatModalDO == null) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.AI_MODAL_NOT_EXIST);
        }
        return aiChatModalDO;
    }

    private void validatePlatform(String platform) {
        try {
            AiPlatformEnum.valueOfPlatform(platform);
        } catch (IllegalArgumentException e) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.AI_MODAL_PLATFORM_PARAMS_INCORRECT, e.getMessage());
        }
    }

    private void validateModalConfig(AiChatModalConfigVO aiChatModalConfigVO) {
        Set<ConstraintViolation<AiChatModalConfigVO>> validate = ValidationUtil.validate(aiChatModalConfigVO);
        for (ConstraintViolation<AiChatModalConfigVO> constraintViolation : validate) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.AI_MODAL_CONFIG_PARAMS_INCORRECT, constraintViolation.getMessage());
        }
    }
}
