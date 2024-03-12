# Output Parsing

* [Documentation](https://docs.spring.io/spring-ai/reference/concepts.html#_output_parsing)
* [Usage examples](https://github.com/spring-projects/spring-ai/blob/main/spring-ai-openai/src/test/java/org/springframework/ai/openai/client/ClientIT.java)

The output of AI models traditionally arrives as a java.util.String, even if you ask for the reply to be in JSON. It may be the correct JSON, but it isn’t a JSON data structure. It is just a string. Also, asking "for JSON" as part of the prompt isn’t 100% accurate.

This intricacy has led to the emergence of a specialized field involving the creation of prompts to yield the intended output, followed by parsing the resulting simple string into a usable data structure for application integration.

Output parsing employs meticulously crafted prompts, often necessitating multiple interactions with the model to achieve the desired formatting.

This challenge has prompted OpenAI to introduce 'OpenAI Functions' as a means to specify the desired output format from the model precisely.
