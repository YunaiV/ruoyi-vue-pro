package com.somle.framework.common.util.json;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Timestamp;
import java.time.*;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class JsonUtilsTest {
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Person {
        private String name;
    }

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



    @Test
    public void toJsonString_String_ReturnsJson() throws Exception {
        assertEquals("\"John\"", JsonUtils.toJsonString("John"));
    }

    @Test
    public void toJsonString_NormalObject_ReturnsJson() throws Exception {
        assertEquals("{\"name\":\"John\"}", JsonUtils.toJsonString(new Person("John")));
    }

    @Test
    public void toJsonString_JSONObject_ReturnsJson() throws Exception {
        var json = JsonUtils.newObject();
        json.put("name", "John");
        assertEquals("{\"name\":\"John\"}", JsonUtils.toJsonString((Object) json));

    }

//    @Test
//    public void toJsonString_NullObject_ThrowsException() {
//        assertThrows(NullPointerException.class, () -> {
//            JsonUtils.toJsonString(null);
//        });
//    }

    @Test
    public void parseObject_ValidJson_ReturnsObject() throws Exception {
        String json = "{\"name\":\"John\"}";
        Person result = JsonUtils.parseObject(json, Person.class);
        assertEquals("John", result.getName());
    }

    @Test
    public void parseObject_ValidJson_ReturnsObjectNode() throws Exception {
        String json = "{\"name\":\"John\"}";
        ObjectNode result = JsonUtils.parseObject(json, ObjectNode.class);
        assertEquals("John", result.get("name").asText());
    }

    @Test
    public void parseObject_ValidJson_ReturnsJSONObject() throws Exception {
        String json = "{\"name\":\"John\"}";
        JSONObject result = JsonUtils.parseObject(json, JSONObject.class);
        assertEquals("John", result.getString("name"));
    }

    @Test
    public void parseObject_EmptyJson_ReturnsNull() {
        assertNull(JsonUtils.parseObject("", Person.class));
    }

    @Test
    public void parseObject_InvalidJson_ThrowsException() {
        assertThrows(Exception.class, () -> {
            JsonUtils.parseObject("{\"name\":}", Person.class);
        });
    }

    @Test
    public void parseArray_ValidJsonArray_ReturnsList() throws Exception {
        String jsonArray = "[{\"name\":\"John\"}, {\"name\":\"Jane\"}]";
        List<Person> result = JsonUtils.parseArray(jsonArray, Person.class);
        assertEquals(2, result.size());
        assertEquals("John", result.get(0).getName());
    }


    @Test
    public void parseArray_ValidJsonArray_ReturnsJSON() throws Exception {
        String jsonArray = "[{\"name\":\"John\"}, {\"name\":\"Jane\"}]";
        JSONObject result = JsonUtils.parseObject(jsonArray, JSONObject.class);
        assertEquals(2, result.size());
        assertEquals("John", result.get(0).get("name"));
    }

    @Test
    public void parseArray_EmptyJsonArray_ReturnsEmptyList() {
        assertTrue(JsonUtils.parseArray("", Person.class).isEmpty());
    }

    @Test
    public void parseArray_InvalidJsonArray_ThrowsException() {
        assertThrows(Exception.class, () -> {
            JsonUtils.parseArray("[{\"name\":}]", Person.class);
        });
    }

    @Test
    public void parseArray_ValidStringArray_ReturnsList() throws Exception {
        String jsonArray = "{\"name\":[\"John\", \"Jane\"]}";
        Map<String, List<String>> result = JsonUtils.parseObject(jsonArray, Map.class);
        assertEquals(1, result.size());
        assertEquals("John", result.get("name").get(0));
    }

    @Test
    public void toStringMap_ValidPOJO() throws Exception {
        People people = new People(List.of(new Person("John"), new Person("Jane")));
        Map<String, String> result = JsonUtils.toStringMap(people);
        assertEquals(1, result.size());
        assertEquals("{list=[{\"name\":\"John\"},{\"name\":\"Jane\"}]}", result.toString());
        assertEquals("[{\"name\":\"John\"},{\"name\":\"Jane\"}]", result.get("list"));
    }

    @Test
    public void serializeTime() throws Exception {
        var dateTime = LocalDateTime.of(2024,12,31,23,59);
        var instant = dateTime.atZone(ZoneId.systemDefault()).toInstant();

        var birthday = new Birthday(dateTime,  Timestamp.from(instant));
        assertEquals("{\"dateTime\":\"20241231\",\"timeStamp\":1735660740000}", JsonUtils.toJsonString(birthday));
    }

//    @Test
//    public void parseObject2_ValidJson_ReturnsObject() {
//        String json = "{\"name\":\"John\"}";
//        Person result = JsonUtils.parseObject2(json, Person.class);
//        assertEquals("John", result.getName());
//    }
//
//    @Test
//    public void parseObject2_EmptyJson_ReturnsNull() {
//        assertNull(JsonUtils.parseObject2("", Person.class));
//    }
}