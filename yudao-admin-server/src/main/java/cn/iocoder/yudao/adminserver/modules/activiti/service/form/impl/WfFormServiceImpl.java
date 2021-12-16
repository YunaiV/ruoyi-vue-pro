package cn.iocoder.yudao.adminserver.modules.activiti.service.form.impl;

import cn.iocoder.yudao.adminserver.modules.activiti.controller.form.vo.WfFormCreateReqVO;
import cn.iocoder.yudao.adminserver.modules.activiti.controller.form.vo.WfFormExportReqVO;
import cn.iocoder.yudao.adminserver.modules.activiti.controller.form.vo.WfFormPageReqVO;
import cn.iocoder.yudao.adminserver.modules.activiti.controller.form.vo.WfFormUpdateReqVO;
import cn.iocoder.yudao.adminserver.modules.activiti.convert.form.WfFormConvert;
import cn.iocoder.yudao.adminserver.modules.activiti.dal.dataobject.form.WfForm;
import cn.iocoder.yudao.adminserver.modules.activiti.dal.mysql.form.WfFormMapper;
import cn.iocoder.yudao.adminserver.modules.activiti.service.form.WfFormService;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;

import static cn.iocoder.yudao.adminserver.modules.activiti.enums.form.WfFormErrorCodeConstants.FORM_NOT_EXISTS;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;

/**
 * 动态表单 Service 实现类
 *
 * TODO @风里雾里
 */
@Service
@Validated
public class WfFormServiceImpl implements WfFormService {

    @Resource
    private WfFormMapper formMapper;

    @Override
    public Long createForm(WfFormCreateReqVO createReqVO) {
        // 插入
        WfForm form = WfFormConvert.INSTANCE.convert(createReqVO);
        formMapper.insert(form);
        // 返回
        return form.getId();
    }

    @Override
    public void updateForm(WfFormUpdateReqVO updateReqVO) {
        // 校验存在
        this.validateFormExists(updateReqVO.getId());
        // 更新
        WfForm updateObj = WfFormConvert.INSTANCE.convert(updateReqVO);
        formMapper.updateById(updateObj);
    }

    @Override
    public void deleteForm(Long id) {
        // 校验存在
        this.validateFormExists(id);
        // 删除
        formMapper.deleteById(id);
    }

    private void validateFormExists(Long id) {
        if (formMapper.selectById(id) == null) {
            throw exception(FORM_NOT_EXISTS);
        }
    }

    @Override
    public WfForm getForm(Long id) {
        return formMapper.selectById(id);
    }

    @Override
    public List<WfForm> getFormList(Collection<Long> ids) {
        return formMapper.selectBatchIds(ids);
    }

    @Override
    public PageResult<WfForm> getFormPage(WfFormPageReqVO pageReqVO) {
        return formMapper.selectPage(pageReqVO);
    }

    @Override
    public List<WfForm> getFormList(WfFormExportReqVO exportReqVO) {
        return formMapper.selectList(exportReqVO);
    }

}
