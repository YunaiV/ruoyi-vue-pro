package cn.iocoder.yudao.module.infra.service.demo02;

import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import cn.iocoder.yudao.module.infra.controller.admin.demo02.vo.*;
import cn.iocoder.yudao.module.infra.dal.dataobject.demo02.InfraDemoStudentDO;
import cn.iocoder.yudao.module.infra.dal.dataobject.demo02.InfraDemoStudentContactDO;
import cn.iocoder.yudao.module.infra.dal.dataobject.demo02.InfraDemoStudentAddressDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

import cn.iocoder.yudao.module.infra.convert.demo02.InfraDemoStudentConvert;
import cn.iocoder.yudao.module.infra.dal.mysql.demo02.InfraDemoStudentMapper;
import cn.iocoder.yudao.module.infra.dal.mysql.demo02.InfraDemoStudentContactMapper;
import cn.iocoder.yudao.module.infra.dal.mysql.demo02.InfraDemoStudentAddressMapper;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.infra.enums.ErrorCodeConstants.*;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;

/**
 * 学生 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class InfraDemoStudentServiceImpl implements InfraDemoStudentService {

    @Resource
    private InfraDemoStudentMapper demoStudentMapper;
    @Resource
    private InfraDemoStudentContactMapper demoStudentContactMapper;
    @Resource
    private InfraDemoStudentAddressMapper demoStudentAddressMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createDemoStudent(InfraDemoStudentCreateReqVO createReqVO) {
        // 插入
        InfraDemoStudentDO demoStudent = InfraDemoStudentConvert.INSTANCE.convert(createReqVO);
        demoStudentMapper.insert(demoStudent);

        // 插入子表（$subTable.classComment）
        createDemoStudentContactList(demoStudent.getId(), createReqVO.getDemoStudentContacts());
        createDemoStudentAddress(demoStudent.getId(), createReqVO.getDemoStudentAddress());
        // 返回
        return demoStudent.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateDemoStudent(InfraDemoStudentUpdateReqVO updateReqVO) {
        // 校验存在
        validateDemoStudentExists(updateReqVO.getId());
        // 更新
        InfraDemoStudentDO updateObj = InfraDemoStudentConvert.INSTANCE.convert(updateReqVO);
        demoStudentMapper.updateById(updateObj);

        // 更新子表
        updateDemoStudentContactList(updateReqVO.getId(), updateReqVO.getDemoStudentContacts());
        updateDemoStudentAddress(updateReqVO.getId(), updateReqVO.getDemoStudentAddress());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDemoStudent(Long id) {
        // 校验存在
        validateDemoStudentExists(id);
        // 删除
        demoStudentMapper.deleteById(id);

        // 删除子表
        deleteDemoStudentContactByStudentId(id);
        deleteDemoStudentAddressByStudentId(id);
    }

    private void validateDemoStudentExists(Long id) {
        if (demoStudentMapper.selectById(id) == null) {
            throw exception(DEMO_STUDENT_NOT_EXISTS);
        }
    }

    @Override
    public InfraDemoStudentDO getDemoStudent(Long id) {
        return demoStudentMapper.selectById(id);
    }

    @Override
    public PageResult<InfraDemoStudentDO> getDemoStudentPage(InfraDemoStudentPageReqVO pageReqVO) {
        return demoStudentMapper.selectPage(pageReqVO);
    }

    @Override
    public List<InfraDemoStudentDO> getDemoStudentList(InfraDemoStudentExportReqVO exportReqVO) {
        return demoStudentMapper.selectList(exportReqVO);
    }

    // ==================== 子表（学生联系人） ====================

    @Override
    public List<InfraDemoStudentContactDO> getDemoStudentContactListByStudentId(Long studentId) {
        return demoStudentContactMapper.selectListByStudentId(studentId);
    }

    private void createDemoStudentContactList(Long studentId, List<InfraDemoStudentContactDO> list) {
        list.forEach(o -> o.setStudentId(studentId));
        demoStudentContactMapper.insertBatch(list);
    }

    private void updateDemoStudentContactList(Long studentId, List<InfraDemoStudentContactDO> list) {
        deleteDemoStudentContactByStudentId(studentId);
        createDemoStudentContactList(studentId, list);
    }

    private void deleteDemoStudentContactByStudentId(Long studentId) {
        demoStudentContactMapper.deleteByStudentId(studentId);
    }

    // ==================== 子表（学生地址） ====================

    @Override
    public InfraDemoStudentAddressDO getDemoStudentAddressByStudentId(Long studentId) {
        return demoStudentAddressMapper.selectByStudentId(studentId);
    }

    private void createDemoStudentAddress(Long studentId, InfraDemoStudentAddressDO demoStudentAddress) {
        demoStudentAddress.setStudentId(studentId);
        demoStudentAddressMapper.insert(demoStudentAddress);
    }

    private void updateDemoStudentAddress(Long studentId, InfraDemoStudentAddressDO demoStudentAddress) {
        demoStudentAddress.setStudentId(studentId);
        demoStudentAddressMapper.updateById(demoStudentAddress);
    }

    private void deleteDemoStudentAddressByStudentId(Long studentId) {
        demoStudentAddressMapper.deleteByStudentId(studentId);
    }

}