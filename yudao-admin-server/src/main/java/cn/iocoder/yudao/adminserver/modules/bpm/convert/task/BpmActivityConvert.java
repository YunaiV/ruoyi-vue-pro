package cn.iocoder.yudao.adminserver.modules.bpm.convert.task;

import cn.iocoder.yudao.adminserver.modules.bpm.controller.task.vo.activity.BpmActivityRespVO;
import cn.iocoder.yudao.adminserver.modules.bpm.dal.dataobject.task.BpmTaskExtDO;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import org.activiti.engine.history.HistoricActivityInstance;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.Map;

/**
 * BPM 活动 Convert
 *
 * @author 芋道源码
 */
@Mapper
public interface BpmActivityConvert {

    BpmActivityConvert INSTANCE = Mappers.getMapper(BpmActivityConvert.class);

    List<BpmActivityRespVO> convertList(List<HistoricActivityInstance> list);

    @Mappings({
            @Mapping(source = "activityId", target = "key"),
            @Mapping(source = "activityType", target = "type")
    })
    BpmActivityRespVO convert(HistoricActivityInstance bean);

}
