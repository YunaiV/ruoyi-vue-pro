package cn.iocoder.yudao.module.infra.service.demo.demo03.erp;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.infra.controller.admin.demo.demo03.erp.vo.Demo03StudentPageReqVO;
import cn.iocoder.yudao.module.infra.controller.admin.demo.demo03.erp.vo.Demo03StudentSaveReqVO;
import cn.iocoder.yudao.module.infra.dal.dataobject.demo.demo03.Demo03CourseDO;
import cn.iocoder.yudao.module.infra.dal.dataobject.demo.demo03.Demo03GradeDO;
import cn.iocoder.yudao.module.infra.dal.dataobject.demo.demo03.Demo03StudentDO;
import cn.iocoder.yudao.module.infra.dal.mysql.demo.demo03.erp.Demo03CourseMapper;
import cn.iocoder.yudao.module.infra.dal.mysql.demo.demo03.erp.Demo03GradeMapper;
import cn.iocoder.yudao.module.infra.dal.mysql.demo.demo03.erp.Demo03StudentMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

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
    public Long createDemo03Student(Demo03StudentSaveReqVO createReqVO) {
        // 插入
        Demo03StudentDO demo03Student = BeanUtils.toBean(createReqVO, Demo03StudentDO.class);
        demo03StudentMapper.insert(demo03Student);
        // 返回
        return demo03Student.getId();
    }

    @Override
    public void updateDemo03Student(Demo03StudentSaveReqVO updateReqVO) {
        // 校验存在
        validateDemo03StudentExists(updateReqVO.getId());
        // 更新
        Demo03StudentDO updateObj = BeanUtils.toBean(updateReqVO, Demo03StudentDO.class);
        demo03StudentMapper.updateById(updateObj);
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDemo03StudentByIds(List<Long> ids) {
        // 校验存在
        validateDemo03StudentExists(ids);
        // 删除
        demo03StudentMapper.deleteByIds(ids);

        // 删除子表
        deleteDemo03CourseByStudentIds(ids);
        deleteDemo03GradeByStudentIds(ids);
    }

    private void validateDemo03StudentExists(List<Long> ids) {
        List<Demo03StudentDO> list = demo03StudentMapper.selectByIds(ids);
        if (CollUtil.isEmpty(list) || list.size() != ids.size()) {
            throw exception(DEMO03_STUDENT_NOT_EXISTS);
        }
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
        // 校验存在
        validateDemo03CourseExists(demo03Course.getId());
        // 更新
        demo03Course.setUpdater(null).setUpdateTime(null); // 解决更新情况下：updateTime 不更新
        demo03CourseMapper.updateById(demo03Course);
    }

    @Override
    public void deleteDemo03Course(Long id) {
        // 校验存在
        validateDemo03CourseExists(id);
        // 删除
        demo03CourseMapper.deleteById(id);
    }

    @Override
    public void deleteDemo03CourseByIds(List<Long> ids) {
        // 校验存在
        validateDemo03CourseExists(ids);
        // 删除
        demo03CourseMapper.deleteByIds(ids);
    }

    @Override
    public Demo03CourseDO getDemo03Course(Long id) {
        return demo03CourseMapper.selectById(id);
    }

    private void validateDemo03CourseExists(Long id) {
        if (demo03CourseMapper.selectById(id) == null) {
            throw exception(DEMO03_COURSE_NOT_EXISTS);
        }
    }

    private void validateDemo03CourseExists(List<Long> ids) {
        List<Demo03CourseDO> list = demo03CourseMapper.selectByIds(ids);
        if (CollUtil.isEmpty(list) || list.size() != ids.size()) {
            throw exception(DEMO03_COURSE_NOT_EXISTS);
        }
    }

    private void deleteDemo03CourseByStudentId(Long studentId) {
        demo03CourseMapper.deleteByStudentId(studentId);
    }

    private void deleteDemo03CourseByStudentIds(List<Long> studentIds) {
        demo03CourseMapper.deleteByStudentIds(studentIds);
    }

    // ==================== 子表（学生班级） ====================

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
        // 插入
        demo03GradeMapper.insert(demo03Grade);
        return demo03Grade.getId();
    }

    @Override
    public void updateDemo03Grade(Demo03GradeDO demo03Grade) {
        // 校验存在
        validateDemo03GradeExists(demo03Grade.getId());
        // 更新
        demo03Grade.setUpdater(null).setUpdateTime(null); // 解决更新情况下：updateTime 不更新
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
    public void deleteDemo03GradeByIds(List<Long> ids) {
        // 校验存在
        validateDemo03GradeExists(ids);
        // 删除
        demo03GradeMapper.deleteByIds(ids);
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

    private void validateDemo03GradeExists(List<Long> ids) {
        List<Demo03GradeDO> list = demo03GradeMapper.selectByIds(ids);
        if (CollUtil.isEmpty(list) || list.size() != ids.size()) {
            throw exception(DEMO03_GRADE_NOT_EXISTS);
        }
    }

    private void deleteDemo03GradeByStudentId(Long studentId) {
        demo03GradeMapper.deleteByStudentId(studentId);
    }

    private void deleteDemo03GradeByStudentIds(List<Long> studentIds) {
        demo03GradeMapper.deleteByStudentIds(studentIds);
    }

}