package cn.iocoder.yudao.adminserver.modules.activiti.convert.form;

import cn.iocoder.yudao.adminserver.modules.activiti.controller.form.vo.OsFormCreateReqVO;
import cn.iocoder.yudao.adminserver.modules.activiti.controller.form.vo.OsFormExcelVO;
import cn.iocoder.yudao.adminserver.modules.activiti.controller.form.vo.OsFormRespVO;
import cn.iocoder.yudao.adminserver.modules.activiti.controller.form.vo.OsFormUpdateReqVO;
import cn.iocoder.yudao.adminserver.modules.activiti.dal.dataobject.form.OsFormDO;
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
public interface OsFormConvert {

    OsFormConvert INSTANCE = Mappers.getMapper(OsFormConvert.class);

    OsFormDO convert(OsFormCreateReqVO bean);

    OsFormDO convert(OsFormUpdateReqVO bean);

    OsFormRespVO convert(OsFormDO bean);

    List<OsFormRespVO> convertList(List<OsFormDO> list);

    PageResult<OsFormRespVO> convertPage(PageResult<OsFormDO> page);

    List<OsFormExcelVO> convertList02(List<OsFormDO> list);

}
