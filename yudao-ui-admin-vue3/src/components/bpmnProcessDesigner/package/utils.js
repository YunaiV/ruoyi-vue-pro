// 创建监听器实例
export function createListenerObject(options, isTask, prefix) {
  const listenerObj = Object.create(null);
  listenerObj.event = options.event;
  isTask && (listenerObj.id = options.id); // 任务监听器特有的 id 字段
  switch (options.listenerType) {
    case "scriptListener":
      listenerObj.script = createScriptObject(options, prefix);
      break;
    case "expressionListener":
      listenerObj.expression = options.expression;
      break;
    case "delegateExpressionListener":
      listenerObj.delegateExpression = options.delegateExpression;
      break;
    default:
      listenerObj.class = options.class;
  }
  // 注入字段
  if (options.fields) {
    listenerObj.fields = options.fields.map(field => {
      return createFieldObject(field, prefix);
    });
  }
  // 任务监听器的 定时器 设置
  if (isTask && options.event === "timeout" && !!options.eventDefinitionType) {
    const timeDefinition = window.bpmnInstances.moddle.create("bpmn:FormalExpression", { body: options.eventTimeDefinitions });
    const TimerEventDefinition = window.bpmnInstances.moddle.create("bpmn:TimerEventDefinition", {
      id: `TimerEventDefinition_${uuid(8)}`,
      [`time${options.eventDefinitionType.replace(/^\S/, s => s.toUpperCase())}`]: timeDefinition
    });
    listenerObj.eventDefinitions = [TimerEventDefinition];
  }
  return window.bpmnInstances.moddle.create(`${prefix}:${isTask ? "TaskListener" : "ExecutionListener"}`, listenerObj);
}

// 创建 监听器的注入字段 实例
export function createFieldObject(option, prefix) {
  const { name, fieldType, string, expression } = option;
  const fieldConfig = fieldType === "string" ? { name, string } : { name, expression };
  return window.bpmnInstances.moddle.create(`${prefix}:Field`, fieldConfig);
}

// 创建脚本实例
export function createScriptObject(options, prefix) {
  const { scriptType, scriptFormat, value, resource } = options;
  const scriptConfig = scriptType === "inlineScript" ? { scriptFormat, value } : { scriptFormat, resource };
  return window.bpmnInstances.moddle.create(`${prefix}:Script`, scriptConfig);
}

// 更新元素扩展属性
export function updateElementExtensions(element, extensionList) {
  const extensions = window.bpmnInstances.moddle.create("bpmn:ExtensionElements", {
    values: extensionList
  });
  window.bpmnInstances.modeling.updateProperties(element, {
    extensionElements: extensions
  });
}

// 创建一个id
export function uuid(length = 8, chars) {
  let result = "";
  let charsString = chars || "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
  for (let i = length; i > 0; --i) {
    result += charsString[Math.floor(Math.random() * charsString.length)];
  }
  return result;
}
