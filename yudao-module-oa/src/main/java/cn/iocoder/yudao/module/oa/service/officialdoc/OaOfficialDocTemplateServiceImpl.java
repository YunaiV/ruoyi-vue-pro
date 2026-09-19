package cn.iocoder.yudao.module.oa.service.officialdoc;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.oa.controller.admin.officialdoc.vo.template.*;
import cn.iocoder.yudao.module.oa.dal.dataobject.officialdoc.OaOfficialDocTemplateDO;
import cn.iocoder.yudao.module.oa.dal.mysql.officialdoc.OaOfficialDocTemplateMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.context.annotation.Lazy;
import org.springframework.validation.annotation.Validated;

import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.oa.enums.ErrorCodeConstants.OFFICIAL_DOC_TEMPLATE_NOT_EXISTS;
import static cn.iocoder.yudao.module.oa.enums.ErrorCodeConstants.OFFICIAL_DOC_TEMPLATE_DISABLED;
import static cn.iocoder.yudao.module.oa.enums.ErrorCodeConstants.OFFICIAL_DOC_TEMPLATE_IN_USE;

/**
 * 套红模板 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class OaOfficialDocTemplateServiceImpl implements OaOfficialDocTemplateService {

    @Resource
    private OaOfficialDocTemplateMapper officialDocTemplateMapper;

    @Resource
    @Lazy // 延迟，避免循环依赖报错
    private OaOfficialDocSendService officialDocSendService;

    @Override
    public Long createOfficialDocTemplate(OaOfficialDocTemplateSaveReqVO reqVO) {
        // 1. 转换套红模板内容
        OaOfficialDocTemplateDO template = BeanUtils.toBean(reqVO, OaOfficialDocTemplateDO.class).setId(null);

        // 2. 保存记录
        officialDocTemplateMapper.insert(template);
        return template.getId();
    }

    @Override
    public void updateOfficialDocTemplate(OaOfficialDocTemplateSaveReqVO reqVO) {
        // 1. 校验模板存在
        validateOfficialDocTemplateExists(reqVO.getId());

        // 2. 更新模板
        officialDocTemplateMapper.updateById(BeanUtils.toBean(reqVO, OaOfficialDocTemplateDO.class));
    }

    @Override
    public void deleteOfficialDocTemplate(Long id) {
        // 1.1 校验模板存在
        validateOfficialDocTemplateExists(id);
        // 1.2 校验模板未被业务单据引用
        if (officialDocSendService.getOfficialDocSendCountByTemplateId(id) > 0) {
            throw exception(OFFICIAL_DOC_TEMPLATE_IN_USE);
        }

        // 2. 删除模板，不删除已有关联发文
        officialDocTemplateMapper.deleteById(id);
    }

    @Override
    public OaOfficialDocTemplateDO getOfficialDocTemplate(Long id) {
        return officialDocTemplateMapper.selectById(id);
    }

    @Override
    public OaOfficialDocTemplateDO validateOfficialDocTemplate(Long id) {
        // 1.1 校验模板存在
        OaOfficialDocTemplateDO template = validateOfficialDocTemplateExists(id);
        // 2. 校验模板已启用
        if (CommonStatusEnum.isDisable(template.getStatus())) {
            throw exception(OFFICIAL_DOC_TEMPLATE_DISABLED);
        }
        return template;
    }

    @Override
    public PageResult<OaOfficialDocTemplateDO> getOfficialDocTemplatePage(OaOfficialDocTemplatePageReqVO reqVO) {
        return officialDocTemplateMapper.selectPage(reqVO);
    }

    @Override
    public List<OaOfficialDocTemplateDO> getOfficialDocTemplateList(Integer status) {
        return officialDocTemplateMapper.selectListByStatus(status);
    }

    /**
     * 校验套红模板存在
     *
     * @param id 套红模板编号
     * @return 套红模板
     */
    private OaOfficialDocTemplateDO validateOfficialDocTemplateExists(Long id) {
        OaOfficialDocTemplateDO template = officialDocTemplateMapper.selectById(id);
        if (template == null) {
            throw exception(OFFICIAL_DOC_TEMPLATE_NOT_EXISTS);
        }
        return template;
    }

}
