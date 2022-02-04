package cn.iocoder.yudao.module.tool.convert.codegen;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.tool.controller.admin.codegen.vo.CodegenDetailRespVO;
import cn.iocoder.yudao.module.tool.controller.admin.codegen.vo.CodegenPreviewRespVO;
import cn.iocoder.yudao.module.tool.controller.admin.codegen.vo.CodegenUpdateReqVO;
import cn.iocoder.yudao.module.tool.controller.admin.codegen.vo.column.CodegenColumnRespVO;
import cn.iocoder.yudao.module.tool.controller.admin.codegen.vo.table.CodegenTableRespVO;
import cn.iocoder.yudao.module.tool.controller.admin.codegen.vo.table.SchemaTableRespVO;
import cn.iocoder.yudao.module.tool.dal.dataobject.codegen.CodegenColumnDO;
import cn.iocoder.yudao.module.tool.dal.dataobject.codegen.CodegenTableDO;
import cn.iocoder.yudao.module.tool.dal.dataobject.codegen.SchemaColumnDO;
import cn.iocoder.yudao.module.tool.dal.dataobject.codegen.SchemaTableDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Mapper
public interface CodegenConvert {

    CodegenConvert INSTANCE = Mappers.getMapper(CodegenConvert.class);

    // ========== InformationSchemaTableDO 和 InformationSchemaColumnDO 相关 ==========

    CodegenTableDO convert(SchemaTableDO bean);

    List<CodegenColumnDO> convertList(List<SchemaColumnDO> list);

    CodegenTableRespVO convert(SchemaColumnDO bean);

    // ========== CodegenTableDO 相关 ==========

//    List<CodegenTableRespVO> convertList02(List<CodegenTableDO> list);

    CodegenTableRespVO convert(CodegenTableDO bean);

    PageResult<CodegenTableRespVO> convertPage(PageResult<CodegenTableDO> page);

    // ========== CodegenTableDO 相关 ==========

    List<CodegenColumnRespVO> convertList02(List<CodegenColumnDO> list);

    CodegenTableDO convert(CodegenUpdateReqVO.Table bean);

    List<CodegenColumnDO> convertList03(List<CodegenUpdateReqVO.Column> columns);

    List<SchemaTableRespVO> convertList04(List<SchemaTableDO> list);

    // ========== 其它 ==========

    default CodegenDetailRespVO convert(CodegenTableDO table, List<CodegenColumnDO> columns) {
        CodegenDetailRespVO respVO = new CodegenDetailRespVO();
        respVO.setTable(convert(table));
        respVO.setColumns(convertList02(columns));
        return respVO;
    }

    default List<CodegenPreviewRespVO> convert(Map<String, String> codes) {
        return codes.entrySet().stream().map(entry -> {
            CodegenPreviewRespVO respVO = new CodegenPreviewRespVO();
            respVO.setFilePath(entry.getKey());
            respVO.setCode(entry.getValue());
            return respVO;
        }).collect(Collectors.toList());
    }

}
