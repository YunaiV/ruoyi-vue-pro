package cn.iocoder.dashboard.modules.system.service.test;

import cn.iocoder.dashboard.common.pojo.PageResult;
import cn.iocoder.dashboard.modules.system.controller.test.vo.SysTestDemoCreateReqVO;
import cn.iocoder.dashboard.modules.system.controller.test.vo.SysTestDemoPageReqVO;
import cn.iocoder.dashboard.modules.system.controller.test.vo.SysTestDemoUpdateReqVO;
import cn.iocoder.dashboard.modules.system.dal.dataobject.test.SysTestDemoDO;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;

/**
* 字典类型 Service 接口
*
* @author 芋艿
*/
public interface SysTestDemoService {

    /**
     * 创建字典类型
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createTestDemo(@Valid SysTestDemoCreateReqVO createReqVO);

    /**
     * 更新字典类型
     *
     * @param updateReqVO 更新信息
     */
    void updateTestDemo(@Valid SysTestDemoUpdateReqVO updateReqVO);

    /**
     * 删除字典类型
     *
     * @param id 编号
     */
    void deleteTestDemo(Long id);

    /**
     * 获得字典类型
     *
     * @param id 编号
     * @return 字典类型
     */
    SysTestDemoDO getTestDemo(Long id);

    /**
     * 获得字典类型列表
     *
     * @param ids 编号
     * @return 字典类型列表
     */
    List<SysTestDemoDO> getTestDemoList(Collection<Long> ids);

    /**
     * 获得字典类型分页
     *
     * @param pageReqVO 分页查询
     * @return 字典类型分页
     */
	PageResult<SysTestDemoDO> getTestDemoPage(SysTestDemoPageReqVO pageReqVO);

}
