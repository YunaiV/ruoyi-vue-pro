'use strict'

import { isFunction, isObject, some } from 'min-dash'

// const isFunction = isFunction,
//   isObject = isObject,
//   some = some
// const isFunction = require('min-dash').isFunction,
//   isObject = require('min-dash').isObject,
//   some = require('min-dash').some

const WILDCARD = '*'

function CamundaModdleExtension(eventBus) {
  // eslint-disable-next-line @typescript-eslint/no-this-alias
  const self = this

  eventBus.on('moddleCopy.canCopyProperty', function (context) {
    const property = context.property,
      parent = context.parent

    return self.canCopyProperty(property, parent)
  })
}

CamundaModdleExtension.$inject = ['eventBus']

/**
 * Check wether to disallow copying property.
 */
CamundaModdleExtension.prototype.canCopyProperty = function (property, parent) {
  // (1) check wether property is allowed in parent
  if (isObject(property) && !isAllowedInParent(property, parent)) {
    return false
  }

  // (2) check more complex scenarios

  if (is(property, 'camunda:InputOutput') && !this.canHostInputOutput(parent)) {
    return false
  }

  if (isAny(property, ['camunda:Connector', 'camunda:Field']) && !this.canHostConnector(parent)) {
    return false
  }

  if (is(property, 'camunda:In') && !this.canHostIn(parent)) {
    return false
  }
}

CamundaModdleExtension.prototype.canHostInputOutput = function (parent) {
  // allowed in camunda:Connector
  const connector = getParent(parent, 'camunda:Connector')

  if (connector) {
    return true
  }

  // special rules inside bpmn:FlowNode
  const flowNode = getParent(parent, 'bpmn:FlowNode')

  if (!flowNode) {
    return false
  }

  if (isAny(flowNode, ['bpmn:StartEvent', 'bpmn:Gateway', 'bpmn:BoundaryEvent'])) {
    return false
  }

  return !(is(flowNode, 'bpmn:SubProcess') && flowNode.get('triggeredByEvent'))
}

CamundaModdleExtension.prototype.canHostConnector = function (parent) {
  const serviceTaskLike = getParent(parent, 'camunda:ServiceTaskLike')

  if (is(serviceTaskLike, 'bpmn:MessageEventDefinition')) {
    // only allow on throw and end events
    return getParent(parent, 'bpmn:IntermediateThrowEvent') || getParent(parent, 'bpmn:EndEvent')
  }

  return true
}

CamundaModdleExtension.prototype.canHostIn = function (parent) {
  const callActivity = getParent(parent, 'bpmn:CallActivity')

  if (callActivity) {
    return true
  }

  const signalEventDefinition = getParent(parent, 'bpmn:SignalEventDefinition')

  if (signalEventDefinition) {
    // only allow on throw and end events
    return getParent(parent, 'bpmn:IntermediateThrowEvent') || getParent(parent, 'bpmn:EndEvent')
  }

  return true
}

// module.exports = CamundaModdleExtension;
export default CamundaModdleExtension

// helpers //////////

function is(element, type) {
  return element && isFunction(element.$instanceOf) && element.$instanceOf(type)
}

function isAny(element, types) {
  return some(types, function (t) {
    return is(element, t)
  })
}

function getParent(element, type) {
  if (!type) {
    return element.$parent
  }

  if (is(element, type)) {
    return element
  }

  if (!element.$parent) {
    return
  }

  return getParent(element.$parent, type)
}

function isAllowedInParent(property, parent) {
  // (1) find property descriptor
  const descriptor = property.$type && property.$model.getTypeDescriptor(property.$type)

  const allowedIn = descriptor && descriptor.meta && descriptor.meta.allowedIn

  if (!allowedIn || isWildcard(allowedIn)) {
    return true
  }

  // (2) check wether property has parent of allowed type
  return some(allowedIn, function (type) {
    return getParent(parent, type)
  })
}

function isWildcard(allowedIn) {
  return allowedIn.indexOf(WILDCARD) !== -1
}
