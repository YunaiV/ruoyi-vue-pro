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
package org.springframework.ai.model.function;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import org.springframework.beans.BeansException;
import org.springframework.cloud.function.context.catalog.FunctionTypeUtils;
import org.springframework.cloud.function.context.config.FunctionContextUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Description;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

import java.lang.reflect.Type;
import java.util.function.Function;

/**
 * A Spring {@link ApplicationContextAware} implementation that provides a way to retrieve
 * a {@link Function} from the Spring context and wrap it into a {@link FunctionCallback}.
 *
 * The name of the function is determined by the bean name.
 *
 * The description of the function is determined by the following rules:
 * <ul>
 * <li>Provided as a default description</li>
 * <li>Provided as a {@code @Description} annotation on the bean</li>
 * <li>Provided as a {@code @JsonClassDescription} annotation on the input class</li>
 * </ul>
 *
 * @author Christian Tzolov
 * @author Christopher Smith
 */
public class FunctionCallbackContext implements ApplicationContextAware {

	private GenericApplicationContext applicationContext;

	private FunctionCallbackWrapper.Builder.SchemaType schemaType = FunctionCallbackWrapper.Builder.SchemaType.JSON_SCHEMA;

	public void setSchemaType(FunctionCallbackWrapper.Builder.SchemaType schemaType) {
		this.schemaType = schemaType;
	}

	@Override
	public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = (GenericApplicationContext) applicationContext;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public FunctionCallback getFunctionCallback(@NonNull String beanName, @Nullable String defaultDescription) {

		Type beanType = FunctionContextUtils.findType(this.applicationContext.getBeanFactory(), beanName);

		if (beanType == null) {
			throw new IllegalArgumentException(
					"Functional bean with name: " + beanName + " does not exist in the context.");
		}

		if (!Function.class.isAssignableFrom(FunctionTypeUtils.getRawType(beanType))) {
			throw new IllegalArgumentException(
					"Function call Bean must be of type Function. Found: " + beanType.getTypeName());
		}

		Type functionInputType = TypeResolverHelper.getFunctionArgumentType(beanType, 0);

		Class<?> functionInputClass = FunctionTypeUtils.getRawType(functionInputType);
		String functionName = beanName;
		String functionDescription = defaultDescription;

		if (!StringUtils.hasText(functionDescription)) {
			// Look for a Description annotation on the bean
			Description descriptionAnnotation = applicationContext.findAnnotationOnBean(beanName, Description.class);

			if (descriptionAnnotation != null) {
				functionDescription = descriptionAnnotation.value();
			}

			if (!StringUtils.hasText(functionDescription)) {
				// Look for a JsonClassDescription annotation on the input class
				JsonClassDescription jsonClassDescriptionAnnotation = functionInputClass
					.getAnnotation(JsonClassDescription.class);
				if (jsonClassDescriptionAnnotation != null) {
					functionDescription = jsonClassDescriptionAnnotation.value();
				}
			}

			if (!StringUtils.hasText(functionDescription)) {
				throw new IllegalStateException("Could not determine function description."
						+ "Please provide a description either as a default parameter, via @Description annotation on the bean "
						+ "or @JsonClassDescription annotation on the input class.");
			}
		}

		Object bean = this.applicationContext.getBean(beanName);

        // TODO: 2024/3/16 fansili 适配jdk8
        return null;
//		if (bean instanceof Function<?, ?> function) {
//			return FunctionCallbackWrapper.builder(function)
//				.withName(functionName)
//				.withSchemaType(this.schemaType)
//				.withDescription(functionDescription)
//				.withInputType(functionInputClass)
//				.build();
//		}
//		else {
//			throw new IllegalArgumentException("Bean must be of type Function");
//		}
	}

}
