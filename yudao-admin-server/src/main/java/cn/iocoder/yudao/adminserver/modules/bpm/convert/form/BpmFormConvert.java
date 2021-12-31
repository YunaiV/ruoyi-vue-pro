package cn.iocoder.yudao.adminserver.modules.bpm.convert.form;

import cn.iocoder.yudao.adminserver.modules.bpm.controller.form.vo.BpmFormCreateReqVO;
import cn.iocoder.yudao.adminserver.modules.bpm.controller.form.vo.BpmFormRespVO;
import cn.iocoder.yudao.adminserver.modules.bpm.controller.form.vo.BpmFormUpdateReqVO;
import cn.iocoder.yudao.adminserver.modules.bpm.dal.dataobject.form.BpmFormDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 动态表单 Convert
 *
 * @author 芋艿
 */
@Mapper
public interface BpmFormConvert {

    BpmFormConvert INSTANCE = Mappers.getMapper(BpmFormConvert.class);

    BpmFormDO convert(BpmFormCreateReqVO bean);

    BpmFormDO convert(BpmFormUpdateReqVO bean);

    BpmFormRespVO convert(BpmFormDO bean);

    List<BpmFormRespVO> convertList(List<BpmFormDO> list);

    PageResult<BpmFormRespVO> convertPage(PageResult<BpmFormDO> page);

}
