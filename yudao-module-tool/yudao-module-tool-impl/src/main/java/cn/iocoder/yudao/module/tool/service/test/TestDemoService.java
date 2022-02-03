package cn.iocoder.yudao.module.tool.service.test;

import java.util.*;
import javax.validation.*;
import cn.iocoder.yudao.module.tool.controller.admin.test.vo.*;
import cn.iocoder.yudao.module.tool.dal.dataobject.test.TestDemoDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

/**
 * 字典类型 Service 接口
 *
 * @author 芋道源码
 */
public interface TestDemoService {

    /**
     * 创建字典类型
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createTestDemo(@Valid TestDemoCreateReqVO createReqVO);

    /**
     * 更新字典类型
     *
     * @param updateReqVO 更新信息
     */
    void updateTestDemo(@Valid TestDemoUpdateReqVO updateReqVO);

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
    TestDemoDO getTestDemo(Long id);

    /**
     * 获得字典类型列表
     *
     * @param ids 编号
     * @return 字典类型列表
     */
    List<TestDemoDO> getTestDemoList(Collection<Long> ids);

    /**
     * 获得字典类型分页
     *
     * @param pageReqVO 分页查询
     * @return 字典类型分页
     */
    PageResult<TestDemoDO> getTestDemoPage(TestDemoPageReqVO pageReqVO);

    /**
     * 获得字典类型列表, 用于 Excel 导出
     *
     * @param exportReqVO 查询条件
     * @return 字典类型列表
     */
    List<TestDemoDO> getTestDemoList(TestDemoExportReqVO exportReqVO);

}
