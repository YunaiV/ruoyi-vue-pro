package cn.iocoder.yudao.module.bpm.convert.cc;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.module.bpm.controller.admin.task.vo.instance.BpmProcessInstanceCCPageItemRespVO;
import cn.iocoder.yudao.module.bpm.dal.dataobject.cc.BpmProcessInstanceCopyDO;
import cn.iocoder.yudao.module.bpm.service.cc.BpmProcessInstanceCopyVO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.Map;

// TODO kyle：类注释不太对
/**
 * 动态表单 Convert
 *
 * @author 芋艿
 */
@Mapper
public interface BpmProcessInstanceCopyConvert {

    BpmProcessInstanceCopyConvert INSTANCE = Mappers.getMapper(BpmProcessInstanceCopyConvert.class);

    // TODO @kyle：可以使用 BeanUtils copy 替代这些简单的哈；
    BpmProcessInstanceCopyDO copy(BpmProcessInstanceCopyDO bean);

    BpmProcessInstanceCopyVO convert(BpmProcessInstanceCopyDO bean);

    List<BpmProcessInstanceCCPageItemRespVO> convertList(List<BpmProcessInstanceCopyDO> list);

    // TODO @kyle：/* taskId */ 这种注释一般不用写，可以一眼看明白的；避免变量看着略微不清晰哈
    default PageResult<BpmProcessInstanceCCPageItemRespVO> convertPage(PageResult<BpmProcessInstanceCopyDO> page
            , Map<String/* taskId */, String/* taskName */> taskMap
            , Map<String/* processInstaneId */, String/* processInstaneName */> processInstaneMap
            , Map<Long/* userId */, String/* userName */> userMap
    ) {
        List<BpmProcessInstanceCCPageItemRespVO> list = convertList(page.getList());
        for (BpmProcessInstanceCCPageItemRespVO vo : list) {
            MapUtils.findAndThen(userMap, Long.valueOf(vo.getCreator()),
                    vo::setCreatorNickname);
            MapUtils.findAndThen(userMap, vo.getStartUserId(),
                    vo::setStartUserNickname);
            MapUtils.findAndThen(taskMap, vo.getTaskId(),
                    vo::setTaskName);
            MapUtils.findAndThen(processInstaneMap, vo.getProcessInstanceId(),
                    vo::setProcessInstanceName);
        }
        // TODO @kyle：可以精简成下面的哈；
//        List<BpmProcessInstanceCCPageItemRespVO> list2 = BeanUtils.toBean(page.getList(),
//                BpmProcessInstanceCCPageItemRespVO.class,
//                copy -> {
//                    MapUtils.findAndThen(userMap, Long.valueOf(copy.getCreator()), copy::setCreatorNickname);
//                    MapUtils.findAndThen(userMap, copy.getStartUserId(), copy::setStartUserNickname);
//                    MapUtils.findAndThen(taskMap, copy.getTaskId(), copy::setTaskName);
//                    MapUtils.findAndThen(processInstaneMap, copy.getProcessInstanceId(), copy::setProcessInstanceName);
//                });
        return new PageResult<>(list, page.getTotal());
    }

}
