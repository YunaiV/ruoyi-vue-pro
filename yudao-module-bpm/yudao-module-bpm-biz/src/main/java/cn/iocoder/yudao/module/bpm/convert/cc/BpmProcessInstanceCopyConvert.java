package cn.iocoder.yudao.module.bpm.convert.cc;

import cn.iocoder.yudao.module.bpm.dal.dataobject.cc.BpmProcessInstanceCopyDO;
import cn.iocoder.yudao.module.bpm.service.cc.BpmProcessInstanceCopyVO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

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

}
