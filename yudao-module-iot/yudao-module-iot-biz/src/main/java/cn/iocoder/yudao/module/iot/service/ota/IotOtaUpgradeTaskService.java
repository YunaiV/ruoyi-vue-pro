package cn.iocoder.yudao.module.iot.service.ota;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.ota.vo.upgrade.task.IotOtaUpgradeTaskPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.ota.vo.upgrade.task.IotOtaUpgradeTaskSaveReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.ota.IotOtaUpgradeTaskDO;

import javax.validation.Valid;
import java.util.List;

/**
 * IoT OTA升级任务 Service 接口
 *
 * @author Shelly Chan
 */
public interface IotOtaUpgradeTaskService {

    /**
     * 创建OTA升级任务
     *
     * @param createReqVO OTA升级任务的创建请求对象，包含创建任务所需的信息
     * @return 创建成功的OTA升级任务的ID
     */
    Long createUpgradeTask(@Valid IotOtaUpgradeTaskSaveReqVO createReqVO);

    /**
     * 取消OTA升级任务
     *
     * @param id 要取消的OTA升级任务的ID
     */
    void cancelUpgradeTask(Long id);

    /**
     * 根据ID获取OTA升级任务的详细信息
     *
     * @param id OTA升级任务的ID
     * @return OTA升级任务的详细信息对象
     */
    IotOtaUpgradeTaskDO getUpgradeTask(Long id);

    /**
     * 分页查询OTA升级任务
     *
     * @param pageReqVO OTA升级任务的分页查询请求对象，包含查询条件和分页信息
     * @return 分页查询结果，包含OTA升级任务列表和总记录数
     */
    PageResult<IotOtaUpgradeTaskDO> getUpgradeTaskPage(@Valid IotOtaUpgradeTaskPageReqVO pageReqVO);

    /**
     * 根据任务状态获取升级任务列表
     *
     * @param state 任务状态，用于筛选符合条件的升级任务
     * @return 返回符合指定状态的升级任务列表，列表中的元素为 IotOtaUpgradeTaskDO 对象
     */
    List<IotOtaUpgradeTaskDO> getUpgradeTaskByState(Integer state);

    /**
     * 更新升级任务的状态。
     * <p>
     * 该函数用于根据任务ID更新指定升级任务的状态。通常用于在任务执行过程中
     * 更新任务的状态，例如从“进行中”变为“已完成”或“失败”。
     *
     * @param id     升级任务的唯一标识符，类型为Long。不能为null。
     * @param status 要更新的任务状态，类型为Integer。通常表示任务的状态码，如0表示未开始，1表示进行中，2表示已完成等。
     */
    void updateUpgradeTaskStatus(Long id, Integer status);

}
