package cn.iocoder.yudao.module.infra.service.demo11;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.infra.controller.admin.demo11.vo.InfraDemo11StudentCreateReqVO;
import cn.iocoder.yudao.module.infra.controller.admin.demo11.vo.InfraDemo11StudentExportReqVO;
import cn.iocoder.yudao.module.infra.controller.admin.demo11.vo.InfraDemo11StudentPageReqVO;
import cn.iocoder.yudao.module.infra.controller.admin.demo11.vo.InfraDemo11StudentUpdateReqVO;
import cn.iocoder.yudao.module.infra.convert.demo11.InfraDemo11StudentConvert;
import cn.iocoder.yudao.module.infra.dal.dataobject.demo11.InfraDemo11StudentContactDO;
import cn.iocoder.yudao.module.infra.dal.dataobject.demo11.InfraDemo11StudentDO;
import cn.iocoder.yudao.module.infra.dal.dataobject.demo11.InfraDemo11StudentTeacherDO;
import cn.iocoder.yudao.module.infra.dal.mysql.demo11.InfraDemo11StudentContactMapper;
import cn.iocoder.yudao.module.infra.dal.mysql.demo11.InfraDemo11StudentMapper;
import cn.iocoder.yudao.module.infra.dal.mysql.demo11.InfraDemo11StudentTeacherMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.infra.enums.ErrorCodeConstants.DEMO11_STUDENT_NOT_EXISTS;

/**
 * 学生 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class InfraDemo11StudentServiceImpl implements InfraDemo11StudentService {

    @Resource
    private InfraDemo11StudentMapper demo11StudentMapper;
    @Resource
    private InfraDemo11StudentContactMapper demo11StudentContactMapper;
    @Resource
    private InfraDemo11StudentTeacherMapper demo11StudentTeacherMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createDemo11Student(InfraDemo11StudentCreateReqVO createReqVO) {
        // 插入
        InfraDemo11StudentDO demo11Student = InfraDemo11StudentConvert.INSTANCE.convert(createReqVO);
        demo11StudentMapper.insert(demo11Student);

        // 插入子表
        createDemo11StudentContactList(demo11Student.getId(), createReqVO.getDemo11StudentContacts());
        createDemo11StudentTeacher(demo11Student.getId(), createReqVO.getDemo11StudentTeacher());
        // 返回
        return demo11Student.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateDemo11Student(InfraDemo11StudentUpdateReqVO updateReqVO) {
        // 校验存在
        validateDemo11StudentExists(updateReqVO.getId());
        // 更新
        InfraDemo11StudentDO updateObj = InfraDemo11StudentConvert.INSTANCE.convert(updateReqVO);
        demo11StudentMapper.updateById(updateObj);

        // 更新子表
        updateDemo11StudentContactList(updateReqVO.getId(), updateReqVO.getDemo11StudentContacts());
        updateDemo11StudentTeacher(updateReqVO.getId(), updateReqVO.getDemo11StudentTeacher());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDemo11Student(Long id) {
        // 校验存在
        validateDemo11StudentExists(id);
        // 删除
        demo11StudentMapper.deleteById(id);

        // 删除子表
        deleteDemo11StudentContactByStudentId(id);
        deleteDemo11StudentTeacherByStudentId(id);
    }

    private void validateDemo11StudentExists(Long id) {
        if (demo11StudentMapper.selectById(id) == null) {
            throw exception(DEMO11_STUDENT_NOT_EXISTS);
        }
    }

    @Override
    public InfraDemo11StudentDO getDemo11Student(Long id) {
        return demo11StudentMapper.selectById(id);
    }

    @Override
    public PageResult<InfraDemo11StudentDO> getDemo11StudentPage(InfraDemo11StudentPageReqVO pageReqVO) {
        return demo11StudentMapper.selectPage(pageReqVO);
    }

    @Override
    public List<InfraDemo11StudentDO> getDemo11StudentList(InfraDemo11StudentExportReqVO exportReqVO) {
        return demo11StudentMapper.selectList(exportReqVO);
    }

    // ==================== 子表（学生联系人） ====================

    @Override
    public List<InfraDemo11StudentContactDO> getDemo11StudentContactListByStudentId(Long studentId) {
        return demo11StudentContactMapper.selectListByStudentId(studentId);
    }

    private void createDemo11StudentContactList(Long studentId, List<InfraDemo11StudentContactDO> list) {
        list.forEach(o -> o.setStudentId(studentId));
        demo11StudentContactMapper.insertBatch(list);
    }

    private void updateDemo11StudentContactList(Long studentId, List<InfraDemo11StudentContactDO> list) {
        deleteDemo11StudentContactByStudentId(studentId);
        list.forEach(o -> o.setId(null).setUpdater(null).setUpdateTime(null)); // 解决更新情况下：1）id 冲突；2）updateTime 不更新
        createDemo11StudentContactList(studentId, list);
    }

    private void deleteDemo11StudentContactByStudentId(Long studentId) {
        demo11StudentContactMapper.deleteByStudentId(studentId);
    }

    // ==================== 子表（学生班主任） ====================

    @Override
    public InfraDemo11StudentTeacherDO getDemo11StudentTeacherByStudentId(Long studentId) {
        return demo11StudentTeacherMapper.selectByStudentId(studentId);
    }

    private void createDemo11StudentTeacher(Long studentId, InfraDemo11StudentTeacherDO demo11StudentTeacher) {
        if (demo11StudentTeacher == null) {
            return;
        }
        demo11StudentTeacher.setStudentId(studentId);
        demo11StudentTeacherMapper.insert(demo11StudentTeacher);
    }

    private void updateDemo11StudentTeacher(Long studentId, InfraDemo11StudentTeacherDO demo11StudentTeacher) {
        if (demo11StudentTeacher == null) {
            return;
        }
        demo11StudentTeacher.setStudentId(studentId);
        demo11StudentTeacher.setUpdater(null).setUpdateTime(null); // 解决更新情况下：updateTime 不更新
        demo11StudentTeacherMapper.insertOrUpdate(demo11StudentTeacher);
    }

    private void deleteDemo11StudentTeacherByStudentId(Long studentId) {
        demo11StudentTeacherMapper.deleteByStudentId(studentId);
    }

}