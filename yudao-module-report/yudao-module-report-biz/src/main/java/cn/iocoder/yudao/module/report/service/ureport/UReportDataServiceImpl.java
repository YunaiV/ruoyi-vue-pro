package cn.iocoder.yudao.module.report.service.ureport;

import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;

import cn.iocoder.yudao.module.report.controller.admin.ureport.vo.*;
import cn.iocoder.yudao.module.report.dal.dataobject.ureport.UReportDataDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;

import cn.iocoder.yudao.module.report.dal.mysql.ureport.UReportDataMapper;

import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.report.enums.ErrorCodeConstants.*;

/**
 * Ureport2报表 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class UReportDataServiceImpl implements UReportDataService {

    @Resource
    private UReportDataMapper uReportDataMapper;

    @Override
    public Long createUReportData(UReportDataSaveReqVO createReqVO) {
        // 插入
        UReportDataDO uReportData = BeanUtils.toBean(createReqVO, UReportDataDO.class);
        uReportDataMapper.insert(uReportData);
        // 返回
        return uReportData.getId();
    }

    @Override
    public void updateUReportData(UReportDataSaveReqVO updateReqVO) {
        // 校验存在
        validateUReportDataExists(updateReqVO.getId());
        // 更新
        UReportDataDO updateObj = BeanUtils.toBean(updateReqVO, UReportDataDO.class);
        uReportDataMapper.updateById(updateObj);
    }

    @Override
    public void deleteUReportData(Long id) {
        // 校验存在
        validateUReportDataExists(id);
        // 删除
        uReportDataMapper.deleteById(id);
    }

    private void validateUReportDataExists(Long id) {
        if (uReportDataMapper.selectById(id) == null) {
            throw exception(REPORT_DATA_NOT_EXISTS);
        }
    }

    @Override
    public void validateUReportDataExists(String name) {
        if (uReportDataMapper.selectByName(name) == null) {
            throw exception(REPORT_DATA_NOT_EXISTS);
        }
    }

    @Override
    public UReportDataDO getUReportData(Long id) {
        return uReportDataMapper.selectById(id);
    }

    @Override
    public PageResult<UReportDataDO> getUReportDataPage(UReportDataPageReqVO pageReqVO) {
        return uReportDataMapper.selectPage(pageReqVO);
    }

    @Override
    public int deleteByName(String name) {
        return uReportDataMapper.deleteByName(name);
    }

    @Override
    public UReportDataDO selectOneByName(String name) {
        return uReportDataMapper.selectOneByName(name);
    }

    @Override
    public List<UReportDataDO> getReportDataList() {
        return uReportDataMapper.selectList();
    }
}
