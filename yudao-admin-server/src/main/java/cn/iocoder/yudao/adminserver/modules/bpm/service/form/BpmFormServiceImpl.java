package cn.iocoder.yudao.adminserver.modules.bpm.service.form;

import cn.iocoder.yudao.adminserver.modules.bpm.controller.form.vo.BpmFormCreateReqVO;
import cn.iocoder.yudao.adminserver.modules.bpm.controller.form.vo.BpmFormExportReqVO;
import cn.iocoder.yudao.adminserver.modules.bpm.controller.form.vo.BpmFormPageReqVO;
import cn.iocoder.yudao.adminserver.modules.bpm.controller.form.vo.BpmFormUpdateReqVO;
import cn.iocoder.yudao.adminserver.modules.bpm.convert.form.BpmFormConvert;
import cn.iocoder.yudao.adminserver.modules.bpm.dal.dataobject.form.BpmForm;
import cn.iocoder.yudao.adminserver.modules.bpm.dal.mysql.form.BpmFormMapper;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;

import static cn.iocoder.yudao.adminserver.modules.bpm.enums.BpmErrorCodeConstants.BPM_FORM_NOT_EXISTS;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;

/**
 * 动态表单 Service 实现类
 *
 * @author 风里雾里
 */
@Service
@Validated
public class BpmFormServiceImpl implements BpmFormService {

    @Resource
    private BpmFormMapper formMapper;

    @Override
    public Long createForm(BpmFormCreateReqVO createReqVO) {
        // 插入
        BpmForm form = BpmFormConvert.INSTANCE.convert(createReqVO);
        formMapper.insert(form);
        // 返回
        return form.getId();
    }

    @Override
    public void updateForm(BpmFormUpdateReqVO updateReqVO) {
        // 校验存在
        this.validateFormExists(updateReqVO.getId());
        // 更新
        BpmForm updateObj = BpmFormConvert.INSTANCE.convert(updateReqVO);
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
            throw exception(BPM_FORM_NOT_EXISTS);
        }
    }

    @Override
    public BpmForm getForm(Long id) {
        return formMapper.selectById(id);
    }

    @Override
    public List<BpmForm> getFormList(Collection<Long> ids) {
        return formMapper.selectBatchIds(ids);
    }

    @Override
    public PageResult<BpmForm> getFormPage(BpmFormPageReqVO pageReqVO) {
        return formMapper.selectPage(pageReqVO);
    }

}
