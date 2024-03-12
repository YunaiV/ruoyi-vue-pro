package cn.iocoder.yudao.framework.ai.model.function;

import cn.iocoder.yudao.framework.ai.model.ModelOptionsUtils;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.util.Assert;

import java.util.function.Function;

/**
 * Note that the underlying function is responsible for converting the output into format
 * that can be consumed by the Model. The default implementation converts the output into
 * String before sending it to the Model. Provide a custom function responseConverter
 * implementation to override this.
 *
 */
public class FunctionCallbackWrapper<I, O> extends AbstractFunctionCallback<I, O> {

	private final Function<I, O> function;

	private FunctionCallbackWrapper(String name, String description, String inputTypeSchema, Class<I> inputType,
			Function<O, String> responseConverter, Function<I, O> function) {
		super(name, description, inputTypeSchema, inputType, responseConverter,
				new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false));
		Assert.notNull(function, "Function must not be null");
		this.function = function;
	}

	@SuppressWarnings("unchecked")
	private static <I, O> Class<I> resolveInputType(Function<I, O> function) {
		return (Class<I>) TypeResolverHelper.getFunctionInputClass((Class<Function<I, O>>) function.getClass());
	}

	@Override
	public O apply(I input) {
		return this.function.apply(input);
	}

	public static <I, O> Builder<I, O> builder(Function<I, O> function) {
		return new Builder<>(function);
	}

	public static class Builder<I, O> {

		public enum SchemaType {

			JSON_SCHEMA, OPEN_API_SCHEMA

		}

		private String name;

		private String description;

		private Class<I> inputType;

		private final Function<I, O> function;

		private SchemaType schemaType = SchemaType.JSON_SCHEMA;

		public Builder(Function<I, O> function) {
			Assert.notNull(function, "Function must not be null");
			this.function = function;
		}

		// By default the response is converted to a JSON string.
		private Function<O, String> responseConverter = (response) -> ModelOptionsUtils.toJsonString(response);

		private String inputTypeSchema;

		private ObjectMapper objectMapper = new ObjectMapper()
			.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

		public Builder<I, O> withName(String name) {
			Assert.hasText(name, "Name must not be empty");
			this.name = name;
			return this;
		}

		public Builder<I, O> withDescription(String description) {
			Assert.hasText(description, "Description must not be empty");
			this.description = description;
			return this;
		}

		@SuppressWarnings("unchecked")
		public Builder<I, O> withInputType(Class<?> inputType) {
			this.inputType = (Class<I>) inputType;
			return this;
		}

		public Builder<I, O> withResponseConverter(Function<O, String> responseConverter) {
			Assert.notNull(responseConverter, "ResponseConverter must not be null");
			this.responseConverter = responseConverter;
			return this;
		}

		public Builder<I, O> withInputTypeSchema(String inputTypeSchema) {
			Assert.hasText(inputTypeSchema, "InputTypeSchema must not be empty");
			this.inputTypeSchema = inputTypeSchema;
			return this;
		}

		public Builder<I, O> withObjectMapper(ObjectMapper objectMapper) {
			Assert.notNull(objectMapper, "ObjectMapper must not be null");
			this.objectMapper = objectMapper;
			return this;
		}

		public Builder<I, O> withSchemaType(SchemaType schemaType) {
			Assert.notNull(schemaType, "SchemaType must not be null");
			this.schemaType = schemaType;
			return this;
		}

		public FunctionCallbackWrapper<I, O> build() {

			Assert.hasText(this.name, "Name must not be empty");
			Assert.hasText(this.description, "Description must not be empty");
			// Assert.notNull(this.inputType, "InputType must not be null");
			Assert.notNull(this.function, "Function must not be null");
			Assert.notNull(this.responseConverter, "ResponseConverter must not be null");
			Assert.notNull(this.objectMapper, "ObjectMapper must not be null");

			if (this.inputType == null) {
				this.inputType = resolveInputType(this.function);
			}

			if (this.inputTypeSchema == null) {
				boolean upperCaseTypeValues = this.schemaType == SchemaType.OPEN_API_SCHEMA;
				this.inputTypeSchema = ModelOptionsUtils.getJsonSchema(this.inputType, upperCaseTypeValues);
			}

			return new FunctionCallbackWrapper<>(this.name, this.description, this.inputTypeSchema, this.inputType,
					this.responseConverter, this.function);
		}

	}

}