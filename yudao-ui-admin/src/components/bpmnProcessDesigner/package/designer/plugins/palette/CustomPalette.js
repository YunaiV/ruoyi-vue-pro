import PaletteProvider from "bpmn-js/lib/features/palette/PaletteProvider";
import { assign } from "min-dash";

export default function CustomPalette(palette, create, elementFactory, spaceTool, lassoTool, handTool, globalConnect, translate) {
  PaletteProvider.call(this, palette, create, elementFactory, spaceTool, lassoTool, handTool, globalConnect, translate, 2000);
}

const F = function() {}; // 核心，利用空对象作为中介；
F.prototype = PaletteProvider.prototype; // 核心，将父类的原型赋值给空对象F；

// 利用中介函数重写原型链方法
F.prototype.getPaletteEntries = function() {
  var actions = {},
    create = this._create,
    elementFactory = this._elementFactory,
    spaceTool = this._spaceTool,
    lassoTool = this._lassoTool,
    handTool = this._handTool,
    globalConnect = this._globalConnect,
    translate = this._translate;

  function createAction(type, group, className, title, options) {
    function createListener(event) {
      var shape = elementFactory.createShape(assign({ type: type }, options));

      if (options) {
        shape.businessObject.di.isExpanded = options.isExpanded;
      }

      create.start(event, shape);
    }

    var shortType = type.replace(/^bpmn:/, "");

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
    var subProcess = elementFactory.createShape({
      type: "bpmn:SubProcess",
      x: 0,
      y: 0,
      isExpanded: true
    });

    var startEvent = elementFactory.createShape({
      type: "bpmn:StartEvent",
      x: 40,
      y: 82,
      parent: subProcess
    });

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

CustomPalette.$inject = ["palette", "create", "elementFactory", "spaceTool", "lassoTool", "handTool", "globalConnect", "translate"];

CustomPalette.prototype = new F(); // 核心，将 F的实例赋值给子类；
CustomPalette.prototype.constructor = CustomPalette; // 修复子类CustomPalette的构造器指向，防止原型链的混乱；
