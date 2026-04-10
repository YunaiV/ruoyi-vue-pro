package cn.iocoder.yudao.module.iot.dal.tdengine;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.property.IotDevicePropertyHistoryListReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.property.IotDevicePropertyRespVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO;
import cn.iocoder.yudao.module.iot.framework.tdengine.core.TDengineTableField;
import cn.iocoder.yudao.module.iot.framework.tdengine.core.annotation.TDengineDS;
import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Mapper
@TDengineDS
@InterceptorIgnore(tenantLine = "true") // 避免 SQL 解析，因为 JSqlParser 对 TDengine 的 SQL 解析会报错
public interface IotDevicePropertyMapper {

    List<TDengineTableField> getProductPropertySTableFieldList(@Param("productId") Long productId);

    void createProductPropertySTable(@Param("productId") Long productId,
                                     @Param("fields") List<TDengineTableField> fields);

    @SuppressWarnings("SimplifyStreamApiCallChains") // 保持 JDK8 兼容性
    default void alterProductPropertySTable(Long productId,
                                            List<TDengineTableField> oldFields,
                                            List<TDengineTableField> newFields) {
        oldFields.removeIf(field -> StrUtil.equalsAny(field.getField(),
                TDengineTableField.FIELD_TS, "report_time", "device_id"));
        List<TDengineTableField> addFields = newFields.stream().filter( // 新增的字段
                        newField -> oldFields.stream().noneMatch(oldField -> oldField.getField().equals(newField.getField())))
                .collect(Collectors.toList());
        List<TDengineTableField> dropFields = oldFields.stream().filter( // 删除的字段
                        oldField -> newFields.stream().noneMatch(n -> n.getField().equals(oldField.getField())))
                .collect(Collectors.toList());
        List<TDengineTableField> modifyTypeFields = new ArrayList<>(); // 变更类型的字段
        List<TDengineTableField> modifyLengthFields = new ArrayList<>(); // 变更长度的字段
        newFields.forEach(newField -> {
            TDengineTableField oldField = CollUtil.findOne(oldFields, field -> field.getField().equals(newField.getField()));
            if (oldField == null) {
                return;
            }
            if (ObjectUtil.notEqual(oldField.getType(), newField.getType())) {
                modifyTypeFields.add(newField);
                return;
            }
            if (newField.getLength() != null) {
                if (newField.getLength() > oldField.getLength()) {
                    modifyLengthFields.add(newField);
                } else if (newField.getLength() < oldField.getLength()) {
                    // 特殊：TDengine 长度修改时，只允许变长，所以此时认为是修改类型
                    modifyTypeFields.add(newField);
                }
            }
        });

        // 执行
        addFields.forEach(field -> alterProductPropertySTableAddField(productId, field));
        dropFields.forEach(field -> alterProductPropertySTableDropField(productId, field));
        modifyLengthFields.forEach(field -> alterProductPropertySTableModifyField(productId, field));
        modifyTypeFields.forEach(field -> {
            alterProductPropertySTableDropField(productId, field);
            alterProductPropertySTableAddField(productId, field);
        });
    }

    void alterProductPropertySTableAddField(@Param("productId") Long productId,
                                            @Param("field") TDengineTableField field);

    void alterProductPropertySTableModifyField(@Param("productId") Long productId,
                                               @Param("field") TDengineTableField field);

    void alterProductPropertySTableDropField(@Param("productId") Long productId,
                                             @Param("field") TDengineTableField field);

    void insert(@Param("device") IotDeviceDO device,
                @Param("properties") Map<String, Object> properties,
                @Param("reportTime") Long reportTime);

    List<IotDevicePropertyRespVO> selectListByHistory(@Param("reqVO") IotDevicePropertyHistoryListReqVO reqVO);

}
