package cn.iocoder.yudao.framework.common.util.json;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.annotation.Resource;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.Test;

import java.sql.Timestamp;
import java.time.*;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class JsonUtilsXTest {


    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Birthday {
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyyMMdd")
        private LocalDateTime dateTime;

        @JsonFormat(shape = JsonFormat.Shape.NUMBER)
        private Timestamp timeStamp;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class People {
        private List<Person> list;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Person {
        private String name;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Today {
        private LocalDateTime datetime;
    }

    @Data
    @Builder
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class EccangOrderVO {
        // 当前页
        private Integer page;

        // 每页数量（最大100）
        private Integer pageSize;

        // 是否返回订单明细数据，1:返回，0：不返回
        private Integer getDetail;

        // 是否返回订单地址数据，1:返回，0：不返回
        private Integer getAddress;

        // 是否返回自定义订单类型，1:返回，0：不返回
        private Integer getCustomOrderType;

        // 按年份查询订单，传年份：2019
        private Integer year;

        // 查询条件：biz_content.condition
        private Condition condition;

        @Data
        @Builder
        @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
        public static class Condition {
            private List<String> referenceNoList;
            private List<String> orderCodeList;
            private List<String> warehouseOrderCodeList;
            private List<String> productSkuList;
            private List<String> userAccountList;
            private List<String> shippingMethod;
            private List<Integer> warehouseIdList;
            private String status;
            private List<Integer> processAgains;
            @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
            private LocalDateTime createdDateStart;
            @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
            private LocalDateTime createdDateEnd;
            @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
            private LocalDateTime updateDateStart;
            @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
            private LocalDateTime updateDateEnd;
            @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
            private LocalDateTime shipDateStart;
            @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
            private LocalDateTime shipDateEnd;
            @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
            private LocalDateTime platformShipDateStart;
            @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
            private LocalDateTime platformShipDateEnd;
            @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
            private LocalDateTime warehouseShipDateStart;
            @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
            private LocalDateTime warehouseShipDateEnd;
            @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
            private LocalDateTime platformPaidDateStart;
            @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
            private LocalDateTime platformPaidDateEnd;
            @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
            private LocalDateTime platformCreateDateStart;
            @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
            private LocalDateTime platformCreateDateEnd;
            @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
            private LocalDateTime dateCreateSysStart;
            @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
            private LocalDateTime dateCreateSysEnd;
            @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
            private LocalDateTime trackDeliveredTimeStart;
            @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
            private LocalDateTime trackDeliveredTimeEnd;
            private String buyerName;
            private String platform;
            private Integer idDesc;
            private String customOrderType;
            private String isMark;
            private String createType;
            private Integer isCod;
            private Integer isTransferFbaDelivery;
        }
    }



    @Test
    public void toJsonString_String_ReturnsJson() throws Exception {
        assertEquals("\"John\"", JsonUtilsX.toJsonString("John"));
    }

    @Test
    public void toJsonString_NormalObject_ReturnsJson() throws Exception {
        assertEquals("{\"name\":\"John\"}", JsonUtilsX.toJsonString(new Person("John")));
    }

    @Test
    public void toJsonString_LocalDateTime_ReturnsJson() throws Exception {
        assertEquals("{\"datetime\":\"2025-02-17T14:38:00\"}", JsonUtilsX.toJsonString(new Today(LocalDateTime.of(2025, 2, 17, 14, 38))));
    }

    @Test
    public void toJsonString_VO_ReturnsJson() throws Exception {
        var datetime1 = LocalDateTime.of(2025, 2, 17, 14, 38);
        var reqVO = EccangOrderVO.builder()
            .condition(EccangOrderVO.Condition.builder()
                .platformShipDateStart(datetime1)
                .build())
            .build();
        assertEquals("{\"condition\":{\"platform_ship_date_start\":\"2025-02-17 14:38:00\"}}", JsonUtilsX.toJsonString(reqVO));
    }



//    @Test
//    public void toJsonString_NullObject_ThrowsException() {
//        assertThrows(NullPointerException.class, () -> {
//            JsonUtilsX.toJsonString(null);
//        });
//    }

    @Test
    public void parseObject_ValidJson_ReturnsObject() throws Exception {
        String json = "{\"name\":\"John\"}";
        Person result = JsonUtilsX.parseObject(json, Person.class);
        assertEquals("John", result.getName());
    }

    @Test
    public void parseObject_ValidJson_ReturnsObjectNode() throws Exception {
        String json = "{\"name\":\"John\"}";
        ObjectNode result = JsonUtilsX.parseObject(json, ObjectNode.class);
        assertEquals("John", result.get("name").asText());
    }

    @Test
    public void parseObject_ValidJson_ReturnsJSONObject() throws Exception {
        String json = "{\"name\":\"John\"}";
        JSONObject result = JsonUtilsX.parseObject(json, JSONObject.class);
        assertEquals("John", result.getString("name"));
    }

    @Test
    public void parseObject_EmptyJson_ReturnsNull() {
        assertNull(JsonUtilsX.parseObject("", Person.class));
    }

    @Test
    public void parseObject_InvalidJson_ThrowsException() {
        assertThrows(Exception.class, () -> {
            JsonUtilsX.parseObject("{\"name\":}", Person.class);
        });
    }

    @Test
    public void parseArray_ValidJsonArray_ReturnsList() throws Exception {
        String jsonArray = "[{\"name\":\"John\"}, {\"name\":\"Jane\"}]";
        List<Person> result = JsonUtilsX.parseArray(jsonArray, Person.class);
        assertEquals(2, result.size());
        assertEquals("John", result.get(0).getName());
    }


    @Test
    public void parseArray_ValidJsonArray_ReturnsJSON() throws Exception {
        String jsonArray = "[{\"name\":\"John\"}, {\"name\":\"Jane\"}]";
        List<Person> result = JsonUtilsX.parseArray(jsonArray, Person.class);
        assertEquals(2, result.size());
        assertEquals("John", result.get(0).getName());
    }

    @Test
    public void parseArray_EmptyJsonArray_ReturnsEmptyList() {
        assertTrue(JsonUtilsX.parseArray("", Person.class).isEmpty());
    }

    @Test
    public void parseArray_InvalidJsonArray_ThrowsException() {
        assertThrows(Exception.class, () -> {
            JsonUtilsX.parseArray("[{\"name\":}]", Person.class);
        });
    }

    @Test
    public void parseArray_ValidStringArray_ReturnsList() throws Exception {
        String jsonArray = "{\"name\":[\"John\", \"Jane\"]}";
        Map<String, List<String>> result = JsonUtilsX.parseObject(jsonArray, Map.class);
        assertEquals(1, result.size());
        assertEquals("John", result.get("name").get(0));
    }



    @Test
    public void serializeTime() throws Exception {
        var dateTime = LocalDateTime.of(2024,12,31,23,59);
        var instant = dateTime.atZone(ZoneId.systemDefault()).toInstant();

        var birthday = new Birthday(dateTime,  Timestamp.from(instant));
        assertEquals("{\"dateTime\":\"20241231\",\"timeStamp\":1735660740000}", JsonUtilsX.toJsonString(birthday));
    }

//    @Test
//    public void parseObject2_ValidJson_ReturnsObject() {
//        String json = "{\"name\":\"John\"}";
//        Person result = JsonUtilsX.parseObject2(json, Person.class);
//        assertEquals("John", result.getName());
//    }
//
//    @Test
//    public void parseObject2_EmptyJson_ReturnsNull() {
//        assertNull(JsonUtilsX.parseObject2("", Person.class));
//    }
}