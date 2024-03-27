package cn.iocoder.yudao.module.infra.service.demo.demo03;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.infra.controller.admin.demo.demo03.vo.Demo03StudentPageReqVO;
import cn.iocoder.yudao.module.infra.controller.admin.demo.demo03.vo.Demo03StudentSaveReqVO;
import cn.iocoder.yudao.module.infra.dal.dataobject.demo.demo03.Demo03CourseDO;
import cn.iocoder.yudao.module.infra.dal.dataobject.demo.demo03.Demo03GradeDO;
import cn.iocoder.yudao.module.infra.dal.dataobject.demo.demo03.Demo03StudentDO;

import javax.validation.Valid;
import java.util.List;

/**
 * 学生 Service 接口
 *
 * @author 芋道源码
 */
public interface Demo03StudentService {

    /**
     * 创建学生
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createDemo03Student(@Valid Demo03StudentSaveReqVO createReqVO);

    /**
     * 更新学生
     *
     * @param updateReqVO 更新信息
     */
    void updateDemo03Student(@Valid Demo03StudentSaveReqVO updateReqVO);

    /**
     * 删除学生
     *
     * @param id 编号
     */
    void deleteDemo03Student(Long id);

    /**
     * 获得学生
     *
     * @param id 编号
     * @return 学生
     */
    Demo03StudentDO getDemo03Student(Long id);

    /**
     * 获得学生分页
     *
     * @param pageReqVO 分页查询
     * @return 学生分页
     */
    PageResult<Demo03StudentDO> getDemo03StudentPage(Demo03StudentPageReqVO pageReqVO);


    // ==================== 子表（学生课程） ====================

    /**
     * 获得学生课程列表
     *
     * @param studentId 学生编号
     * @return 学生课程列表
     */
    List<Demo03CourseDO> getDemo03CourseListByStudentId(Long studentId);

    /**
     * 获得学生课程分页
     *
     * @param pageReqVO 分页查询
     * @param studentId 学生编号
     * @return 学生课程分页
     */
    PageResult<Demo03CourseDO> getDemo03CoursePage(PageParam pageReqVO, Long studentId);

    /**
     * 创建学生课程
     *
     * @param demo03Course 创建信息
     * @return 编号
     */
    Long createDemo03Course(@Valid Demo03CourseDO demo03Course);

    /**
     * 更新学生课程
     *
     * @param demo03Course 更新信息
     */
    void updateDemo03Course(@Valid Demo03CourseDO demo03Course);

    /**
     * 删除学生课程
     *
     * @param id 编号
     */
    void deleteDemo03Course(Long id);

    /**
     * 获得学生课程
     *
     * @param id 编号
     * @return 学生课程
     */
    Demo03CourseDO getDemo03Course(Long id);

    // ==================== 子表（学生班级） ====================

    /**
     * 获得学生班级
     *
     * @param studentId 学生编号
     * @return 学生班级
     */
    Demo03GradeDO getDemo03GradeByStudentId(Long studentId);

    /**
     * 获得学生班级分页
     *
     * @param pageReqVO 分页查询
     * @param studentId 学生编号
     * @return 学生班级分页
     */
    PageResult<Demo03GradeDO> getDemo03GradePage(PageParam pageReqVO, Long studentId);

    /**
     * 创建学生班级
     *
     * @param demo03Grade 创建信息
     * @return 编号
     */
    Long createDemo03Grade(@Valid Demo03GradeDO demo03Grade);

    /**
     * 更新学生班级
     *
     * @param demo03Grade 更新信息
     */
    void updateDemo03Grade(@Valid Demo03GradeDO demo03Grade);

    /**
     * 删除学生班级
     *
     * @param id 编号
     */
    void deleteDemo03Grade(Long id);

    /**
     * 获得学生班级
     *
     * @param id 编号
     * @return 学生班级
     */
    Demo03GradeDO getDemo03Grade(Long id);

}