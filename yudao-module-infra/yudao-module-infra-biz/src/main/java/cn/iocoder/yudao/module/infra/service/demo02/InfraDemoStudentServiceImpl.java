package cn.iocoder.yudao.module.infra.service.demo02;

import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import cn.iocoder.yudao.module.infra.controller.admin.demo02.vo.*;
import cn.iocoder.yudao.module.infra.dal.dataobject.demo02.InfraDemoStudentDO;
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
        // 插入子表（学生联系人）
        createReqVO.getDemoStudentContacts().forEach(o -> o.setStudentId(demoStudent.getId()));
        demoStudentContactMapper.insertBatch(createReqVO.getDemoStudentContacts());
        // 插入子表（学生地址）
        createReqVO.getDemoStudentAddress().setStudentId(demoStudent.getId());
        demoStudentAddressMapper.insert(createReqVO.getDemoStudentAddress());
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
        // 更新子表（学生联系人）
        demoStudentContactMapper.deleteByStudentId(updateReqVO.getId());
        updateReqVO.getDemoStudentContacts().forEach(o -> o.setStudentId(updateReqVO.getId()));
        demoStudentContactMapper.insertBatch(updateReqVO.getDemoStudentContacts());
        // 更新子表（学生地址）
        updateReqVO.getDemoStudentAddress().setStudentId(updateReqVO.getId());
        demoStudentAddressMapper.updateById(updateReqVO.getDemoStudentAddress());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDemoStudent(Long id) {
        // 校验存在
        validateDemoStudentExists(id);
        // 删除
        demoStudentMapper.deleteById(id);
        // 删除子表（学生联系人）
        demoStudentContactMapper.deleteByStudentId(id);
        // 删除子表（学生地址）
        demoStudentAddressMapper.deleteByStudentId(id);
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

}