package cn.iocoder.yudao.module.infra.service.demo12;

import java.util.*;
import javax.validation.*;
import cn.iocoder.yudao.module.infra.controller.admin.demo12.vo.*;
import cn.iocoder.yudao.module.infra.dal.dataobject.demo12.InfraDemo12StudentDO;
import cn.iocoder.yudao.module.infra.dal.dataobject.demo12.InfraDemo12StudentContactDO;
import cn.iocoder.yudao.module.infra.dal.dataobject.demo12.InfraDemo12StudentTeacherDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

/**
 * 学生 Service 接口
 *
 * @author 芋道源码
 */
public interface InfraDemo12StudentService {

    /**
     * 创建学生
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createDemo12Student(@Valid InfraDemo12StudentCreateReqVO createReqVO);

    /**
     * 更新学生
     *
     * @param updateReqVO 更新信息
     */
    void updateDemo12Student(@Valid InfraDemo12StudentUpdateReqVO updateReqVO);

    /**
     * 删除学生
     *
     * @param id 编号
     */
    void deleteDemo12Student(Long id);

    /**
     * 获得学生
     *
     * @param id 编号
     * @return 学生
     */
    InfraDemo12StudentDO getDemo12Student(Long id);

    /**
     * 获得学生分页
     *
     * @param pageReqVO 分页查询
     * @return 学生分页
     */
    PageResult<InfraDemo12StudentDO> getDemo12StudentPage(InfraDemo12StudentPageReqVO pageReqVO);

    /**
     * 获得学生列表, 用于 Excel 导出
     *
     * @param exportReqVO 查询条件
     * @return 学生列表
     */
    List<InfraDemo12StudentDO> getDemo12StudentList(InfraDemo12StudentExportReqVO exportReqVO);


    // ==================== 子表（学生联系人） ====================

    /**
     * 获得学生联系人列表
     *
     * @param studentId 学生编号
     * @return 学生联系人列表
     */
    List<InfraDemo12StudentContactDO> getDemo12StudentContactListByStudentId(Long studentId);


    // ==================== 子表（学生班主任） ====================

    /**
     * 获得学生班主任
     *
     * @param studentId 学生编号
     * @return 学生班主任
     */
    InfraDemo12StudentTeacherDO getDemo12StudentTeacherByStudentId(Long studentId);

}