package cn.iocoder.yudao.module.infra.service.demo01;

import java.util.*;
import javax.validation.*;
import cn.iocoder.yudao.module.infra.controller.admin.demo01.vo.*;
import cn.iocoder.yudao.module.infra.dal.dataobject.demo01.InfraDemo01StudentDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

/**
 * 学生 Service 接口
 *
 * @author 芋道源码
 */
public interface InfraDemo01StudentService {

    /**
     * 创建学生
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createDemo01Student(@Valid InfraDemo01StudentCreateReqVO createReqVO);

    /**
     * 更新学生
     *
     * @param updateReqVO 更新信息
     */
    void updateDemo01Student(@Valid InfraDemo01StudentUpdateReqVO updateReqVO);

    /**
     * 删除学生
     *
     * @param id 编号
     */
    void deleteDemo01Student(Long id);

    /**
     * 获得学生
     *
     * @param id 编号
     * @return 学生
     */
    InfraDemo01StudentDO getDemo01Student(Long id);

    /**
     * 获得学生分页
     *
     * @param pageReqVO 分页查询
     * @return 学生分页
     */
    PageResult<InfraDemo01StudentDO> getDemo01StudentPage(InfraDemo01StudentPageReqVO pageReqVO);

    /**
     * 获得学生列表, 用于 Excel 导出
     *
     * @param exportReqVO 查询条件
     * @return 学生列表
     */
    List<InfraDemo01StudentDO> getDemo01StudentList(InfraDemo01StudentExportReqVO exportReqVO);

}