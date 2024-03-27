package cn.iocoder.yudao.module.report.service.ureport;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.report.controller.admin.ureport.vo.UReportDataPageReqVO;
import cn.iocoder.yudao.module.report.controller.admin.ureport.vo.UReportDataSaveReqVO;
import cn.iocoder.yudao.module.report.dal.dataobject.ureport.UReportDataDO;

import javax.validation.Valid;
import java.util.List;

/**
 * Ureport2 报表 Service 接口
 *
 * @author 芋道源码
 */
public interface UReportDataService {

    /**
     * 创建 Ureport2 报表
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createUReportData(@Valid UReportDataSaveReqVO createReqVO);

    /**
     * 更新 Ureport2 报表
     *
     * @param updateReqVO 更新信息
     */
    void updateUReportData(@Valid UReportDataSaveReqVO updateReqVO);

    /**
     * 删除 Ureport2 报表
     *
     * @param id 编号
     */
    void deleteUReportData(Long id);

    /**
     * 获得 Ureport2 报表
     *
     * @param id 编号
     * @return Ureport2 报表
     */
    UReportDataDO getUReportData(Long id);

    /**
     * 获得 Ureport2 报表分页
     *
     * @param pageReqVO 分页查询
     * @return Ureport2 报表分页
     */
    PageResult<UReportDataDO> getUReportDataPage(UReportDataPageReqVO pageReqVO);

    // TODO @赤焰：可以不用返回 int。如果不需要哈。
    /**
     * 根据名称删除报表
     *
     * @param name 报表名称
     * @return
     */
    int deleteByName(String name);

    // TODO @赤焰：这里直接返回 UReportDataDO 是不是更好？上层业务直接使用啦
    /**
     * 根据名称校验报表是否存在
     *
     * @param name 报表名称
     */
    void validateUReportDataExists(String name);

    // TODO @赤焰：这里方法名改成 getUReportDataByName。select 只用于 mapper；
    /**
     * 根据名称查询报表
     *
     * @param name 报表名称
     * @return Ureport2 报表
     */
    UReportDataDO selectOneByName(String name);

    /**
     * 获取全部报表
     *
     * @return 全部报表
     */
    List<UReportDataDO> getReportDataList();

}
