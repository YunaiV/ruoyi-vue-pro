package cn.iocoder.yudao.module.infra.service.demo01;

import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import cn.iocoder.yudao.module.infra.controller.admin.demo01.vo.*;
import cn.iocoder.yudao.module.infra.dal.dataobject.demo01.InfraDemo01StudentDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

import cn.iocoder.yudao.module.infra.convert.demo01.InfraDemo01StudentConvert;
import cn.iocoder.yudao.module.infra.dal.mysql.demo01.InfraDemo01StudentMapper;

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
public class InfraDemo01StudentServiceImpl implements InfraDemo01StudentService {

    @Resource
    private InfraDemo01StudentMapper demo01StudentMapper;

    @Override
    public Long createDemo01Student(InfraDemo01StudentCreateReqVO createReqVO) {
        // 插入
        InfraDemo01StudentDO demo01Student = InfraDemo01StudentConvert.INSTANCE.convert(createReqVO);
        demo01StudentMapper.insert(demo01Student);
        // 返回
        return demo01Student.getId();
    }

    @Override
    public void updateDemo01Student(InfraDemo01StudentUpdateReqVO updateReqVO) {
        // 校验存在
        validateDemo01StudentExists(updateReqVO.getId());
        // 更新
        InfraDemo01StudentDO updateObj = InfraDemo01StudentConvert.INSTANCE.convert(updateReqVO);
        demo01StudentMapper.updateById(updateObj);
    }

    @Override
    public void deleteDemo01Student(Long id) {
        // 校验存在
        validateDemo01StudentExists(id);
        // 删除
        demo01StudentMapper.deleteById(id);
    }

    private void validateDemo01StudentExists(Long id) {
        if (demo01StudentMapper.selectById(id) == null) {
            throw exception(DEMO01_STUDENT_NOT_EXISTS);
        }
    }

    @Override
    public InfraDemo01StudentDO getDemo01Student(Long id) {
        return demo01StudentMapper.selectById(id);
    }

    @Override
    public PageResult<InfraDemo01StudentDO> getDemo01StudentPage(InfraDemo01StudentPageReqVO pageReqVO) {
        return demo01StudentMapper.selectPage(pageReqVO);
    }

    @Override
    public List<InfraDemo01StudentDO> getDemo01StudentList(InfraDemo01StudentExportReqVO exportReqVO) {
        return demo01StudentMapper.selectList(exportReqVO);
    }

}