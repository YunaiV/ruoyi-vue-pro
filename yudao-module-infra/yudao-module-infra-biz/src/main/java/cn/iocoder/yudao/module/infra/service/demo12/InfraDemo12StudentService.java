package cn.iocoder.yudao.module.infra.service.demo12;

import java.util.*;
import javax.validation.*;
import cn.iocoder.yudao.module.infra.controller.admin.demo12.vo.*;
import cn.iocoder.yudao.module.infra.dal.dataobject.demo12.InfraDemo12StudentDO;
import cn.iocoder.yudao.module.infra.dal.dataobject.demo12.InfraDemo12StudentContactDO;
import cn.iocoder.yudao.module.infra.dal.dataobject.demo12.InfraDemo12StudentTeacherDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;

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
     * 获得学生联系人分页
     *
     * @param pageReqVO 分页查询
     * @param studentId 学生编号
     * @return 学生联系人分页
     */
    PageResult<InfraDemo12StudentContactDO> getDemo12StudentContactPage(PageParam pageReqVO, Long studentId);

    /**
     * 创建学生联系人
     *
     * @param demo12StudentContact 创建信息
     * @return 编号
     */
    Long createDemo12StudentContact(@Valid InfraDemo12StudentContactDO demo12StudentContact);

    /**
     * 更新学生联系人
     *
     * @param demo12StudentContact 更新信息
     */
    void updateDemo12StudentContact(@Valid InfraDemo12StudentContactDO demo12StudentContact);

    /**
     * 删除学生联系人
     *
     * @param id 编号
     */
    void deleteDemo12StudentContact(Long id);

	/**
	 * 获得学生联系人
	 *
	 * @param id 编号
     * @return 学生联系人
	 */
    InfraDemo12StudentContactDO getDemo12StudentContact(Long id);


    // ==================== 子表（学生班主任） ====================

    /**
     * 获得学生班主任分页
     *
     * @param pageReqVO 分页查询
     * @param studentId 学生编号
     * @return 学生班主任分页
     */
    PageResult<InfraDemo12StudentTeacherDO> getDemo12StudentTeacherPage(PageParam pageReqVO, Long studentId);

    /**
     * 创建学生班主任
     *
     * @param demo12StudentTeacher 创建信息
     * @return 编号
     */
    Long createDemo12StudentTeacher(@Valid InfraDemo12StudentTeacherDO demo12StudentTeacher);

    /**
     * 更新学生班主任
     *
     * @param demo12StudentTeacher 更新信息
     */
    void updateDemo12StudentTeacher(@Valid InfraDemo12StudentTeacherDO demo12StudentTeacher);

    /**
     * 删除学生班主任
     *
     * @param id 编号
     */
    void deleteDemo12StudentTeacher(Long id);

	/**
	 * 获得学生班主任
	 *
	 * @param id 编号
     * @return 学生班主任
	 */
    InfraDemo12StudentTeacherDO getDemo12StudentTeacher(Long id);

}