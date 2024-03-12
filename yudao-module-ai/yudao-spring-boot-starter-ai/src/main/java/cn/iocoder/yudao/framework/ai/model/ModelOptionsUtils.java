/*
 * Copyright 2024-2024 the original author or authors.
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

package cn.iocoder.yudao.framework.ai.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.victools.jsonschema.generator.*;
import com.github.victools.jsonschema.module.jackson.JacksonModule;
import com.github.victools.jsonschema.module.jackson.JacksonOption;
import com.github.victools.jsonschema.module.swagger2.Swagger2Module;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * Utility class for manipulating {@link ModelOptions} objects.
 *
 * @author Christian Tzolov
 * @since 0.8.0
 */
public final class ModelOptionsUtils {

	private final static ObjectMapper OBJECT_MAPPER = new ObjectMapper()
		.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
		.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);

	private final static List<String> BEAN_MERGE_FIELD_EXCISIONS = List.of("class");

	private static ConcurrentHashMap<Class<?>, List<String>> REQUEST_FIELD_NAMES_PER_CLASS = new ConcurrentHashMap<Class<?>, List<String>>();

	private static AtomicReference<SchemaGenerator> SCHEMA_GENERATOR_CACHE = new AtomicReference<>();

	private ModelOptionsUtils() {

	}

	/**
	 * Converts the given JSON string to a Map of String and Object.
	 * @param json the JSON string to convert to a Map.
	 * @return the converted Map.
	 */
	public static Map<String, Object> jsonToMap(String json) {
		try {
			return OBJECT_MAPPER.readValue(json, MAP_TYPE_REF);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private static TypeReference<HashMap<String, Object>> MAP_TYPE_REF = new TypeReference<HashMap<String, Object>>() {
	};

	/**
	 * Converts the given JSON string to an Object of the given type.
	 * @param <T> the type of the object to return.
	 * @param json the JSON string to convert to an object.
	 * @param type the type of the object to return.
	 * @return Object instance of the given type.
	 */
	public static <T> T jsonToObject(String json, Class<T> type) {
		try {
			return OBJECT_MAPPER.readValue(json, type);
		}
		catch (Exception e) {
			throw new RuntimeException("Failed to json: " + json, e);
		}
	}

	/**
	 * Converts the given object to a JSON string.
	 * @param object the object to convert to a JSON string.
	 * @return the JSON string.
	 */
	public static String toJsonString(Object object) {
		try {
			return OBJECT_MAPPER.writeValueAsString(object);
		}
		catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Merges the source object into the target object and returns an object represented
	 * by the given class. The JSON property names are used to match the fields to merge.
	 * The source non-null values override the target values with the same field name. The
	 * source null values are ignored. If the acceptedFieldNames is not empty, only the
	 * fields with the given names are merged and returned. If the acceptedFieldNames is
	 * empty, use the {@code @JsonProperty} names, inferred from the provided clazz.
	 * @param <T> they type of the class to return.
	 * @param source the source object to merge.
	 * @param target the target object to merge into.
	 * @param clazz the class to return.
	 * @param acceptedFieldNames the list of field names accepted for the target object.
	 * @return the merged object represented by the given class.
	 */
	public static <T> T merge(Object source, Object target, Class<T> clazz, List<String> acceptedFieldNames) {

		if (source == null) {
			source = Map.of();
		}

		List<String> requestFieldNames = CollectionUtils.isEmpty(acceptedFieldNames)
				? REQUEST_FIELD_NAMES_PER_CLASS.computeIfAbsent(clazz, ModelOptionsUtils::getJsonPropertyValues)
				: acceptedFieldNames;

		if (CollectionUtils.isEmpty(requestFieldNames)) {
			throw new IllegalArgumentException("No @JsonProperty fields found in the " + clazz.getName());
		}

		Map<String, Object> sourceMap = ModelOptionsUtils.objectToMap(source);
		Map<String, Object> targetMap = ModelOptionsUtils.objectToMap(target);

		targetMap.putAll(sourceMap.entrySet()
			.stream()
			.filter(e -> e.getValue() != null)
			.collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue())));

		targetMap = targetMap.entrySet()
			.stream()
			.filter(e -> requestFieldNames.contains(e.getKey()))
			.collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));

		return ModelOptionsUtils.mapToClass(targetMap, clazz);
	}

	/**
	 * Merges the source object into the target object and returns an object represented
	 * by the given class. The JSON property names are used to match the fields to merge.
	 * The source non-null values override the target values with the same field name. The
	 * source null values are ignored. Returns the only field names that match the
	 * {@code @JsonProperty} names, inferred from the provided clazz.
	 * @param <T> they type of the class to return.
	 * @param source the source object to merge.
	 * @param target the target object to merge into.
	 * @param clazz the class to return.
	 * @return the merged object represented by the given class.
	 */
	public static <T> T merge(Object source, Object target, Class<T> clazz) {
		return ModelOptionsUtils.merge(source, target, clazz, null);
	}

	/**
	 * Converts the given object to a Map.
	 * @param source the object to convert to a Map.
	 * @return the converted Map.
	 */
	public static Map<String, Object> objectToMap(Object source) {
		if (source == null) {
			return new HashMap<>();
		}
		try {
			String json = OBJECT_MAPPER.writeValueAsString(source);
			return OBJECT_MAPPER.readValue(json, new TypeReference<Map<String, Object>>() {
			})
				.entrySet()
				.stream()
				.filter(e -> e.getValue() != null)
				.collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));
		}
		catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Converts the given Map to the given class.
	 * @param <T> the type of the class to return.
	 * @param source the Map to convert to the given class.
	 * @param clazz the class to convert the Map to.
	 * @return the converted class.
	 */
	public static <T> T mapToClass(Map<String, Object> source, Class<T> clazz) {
		try {
			String json = OBJECT_MAPPER.writeValueAsString(source);
			return OBJECT_MAPPER.readValue(json, clazz);
		}
		catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Returns the list of name values of the {@link JsonProperty} annotations.
	 * @param clazz the class that contains fields annotated with {@link JsonProperty}.
	 * @return the list of values of the {@link JsonProperty} annotations.
	 */
	public static List<String> getJsonPropertyValues(Class<?> clazz) {
		List<String> values = new ArrayList<>();
		Field[] fields = clazz.getDeclaredFields();
		for (Field field : fields) {
			JsonProperty jsonPropertyAnnotation = field.getAnnotation(JsonProperty.class);
			if (jsonPropertyAnnotation != null) {
				values.add(jsonPropertyAnnotation.value());
			}
		}
		return values;
	}

	/**
	 * Returns a new instance of the targetBeanClazz that copies the bean values from the
	 * sourceBean instance.
	 * @param sourceBean the source bean to copy the values from.
	 * @param sourceInterfaceClazz the source interface class. Only the fields with the
	 * same name as the interface methods are copied. This allow the source object to be a
	 * subclass of the source interface with additional, non-interface fields.
	 * @param targetBeanClazz the target class, a subclass of the ChatOptions, to convert
	 * into.
	 * @param <T> the target class type.
	 * @return a new instance of the targetBeanClazz with the values from the sourceBean
	 * instance.
	 */
	public static <I, S extends I, T extends S> T copyToTarget(S sourceBean, Class<I> sourceInterfaceClazz,
			Class<T> targetBeanClazz) {

		Assert.notNull(sourceInterfaceClazz, "SourceOptionsClazz must not be null");
		Assert.notNull(targetBeanClazz, "TargetOptionsClazz must not be null");

		if (sourceBean == null) {
			return null;
		}

		if (sourceBean.getClass().isAssignableFrom(targetBeanClazz)) {
			return (T) sourceBean;
		}

		try {
			T targetOptions = targetBeanClazz.getConstructor().newInstance();

			ModelOptionsUtils.mergeBeans(sourceBean, targetOptions, sourceInterfaceClazz, true);

			return targetOptions;
		}
		catch (Exception e) {
			throw new RuntimeException(
					"Failed to convert the " + sourceInterfaceClazz.getName() + " into " + targetBeanClazz.getName(),
					e);
		}
	}

	/**
	 * Merges the source object into the target object. The source null values are
	 * ignored. Only objects with Getter and Setter methods are supported.
	 * @param <T> the type of the source and target object.
	 * @param source the source object to merge.
	 * @param target the target object to merge into.
	 * @param sourceInterfaceClazz the source interface class. Only the fields with the
	 * same name as the interface methods are merged. This allow the source object to be a
	 * subclass of the source interface with additional, non-interface fields.
	 * @param overrideNonNullTargetValues if true, the source non-null values override the
	 * target values with the same field name. If false, the source non-null values are
	 * ignored.
	 * @return the merged target object.
	 */
	public static <I, S extends I, T extends S> T mergeBeans(S source, T target, Class<I> sourceInterfaceClazz,
			boolean overrideNonNullTargetValues) {
		Assert.notNull(source, "Source object must not be null");
		Assert.notNull(target, "Target object must not be null");

		BeanWrapper sourceBeanWrap = new BeanWrapperImpl(source);
		BeanWrapper targetBeanWrap = new BeanWrapperImpl(target);

		List<String> interfaceNames = Arrays.stream(sourceInterfaceClazz.getMethods()).map(m -> m.getName()).toList();

		for (PropertyDescriptor descriptor : sourceBeanWrap.getPropertyDescriptors()) {

			if (!BEAN_MERGE_FIELD_EXCISIONS.contains(descriptor.getName())
					&& interfaceNames.contains(toGetName(descriptor.getName()))) {

				String propertyName = descriptor.getName();
				Object value = sourceBeanWrap.getPropertyValue(propertyName);

				// Copy value to the target object
				if (value != null) {
					var targetValue = targetBeanWrap.getPropertyValue(propertyName);

					if (targetValue == null || overrideNonNullTargetValues) {
						targetBeanWrap.setPropertyValue(propertyName, value);
					}
				}
			}
		}

		return target;
	}

	private static String toGetName(String name) {
		return "get" + name.substring(0, 1).toUpperCase() + name.substring(1);
	}

	/**
	 * Generates JSON Schema (version 2020_12) for the given class.
	 * @param clazz the class to generate JSON Schema for.
	 * @param toUpperCaseTypeValues if true, the type values are converted to upper case.
	 * @return the generated JSON Schema as a String.
	 */
	public static String getJsonSchema(Class<?> clazz, boolean toUpperCaseTypeValues) {

		if (SCHEMA_GENERATOR_CACHE.get() == null) {

			JacksonModule jacksonModule = new JacksonModule(JacksonOption.RESPECT_JSONPROPERTY_REQUIRED);
			Swagger2Module swaggerModule = new Swagger2Module();

			SchemaGeneratorConfigBuilder configBuilder = new SchemaGeneratorConfigBuilder(SchemaVersion.DRAFT_2020_12,
					OptionPreset.PLAIN_JSON)
				.with(Option.EXTRA_OPEN_API_FORMAT_VALUES)
				.with(Option.PLAIN_DEFINITION_KEYS)
				.with(swaggerModule)
				.with(jacksonModule);

			SchemaGeneratorConfig config = configBuilder.build();
			SchemaGenerator generator = new SchemaGenerator(config);
			SCHEMA_GENERATOR_CACHE.compareAndSet(null, generator);
		}

		ObjectNode node = SCHEMA_GENERATOR_CACHE.get().generateSchema(clazz);
		if (toUpperCaseTypeValues) { // Required for OpenAPI 3.0 (at least Vertex AI
										// version of it).
			toUpperCaseTypeValues(node);
		}

		return node.toPrettyString();
	}

	public static void toUpperCaseTypeValues(ObjectNode node) {
		if (node == null) {
			return;
		}
		if (node.isObject()) {
			node.fields().forEachRemaining(entry -> {
				JsonNode value = entry.getValue();
				if (value.isObject()) {
					toUpperCaseTypeValues((ObjectNode) value);
				}
				else if (value.isArray()) {
					((ArrayNode) value).elements().forEachRemaining(element -> {
						if (element.isObject() || element.isArray()) {
							toUpperCaseTypeValues((ObjectNode) element);
						}
					});
				}
				else if (value.isTextual() && entry.getKey().equals("type")) {
					String oldValue = ((ObjectNode) node).get("type").asText();
					((ObjectNode) node).put("type", oldValue.toUpperCase());
				}
			});
		}
		else if (node.isArray()) {
			node.elements().forEachRemaining(element -> {
				if (element.isObject() || element.isArray()) {
					toUpperCaseTypeValues((ObjectNode) element);
				}
			});
		}
	}

}
