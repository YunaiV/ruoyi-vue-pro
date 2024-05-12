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

import net.jodah.typetools.TypeResolver;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.function.Function;

/**
 * @author Christian Tzolov
 */
public class TypeResolverHelper {

	public static Class<?> getFunctionInputClass(Class<? extends Function<?, ?>> functionClass) {
		return getFunctionArgumentClass(functionClass, 0);
	}

	public static Class<?> getFunctionOutputClass(Class<? extends Function<?, ?>> functionClass) {
		return getFunctionArgumentClass(functionClass, 1);
	}

	public static Class<?> getFunctionArgumentClass(Class<? extends Function<?, ?>> functionClass, int argumentIndex) {
		Type type = TypeResolver.reify(Function.class, functionClass);

		var argumentType = type instanceof ParameterizedType
				? ((ParameterizedType) type).getActualTypeArguments()[argumentIndex] : Object.class;

		return toRawClass(argumentType);
	}

	public static Type getFunctionInputType(Class<? extends Function<?, ?>> functionClass) {
		return getFunctionArgumentType(functionClass, 0);
	}

	public static Type getFunctionOutputType(Class<? extends Function<?, ?>> functionClass) {
		return getFunctionArgumentType(functionClass, 1);
	}

	public static Type getFunctionArgumentType(Class<? extends Function<?, ?>> functionClass, int argumentIndex) {
		Type functionType = TypeResolver.reify(Function.class, functionClass);
		return getFunctionArgumentType(functionType, argumentIndex);
	}

	public static Type getFunctionArgumentType(Type functionType, int argumentIndex) {
		var argumentType = functionType instanceof ParameterizedType
				? ((ParameterizedType) functionType).getActualTypeArguments()[argumentIndex] : Object.class;

		return argumentType;
	}

	/**
	 * Effectively converts {@link Type} which could be {@link ParameterizedType} to raw
	 * Class (no generics).
	 * @param type actual {@link Type} instance
	 * @return instance of {@link Class} as raw representation of the provided
	 * {@link Type}
	 */
	public static Class<?> toRawClass(Type type) {
		return type != null
				? TypeResolver.resolveRawClass(type instanceof GenericArrayType ? type : TypeResolver.reify(type), null)
				: null;
	}

	// public static void main(String[] args) {
	// Class<? extends Function<?, ?>> clazz = MockWeatherService.class;
	// System.out.println(getFunctionInputType(clazz));

	// }

}
