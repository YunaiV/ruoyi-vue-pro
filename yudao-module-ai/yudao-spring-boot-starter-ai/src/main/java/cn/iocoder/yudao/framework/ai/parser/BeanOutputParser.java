/*
 * Copyright 2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cn.iocoder.yudao.framework.ai.parser;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.github.victools.jsonschema.generator.SchemaGenerator;
import com.github.victools.jsonschema.generator.SchemaGeneratorConfig;
import com.github.victools.jsonschema.generator.SchemaGeneratorConfigBuilder;
import com.github.victools.jsonschema.module.jackson.JacksonModule;

import java.util.Map;
import java.util.Objects;

import static com.github.victools.jsonschema.generator.OptionPreset.PLAIN_JSON;
import static com.github.victools.jsonschema.generator.SchemaVersion.DRAFT_2020_12;

/**
 * An implementation of {@link OutputParser} that transforms the LLM output to a specific
 * object type using JSON schema. This parser works by generating a JSON schema based on a
 * given Java class, which is then used to validate and transform the LLM output into the
 * desired type.
 *
 * @param <T> The target type to which the output will be converted.
 * @author Mark Pollack
 * @author Christian Tzolov
 * @author Sebastian Ullrich
 * @author Kirk Lund
 */
public class BeanOutputParser<T> implements OutputParser<T> {

	/** Holds the generated JSON schema for the target type. */
	private String jsonSchema;

	/** The Java class representing the target type. */
	@SuppressWarnings({ "FieldMayBeFinal", "rawtypes" })
	private Class<T> clazz;

	/** The object mapper used for deserialization and other JSON operations. */
	@SuppressWarnings("FieldMayBeFinal")
	private ObjectMapper objectMapper;

	/**
	 * Constructor to initialize with the target type's class.
	 * @param clazz The target type's class.
	 */
	public BeanOutputParser(Class<T> clazz) {
		this(clazz, null);
	}

	/**
	 * Constructor to initialize with the target type's class, a custom object mapper, and
	 * a line endings normalizer to ensure consistent line endings on any platform.
	 * @param clazz The target type's class.
	 * @param objectMapper Custom object mapper for JSON operations. endings.
	 */
	public BeanOutputParser(Class<T> clazz, ObjectMapper objectMapper) {
		Objects.requireNonNull(clazz, "Java Class cannot be null;");
		this.clazz = clazz;
		this.objectMapper = objectMapper != null ? objectMapper : getObjectMapper();
		generateSchema();
	}

	/**
	 * Generates the JSON schema for the target type.
	 */
	private void generateSchema() {
		JacksonModule jacksonModule = new JacksonModule();
		SchemaGeneratorConfigBuilder configBuilder = new SchemaGeneratorConfigBuilder(DRAFT_2020_12, PLAIN_JSON)
			.with(jacksonModule);
		SchemaGeneratorConfig config = configBuilder.build();
		SchemaGenerator generator = new SchemaGenerator(config);
		JsonNode jsonNode = generator.generateSchema(this.clazz);
		ObjectWriter objectWriter = new ObjectMapper()
			.writer(new DefaultPrettyPrinter().withObjectIndenter(new DefaultIndenter().withLinefeed("\n")));
		try {
			this.jsonSchema = objectWriter.writeValueAsString(jsonNode);
		}
		catch (JsonProcessingException e) {
			throw new RuntimeException("Could not pretty print json schema for " + this.clazz, e);
		}
	}

	@Override
	/**
	 * Parses the given text to transform it to the desired target type.
	 * @param text The LLM output in string format.
	 * @return The parsed output in the desired target type.
	 */
	public T parse(String text) {
		try {
			// If the response is a JSON Schema, extract the properties and use them as
			// the response.
			text = this.jsonSchemaToInstance(text);
			return (T) this.objectMapper.readValue(text, this.clazz);
		}
		catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Converts a JSON Schema to an instance based on a given text.
	 * @param text The JSON Schema in string format.
	 * @return The JSON instance generated from the JSON Schema, or the original text if
	 * the input is not a JSON Schema.
	 */
	private String jsonSchemaToInstance(String text) {
		try {
			Map<String, Object> map = this.objectMapper.readValue(text, Map.class);
			if (map.containsKey("$schema")) {
				return this.objectMapper.writeValueAsString(map.get("properties"));
			}
		}
		catch (Exception e) {
		}
		return text;
	}

	/**
	 * Configures and returns an object mapper for JSON operations.
	 * @return Configured object mapper.
	 */
	protected ObjectMapper getObjectMapper() {
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		return mapper;
	}

	/**
	 * Provides the expected format of the response, instructing that it should adhere to
	 * the generated JSON schema.
	 * @return The instruction format string.
	 */
	@Override
	public String getFormat() {
		String template = """
				Your response should be in JSON format.
				Do not include any explanations, only provide a RFC8259 compliant JSON response following this format without deviation.
				Do not include markdown code blocks in your response.
				Here is the JSON Schema instance your output must adhere to:
				```%s```
				""";
		return String.format(template, this.jsonSchema);
	}

}
