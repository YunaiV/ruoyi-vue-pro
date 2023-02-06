import BpmnRules from 'bpmn-js/lib/features/rules/BpmnRules'
import inherits from 'inherits'

export default function CustomRules(eventBus) {
  BpmnRules.call(this, eventBus)
}

inherits(CustomRules, BpmnRules)

CustomRules.prototype.canDrop = function () {
  return false
}

CustomRules.prototype.canMove = function () {
  return false
}
