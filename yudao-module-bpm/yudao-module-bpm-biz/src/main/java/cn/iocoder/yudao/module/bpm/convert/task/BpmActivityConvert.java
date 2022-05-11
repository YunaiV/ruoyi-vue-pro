package cn.iocoder.yudao.module.bpm.convert.task;

import cn.iocoder.yudao.module.bpm.controller.admin.task.vo.activity.BpmActivityRespVO;
import cn.iocoder.yudao.module.bpm.dal.dataobject.task.BpmActivityDO;
import org.flowable.engine.history.HistoricActivityInstance;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * BPM 活动 Convert
 *
 * @author 芋道源码
 */
@Mapper
public interface BpmActivityConvert {

    BpmActivityConvert INSTANCE = Mappers.getMapper(BpmActivityConvert.class);

    List<BpmActivityRespVO> convertList(List<BpmActivityDO> list);

    @Mappings({
            @Mapping(source = "activityId", target = "key"),
            @Mapping(source = "activityType", target = "type")
    })
    BpmActivityRespVO convert(BpmActivityDO bean);
}
