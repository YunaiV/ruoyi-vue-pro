package cn.iocoder.yudao.module.point.service.pointrecord;

import java.util.*;
import javax.validation.*;
import cn.iocoder.yudao.module.point.controller.admin.pointrecord.vo.*;
import cn.iocoder.yudao.module.point.dal.dataobject.pointrecord.PointRecordDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

/**
 * 用户积分记录 Service 接口
 *
 * @author QingX
 */
public interface PointRecordService {

    /**
     * 创建用户积分记录
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createRecord(@Valid PointRecordCreateReqVO createReqVO);

    /**
     * 更新用户积分记录
     *
     * @param updateReqVO 更新信息
     */
    void updateRecord(@Valid PointRecordUpdateReqVO updateReqVO);

    /**
     * 删除用户积分记录
     *
     * @param id 编号
     */
    void deleteRecord(Long id);

    /**
     * 获得用户积分记录
     *
     * @param id 编号
     * @return 用户积分记录
     */
    PointRecordDO getRecord(Long id);

    /**
     * 获得用户积分记录列表
     *
     * @param ids 编号
     * @return 用户积分记录列表
     */
    List<PointRecordDO> getRecordList(Collection<Long> ids);

    /**
     * 获得用户积分记录分页
     *
     * @param pageReqVO 分页查询
     * @return 用户积分记录分页
     */
    PageResult<PointRecordDO> getRecordPage(PointRecordPageReqVO pageReqVO);

    /**
     * 获得用户积分记录列表, 用于 Excel 导出
     *
     * @param exportReqVO 查询条件
     * @return 用户积分记录列表
     */
    List<PointRecordDO> getRecordList(PointRecordExportReqVO exportReqVO);

}
