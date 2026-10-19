package cn.iocoder.yudao.module.pms.service.pm.workitem;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.pms.controller.admin.pm.workitem.vo.label.PmsWorkItemLabelSaveReqVO;
import cn.iocoder.yudao.module.pms.dal.dataobject.pm.workitem.PmsWorkItemLabelDO;
import cn.iocoder.yudao.module.pms.dal.mysql.pm.workitem.PmsWorkItemLabelMapper;
import javax.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.pms.enums.ErrorCodeConstants.WORK_ITEM_LABELS_INVALID;
import static cn.iocoder.yudao.module.pms.enums.ErrorCodeConstants.WORK_ITEM_LABEL_NOT_EXISTS;

/**
 * PMS 工作项标签 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class PmsWorkItemLabelServiceImpl implements PmsWorkItemLabelService {

    @Resource
    private PmsWorkItemLabelMapper labelMapper;

    @Override
    public Long createWorkItemLabel(PmsWorkItemLabelSaveReqVO saveReqVO) {
        PmsWorkItemLabelDO label = BeanUtils.toBean(saveReqVO, PmsWorkItemLabelDO.class)
                .setName(StrUtil.trim(saveReqVO.getName())).setColor(StrUtil.trim(saveReqVO.getColor()));
        labelMapper.insert(label);
        return label.getId();
    }

    @Override
    public void updateWorkItemLabel(PmsWorkItemLabelSaveReqVO saveReqVO) {
        // 1. 校验标签存在
        validateLabelExists(saveReqVO.getId());

        // 2. 更新标签
        labelMapper.updateById(BeanUtils.toBean(saveReqVO, PmsWorkItemLabelDO.class)
                .setName(StrUtil.trim(saveReqVO.getName())).setColor(StrUtil.trim(saveReqVO.getColor())));
    }

    @Override
    public void deleteWorkItemLabel(Long id) {
        // 1. 校验标签存在
        validateLabelExists(id);

        // 2. 删除标签；工作项中的历史编号在展示时自动忽略
        labelMapper.deleteById(id);
    }

    @Override
    public List<PmsWorkItemLabelDO> getWorkItemLabelList(String name) {
        return labelMapper.selectListByName(name);
    }

    @Override
    public List<PmsWorkItemLabelDO> getWorkItemLabelList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return labelMapper.selectByIds(ids);
    }

    @Override
    public void validateWorkItemLabelIds(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return;
        }
        if (labelMapper.selectByIds(ids).size() != ids.size()) {
            throw exception(WORK_ITEM_LABELS_INVALID);
        }
    }

    /**
     * 校验工作项标签存在
     *
     * @param id 标签编号
     */
    private void validateLabelExists(Long id) {
        if (labelMapper.selectById(id) == null) {
            throw exception(WORK_ITEM_LABEL_NOT_EXISTS);
        }
    }

}
