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

import org.springframework.core.convert.support.DefaultConversionService;

import java.util.List;

/**
 * {@link OutputParser} implementation that uses a {@link DefaultConversionService} to
 * convert the LLM output into a {@link List} instance.
 *
 * @author Mark Pollack
 * @author Christian Tzolov
 */
public class ListOutputParser extends AbstractConversionServiceOutputParser<List<String>> {

	public ListOutputParser(DefaultConversionService defaultConversionService) {
		super(defaultConversionService);
	}

	@Override
	public String getFormat() {
//		return """
//				Your response should be a list of comma separated values
//				eg: `foo, bar, baz`
//				""";
		return "Your response should be a list of comma separated values\n" +
				"\t\t\t\teg: `foo, bar, baz`";
	}

	@Override
	public List<String> parse(String text) {
		return getConversionService().convert(text, List.class);
	}

}
