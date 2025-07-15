package cn.iocoder.yudao.module.trade.dal.dataobject.order;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

// 映射Java类型为Map
@MappedTypes(Map.class)
// 映射JDBC类型为VARCHAR(可根据实际情况调整)
@MappedJdbcTypes(JdbcType.VARCHAR)
@Slf4j
public class JacksonMapTypeHandler extends BaseTypeHandler<Map<String, Object>> {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Map<String, Object> parameter, JdbcType jdbcType) throws SQLException {
        try {
            // 将Map转换为JSON字符串
            String json = objectMapper.writeValueAsString(parameter);
            ps.setString(i, json);
        } catch (Exception e) {
            throw new SQLException("Error converting Map to JSON", e);
        }
    }

    @Override
    public Map<String, Object> getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return parseResult(rs, columnName, null);
    }

    @Override
    public Map<String, Object> getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return parseResult(rs, null, columnIndex);
    }

    @Override
    public Map<String, Object> getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String val = cs.getString(columnIndex);
        // 从CallableStatement获取key的逻辑可能需要调整
        String key = cs.getString(1); // 假设key在第1列
        return buildResultMap(key, val);
    }

    private Map<String, Object> parseResult(ResultSet rs, String columnName, Integer columnIndex) throws SQLException {
        String val = columnName != null ? rs.getString(columnName) : rs.getString(columnIndex);
        // 修正：使用正确的索引获取key（假设key在第1列）
        String key = rs.getString(1);
        return buildResultMap(key, val);
    }

    private Map<String, Object> buildResultMap(String key, String value) {
        if (key == null || value == null) {
            return Collections.emptyMap();
        }

        try {
            // 如果value是完整JSON，直接解析
            if (value.startsWith("{") && value.endsWith("}")) {
                Map<String, Object> map = objectMapper.readValue(value, new TypeReference<Map<String, Object>>() {});
                map.put("id", key); // 将id添加到Map中
                return map;
            }
            // 否则，创建简单的键值对
            Map<String, Object> map = new HashMap<>();
            map.put("id", key);
            map.put("value", value);
            return map;
        } catch (Exception e) {
            // 记录错误日志
            log.error("Failed to parse JSON: {}", value, e);
            // 返回包含原始数据的Map，避免异常导致流程中断
            Map<String, Object> errorMap = new HashMap<>();
            errorMap.put("id", key);
            errorMap.put("rawValue", value);
            errorMap.put("parseError", e.getMessage());
            return errorMap;
        }
    }
}    
