const ALLOWED_TYPES = {
  FailedJobRetryTimeCycle: [
    'bpmn:StartEvent',
    'bpmn:BoundaryEvent',
    'bpmn:IntermediateCatchEvent',
    'bpmn:Activity',
  ],
  Connector: ['bpmn:EndEvent', 'bpmn:IntermediateThrowEvent'],
  Field: ['bpmn:EndEvent', 'bpmn:IntermediateThrowEvent'],
};

function is(element, type) {
  return (
    element &&
    typeof element.$instanceOf === 'function' &&
    element.$instanceOf(type)
  );
}

function exists(element) {
  return element && element.length > 0;
}

function includesType(collection, type) {
  return (
    exists(collection) &&
    collection.some((element) => {
      return is(element, type);
    })
  );
}

function anyType(element, types) {
  return types.some((type) => {
    return is(element, type);
  });
}

function isAllowed(propName, propDescriptor, newElement) {
  const name = propDescriptor.name;
  const types = ALLOWED_TYPES[name.replace(/activiti:/, '')];

  return name === propName && anyType(newElement, types);
}

function ActivitiModdleExtension(eventBus) {
  eventBus.on(
    'property.clone',
    function (context) {
      const newElement = context.newElement;
      const propDescriptor = context.propertyDescriptor;

      this.canCloneProperty(newElement, propDescriptor);
    },
    this,
  );
}

ActivitiModdleExtension.$inject = ['eventBus'];

ActivitiModdleExtension.prototype.canCloneProperty = function (
  newElement,
  propDescriptor,
) {
  if (
    isAllowed('activiti:FailedJobRetryTimeCycle', propDescriptor, newElement)
  ) {
    return (
      includesType(newElement.eventDefinitions, 'bpmn:TimerEventDefinition') ||
      includesType(newElement.eventDefinitions, 'bpmn:SignalEventDefinition') ||
      is(
        newElement.loopCharacteristics,
        'bpmn:MultiInstanceLoopCharacteristics',
      )
    );
  }

  if (isAllowed('activiti:Connector', propDescriptor, newElement)) {
    return includesType(
      newElement.eventDefinitions,
      'bpmn:MessageEventDefinition',
    );
  }

  if (isAllowed('activiti:Field', propDescriptor, newElement)) {
    return includesType(
      newElement.eventDefinitions,
      'bpmn:MessageEventDefinition',
    );
  }
};

// module.exports = ActivitiModdleExtension;
export default ActivitiModdleExtension;
