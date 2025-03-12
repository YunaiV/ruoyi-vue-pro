package cn.iocoder.yudao.framework.common.util.object;

import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
class BeanUtilsTest {

    @Builder
    static class Person {
        private String name;
        private Integer age;
    }

    @Builder
    static class Teacher {
        private String name;
        private Integer age;
        private String subject;
    }

    // TODO:这个test结果不对
//    @Test
//    void areAllNonNullFieldsPresent() {
//        var teacher = Teacher.builder()
//            .name("Jesus")
//            .age(18)
//            .subject("religion")
//            .build();
//
//        assertFalse(BeanUtils.areAllNonNullFieldsPresent(teacher,Person.class));
//
//        teacher = Teacher.builder()
//            .name("Jesus")
//            .age(18)
//            .subject(null)
//            .build();
//
//        assertTrue(BeanUtils.areAllNonNullFieldsPresent(teacher,Person.class));
//    }
}