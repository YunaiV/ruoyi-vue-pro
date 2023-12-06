package cn.iocoder.yudao.module.report.service.ureport;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.report.controller.admin.ureport.vo.UReportDataPageReqVO;
import cn.iocoder.yudao.module.report.controller.admin.ureport.vo.UReportDataSaveReqVO;
import cn.iocoder.yudao.module.report.dal.dataobject.ureport.UReportDataDO;

import javax.validation.Valid;
import java.util.List;

/**
 * Ureport2报表 Service 接口
 *
 * @author 芋道源码
 */
public interface UReportDataService {

    /**
     * 创建Ureport2报表
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createUReportData(@Valid UReportDataSaveReqVO createReqVO);

    /**
     * 更新Ureport2报表
     *
     * @param updateReqVO 更新信息
     */
    void updateUReportData(@Valid UReportDataSaveReqVO updateReqVO);

    /**
     * 删除Ureport2报表
     *
     * @param id 编号
     */
    void deleteUReportData(Long id);

    /**
     * 获得Ureport2报表
     *
     * @param id 编号
     * @return Ureport2报表
     */
    UReportDataDO getUReportData(Long id);

    /**
     * 获得Ureport2报表分页
     *
     * @param pageReqVO 分页查询
     * @return Ureport2报表分页
     */
    PageResult<UReportDataDO> getUReportDataPage(UReportDataPageReqVO pageReqVO);

    /**
     * 根据名称删除报表
     * @param name
     * @return
     */
    int deleteByName(String name);

    /**
     * 根据名称校验报表是否存在
     * @param name
     */
    void validateUReportDataExists(String name);

    /**
     * 根据名称查询报表
     * @param name
     * @return
     */
    UReportDataDO selectOneByName(String name);

    /**
     * 获取全部报表
     * @return
     */
    List<UReportDataDO> getReportDataList();

}
