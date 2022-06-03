package cn.iocoder.yudao.module.mp.service.texttemplate;

import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import org.springframework.validation.annotation.Validated;

import java.util.*;

import cn.iocoder.yudao.module.mp.controller.admin.texttemplate.vo.*;
import cn.iocoder.yudao.module.mp.dal.dataobject.texttemplate.WxTextTemplateDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

import cn.iocoder.yudao.module.mp.convert.texttemplate.WxTextTemplateConvert;
import cn.iocoder.yudao.module.mp.dal.mysql.texttemplate.WxTextTemplateMapper;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.mp.enums.ErrorCodeConstants.*;

/**
 * 文本模板 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class WxTextTemplateServiceImpl implements WxTextTemplateService {

    @Resource
    private WxTextTemplateMapper wxTextTemplateMapper;

    @Override
    public Integer createWxTextTemplate(WxTextTemplateCreateReqVO createReqVO) {
        // 插入
        WxTextTemplateDO wxTextTemplate = WxTextTemplateConvert.INSTANCE.convert(createReqVO);
        wxTextTemplateMapper.insert(wxTextTemplate);
        // 返回
        return wxTextTemplate.getId();
    }

    @Override
    public void updateWxTextTemplate(WxTextTemplateUpdateReqVO updateReqVO) {
        // 校验存在
        this.validateWxTextTemplateExists(updateReqVO.getId());
        // 更新
        WxTextTemplateDO updateObj = WxTextTemplateConvert.INSTANCE.convert(updateReqVO);
        wxTextTemplateMapper.updateById(updateObj);
    }

    @Override
    public void deleteWxTextTemplate(Integer id) {
        // 校验存在
        this.validateWxTextTemplateExists(id);
        // 删除
        wxTextTemplateMapper.deleteById(id);
    }

    private void validateWxTextTemplateExists(Integer id) {
        if (wxTextTemplateMapper.selectById(id) == null) {
            throw exception(COMMON_NOT_EXISTS);
        }
    }

    @Override
    public WxTextTemplateDO getWxTextTemplate(Integer id) {
        return wxTextTemplateMapper.selectById(id);
    }

    @Override
    public List<WxTextTemplateDO> getWxTextTemplateList(Collection<Integer> ids) {
        return wxTextTemplateMapper.selectBatchIds(ids);
    }

    @Override
    public PageResult<WxTextTemplateDO> getWxTextTemplatePage(WxTextTemplatePageReqVO pageReqVO) {
        return wxTextTemplateMapper.selectPage(pageReqVO);
    }

    @Override
    public List<WxTextTemplateDO> getWxTextTemplateList(WxTextTemplateExportReqVO exportReqVO) {
        return wxTextTemplateMapper.selectList(exportReqVO);
    }

}
