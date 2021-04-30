package cn.iocoder.dashboard.modules.tool.convert.codegen;

import cn.iocoder.yudao.common.pojo.PageResult;
import cn.iocoder.dashboard.modules.tool.controller.codegen.vo.ToolCodegenDetailRespVO;
import cn.iocoder.dashboard.modules.tool.controller.codegen.vo.ToolCodegenPreviewRespVO;
import cn.iocoder.dashboard.modules.tool.controller.codegen.vo.ToolCodegenUpdateReqVO;
import cn.iocoder.dashboard.modules.tool.controller.codegen.vo.column.ToolCodegenColumnRespVO;
import cn.iocoder.dashboard.modules.tool.controller.codegen.vo.table.ToolCodegenTableRespVO;
import cn.iocoder.dashboard.modules.tool.controller.codegen.vo.table.ToolSchemaTableRespVO;
import cn.iocoder.dashboard.modules.tool.dal.dataobject.codegen.ToolCodegenColumnDO;
import cn.iocoder.dashboard.modules.tool.dal.dataobject.codegen.ToolCodegenTableDO;
import cn.iocoder.dashboard.modules.tool.dal.dataobject.codegen.ToolSchemaColumnDO;
import cn.iocoder.dashboard.modules.tool.dal.dataobject.codegen.ToolSchemaTableDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Mapper
public interface ToolCodegenConvert {

    ToolCodegenConvert INSTANCE = Mappers.getMapper(ToolCodegenConvert.class);

    // ========== ToolInformationSchemaTableDO 和 ToolInformationSchemaColumnDO 相关 ==========

    ToolCodegenTableDO convert(ToolSchemaTableDO bean);

    List<ToolCodegenColumnDO> convertList(List<ToolSchemaColumnDO> list);

    ToolCodegenTableRespVO convert(ToolSchemaColumnDO bean);

    // ========== ToolCodegenTableDO 相关 ==========

//    List<ToolCodegenTableRespVO> convertList02(List<ToolCodegenTableDO> list);

    ToolCodegenTableRespVO convert(ToolCodegenTableDO bean);

    PageResult<ToolCodegenTableRespVO> convertPage(PageResult<ToolCodegenTableDO> page);

    // ========== ToolCodegenTableDO 相关 ==========

    List<ToolCodegenColumnRespVO> convertList02(List<ToolCodegenColumnDO> list);

    ToolCodegenTableDO convert(ToolCodegenUpdateReqVO.Table bean);

    List<ToolCodegenColumnDO> convertList03(List<ToolCodegenUpdateReqVO.Column> columns);

    List<ToolSchemaTableRespVO> convertList04(List<ToolSchemaTableDO> list);

    // ========== 其它 ==========

    default ToolCodegenDetailRespVO convert(ToolCodegenTableDO table, List<ToolCodegenColumnDO> columns) {
        ToolCodegenDetailRespVO respVO = new ToolCodegenDetailRespVO();
        respVO.setTable(convert(table));
        respVO.setColumns(convertList02(columns));
        return respVO;
    }

    default List<ToolCodegenPreviewRespVO> convert(Map<String, String> codes) {
        return codes.entrySet().stream().map(entry -> {
            ToolCodegenPreviewRespVO respVO = new ToolCodegenPreviewRespVO();
            respVO.setFilePath(entry.getKey());
            respVO.setCode(entry.getValue());
            return respVO;
        }).collect(Collectors.toList());
    }

}
