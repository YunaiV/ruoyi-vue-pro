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
    @Transactional(rollbackFor = Exception.class)
    public Long createDemo12Student(InfraDemo12StudentCreateReqVO createReqVO) {
        // 插入
        InfraDemo12StudentDO demo12Student = InfraDemo12StudentConvert.INSTANCE.convert(createReqVO);
        demo12StudentMapper.insert(demo12Student);

        // 插入子表
        createDemo12StudentContactList(demo12Student.getId(), createReqVO.getDemo12StudentContacts());
        createDemo12StudentTeacher(demo12Student.getId(), createReqVO.getDemo12StudentTeacher());
        // 返回
        return demo12Student.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateDemo12Student(InfraDemo12StudentUpdateReqVO updateReqVO) {
        // 校验存在
        validateDemo12StudentExists(updateReqVO.getId());
        // 更新
        InfraDemo12StudentDO updateObj = InfraDemo12StudentConvert.INSTANCE.convert(updateReqVO);
        demo12StudentMapper.updateById(updateObj);

        // 更新子表
        updateDemo12StudentContactList(updateReqVO.getId(), updateReqVO.getDemo12StudentContacts());
        updateDemo12StudentTeacher(updateReqVO.getId(), updateReqVO.getDemo12StudentTeacher());
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
    public List<InfraDemo12StudentContactDO> getDemo12StudentContactListByStudentId(Long studentId) {
        return demo12StudentContactMapper.selectListByStudentId(studentId);
    }

    private void createDemo12StudentContactList(Long studentId, List<InfraDemo12StudentContactDO> list) {
        list.forEach(o -> o.setStudentId(studentId));
        demo12StudentContactMapper.insertBatch(list);
    }

    private void updateDemo12StudentContactList(Long studentId, List<InfraDemo12StudentContactDO> list) {
        deleteDemo12StudentContactByStudentId(studentId);
		list.forEach(o -> o.setId(null).setUpdater(null).setUpdateTime(null)); // 解决更新情况下：1）id 冲突；2）updateTime 不更新
        createDemo12StudentContactList(studentId, list);
    }

    private void deleteDemo12StudentContactByStudentId(Long studentId) {
        demo12StudentContactMapper.deleteByStudentId(studentId);
    }

    // ==================== 子表（学生班主任） ====================

    @Override
    public InfraDemo12StudentTeacherDO getDemo12StudentTeacherByStudentId(Long studentId) {
        return demo12StudentTeacherMapper.selectByStudentId(studentId);
    }

    private void createDemo12StudentTeacher(Long studentId, InfraDemo12StudentTeacherDO demo12StudentTeacher) {
        if (demo12StudentTeacher == null) {
            return;
        }
        demo12StudentTeacher.setStudentId(studentId);
        demo12StudentTeacherMapper.insert(demo12StudentTeacher);
    }

    private void updateDemo12StudentTeacher(Long studentId, InfraDemo12StudentTeacherDO demo12StudentTeacher) {
        if (demo12StudentTeacher == null) {
			return;
        }
        demo12StudentTeacher.setStudentId(studentId);
        demo12StudentTeacher.setUpdater(null).setUpdateTime(null); // 解决更新情况下：updateTime 不更新
        demo12StudentTeacherMapper.insertOrUpdate(demo12StudentTeacher);
    }

    private void deleteDemo12StudentTeacherByStudentId(Long studentId) {
        demo12StudentTeacherMapper.deleteByStudentId(studentId);
    }

}