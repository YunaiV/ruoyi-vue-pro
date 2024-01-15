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

/**
 * 动态表单 Convert
 *
 * @author 芋艿
 */
@Mapper
public interface BpmProcessInstanceCopyConvert {

    BpmProcessInstanceCopyConvert INSTANCE = Mappers.getMapper(BpmProcessInstanceCopyConvert.class);

    BpmProcessInstanceCopyDO copy(BpmProcessInstanceCopyDO bean);

    BpmProcessInstanceCopyVO convert(BpmProcessInstanceCopyDO bean);

    List<BpmProcessInstanceCopyVO> convertList2(List<BpmProcessInstanceCopyDO> list);

    List<BpmProcessInstanceCCPageItemRespVO> convertList(List<BpmProcessInstanceCopyDO> list);

    default PageResult<BpmProcessInstanceCCPageItemRespVO> convertPage(PageResult<BpmProcessInstanceCopyDO> page
            , Map<String/* taskId */, String/* taskName */> taskMap
            , Map<String/* processInstaneId */, String/* processInstaneName */> processInstaneMap
            , Map<Long/* userId */, String/* userName */> userMap
    ) {
        List<BpmProcessInstanceCCPageItemRespVO> list = convertList(page.getList());
        for (BpmProcessInstanceCCPageItemRespVO vo:list){
            MapUtils.findAndThen(userMap, vo.getUserId(),
                    userName -> vo.setUserNickname(userName));
            MapUtils.findAndThen(userMap, vo.getStartUserId(),
                    userName -> vo.setStartUserNickname(userName));
            MapUtils.findAndThen(taskMap, vo.getTaskId(),
                    name -> vo.setTaskName(name));
            MapUtils.findAndThen(processInstaneMap, vo.getProcessInstanceId(),
                    name -> vo.setProcessInstanceName(name));
        }
        return new PageResult<>(list, page.getTotal());
    }

}
