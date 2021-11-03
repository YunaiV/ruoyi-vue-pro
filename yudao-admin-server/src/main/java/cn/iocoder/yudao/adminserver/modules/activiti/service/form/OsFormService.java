package cn.iocoder.yudao.adminserver.modules.activiti.service.form;

import cn.iocoder.yudao.adminserver.modules.activiti.controller.form.vo.OsFormCreateReqVO;
import cn.iocoder.yudao.adminserver.modules.activiti.controller.form.vo.OsFormExportReqVO;
import cn.iocoder.yudao.adminserver.modules.activiti.controller.form.vo.OsFormPageReqVO;
import cn.iocoder.yudao.adminserver.modules.activiti.controller.form.vo.OsFormUpdateReqVO;
import cn.iocoder.yudao.adminserver.modules.activiti.dal.dataobject.form.OsFormDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

import java.util.*;
import javax.validation.*;


/**
 * 动态表单 Service 接口
 *
 * @author 芋艿
 */
public interface OsFormService {

    /**
     * 创建动态表单
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createForm(@Valid OsFormCreateReqVO createReqVO);

    /**
     * 更新动态表单
     *
     * @param updateReqVO 更新信息
     */
    void updateForm(@Valid OsFormUpdateReqVO updateReqVO);

    /**
     * 删除动态表单
     *
     * @param id 编号
     */
    void deleteForm(Long id);

    /**
     * 获得动态表单
     *
     * @param id 编号
     * @return 动态表单
     */
    OsFormDO getForm(Long id);

    /**
     * 获得动态表单列表
     *
     * @param ids 编号
     * @return 动态表单列表
     */
    List<OsFormDO> getFormList(Collection<Long> ids);

    /**
     * 获得动态表单分页
     *
     * @param pageReqVO 分页查询
     * @return 动态表单分页
     */
    PageResult<OsFormDO> getFormPage(OsFormPageReqVO pageReqVO);

    /**
     * 获得动态表单列表, 用于 Excel 导出
     *
     * @param exportReqVO 查询条件
     * @return 动态表单列表
     */
    List<OsFormDO> getFormList(OsFormExportReqVO exportReqVO);

}
