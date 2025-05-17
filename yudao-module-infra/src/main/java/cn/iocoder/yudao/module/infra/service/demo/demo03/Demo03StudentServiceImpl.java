package cn.iocoder.yudao.module.infra.service.demo.demo03;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.infra.controller.admin.demo.demo03.vo.Demo03StudentPageReqVO;
import cn.iocoder.yudao.module.infra.controller.admin.demo.demo03.vo.Demo03StudentSaveReqVO;
import cn.iocoder.yudao.module.infra.dal.dataobject.demo.demo03.Demo03CourseDO;
import cn.iocoder.yudao.module.infra.dal.dataobject.demo.demo03.Demo03GradeDO;
import cn.iocoder.yudao.module.infra.dal.dataobject.demo.demo03.Demo03StudentDO;
import cn.iocoder.yudao.module.infra.dal.mysql.demo.demo03.Demo03CourseMapper;
import cn.iocoder.yudao.module.infra.dal.mysql.demo.demo03.Demo03GradeMapper;
import cn.iocoder.yudao.module.infra.dal.mysql.demo.demo03.Demo03StudentMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.List;

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
        if (list != null) {
            list.forEach(o -> o.setStudentId(studentId));
        }
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

    @Override
    public PageResult<Demo03CourseDO> getDemo03CoursePage(PageParam pageReqVO, Long studentId) {
        return demo03CourseMapper.selectPage(pageReqVO, studentId);
    }

    @Override
    public Long createDemo03Course(Demo03CourseDO demo03Course) {
        demo03CourseMapper.insert(demo03Course);
        return demo03Course.getId();
    }

    @Override
    public void updateDemo03Course(Demo03CourseDO demo03Course) {
        demo03CourseMapper.updateById(demo03Course);
    }

    @Override
    public void deleteDemo03Course(Long id) {
        demo03CourseMapper.deleteById(id);
    }

    @Override
    public Demo03CourseDO getDemo03Course(Long id) {
        return demo03CourseMapper.selectById(id);
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

    @Override
    public PageResult<Demo03GradeDO> getDemo03GradePage(PageParam pageReqVO, Long studentId) {
        return demo03GradeMapper.selectPage(pageReqVO, studentId);
    }

    @Override
    public Long createDemo03Grade(Demo03GradeDO demo03Grade) {
        // 校验是否已经存在
        if (demo03GradeMapper.selectByStudentId(demo03Grade.getStudentId()) != null) {
            throw exception(DEMO03_GRADE_EXISTS);
        }
        demo03GradeMapper.insert(demo03Grade);
        return demo03Grade.getId();
    }

    @Override
    public void updateDemo03Grade(Demo03GradeDO demo03Grade) {
        // 校验存在
        validateDemo03GradeExists(demo03Grade.getId());
        // 更新
        demo03GradeMapper.updateById(demo03Grade);
    }

    @Override
    public void deleteDemo03Grade(Long id) {
        // 校验存在
        validateDemo03GradeExists(id);
        // 删除
        demo03GradeMapper.deleteById(id);
    }

    @Override
    public Demo03GradeDO getDemo03Grade(Long id) {
        return demo03GradeMapper.selectById(id);
    }

    private void validateDemo03GradeExists(Long id) {
        if (demo03GradeMapper.selectById(id) == null) {
            throw exception(DEMO03_GRADE_NOT_EXISTS);
        }
    }

}