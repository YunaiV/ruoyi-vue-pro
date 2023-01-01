package cn.iocoder.yudao.module.mp.service.newstemplate;

import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import org.springframework.validation.annotation.Validated;

import java.util.*;

import cn.iocoder.yudao.module.mp.controller.admin.newstemplate.vo.*;
import cn.iocoder.yudao.module.mp.dal.dataobject.newstemplate.WxNewsTemplateDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

import cn.iocoder.yudao.module.mp.convert.newstemplate.WxNewsTemplateConvert;
import cn.iocoder.yudao.module.mp.dal.mysql.newstemplate.WxNewsTemplateMapper;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.mp.enums.ErrorCodeConstants.*;

/**
 * 图文消息模板 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class WxNewsTemplateServiceImpl implements WxNewsTemplateService {

    @Resource
    private WxNewsTemplateMapper wxNewsTemplateMapper;

    @Override
    public Integer createWxNewsTemplate(WxNewsTemplateCreateReqVO createReqVO) {
        // 插入
        WxNewsTemplateDO wxNewsTemplate = WxNewsTemplateConvert.INSTANCE.convert(createReqVO);
        wxNewsTemplateMapper.insert(wxNewsTemplate);
        // 返回
        return wxNewsTemplate.getId();
    }

    @Override
    public void updateWxNewsTemplate(WxNewsTemplateUpdateReqVO updateReqVO) {
        // 校验存在
        this.validateWxNewsTemplateExists(updateReqVO.getId());
        // 更新
        WxNewsTemplateDO updateObj = WxNewsTemplateConvert.INSTANCE.convert(updateReqVO);
        wxNewsTemplateMapper.updateById(updateObj);
    }

    @Override
    public void deleteWxNewsTemplate(Integer id) {
        // 校验存在
        this.validateWxNewsTemplateExists(id);
        // 删除
        wxNewsTemplateMapper.deleteById(id);
    }

    private void validateWxNewsTemplateExists(Integer id) {
        if (wxNewsTemplateMapper.selectById(id) == null) {
            throw exception(COMMON_NOT_EXISTS);
        }
    }

    @Override
    public WxNewsTemplateDO getWxNewsTemplate(Integer id) {
        return wxNewsTemplateMapper.selectById(id);
    }

    @Override
    public List<WxNewsTemplateDO> getWxNewsTemplateList(Collection<Integer> ids) {
        return wxNewsTemplateMapper.selectBatchIds(ids);
    }

    @Override
    public PageResult<WxNewsTemplateDO> getWxNewsTemplatePage(WxNewsTemplatePageReqVO pageReqVO) {
        return wxNewsTemplateMapper.selectPage(pageReqVO);
    }

    @Override
    public List<WxNewsTemplateDO> getWxNewsTemplateList(WxNewsTemplateExportReqVO exportReqVO) {
        return wxNewsTemplateMapper.selectList(exportReqVO);
    }

}
