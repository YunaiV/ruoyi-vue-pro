package cn.iocoder.yudao.module.ai.tool.method;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Value;
import lombok.experimental.Accessors;

/**
 * 来自 Spring AI 官方文档
 *
 * Represents a person with basic information.
 * This is an immutable record.
 */
@Value
@Accessors(fluent = true)
@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
@AllArgsConstructor
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class Person {

    int id;
    String firstName;
    String lastName;
    String email;
    String sex;
    String ipAddress;
    String jobTitle;
    int age;

}