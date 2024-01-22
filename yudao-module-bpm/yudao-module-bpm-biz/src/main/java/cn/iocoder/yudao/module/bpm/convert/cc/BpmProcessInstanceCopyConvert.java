package cn.iocoder.yudao.module.bpm.convert.cc;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.bpm.controller.admin.task.vo.instance.BpmProcessInstanceCopyPageItemRespVO;
import cn.iocoder.yudao.module.bpm.controller.admin.task.vo.task.BpmProcessInstanceCopyVO;
import cn.iocoder.yudao.module.bpm.dal.dataobject.cc.BpmProcessInstanceCopyDO;
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

    BpmProcessInstanceCopyVO convert(BpmProcessInstanceCopyDO bean);

    List<BpmProcessInstanceCopyPageItemRespVO> convertList(List<BpmProcessInstanceCopyDO> list);

    default PageResult<BpmProcessInstanceCopyPageItemRespVO> convertPage(PageResult<BpmProcessInstanceCopyDO> page
            , Map<String, String/* taskName */> taskMap
            , Map<String, String/* processInstaneName */> processInstaneMap
            , Map<Long, String/* userName */> userMap
    ) {
        List<BpmProcessInstanceCopyPageItemRespVO> list = convertList(page.getList());
        List<BpmProcessInstanceCopyPageItemRespVO> list2 = BeanUtils.toBean(page.getList(),
                BpmProcessInstanceCopyPageItemRespVO.class,
                copy -> {
                    MapUtils.findAndThen(userMap, Long.valueOf(copy.getCreator()), copy::setCreatorNickname);
                    MapUtils.findAndThen(userMap, copy.getStartUserId(), copy::setStartUserNickname);
                    MapUtils.findAndThen(taskMap, copy.getTaskId(), copy::setTaskName);
                    MapUtils.findAndThen(processInstaneMap, copy.getProcessInstanceId(), copy::setProcessInstanceName);
                });
        return new PageResult<>(list, page.getTotal());
    }

}
