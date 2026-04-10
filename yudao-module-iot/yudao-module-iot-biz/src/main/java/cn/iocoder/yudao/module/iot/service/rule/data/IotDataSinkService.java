package cn.iocoder.yudao.module.iot.service.rule.data;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.rule.vo.data.sink.IotDataSinkPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.rule.vo.data.sink.IotDataSinkSaveReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.rule.IotDataSinkDO;
import jakarta.validation.Valid;

import java.util.Collection;
import java.util.List;

/**
 * IoT 数据流转目的 Service 接口
 *
 * @author HUIHUI
 */
public interface IotDataSinkService {

    /**
     * 创建数据流转目的
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createDataSink(@Valid IotDataSinkSaveReqVO createReqVO);

    /**
     * 更新数据流转目的
     *
     * @param updateReqVO 更新信息
     */
    void updateDataSink(@Valid IotDataSinkSaveReqVO updateReqVO);

    /**
     * 删除数据流转目的
     *
     * @param id 编号
     */
    void deleteDataSink(Long id);

    /**
     * 获得数据流转目的
     *
     * @param id 编号
     * @return 数据流转目的
     */
    IotDataSinkDO getDataSink(Long id);

    /**
     * 从缓存中获得数据流转目的
     *
     * @param id 编号
     * @return 数据流转目的
     */
    IotDataSinkDO getDataSinkFromCache(Long id);

    /**
     * 获得数据流转目的分页
     *
     * @param pageReqVO 分页查询
     * @return 数据流转目的分页
     */
    PageResult<IotDataSinkDO> getDataSinkPage(IotDataSinkPageReqVO pageReqVO);

    /**
     * 获取数据流转目的列表
     *
     * @param status 状态，如果为空，则不进行筛选
     * @return 数据流转目的列表
     */
    List<IotDataSinkDO> getDataSinkListByStatus(Integer status);

    /**
     * 批量校验数据目的存在
     *
     * @param ids 数据目的编号集合
     */
    void validateDataSinksExist(Collection<Long> ids);

}
