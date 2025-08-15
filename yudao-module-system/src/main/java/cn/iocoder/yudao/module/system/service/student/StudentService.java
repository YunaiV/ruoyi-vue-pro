package cn.iocoder.yudao.module.system.service.student;

import java.util.*;
import javax.validation.*;
import cn.iocoder.yudao.module.system.controller.admin.student.vo.*;
import cn.iocoder.yudao.module.system.dal.dataobject.student.StudentDO;
import cn.iocoder.yudao.module.system.dal.dataobject.student.StudentCourseDO;
import cn.iocoder.yudao.module.system.dal.dataobject.student.StudentGradeDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;

/**
 * 学生主子表测试 Service 接口
 *
 * @author 京京
 */
public interface StudentService {

    /**
     * 创建学生主子表测试
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createStudent(@Valid StudentSaveReqVO createReqVO);

    /**
     * 更新学生主子表测试
     *
     * @param updateReqVO 更新信息
     */
    void updateStudent(@Valid StudentSaveReqVO updateReqVO);

    /**
     * 删除学生主子表测试
     *
     * @param id 编号
     */
    void deleteStudent(Long id);

    /**
    * 批量删除学生主子表测试
    *
    * @param ids 编号
    */
    void deleteStudentListByIds(List<Long> ids);

    /**
     * 获得学生主子表测试
     *
     * @param id 编号
     * @return 学生主子表测试
     */
    StudentDO getStudent(Long id);

    /**
     * 获得学生主子表测试分页
     *
     * @param pageReqVO 分页查询
     * @return 学生主子表测试分页
     */
    PageResult<StudentDO> getStudentPage(StudentPageReqVO pageReqVO);

    // ==================== 子表（学生课程） ====================

    /**
     * 获得学生课程分页
     *
     * @param pageReqVO 分页查询
     * @param studentId 学生编号
     * @return 学生课程分页
     */
    PageResult<StudentCourseDO> getStudentCoursePage(PageParam pageReqVO, Long studentId);

    /**
     * 创建学生课程
     *
     * @param studentCourse 创建信息
     * @return 编号
     */
    Long createStudentCourse(@Valid StudentCourseDO studentCourse);

    /**
     * 更新学生课程
     *
     * @param studentCourse 更新信息
     */
    void updateStudentCourse(@Valid StudentCourseDO studentCourse);

    /**
     * 删除学生课程
     *
     * @param id 编号
     */
    void deleteStudentCourse(Long id);

    /**
    * 批量删除学生课程
    *
    * @param ids 编号
    */
    void deleteStudentCourseListByIds(List<Long> ids);

	/**
	 * 获得学生课程
	 *
	 * @param id 编号
     * @return 学生课程
	 */
    StudentCourseDO getStudentCourse(Long id);

    // ==================== 子表（学生班级） ====================

    /**
     * 获得学生班级分页
     *
     * @param pageReqVO 分页查询
     * @param studentId 学生编号
     * @return 学生班级分页
     */
    PageResult<StudentGradeDO> getStudentGradePage(PageParam pageReqVO, Long studentId);

    /**
     * 创建学生班级
     *
     * @param studentGrade 创建信息
     * @return 编号
     */
    Long createStudentGrade(@Valid StudentGradeDO studentGrade);

    /**
     * 更新学生班级
     *
     * @param studentGrade 更新信息
     */
    void updateStudentGrade(@Valid StudentGradeDO studentGrade);

    /**
     * 删除学生班级
     *
     * @param id 编号
     */
    void deleteStudentGrade(Long id);

    /**
    * 批量删除学生班级
    *
    * @param ids 编号
    */
    void deleteStudentGradeListByIds(List<Long> ids);

	/**
	 * 获得学生班级
	 *
	 * @param id 编号
     * @return 学生班级
	 */
    StudentGradeDO getStudentGrade(Long id);

}