package cn.iocoder.yudao.module.ai.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.validation.ValidationUtil;
import cn.iocoder.yudao.framework.ai.AiPlatformEnum;
import cn.iocoder.yudao.framework.ai.chatqianwen.QianWenChatModal;
import cn.iocoder.yudao.framework.ai.chatxinghuo.XingHuoChatModel;
import cn.iocoder.yudao.framework.ai.chatyiyan.YiYanChatModel;
import cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.ai.ErrorCodeConstants;
import cn.iocoder.yudao.module.ai.convert.AiChatModalConvert;
import cn.iocoder.yudao.module.ai.dal.dataobject.model.AiChatModalDO;
import cn.iocoder.yudao.module.ai.dal.vo.AiChatModalChatConfigVO;
import cn.iocoder.yudao.module.ai.dal.vo.AiChatModalConfigVO;
import cn.iocoder.yudao.module.ai.dal.vo.AiChatModalDallConfigVO;
import cn.iocoder.yudao.module.ai.enums.AiChatModalDisableEnum;
import cn.iocoder.yudao.module.ai.dal.mysql.AiChatModalMapper;
import cn.iocoder.yudao.module.ai.service.AiChatModalService;
import cn.iocoder.yudao.module.ai.controller.admin.model.vo.model.AiChatModalAddReq;
import cn.iocoder.yudao.module.ai.controller.admin.model.vo.model.AiChatModalListReq;
import cn.iocoder.yudao.module.ai.controller.admin.model.vo.model.AiChatModalListRes;
import cn.iocoder.yudao.module.ai.controller.admin.model.vo.model.AiChatModalRes;
import jakarta.validation.ConstraintViolation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
public class AiChatModalServiceImpl implements AiChatModalService {

    private final AiChatModalMapper aiChatModalMapper;

    @Override
    public PageResult<AiChatModalListRes> list(AiChatModalListReq req) {
        LambdaQueryWrapperX<AiChatModalDO> queryWrapperX = new LambdaQueryWrapperX<>();
        // 查询的都是未禁用的模型
        queryWrapperX.eq(AiChatModalDO::getDisable, AiChatModalDisableEnum.NO.getValue());
        // search
        if (!StrUtil.isBlank(req.getSearch())) {
            queryWrapperX.like(AiChatModalDO::getName, req.getSearch().trim());
        }
        // 默认排序
        queryWrapperX.orderByAsc(AiChatModalDO::getSort);
        // 查询
        PageResult<AiChatModalDO> aiChatModalDOPageResult = aiChatModalMapper.selectPage(req, queryWrapperX);
        // 转换 res
        List<AiChatModalListRes> resList = AiChatModalConvert.INSTANCE.convertAiChatModalListRes(aiChatModalDOPageResult.getList());
        return new PageResult<>(resList, aiChatModalDOPageResult.getTotal());
    }

    @Override
    public void add(AiChatModalAddReq req) {
        // 校验 platform、type
        validatePlatform(req.getPlatform());
        validateModal(req.getPlatform(), req.getModal());
        // 转换config
        AiChatModalConfigVO aiChatModalConfigVO = convertConfig(req);
        // 校验 modal config
        validateModalConfig(aiChatModalConfigVO);
        // 转换 do
        AiChatModalDO insertChatModalDO = AiChatModalConvert.INSTANCE.convertAiChatModalDO(req);
        // 设置默认属性
        insertChatModalDO.setDisable(AiChatModalDisableEnum.NO.getValue());
        insertChatModalDO.setConfig(JsonUtils.toJsonString(aiChatModalConfigVO));
        // 保存数据库
        aiChatModalMapper.insert(insertChatModalDO);
    }

    @Override
    public void update(Long id, AiChatModalAddReq req) {
        // 校验 platform、type
        validatePlatform(req.getPlatform());
        validateModal(req.getPlatform(), req.getModal());
        // 转换config
        AiChatModalConfigVO aiChatModalConfigVO = convertConfig(req);
        // 校验 modal config
        validateModalConfig(aiChatModalConfigVO);

        // 校验模型是否存在
        validateChatModalExists(id);
        // 转换 updateChatModalDO
        AiChatModalDO updateChatModalDO = AiChatModalConvert.INSTANCE.convertAiChatModalDO(req);
        updateChatModalDO.setId(id);
        updateChatModalDO.setConfig(JsonUtils.toJsonString(aiChatModalConfigVO));
        // 更新数据库
        aiChatModalMapper.updateById(updateChatModalDO);
    }

    @Override
    public void delete(Long id) {
        // 检查 modal 是否存在
        validateChatModalExists(id);
        // 删除 delete
        aiChatModalMapper.deleteById(id);
    }

    @Override
    public AiChatModalRes getChatModalOfValidate(Long modalId) {
        // 检查 modal 是否存在
        AiChatModalDO aiChatModalDO = validateChatModalExists(modalId);
        return AiChatModalConvert.INSTANCE.convertAiChatModalRes(aiChatModalDO);
    }

    private AiChatModalDO validateChatModalExists(Long id) {
        AiChatModalDO aiChatModalDO = aiChatModalMapper.selectById(id);
        if (aiChatModalDO == null) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.AI_MODAL_NOT_EXIST);
        }
        return aiChatModalDO;
    }

    private void validateModal(String platform, String modal) {
        AiPlatformEnum platformEnum = AiPlatformEnum.valueOfPlatform(platform);
        try {
            if (AiPlatformEnum.QIAN_WEN == platformEnum) {
                QianWenChatModal.valueOfModel(modal);
            } else if (AiPlatformEnum.XING_HUO == platformEnum) {
                XingHuoChatModel.valueOfModel(modal);
            } else if (AiPlatformEnum.YI_YAN == platformEnum) {
                YiYanChatModel.valueOfModel(modal);
            } else {
                throw ServiceExceptionUtil.exception(ErrorCodeConstants.AI_MODAL_NOT_SUPPORTED_MODAL, platform);
            }
        } catch (IllegalArgumentException e) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.AI_MODAL_CONFIG_PARAMS_INCORRECT, e.getMessage());
        }
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

    private static AiChatModalConfigVO convertConfig(AiChatModalAddReq req) {
        AiPlatformEnum platformEnum = AiPlatformEnum.valueOfPlatform(req.getPlatform());
        AiChatModalConfigVO resVo = null;
        if (AiPlatformEnum.CHAT_PLATFORM_LIST.contains(platformEnum)) {
            resVo = JsonUtils.parseObject(JsonUtils.toJsonString(req.getConfig()), AiChatModalChatConfigVO.class);
        } else if (AiPlatformEnum.OPEN_AI_DALL == platformEnum) {
            resVo = JsonUtils.parseObject(JsonUtils.toJsonString(req.getConfig()), AiChatModalDallConfigVO.class);
        }
        if (resVo == null) {
            throw new IllegalArgumentException("ai模型中config不能转换! json: " + req.getConfig());
        }
        resVo.setType(req.getModal());
        resVo.setPlatform(req.getPlatform());
        return resVo;
    }
}
