package cn.iocoder.yudao.module.infra.service.demo12;

import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import cn.iocoder.yudao.module.infra.controller.admin.demo12.vo.*;
import cn.iocoder.yudao.module.infra.dal.dataobject.demo12.InfraDemo12StudentDO;
import cn.iocoder.yudao.module.infra.dal.dataobject.demo12.InfraDemo12StudentContactDO;
import cn.iocoder.yudao.module.infra.dal.dataobject.demo12.InfraDemo12StudentTeacherDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;

import cn.iocoder.yudao.module.infra.convert.demo12.InfraDemo12StudentConvert;
import cn.iocoder.yudao.module.infra.dal.mysql.demo12.InfraDemo12StudentMapper;
import cn.iocoder.yudao.module.infra.dal.mysql.demo12.InfraDemo12StudentContactMapper;
import cn.iocoder.yudao.module.infra.dal.mysql.demo12.InfraDemo12StudentTeacherMapper;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.infra.enums.ErrorCodeConstants.*;

/**
 * 学生 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class InfraDemo12StudentServiceImpl implements InfraDemo12StudentService {

    @Resource
    private InfraDemo12StudentMapper demo12StudentMapper;
    @Resource
    private InfraDemo12StudentContactMapper demo12StudentContactMapper;
    @Resource
    private InfraDemo12StudentTeacherMapper demo12StudentTeacherMapper;

    @Override
    public Long createDemo12Student(InfraDemo12StudentCreateReqVO createReqVO) {
        // 插入
        InfraDemo12StudentDO demo12Student = InfraDemo12StudentConvert.INSTANCE.convert(createReqVO);
        demo12StudentMapper.insert(demo12Student);
        // 返回
        return demo12Student.getId();
    }

    @Override
    public void updateDemo12Student(InfraDemo12StudentUpdateReqVO updateReqVO) {
        // 校验存在
        validateDemo12StudentExists(updateReqVO.getId());
        // 更新
        InfraDemo12StudentDO updateObj = InfraDemo12StudentConvert.INSTANCE.convert(updateReqVO);
        demo12StudentMapper.updateById(updateObj);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDemo12Student(Long id) {
        // 校验存在
        validateDemo12StudentExists(id);
        // 删除
        demo12StudentMapper.deleteById(id);

        // 删除子表
        deleteDemo12StudentContactByStudentId(id);
        deleteDemo12StudentTeacherByStudentId(id);
    }

    private void validateDemo12StudentExists(Long id) {
        if (demo12StudentMapper.selectById(id) == null) {
            throw exception(DEMO12_STUDENT_NOT_EXISTS);
        }
    }

    @Override
    public InfraDemo12StudentDO getDemo12Student(Long id) {
        return demo12StudentMapper.selectById(id);
    }

    @Override
    public PageResult<InfraDemo12StudentDO> getDemo12StudentPage(InfraDemo12StudentPageReqVO pageReqVO) {
        return demo12StudentMapper.selectPage(pageReqVO);
    }

    @Override
    public List<InfraDemo12StudentDO> getDemo12StudentList(InfraDemo12StudentExportReqVO exportReqVO) {
        return demo12StudentMapper.selectList(exportReqVO);
    }

    // ==================== 子表（学生联系人） ====================

    @Override
    public PageResult<InfraDemo12StudentContactDO> getDemo12StudentContactPage(PageParam pageReqVO, Long studentId) {
        return demo12StudentContactMapper.selectPage(pageReqVO, studentId);
    }

    @Override
    public Long createDemo12StudentContact(InfraDemo12StudentContactDO demo12StudentContact) {
        demo12StudentContactMapper.insert(demo12StudentContact);
        return demo12StudentContact.getId();
    }

    @Override
    public void updateDemo12StudentContact(InfraDemo12StudentContactDO demo12StudentContact) {
        demo12StudentContactMapper.updateById(demo12StudentContact);
    }

    @Override
    public void deleteDemo12StudentContact(Long id) {
        demo12StudentContactMapper.deleteById(id);
    }

	@Override
	public InfraDemo12StudentContactDO getDemo12StudentContact(Long id) {
        return demo12StudentContactMapper.selectById(id);
	}

    private void deleteDemo12StudentContactByStudentId(Long studentId) {
        demo12StudentContactMapper.deleteByStudentId(studentId);
    }

    // ==================== 子表（学生班主任） ====================

    @Override
    public PageResult<InfraDemo12StudentTeacherDO> getDemo12StudentTeacherPage(PageParam pageReqVO, Long studentId) {
        return demo12StudentTeacherMapper.selectPage(pageReqVO, studentId);
    }

    @Override
    public Long createDemo12StudentTeacher(InfraDemo12StudentTeacherDO demo12StudentTeacher) {
        demo12StudentTeacherMapper.insert(demo12StudentTeacher);
        return demo12StudentTeacher.getId();
    }

    @Override
    public void updateDemo12StudentTeacher(InfraDemo12StudentTeacherDO demo12StudentTeacher) {
        demo12StudentTeacherMapper.updateById(demo12StudentTeacher);
    }

    @Override
    public void deleteDemo12StudentTeacher(Long id) {
        demo12StudentTeacherMapper.deleteById(id);
    }

	@Override
	public InfraDemo12StudentTeacherDO getDemo12StudentTeacher(Long id) {
        return demo12StudentTeacherMapper.selectById(id);
	}

    private void deleteDemo12StudentTeacherByStudentId(Long studentId) {
        demo12StudentTeacherMapper.deleteByStudentId(studentId);
    }

}