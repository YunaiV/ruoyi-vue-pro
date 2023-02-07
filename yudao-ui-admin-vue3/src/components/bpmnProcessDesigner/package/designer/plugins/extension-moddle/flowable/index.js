/*
 * @author igdianov
 * address https://github.com/igdianov/activiti-bpmn-moddle
 * */
import flowableExtension from './flowableExtension'

export default {
  __init__: ['FlowableModdleExtension'],
  FlowableModdleExtension: ['type', flowableExtension]
}
