package cn.iocoder.yudao.module.mp.service.newstemplate;

import java.util.*;
import javax.validation.*;

import cn.iocoder.yudao.module.mp.controller.admin.newstemplate.vo.*;
import cn.iocoder.yudao.module.mp.dal.dataobject.newstemplate.WxNewsTemplateDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

/**
 * 图文消息模板 Service 接口
 *
 * @author 芋道源码
 */
public interface WxNewsTemplateService {

    /**
     * 创建图文消息模板
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Integer createWxNewsTemplate(@Valid WxNewsTemplateCreateReqVO createReqVO);

    /**
     * 更新图文消息模板
     *
     * @param updateReqVO 更新信息
     */
    void updateWxNewsTemplate(@Valid WxNewsTemplateUpdateReqVO updateReqVO);

    /**
     * 删除图文消息模板
     *
     * @param id 编号
     */
    void deleteWxNewsTemplate(Integer id);

    /**
     * 获得图文消息模板
     *
     * @param id 编号
     * @return 图文消息模板
     */
    WxNewsTemplateDO getWxNewsTemplate(Integer id);

    /**
     * 获得图文消息模板列表
     *
     * @param ids 编号
     * @return 图文消息模板列表
     */
    List<WxNewsTemplateDO> getWxNewsTemplateList(Collection<Integer> ids);

    /**
     * 获得图文消息模板分页
     *
     * @param pageReqVO 分页查询
     * @return 图文消息模板分页
     */
    PageResult<WxNewsTemplateDO> getWxNewsTemplatePage(WxNewsTemplatePageReqVO pageReqVO);

    /**
     * 获得图文消息模板列表, 用于 Excel 导出
     *
     * @param exportReqVO 查询条件
     * @return 图文消息模板列表
     */
    List<WxNewsTemplateDO> getWxNewsTemplateList(WxNewsTemplateExportReqVO exportReqVO);

}
