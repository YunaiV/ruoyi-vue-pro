package cn.iocoder.yudao.module.mes.service.tm.tool;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.mes.controller.admin.tm.tool.vo.type.MesTmToolTypePageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.tm.tool.vo.type.MesTmToolTypeSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.tm.tool.MesTmToolTypeDO;
import cn.iocoder.yudao.module.mes.dal.mysql.tm.tool.MesTmToolMapper;
import cn.iocoder.yudao.module.mes.dal.mysql.tm.tool.MesTmToolTypeMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import cn.hutool.core.util.ObjUtil;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.mes.enums.ErrorCodeConstants.*;

/**
 * MES 工具类型 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class MesTmToolTypeServiceImpl implements MesTmToolTypeService {

    @Resource
    private MesTmToolTypeMapper toolTypeMapper;

    @Resource
    private MesTmToolMapper toolMapper;

    @Override
    public Long createToolType(MesTmToolTypeSaveReqVO createReqVO) {
        // 校验数据
        validateToolTypeSaveData(null, createReqVO);

        // 插入
        MesTmToolTypeDO toolType = BeanUtils.toBean(createReqVO, MesTmToolTypeDO.class);
        toolTypeMapper.insert(toolType);
        return toolType.getId();
    }

    @Override
    public void updateToolType(MesTmToolTypeSaveReqVO updateReqVO) {
        // 校验存在
        validateToolTypeExists(updateReqVO.getId());
        // 校验数据
        validateToolTypeSaveData(updateReqVO.getId(), updateReqVO);

        // 更新
        MesTmToolTypeDO updateObj = BeanUtils.toBean(updateReqVO, MesTmToolTypeDO.class);
        toolTypeMapper.updateById(updateObj);
    }

    private void validateToolTypeSaveData(Long id, MesTmToolTypeSaveReqVO saveReqVO) {
        // 校验编码唯一
        validateToolTypeCodeUnique(id, saveReqVO.getCode());
        // 校验名称唯一
        validateToolTypeNameUnique(id, saveReqVO.getName());
    }

    @Override
    public void deleteToolType(Long id) {
        // 校验存在
        validateToolTypeExists(id);
        // 校验是否被工具引用
        if (toolMapper.selectCountByToolTypeId(id) > 0) {
            throw exception(TM_TOOL_TYPE_HAS_TOOL);
        }

        // 删除
        toolTypeMapper.deleteById(id);
    }

    @Override
    public void validateToolTypeExists(Long id) {
        if (toolTypeMapper.selectById(id) == null) {
            throw exception(TM_TOOL_TYPE_NOT_EXISTS);
        }
    }

    private void validateToolTypeCodeUnique(Long id, String code) {
        MesTmToolTypeDO toolType = toolTypeMapper.selectByCode(code);
        if (toolType == null) {
            return;
        }
        if (ObjUtil.notEqual(toolType.getId(), id)) {
            throw exception(TM_TOOL_TYPE_CODE_DUPLICATE);
        }
    }

    private void validateToolTypeNameUnique(Long id, String name) {
        MesTmToolTypeDO toolType = toolTypeMapper.selectByName(name);
        if (toolType == null) {
            return;
        }
        if (ObjUtil.notEqual(toolType.getId(), id)) {
            throw exception(TM_TOOL_TYPE_NAME_DUPLICATE);
        }
    }

    @Override
    public MesTmToolTypeDO getToolType(Long id) {
        return toolTypeMapper.selectById(id);
    }

    @Override
    public PageResult<MesTmToolTypeDO> getToolTypePage(MesTmToolTypePageReqVO pageReqVO) {
        return toolTypeMapper.selectPage(pageReqVO);
    }

    @Override
    public List<MesTmToolTypeDO> getToolTypeList() {
        return toolTypeMapper.selectList();
    }

    @Override
    public List<MesTmToolTypeDO> getToolTypeList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return toolTypeMapper.selectByIds(ids);
    }

}
