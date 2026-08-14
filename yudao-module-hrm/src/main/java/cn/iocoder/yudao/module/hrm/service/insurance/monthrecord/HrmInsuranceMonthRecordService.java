package cn.iocoder.yudao.module.hrm.service.insurance.monthrecord;

import cn.iocoder.yudao.module.hrm.controller.admin.insurance.vo.monthrecord.HrmInsuranceMonthRecordCreateReqVO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.insurance.monthrecord.HrmInsuranceMonthRecordDO;

import java.util.List;

/**
 * HRM 月度社保 Service 接口
 *
 * @author 芋道源码
 */
public interface HrmInsuranceMonthRecordService {

    /**
     * 创建首月社保表
     *
     * @param reqVO 首月年月
     * @return 月度社保表编号
     */
    Long createFirstMonthRecord(HrmInsuranceMonthRecordCreateReqVO reqVO);

    /**
     * 创建次月社保表
     *
     * @return 月度社保表编号
     */
    Long createNextMonthRecord();

    /**
     * 删除月度社保表
     *
     * @param id 月度社保表编号
     */
    void deleteMonthRecord(Long id);

    /**
     * 获得月度社保表
     *
     * @param id 月度社保表编号
     * @return 月度社保表
     */
    HrmInsuranceMonthRecordDO getMonthRecord(Long id);

    /**
     * 获得最近月度社保表
     *
     * @return 最近月度社保表
     */
    HrmInsuranceMonthRecordDO getLastMonthRecord();

    /**
     * 获得月度社保表列表
     *
     * @param year 年份
     * @return 月度社保表列表
     */
    List<HrmInsuranceMonthRecordDO> getMonthRecordList(Integer year);

    /**
     * 校验月度社保表是否存在
     *
     * @param id 月度社保表编号
     * @return 月度社保表
     */
    HrmInsuranceMonthRecordDO validateMonthRecordExists(Long id);

    /**
     * 校验指定年月的月度社保表是否存在
     *
     * @param year 年份
     * @param month 月份
     * @return 月度社保表
     */
    HrmInsuranceMonthRecordDO validateMonthRecordExists(Integer year, Integer month);

    /**
     * 校验月度社保表是否可编辑，并锁定记录
     *
     * @param id 月度社保表编号
     * @return 月度社保表
     */
    HrmInsuranceMonthRecordDO validateMonthRecordEditableForUpdate(Long id);

    /**
     * 更新月度社保表汇总
     *
     * @param id 月度社保表编号
     */
    void updateMonthRecordSummary(Long id);

}
