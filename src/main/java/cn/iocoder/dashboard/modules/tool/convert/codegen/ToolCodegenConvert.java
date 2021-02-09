package cn.iocoder.dashboard.modules.tool.convert.codegen;

import cn.iocoder.dashboard.common.pojo.PageResult;
import cn.iocoder.dashboard.modules.tool.controller.codegen.vo.ToolCodegenColumnRespVO;
import cn.iocoder.dashboard.modules.tool.controller.codegen.vo.ToolCodegenDetailRespVO;
import cn.iocoder.dashboard.modules.tool.controller.codegen.vo.ToolCodegenTableRespVO;
import cn.iocoder.dashboard.modules.tool.dal.mysql.dataobject.codegen.ToolCodegenColumnDO;
import cn.iocoder.dashboard.modules.tool.dal.mysql.dataobject.codegen.ToolCodegenTableDO;
import cn.iocoder.dashboard.modules.tool.dal.mysql.dataobject.codegen.ToolInformationSchemaColumnDO;
import cn.iocoder.dashboard.modules.tool.dal.mysql.dataobject.codegen.ToolInformationSchemaTableDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface ToolCodegenConvert {

    ToolCodegenConvert INSTANCE = Mappers.getMapper(ToolCodegenConvert.class);

    // ========== ToolInformationSchemaTableDO 和 ToolInformationSchemaColumnDO 相关 ==========

    ToolCodegenTableDO convert(ToolInformationSchemaTableDO bean);

    List<ToolCodegenColumnDO> convertList(List<ToolInformationSchemaColumnDO> list);

    ToolCodegenTableRespVO convert(ToolInformationSchemaColumnDO bean);

    // ========== ToolCodegenTableDO 相关 ==========

//    List<ToolCodegenTableRespVO> convertList02(List<ToolCodegenTableDO> list);

    ToolCodegenTableRespVO convert(ToolCodegenTableDO bean);

    PageResult<ToolCodegenTableRespVO> convertPage(PageResult<ToolCodegenTableDO> page);

    // ========== ToolCodegenTableDO 相关 ==========

    List<ToolCodegenColumnRespVO> convertList02(List<ToolCodegenColumnDO> list);

    // ========== 其它 ==========

    default ToolCodegenDetailRespVO convert(ToolCodegenTableDO table, List<ToolCodegenColumnDO> columns) {
        ToolCodegenDetailRespVO respVO = new ToolCodegenDetailRespVO();
        respVO.setTable(convert(table));
        respVO.setColumns(convertList02(columns));
        return respVO;
    }

}
