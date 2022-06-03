package cn.iocoder.yudao.module.mp.service.texttemplate;

import java.util.*;
import javax.validation.*;

import cn.iocoder.yudao.module.mp.controller.admin.texttemplate.vo.*;
import cn.iocoder.yudao.module.mp.dal.dataobject.texttemplate.WxTextTemplateDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

/**
 * 文本模板 Service 接口
 *
 * @author 芋道源码
 */
public interface WxTextTemplateService {

    /**
     * 创建文本模板
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Integer createWxTextTemplate(@Valid WxTextTemplateCreateReqVO createReqVO);

    /**
     * 更新文本模板
     *
     * @param updateReqVO 更新信息
     */
    void updateWxTextTemplate(@Valid WxTextTemplateUpdateReqVO updateReqVO);

    /**
     * 删除文本模板
     *
     * @param id 编号
     */
    void deleteWxTextTemplate(Integer id);

    /**
     * 获得文本模板
     *
     * @param id 编号
     * @return 文本模板
     */
    WxTextTemplateDO getWxTextTemplate(Integer id);

    /**
     * 获得文本模板列表
     *
     * @param ids 编号
     * @return 文本模板列表
     */
    List<WxTextTemplateDO> getWxTextTemplateList(Collection<Integer> ids);

    /**
     * 获得文本模板分页
     *
     * @param pageReqVO 分页查询
     * @return 文本模板分页
     */
    PageResult<WxTextTemplateDO> getWxTextTemplatePage(WxTextTemplatePageReqVO pageReqVO);

    /**
     * 获得文本模板列表, 用于 Excel 导出
     *
     * @param exportReqVO 查询条件
     * @return 文本模板列表
     */
    List<WxTextTemplateDO> getWxTextTemplateList(WxTextTemplateExportReqVO exportReqVO);

}
