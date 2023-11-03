import { assign } from "min-dash";

/**
 * A palette provider for BPMN 2.0 elements.
 */
export default function PaletteProvider(palette, create, elementFactory, spaceTool, lassoTool, handTool, globalConnect, translate) {
  this._palette = palette;
  this._create = create;
  this._elementFactory = elementFactory;
  this._spaceTool = spaceTool;
  this._lassoTool = lassoTool;
  this._handTool = handTool;
  this._globalConnect = globalConnect;
  this._translate = translate;

  palette.registerProvider(this);
}

PaletteProvider.$inject = ["palette", "create", "elementFactory", "spaceTool", "lassoTool", "handTool", "globalConnect", "translate"];

PaletteProvider.prototype.getPaletteEntries = function() {
  const actions = {},
    create = this._create,
    elementFactory = this._elementFactory,
    spaceTool = this._spaceTool,
    lassoTool = this._lassoTool,
    handTool = this._handTool,
    globalConnect = this._globalConnect,
    translate = this._translate

  function createAction(type, group, className, title, options) {
    function createListener(event) {
      const shape = elementFactory.createShape(assign({ type: type }, options))

      if (options) {
        shape.businessObject.di.isExpanded = options.isExpanded;
      }

      create.start(event, shape);
    }

    const shortType = type.replace(/^bpmn:/, '')

    return {
      group: group,
      className: className,
      title: title || translate("Create {type}", { type: shortType }),
      action: {
        dragstart: createListener,
        click: createListener
      }
    };
  }

  function createSubprocess(event) {
    const subProcess = elementFactory.createShape({
      type: 'bpmn:SubProcess',
      x: 0,
      y: 0,
      isExpanded: true
    })

    const startEvent = elementFactory.createShape({
      type: 'bpmn:StartEvent',
      x: 40,
      y: 82,
      parent: subProcess
    })

    create.start(event, [subProcess, startEvent], {
      hints: {
        autoSelect: [startEvent]
      }
    });
  }

  function createParticipant(event) {
    create.start(event, elementFactory.createParticipantShape());
  }

  assign(actions, {
    "hand-tool": {
      group: "tools",
      className: "bpmn-icon-hand-tool",
      title: translate("Activate the hand tool"),
      action: {
        click: function(event) {
          handTool.activateHand(event);
        }
      }
    },
    "lasso-tool": {
      group: "tools",
      className: "bpmn-icon-lasso-tool",
      title: translate("Activate the lasso tool"),
      action: {
        click: function(event) {
          lassoTool.activateSelection(event);
        }
      }
    },
    "space-tool": {
      group: "tools",
      className: "bpmn-icon-space-tool",
      title: translate("Activate the create/remove space tool"),
      action: {
        click: function(event) {
          spaceTool.activateSelection(event);
        }
      }
    },
    "global-connect-tool": {
      group: "tools",
      className: "bpmn-icon-connection-multi",
      title: translate("Activate the global connect tool"),
      action: {
        click: function(event) {
          globalConnect.toggle(event);
        }
      }
    },
    "tool-separator": {
      group: "tools",
      separator: true
    },
    "create.start-event": createAction("bpmn:StartEvent", "event", "bpmn-icon-start-event-none", translate("Create StartEvent")),
    "create.intermediate-event": createAction(
      "bpmn:IntermediateThrowEvent",
      "event",
      "bpmn-icon-intermediate-event-none",
      translate("Create Intermediate/Boundary Event")
    ),
    "create.end-event": createAction("bpmn:EndEvent", "event", "bpmn-icon-end-event-none", translate("Create EndEvent")),
    "create.exclusive-gateway": createAction("bpmn:ExclusiveGateway", "gateway", "bpmn-icon-gateway-none", translate("Create Gateway")),
    "create.user-task": createAction("bpmn:UserTask", "activity", "bpmn-icon-user-task", translate("Create User Task")),
    "create.data-object": createAction("bpmn:DataObjectReference", "data-object", "bpmn-icon-data-object", translate("Create DataObjectReference")),
    "create.data-store": createAction("bpmn:DataStoreReference", "data-store", "bpmn-icon-data-store", translate("Create DataStoreReference")),
    "create.subprocess-expanded": {
      group: "activity",
      className: "bpmn-icon-subprocess-expanded",
      title: translate("Create expanded SubProcess"),
      action: {
        dragstart: createSubprocess,
        click: createSubprocess
      }
    },
    "create.participant-expanded": {
      group: "collaboration",
      className: "bpmn-icon-participant",
      title: translate("Create Pool/Participant"),
      action: {
        dragstart: createParticipant,
        click: createParticipant
      }
    },
    "create.group": createAction("bpmn:Group", "artifact", "bpmn-icon-group", translate("Create Group"))
  });

  return actions;
};
