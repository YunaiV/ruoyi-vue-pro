package cn.iocoder.yudao.module.bpm.convert.cc;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.bpm.controller.admin.task.vo.instance.BpmProcessInstanceCopyPageItemRespVO;
import cn.iocoder.yudao.module.bpm.dal.dataobject.cc.BpmProcessInstanceCopyDO;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.Map;

/**
 * 流程抄送 Convert
 *
 * @author 芋艿
 */
@Mapper
public interface BpmProcessInstanceCopyConvert {

    BpmProcessInstanceCopyConvert INSTANCE = Mappers.getMapper(BpmProcessInstanceCopyConvert.class);

    default PageResult<BpmProcessInstanceCopyPageItemRespVO> convertPage(PageResult<BpmProcessInstanceCopyDO> page,
                                                                         Map<String, String> taskNameMap,
                                                                         Map<String, String> processInstaneNameMap,
                                                                         Map<Long, AdminUserRespDTO> userMap) {
        List<BpmProcessInstanceCopyPageItemRespVO> list = BeanUtils.toBean(page.getList(),
                BpmProcessInstanceCopyPageItemRespVO.class,
                copy -> {
                    MapUtils.findAndThen(userMap, Long.valueOf(copy.getCreator()), user -> user.setNickname(user.getNickname()));
                    MapUtils.findAndThen(userMap, copy.getStartUserId(), user -> copy.setStartUserNickname(user.getNickname()));
                    MapUtils.findAndThen(taskNameMap, copy.getTaskId(), copy::setTaskName);
                    MapUtils.findAndThen(processInstaneNameMap, copy.getProcessInstanceId(), copy::setProcessInstanceName);
                });
        return new PageResult<>(list, page.getTotal());
    }

}
