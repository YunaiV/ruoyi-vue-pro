package cn.iocoder.yudao.adminserver.modules.activiti.service.form.impl;

import cn.iocoder.yudao.adminserver.modules.activiti.controller.form.vo.OsFormCreateReqVO;
import cn.iocoder.yudao.adminserver.modules.activiti.controller.form.vo.OsFormExportReqVO;
import cn.iocoder.yudao.adminserver.modules.activiti.controller.form.vo.OsFormPageReqVO;
import cn.iocoder.yudao.adminserver.modules.activiti.controller.form.vo.OsFormUpdateReqVO;
import cn.iocoder.yudao.adminserver.modules.activiti.convert.form.OsFormConvert;
import cn.iocoder.yudao.adminserver.modules.activiti.dal.dataobject.form.OsFormDO;
import cn.iocoder.yudao.adminserver.modules.activiti.dal.mysql.form.OsFormMapper;
import cn.iocoder.yudao.adminserver.modules.activiti.service.form.OsFormService;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;

import static cn.iocoder.yudao.adminserver.modules.activiti.enums.form.FormErrorCodeConstants.FORM_NOT_EXISTS;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;

/**
 * 动态表单 Service 实现类
 *
 * @author 芋艿 // TODO @风里雾里：作者改成你自己哈
 */
@Service
@Validated
public class OsFormServiceImpl implements OsFormService {

    @Resource
    private OsFormMapper formMapper;

    @Override
    public Long createForm(OsFormCreateReqVO createReqVO) {
        // 插入
        OsFormDO form = OsFormConvert.INSTANCE.convert(createReqVO);
        formMapper.insert(form);
        // 返回
        return form.getId();
    }

    @Override
    public void updateForm(OsFormUpdateReqVO updateReqVO) {
        // 校验存在
        this.validateFormExists(updateReqVO.getId());
        // 更新
        OsFormDO updateObj = OsFormConvert.INSTANCE.convert(updateReqVO);
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
    public OsFormDO getForm(Long id) {
        return formMapper.selectById(id);
    }

    @Override
    public List<OsFormDO> getFormList(Collection<Long> ids) {
        return formMapper.selectBatchIds(ids);
    }

    @Override
    public PageResult<OsFormDO> getFormPage(OsFormPageReqVO pageReqVO) {
        return formMapper.selectPage(pageReqVO);
    }

    @Override
    public List<OsFormDO> getFormList(OsFormExportReqVO exportReqVO) {
        return formMapper.selectList(exportReqVO);
    }

}
