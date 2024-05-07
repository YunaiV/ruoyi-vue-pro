package cn.iocoder.yudao.module.weapp.service.appsclass;

import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import cn.iocoder.yudao.module.weapp.controller.admin.appsclass.vo.*;
import cn.iocoder.yudao.module.weapp.dal.dataobject.appsclass.AppsClassDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;

import cn.iocoder.yudao.module.weapp.dal.mysql.appsclass.AppsClassMapper;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.weapp.enums.ErrorCodeConstants.*;

/**
 * 小程序分类 Service 实现类
 *
 * @author jingjianqian
 */
@Service
@Validated
public class AppsClassServiceImpl implements AppsClassService {

    @Resource
    private AppsClassMapper appsClassMapper;

    @Override
    public Integer createAppsClass(AppsClassSaveReqVO createReqVO) {
        // 插入
        AppsClassDO appsClass = BeanUtils.toBean(createReqVO, AppsClassDO.class);
        appsClassMapper.insert(appsClass);
        // 返回
        return appsClass.getId();
    }

    @Override
    public void updateAppsClass(AppsClassSaveReqVO updateReqVO) {
        // 校验存在
        validateAppsClassExists(updateReqVO.getId());
        // 更新
        AppsClassDO updateObj = BeanUtils.toBean(updateReqVO, AppsClassDO.class);
        appsClassMapper.updateById(updateObj);
    }

    @Override
    public void deleteAppsClass(Integer id) {
        // 校验存在
        validateAppsClassExists(id);
        // 删除
        appsClassMapper.deleteById(id);
    }

    private void validateAppsClassExists(Integer id) {
        if (appsClassMapper.selectById(id) == null) {
            throw exception(APPS_CLASS_NOT_EXISTS);
        }
    }

    @Override
    public AppsClassDO getAppsClass(Integer id) {
        return appsClassMapper.selectById(id);
    }

    @Override
    public PageResult<AppsClassDO> getAppsClassPage(AppsClassPageReqVO pageReqVO) {
        return appsClassMapper.selectPage(pageReqVO);
    }

}