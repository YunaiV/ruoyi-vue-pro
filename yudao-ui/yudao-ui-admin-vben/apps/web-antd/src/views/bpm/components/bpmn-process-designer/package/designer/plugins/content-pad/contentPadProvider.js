import { getChildLanes } from 'bpmn-js/lib/features/modeling/util/LaneUtil';
import { isAny } from 'bpmn-js/lib/features/modeling/util/ModelingUtil';
import { isEventSubProcess, isExpanded } from 'bpmn-js/lib/util/DiUtil';
import { is } from 'bpmn-js/lib/util/ModelUtil';
import { hasPrimaryModifier } from 'diagram-js/lib/util/Mouse';

/**
 * A provider for BPMN 2.0 elements context pad
 */
export default function ContextPadProvider(
  config,
  injector,
  eventBus,
  contextPad,
  modeling,
  elementFactory,
  connect,
  create,
  popupMenu,
  canvas,
  rules,
  translate,
) {
  config = config || {};

  contextPad.registerProvider(this);

  this._contextPad = contextPad;

  this._modeling = modeling;

  this._elementFactory = elementFactory;
  this._connect = connect;
  this._create = create;
  this._popupMenu = popupMenu;
  this._canvas = canvas;
  this._rules = rules;
  this._translate = translate;

  if (config.autoPlace !== false) {
    this._autoPlace = injector.get('autoPlace', false);
  }

  eventBus.on('create.end', 250, (event) => {
    const context = event.context;
    const shape = context.shape;

    if (!hasPrimaryModifier(event) || !contextPad.isOpen(shape)) {
      return;
    }

    const entries = contextPad.getEntries(shape);

    if (entries.replace) {
      entries.replace.action.click(event, shape);
    }
  });
}

ContextPadProvider.$inject = [
  'config.contextPad',
  'injector',
  'eventBus',
  'contextPad',
  'modeling',
  'elementFactory',
  'connect',
  'create',
  'popupMenu',
  'canvas',
  'rules',
  'translate',
  'elementRegistry',
];

ContextPadProvider.prototype.getContextPadEntries = function (element) {
  const autoPlace = this._autoPlace;
  const canvas = this._canvas;
  const connect = this._connect;
  const contextPad = this._contextPad;
  const create = this._create;
  const elementFactory = this._elementFactory;
  const modeling = this._modeling;
  const popupMenu = this._popupMenu;
  const rules = this._rules;
  const translate = this._translate;

  const actions = {};

  if (element.type === 'label') {
    return actions;
  }

  const businessObject = element.businessObject;

  function startConnect(event, element) {
    connect.start(event, element);
  }

  function removeElement() {
    modeling.removeElements([element]);
  }

  function getReplaceMenuPosition(element) {
    const Y_OFFSET = 5;

    const diagramContainer = canvas.getContainer();
    const pad = contextPad.getPad(element).html;

    const diagramRect = diagramContainer.getBoundingClientRect();
    const padRect = pad.getBoundingClientRect();

    const top = padRect.top - diagramRect.top;
    const left = padRect.left - diagramRect.left;

    const pos = {
      x: left,
      y: top + padRect.height + Y_OFFSET,
    };

    return pos;
  }

  /**
   * Create an append action
   *
   * @param {string} type
   * @param {string} className
   * @param {string} [title]
   * @param {object} [options]
   *
   * @return {object} descriptor
   */
  function appendAction(type, className, title, options) {
    if (typeof title !== 'string') {
      options = title;
      title = translate('Append {type}', { type: type.replace(/^bpmn:/, '') });
    }

    function appendStart(event, element) {
      const shape = elementFactory.createShape(
        Object.assign({ type }, options),
      );
      create.start(event, shape, {
        source: element,
      });
    }

    const append = autoPlace
      ? function (event, element) {
          const shape = elementFactory.createShape(
            Object.assign({ type }, options),
          );

          autoPlace.append(element, shape);
        }
      : appendStart;

    return {
      group: 'model',
      className,
      title,
      action: {
        dragstart: appendStart,
        click: append,
      },
    };
  }

  function splitLaneHandler(count) {
    return function (event, element) {
      // actual split
      modeling.splitLane(element, count);

      // refresh context pad after split to
      // get rid of split icons
      contextPad.open(element, true);
    };
  }

  if (
    isAny(businessObject, ['bpmn:Lane', 'bpmn:Participant']) &&
    isExpanded(businessObject)
  ) {
    const childLanes = getChildLanes(element);

    Object.assign(actions, {
      'lane-insert-above': {
        group: 'lane-insert-above',
        className: 'bpmn-icon-lane-insert-above',
        title: translate('Add Lane above'),
        action: {
          click(event, element) {
            modeling.addLane(element, 'top');
          },
        },
      },
    });

    if (childLanes.length < 2) {
      if (element.height >= 120) {
        Object.assign(actions, {
          'lane-divide-two': {
            group: 'lane-divide',
            className: 'bpmn-icon-lane-divide-two',
            title: translate('Divide into two Lanes'),
            action: {
              click: splitLaneHandler(2),
            },
          },
        });
      }

      if (element.height >= 180) {
        Object.assign(actions, {
          'lane-divide-three': {
            group: 'lane-divide',
            className: 'bpmn-icon-lane-divide-three',
            title: translate('Divide into three Lanes'),
            action: {
              click: splitLaneHandler(3),
            },
          },
        });
      }
    }

    Object.assign(actions, {
      'lane-insert-below': {
        group: 'lane-insert-below',
        className: 'bpmn-icon-lane-insert-below',
        title: translate('Add Lane below'),
        action: {
          click(event, element) {
            modeling.addLane(element, 'bottom');
          },
        },
      },
    });
  }

  if (is(businessObject, 'bpmn:FlowNode')) {
    if (is(businessObject, 'bpmn:EventBasedGateway')) {
      Object.assign(actions, {
        'append.receive-task': appendAction(
          'bpmn:ReceiveTask',
          'bpmn-icon-receive-task',
          translate('Append ReceiveTask'),
        ),
        'append.message-intermediate-event': appendAction(
          'bpmn:IntermediateCatchEvent',
          'bpmn-icon-intermediate-event-catch-message',
          translate('Append MessageIntermediateCatchEvent'),
          { eventDefinitionType: 'bpmn:MessageEventDefinition' },
        ),
        'append.timer-intermediate-event': appendAction(
          'bpmn:IntermediateCatchEvent',
          'bpmn-icon-intermediate-event-catch-timer',
          translate('Append TimerIntermediateCatchEvent'),
          { eventDefinitionType: 'bpmn:TimerEventDefinition' },
        ),
        'append.condition-intermediate-event': appendAction(
          'bpmn:IntermediateCatchEvent',
          'bpmn-icon-intermediate-event-catch-condition',
          translate('Append ConditionIntermediateCatchEvent'),
          { eventDefinitionType: 'bpmn:ConditionalEventDefinition' },
        ),
        'append.signal-intermediate-event': appendAction(
          'bpmn:IntermediateCatchEvent',
          'bpmn-icon-intermediate-event-catch-signal',
          translate('Append SignalIntermediateCatchEvent'),
          { eventDefinitionType: 'bpmn:SignalEventDefinition' },
        ),
      });
    } else if (
      isEventType(
        businessObject,
        'bpmn:BoundaryEvent',
        'bpmn:CompensateEventDefinition',
      )
    ) {
      Object.assign(actions, {
        'append.compensation-activity': appendAction(
          'bpmn:Task',
          'bpmn-icon-task',
          translate('Append compensation activity'),
          {
            isForCompensation: true,
          },
        ),
      });
    } else if (
      !is(businessObject, 'bpmn:EndEvent') &&
      !businessObject.isForCompensation &&
      !isEventType(
        businessObject,
        'bpmn:IntermediateThrowEvent',
        'bpmn:LinkEventDefinition',
      ) &&
      !isEventSubProcess(businessObject)
    ) {
      Object.assign(actions, {
        'append.end-event': appendAction(
          'bpmn:EndEvent',
          'bpmn-icon-end-event-none',
          translate('Append EndEvent'),
        ),
        'append.gateway': appendAction(
          'bpmn:ExclusiveGateway',
          'bpmn-icon-gateway-none',
          translate('Append Gateway'),
        ),
        'append.append-task': appendAction(
          'bpmn:UserTask',
          'bpmn-icon-user-task',
          translate('Append Task'),
        ),
        'append.intermediate-event': appendAction(
          'bpmn:IntermediateThrowEvent',
          'bpmn-icon-intermediate-event-none',
          translate('Append Intermediate/Boundary Event'),
        ),
      });
    }
  }

  if (!popupMenu.isEmpty(element, 'bpmn-replace')) {
    // Replace menu entry
    Object.assign(actions, {
      replace: {
        group: 'edit',
        className: 'bpmn-icon-screw-wrench',
        title: '修改类型',
        action: {
          click(event, element) {
            const position = Object.assign(getReplaceMenuPosition(element), {
              cursor: { x: event.x, y: event.y },
            });

            popupMenu.open(element, 'bpmn-replace', position);
          },
        },
      },
    });
  }

  if (
    isAny(businessObject, [
      'bpmn:FlowNode',
      'bpmn:InteractionNode',
      'bpmn:DataObjectReference',
      'bpmn:DataStoreReference',
    ])
  ) {
    Object.assign(actions, {
      'append.text-annotation': appendAction(
        'bpmn:TextAnnotation',
        'bpmn-icon-text-annotation',
      ),

      connect: {
        group: 'connect',
        className: 'bpmn-icon-connection-multi',
        title: translate(
          `Connect using ${
            businessObject.isForCompensation ? '' : 'Sequence/MessageFlow or '
          }Association`,
        ),
        action: {
          click: startConnect,
          dragstart: startConnect,
        },
      },
    });
  }

  if (
    isAny(businessObject, [
      'bpmn:DataObjectReference',
      'bpmn:DataStoreReference',
    ])
  ) {
    Object.assign(actions, {
      connect: {
        group: 'connect',
        className: 'bpmn-icon-connection-multi',
        title: translate('Connect using DataInputAssociation'),
        action: {
          click: startConnect,
          dragstart: startConnect,
        },
      },
    });
  }

  if (is(businessObject, 'bpmn:Group')) {
    Object.assign(actions, {
      'append.text-annotation': appendAction(
        'bpmn:TextAnnotation',
        'bpmn-icon-text-annotation',
      ),
    });
  }

  // delete element entry, only show if allowed by rules
  let deleteAllowed = rules.allowed('elements.delete', { elements: [element] });

  if (Array.isArray(deleteAllowed)) {
    // was the element returned as a deletion candidate?
    deleteAllowed = deleteAllowed[0] === element;
  }

  if (deleteAllowed) {
    Object.assign(actions, {
      delete: {
        group: 'edit',
        className: 'bpmn-icon-trash',
        title: translate('Remove'),
        action: {
          click: removeElement,
        },
      },
    });
  }

  return actions;
};

// helpers /////////

function isEventType(eventBo, type, definition) {
  const isType = eventBo.$instanceOf(type);
  let isDefinition = false;

  const definitions = eventBo.eventDefinitions || [];
  definitions.forEach((def) => {
    if (def.$type === definition) {
      isDefinition = true;
    }
  });

  return isType && isDefinition;
}
