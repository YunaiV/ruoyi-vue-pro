package cn.iocoder.yudao.module.infra.service.demo.demo03;

import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import cn.iocoder.yudao.module.infra.controller.admin.demo.demo03.normal.vo.*;
import cn.iocoder.yudao.module.infra.dal.dataobject.demo.demo03.Demo03StudentDO;
import cn.iocoder.yudao.module.infra.dal.dataobject.demo.demo03.Demo03CourseDO;
import cn.iocoder.yudao.module.infra.dal.dataobject.demo.demo03.Demo03GradeDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;

import cn.iocoder.yudao.module.infra.dal.mysql.demo.demo03.Demo03StudentMapper;
import cn.iocoder.yudao.module.infra.dal.mysql.demo.demo03.Demo03CourseMapper;
import cn.iocoder.yudao.module.infra.dal.mysql.demo.demo03.Demo03GradeMapper;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.infra.enums.ErrorCodeConstants.*;

/**
 * 学生 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class Demo03StudentServiceImpl implements Demo03StudentService {

    @Resource
    private Demo03StudentMapper demo03StudentMapper;
    @Resource
    private Demo03CourseMapper demo03CourseMapper;
    @Resource
    private Demo03GradeMapper demo03GradeMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createDemo03Student(Demo03StudentSaveReqVO createReqVO) {
        // 插入
        Demo03StudentDO demo03Student = BeanUtils.toBean(createReqVO, Demo03StudentDO.class);
        demo03StudentMapper.insert(demo03Student);

        // 插入子表
        createDemo03CourseList(demo03Student.getId(), createReqVO.getDemo03Courses());
        createDemo03Grade(demo03Student.getId(), createReqVO.getDemo03Grade());
        // 返回
        return demo03Student.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateDemo03Student(Demo03StudentSaveReqVO updateReqVO) {
        // 校验存在
        validateDemo03StudentExists(updateReqVO.getId());
        // 更新
        Demo03StudentDO updateObj = BeanUtils.toBean(updateReqVO, Demo03StudentDO.class);
        demo03StudentMapper.updateById(updateObj);

        // 更新子表
        updateDemo03CourseList(updateReqVO.getId(), updateReqVO.getDemo03Courses());
        updateDemo03Grade(updateReqVO.getId(), updateReqVO.getDemo03Grade());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDemo03Student(Long id) {
        // 校验存在
        validateDemo03StudentExists(id);
        // 删除
        demo03StudentMapper.deleteById(id);

        // 删除子表
        deleteDemo03CourseByStudentId(id);
        deleteDemo03GradeByStudentId(id);
    }

    private void validateDemo03StudentExists(Long id) {
        if (demo03StudentMapper.selectById(id) == null) {
            throw exception(DEMO03_STUDENT_NOT_EXISTS);
        }
    }

    @Override
    public Demo03StudentDO getDemo03Student(Long id) {
        return demo03StudentMapper.selectById(id);
    }

    @Override
    public PageResult<Demo03StudentDO> getDemo03StudentPage(Demo03StudentPageReqVO pageReqVO) {
        return demo03StudentMapper.selectPage(pageReqVO);
    }

    // ==================== 子表（学生课程） ====================

    @Override
    public List<Demo03CourseDO> getDemo03CourseListByStudentId(Long studentId) {
        return demo03CourseMapper.selectListByStudentId(studentId);
    }

    private void createDemo03CourseList(Long studentId, List<Demo03CourseDO> list) {
        list.forEach(o -> o.setStudentId(studentId));
        demo03CourseMapper.insertBatch(list);
    }

    private void updateDemo03CourseList(Long studentId, List<Demo03CourseDO> list) {
        deleteDemo03CourseByStudentId(studentId);
		list.forEach(o -> o.setId(null).setUpdater(null).setUpdateTime(null)); // 解决更新情况下：1）id 冲突；2）updateTime 不更新
        createDemo03CourseList(studentId, list);
    }

    private void deleteDemo03CourseByStudentId(Long studentId) {
        demo03CourseMapper.deleteByStudentId(studentId);
    }

    // ==================== 子表（学生班级） ====================

    @Override
    public Demo03GradeDO getDemo03GradeByStudentId(Long studentId) {
        return demo03GradeMapper.selectByStudentId(studentId);
    }

    private void createDemo03Grade(Long studentId, Demo03GradeDO demo03Grade) {
        if (demo03Grade == null) {
            return;
        }
        demo03Grade.setStudentId(studentId);
        demo03GradeMapper.insert(demo03Grade);
    }

    private void updateDemo03Grade(Long studentId, Demo03GradeDO demo03Grade) {
        if (demo03Grade == null) {
			return;
        }
        demo03Grade.setStudentId(studentId);
        demo03Grade.setUpdater(null).setUpdateTime(null); // 解决更新情况下：updateTime 不更新
        demo03GradeMapper.insertOrUpdate(demo03Grade);
    }

    private void deleteDemo03GradeByStudentId(Long studentId) {
        demo03GradeMapper.deleteByStudentId(studentId);
    }

}