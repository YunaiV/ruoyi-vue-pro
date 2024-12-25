package cn.iocoder.yudao.module.iot.dal.tdengine;

import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.module.iot.framework.tdengine.core.TDengineTableField;
import cn.iocoder.yudao.module.iot.framework.tdengine.core.annotation.TDengineDS;
import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.stream.Collectors;

@Mapper
@TDengineDS
@InterceptorIgnore(tenantLine = "true") // 避免 SQL 解析，因为 JSqlParser 对 TDengine 的 SQL 解析会报错
public interface IotDevicePropertyDataMapper {

    List<TDengineTableField> getProductPropertySTableFieldList(@Param("productKey") String productKey);

    void createProductPropertySTable(@Param("productKey") String productKey,
                                     @Param("fields") List<TDengineTableField> fields);

    @SuppressWarnings("SimplifyStreamApiCallChains") // 保持 JDK8 兼容性
    default void alterProductPropertySTable(String productKey,
                                            List<TDengineTableField> oldFields,
                                            List<TDengineTableField> newFields) {
        oldFields.removeIf(field -> TDengineTableField.FIELD_TS.equals(field.getField())
                || TDengineTableField.FIELD_DEVICE_KEY.equals(field.getField()));
        List<TDengineTableField> addFields = newFields.stream().filter( // 新增的字段
                        newField -> oldFields.stream().noneMatch(oldField -> oldField.getField().equals(newField.getField())))
                .collect(Collectors.toList());
        List<TDengineTableField> modifyFields = newFields.stream().filter( // 更新的字段
                        newField -> oldFields.stream().anyMatch(oldField -> oldField.getField().equals(newField.getField())
                                && (ObjectUtil.notEqual(oldField.getType(), newField.getType())
                                    || (newField.getLength() != null && ObjectUtil.notEqual(oldField.getLength(), newField.getLength())))))
                .collect(Collectors.toList());
        List<TDengineTableField> dropFields = oldFields.stream().filter( // 删除的字段
                        oldField -> newFields.stream().noneMatch(n -> n.getField().equals(oldField.getField())))
                .collect(Collectors.toList());
        addFields.forEach(field -> alterProductPropertySTableAddField(productKey, field));
        // TODO 芋艿：tdengine 只允许 modify 长度；如果 type 变化，只能 drop + add
        modifyFields.forEach(field -> alterProductPropertySTableModifyField(productKey, field));
        dropFields.forEach(field -> alterProductPropertySTableDropField(productKey, field));
    }

    void alterProductPropertySTableAddField(@Param("productKey") String productKey,
                                            @Param("field") TDengineTableField field);

    void alterProductPropertySTableModifyField(@Param("productKey") String productKey,
                                               @Param("field") TDengineTableField field);

    void alterProductPropertySTableDropField(@Param("productKey") String productKey,
                                             @Param("field") TDengineTableField field);

}
