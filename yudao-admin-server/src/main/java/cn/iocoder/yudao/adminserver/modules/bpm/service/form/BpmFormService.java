package cn.iocoder.yudao.adminserver.modules.bpm.service.form;

import cn.iocoder.yudao.adminserver.modules.bpm.controller.form.vo.BpmFormCreateReqVO;
import cn.iocoder.yudao.adminserver.modules.bpm.controller.form.vo.BpmFormExportReqVO;
import cn.iocoder.yudao.adminserver.modules.bpm.controller.form.vo.BpmFormPageReqVO;
import cn.iocoder.yudao.adminserver.modules.bpm.controller.form.vo.BpmFormUpdateReqVO;
import cn.iocoder.yudao.adminserver.modules.bpm.dal.dataobject.form.BpmForm;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;


/**
 * 动态表单 Service 接口
 *
 * @author  @风里雾里
 */
public interface BpmFormService {

    /**
     * 创建动态表单
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createForm(@Valid BpmFormCreateReqVO createReqVO);

    /**
     * 更新动态表单
     *
     * @param updateReqVO 更新信息
     */
    void updateForm(@Valid BpmFormUpdateReqVO updateReqVO);

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
    BpmForm getForm(Long id);

    /**
     * 获得动态表单列表
     *
     * @param ids 编号
     * @return 动态表单列表
     */
    List<BpmForm> getFormList(Collection<Long> ids);

    /**
     * 获得动态表单分页
     *
     * @param pageReqVO 分页查询
     * @return 动态表单分页
     */
    PageResult<BpmForm> getFormPage(BpmFormPageReqVO pageReqVO);

    /**
     * 获得动态表单列表, 用于 Excel 导出
     *
     * @param exportReqVO 查询条件
     * @return 动态表单列表
     */
    List<BpmForm> getFormList(BpmFormExportReqVO exportReqVO);

}
