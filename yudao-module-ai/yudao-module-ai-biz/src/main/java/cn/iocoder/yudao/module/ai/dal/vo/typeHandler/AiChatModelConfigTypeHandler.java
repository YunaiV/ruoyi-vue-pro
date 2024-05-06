//package cn.iocoder.yudao.module.ai.mapper.typeHandler;
//
//import cn.hutool.core.util.StrUtil;
//import cn.iocoder.yudao.framework.ai.AiPlatformEnum;
//import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
//import cn.iocoder.yudao.module.ai.dal.dataobject.AiChatModalDO;
//import org.apache.ibatis.type.BaseTypeHandler;
//import org.apache.ibatis.type.JdbcType;
//import org.apache.ibatis.type.MappedTypes;
//
//import java.sql.CallableStatement;
//import java.sql.PreparedStatement;
//import java.sql.ResultSet;
//import java.sql.SQLException;
//
///**
// * chat modal config
// *
// * @author fansili
// * @time 2024/5/6 11:18
// * @since 1.0
// */
//@MappedTypes(value = AiChatModalDO.Config.class)
//public class AiChatModelConfigTypeHandler extends BaseTypeHandler<AiChatModalDO.Config> {
//
//    @Override
//    public void setNonNullParameter(PreparedStatement ps, int i, AiChatModalDO.Config parameter, JdbcType jdbcType) throws SQLException {
//        // 将 MyCustomType 转换为数据库类型并设置到 PreparedStatement 中
//        if (parameter == null) {
//            ps.setString(i, "");
//        } else {
//            ps.setString(i, JsonUtils.toJsonString(parameter));
//        }
//    }
//
//    @Override
//    public AiChatModalDO.Config getNullableResult(ResultSet rs, String columnName) throws SQLException {
//        // 从 ResultSet 中获取数据库类型并转换为 MyCustomType
//        String str = rs.getString(columnName);
//        if (StrUtil.isBlank(str)) {
//            return null;
//        }
//        AiChatModalDO.Config config = JsonUtils.parseObject(str, AiChatModalDO.Config.class);
//        // 获取平台
//        AiPlatformEnum platformEnum = AiPlatformEnum.valueOfPlatform(config.getModelPlatform());
//        if (AiPlatformEnum.CHAT_PLATFORM_LIST.contains(platformEnum)) {
//            return JsonUtils.parseObject(str, AiChatModalDO.ChatConfig.class);
//        } else if (AiPlatformEnum.OPEN_AI_DALL == platformEnum) {
//            return JsonUtils.parseObject(str, AiChatModalDO.OpenAiImageConfig.class);
//        } else if (AiPlatformEnum.MIDJOURNEY == platformEnum) {
//            return JsonUtils.parseObject(str, AiChatModalDO.MidjourneyConfig.class);
//        }
//        throw new IllegalArgumentException("ai模型中config不能转换! json: " + str);
//    }
//
//    @Override
//    public AiChatModalDO.Config getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
//        // 从 ResultSet 中获取数据库类型并转换为 MyCustomType
//        String str = rs.getString(columnIndex);
//        if (StrUtil.isBlank(str)) {
//            return null;
//        }
//        return JsonUtils.parseObject(str, AiChatModalDO.Config.class);
//    }
//
//    @Override
//    public AiChatModalDO.Config getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
//        // 从 CallableStatement 中获取数据库类型并转换为 MyCustomType
//        String str = cs.getString(columnIndex);
//        if (StrUtil.isBlank(str)) {
//            return null;
//        }
//        return JsonUtils.parseObject(str, AiChatModalDO.Config.class);
//    }
//}
