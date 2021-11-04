package cn.iocoder.yudao.adminserver.modules.activiti.convert.form;

import cn.iocoder.yudao.adminserver.modules.activiti.controller.form.vo.WfFormCreateReqVO;
import cn.iocoder.yudao.adminserver.modules.activiti.controller.form.vo.WfFormExcelVO;
import cn.iocoder.yudao.adminserver.modules.activiti.controller.form.vo.WfFormRespVO;
import cn.iocoder.yudao.adminserver.modules.activiti.controller.form.vo.WfFormUpdateReqVO;
import cn.iocoder.yudao.adminserver.modules.activiti.dal.dataobject.form.WfForm;
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
public interface WfFormConvert {

    WfFormConvert INSTANCE = Mappers.getMapper(WfFormConvert.class);

    WfForm convert(WfFormCreateReqVO bean);

    WfForm convert(WfFormUpdateReqVO bean);

    WfFormRespVO convert(WfForm bean);

    List<WfFormRespVO> convertList(List<WfForm> list);

    PageResult<WfFormRespVO> convertPage(PageResult<WfForm> page);

    List<WfFormExcelVO> convertList02(List<WfForm> list);

}
