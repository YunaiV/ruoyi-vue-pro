<template>
  <div class="my-process-designer">
    <div class="my-process-designer__container">
      <div class="my-process-designer__canvas" ref="bpmn-canvas"></div>
    </div>
  </div>
</template>

<script>
import BpmnViewer from "bpmn-js/lib/Viewer";
import DefaultEmptyXML from "./plugins/defaultEmpty";

export default {
  name: "MyProcessViewer",
  componentName: "MyProcessViewer",
  props: {
    value: {  // BPMN XML 字符串
      type: String,
    },
    prefix: { // 使用哪个引擎
      type: String,
      default: "camunda",
    },
    activityData: { // 活动的数据。传递时，可高亮流程
      type: Array,
      default: () => [],
    },
    processInstanceData: { // 流程实例的数据。传递时，可展示流程发起人等信息
      type: Object,
    },
    taskData: { // 任务实例的数据。传递时，可展示 UserTask 审核相关的信息
      type: Array,
      default: () => [],
    }
  },
  data() {
    return {
      xml: '',
      activityList: [],
    };
  },
  mounted() {
    this.xml = this.value;
    this.activityList = this.activityData;
    // 初始化
    this.initBpmnModeler();
    this.createNewDiagram(this.xml);
    this.$once("hook:beforeDestroy", () => {
      if (this.bpmnModeler) this.bpmnModeler.destroy();
      this.$emit("destroy", this.bpmnModeler);
      this.bpmnModeler = null;
    });
    // 初始模型的监听器
    this.initModelListeners();
  },
  watch: {
    value: function (newValue) { // 在 xmlString 发生变化时，重新创建，从而绘制流程图
      this.xml = newValue;
      this.createNewDiagram(this.xml);
    },
    activityData: function (newActivityData) {
      this.activityList = newActivityData;
      this.createNewDiagram(this.xml);
    }
  },
  methods: {
    initBpmnModeler() {
      if (this.bpmnModeler) return;
      this.bpmnModeler = new BpmnViewer({
        container: this.$refs["bpmn-canvas"],
        bpmnRenderer: {
        }
      })
    },
    /* 创建新的流程图 */
    async createNewDiagram(xml) {
      // 将字符串转换成图显示出来
      let newId = `Process_${new Date().getTime()}`;
      let newName = `业务流程_${new Date().getTime()}`;
      let xmlString = xml || DefaultEmptyXML(newId, newName, this.prefix);
      try {
        // console.log(this.bpmnModeler.importXML);
        let { warnings } = await this.bpmnModeler.importXML(xmlString);
        if (warnings && warnings.length) {
          warnings.forEach(warn => console.warn(warn));
        }
        // 高亮流程图
        await this.highlightDiagram();
      } catch (e) {
        console.error(e);
        // console.error(`[Process Designer Warn]: ${e?.message || e}`);
      }
    },
    /* 高亮流程图 */
    // TODO 芋艿：如果多个 endActivity 的话，目前的逻辑可能有一定的问题。https://www.jdon.com/workflow/multi-events.html
    async highlightDiagram() {
      let activityList = this.activityList;
      if (activityList.length === 0) {
        return;
      }
      // 参考自 https://gitee.com/tony2y/RuoYi-flowable/blob/master/ruoyi-ui/src/components/Process/index.vue#L222 实现
      // 再次基础上，增加不同审批结果的颜色等等
      let canvas = this.bpmnModeler.get('canvas');
      let todoActivity = activityList.find(m => !m.endTime) // 找到待办的任务
      let endActivity = activityList[activityList.length - 1] // 找到结束任务
      this.bpmnModeler.getDefinitions().rootElements[0].flowElements?.forEach(n => {
        let activity = activityList.find(m => m.key === n.id) // 找到对应的活动
        if (n.$type === 'bpmn:UserTask') { // 用户任务
          if (!activity) {
            return;
          }
          // TODO 芋艿：
          if (activity.task) {
            const result = activity.task.result;
            if (result === 1) {
              canvas.addMarker(n.id, 'highlight-todo');
            } else if (result === 2) {
              canvas.addMarker(n.id, 'highlight');
            } else if (result === 3) {
              canvas.addMarker(n.id, 'highlight-reject');
            } else if (result === 4) {
              canvas.addMarker(n.id, 'highlight-cancel');
            }
          }
          n.outgoing?.forEach(nn => {
            let targetActivity = activityList.find(m => m.key === nn.targetRef.id)
            if (targetActivity) {
              debugger
              canvas.addMarker(nn.id, targetActivity.endTime ? 'highlight' : 'highlight-todo');
            } else if (nn.targetRef.$type === 'bpmn:ExclusiveGateway') {
              debugger
              canvas.addMarker(nn.id, activity.endTime ? 'highlight' : 'highlight-todo');
              canvas.addMarker(nn.targetRef.id, activity.endTime ? 'highlight' : 'highlight-todo');
            } else if (nn.targetRef.$type === 'bpmn:EndEvent') {
              debugger
              if (!todoActivity && endActivity.key === n.id) {
                canvas.addMarker(nn.id, 'highlight');
                canvas.addMarker(nn.targetRef.id, 'highlight');
              }
              if (!activity.endTime) {
                canvas.addMarker(nn.id, 'highlight-todo');
                canvas.addMarker(nn.targetRef.id, 'highlight-todo');
              }
            }
          });
        } else if (n.$type === 'bpmn:ExclusiveGateway') { // 排它网关
          n.outgoing?.forEach(nn => {
            let targetTask = activityList.find(m => m.key === nn.targetRef.id);
            if (targetTask) {
              canvas.addMarker(nn.id, targetTask.endTime ? 'highlight' : 'highlight-todo');
            }
          })
        } else if (n.$type === 'bpmn:ParallelGateway') { // 并行网关
          if (!activity) {
            return
          }
          // 设置【bpmn:ParallelGateway】并行网关的高亮
          canvas.addMarker(n.id, this.getActivityHighlightCss(activity));
          n.outgoing?.forEach(nn => {
            // 获得连线是否有指向目标。如果有，则进行高亮
            const targetActivity = activityList.find(m => m.key === nn.targetRef.id)
            if (targetActivity) {
              canvas.addMarker(nn.id, this.getActivityHighlightCss(targetActivity)); // 高亮【bpmn:SequenceFlow】连线
              // 高亮【...】目标。其中 ... 可以是 bpm:UserTask、也可以是其它的。当然，如果是 bpm:UserTask 的话，其实不做高亮也没问题，因为上面有逻辑做了这块。
              canvas.addMarker(nn.targetRef.id, this.getActivityHighlightCss(targetActivity));
            }
          })
        } else if (n.$type === 'bpmn:StartEvent') { // 开始节点
          n.outgoing?.forEach(nn => { // outgoing 例如说【bpmn:SequenceFlow】连线
            // 获得连线是否有指向目标。如果有，则进行高亮
            let targetActivity = activityList.find(m => m.key === nn.targetRef.id);
            if (targetActivity) {
              canvas.addMarker(nn.id, 'highlight'); // 高亮【bpmn:SequenceFlow】连线
              canvas.addMarker(n.id, 'highlight'); // 高亮【bpmn:StartEvent】开始节点（自己）
            }
          });
        } else if (n.$type === 'bpmn:EndEvent') { // 结束节点
          if (endActivity.key !== n.id) { // 保证 endActivity 就是 EndEvent
            return;
          }
          // 在并行网关后，跟着多个任务，如果其中一个任务完成，endActivity 的 endTime 就会存在值
          // 所以，通过 todoActivity 在做一次判断
          if (endActivity.endTime && !todoActivity) {
            canvas.addMarker(n.id, 'highlight');
          }
        }
      })
    },
    getActivityHighlightCss(activity) {
      return activity.endTime ? 'highlight' : 'highlight-todo';
    },
    getTaskHighlightCss(task) {
      if (!task) {
        return '';
      }
      return '';
    },
    initModelListeners() {
      const EventBus = this.bpmnModeler.get("eventBus");
      const that = this;
      // 注册需要的监听事件
      EventBus.on('element.hover', function(eventObj) {
        let element = eventObj ? eventObj.element : null;
        that.elementHover(element);
      });
      EventBus.on('element.out', function(eventObj) {
        let element = eventObj ? eventObj.element : null;
        that.elementOut(element);
      });
    },
    // 流程图的元素被 hover
    elementHover(element) {
      this.element = element;
      !this.elementOverlayIds && (this.elementOverlayIds = {});
      !this.overlays && (this.overlays = this.bpmnModeler.get("overlays"));
      // 展示信息
      if (!this.elementOverlayIds[element.id] && element.type !== "bpmn:Process") {
        this.elementOverlayIds[element.id] = this.overlays.add(element, {
          position: { left: 0, bottom: 0 },
          html: `<div class="element-overlays">
            <p>Elemet id: ${element.id}</p>
            <p>Elemet type: ${element.type}</p>
          </div>`
        });
      }
    },
    // 流程图的元素被 out
    elementOut(element) {
      this.overlays.remove({ element });
      this.elementOverlayIds[element.id] = null;
    },
  }
};
</script>

<style>

/** 处理中 */
.highlight-todo.djs-connection > .djs-visual > path {
  stroke: orange !important;
  stroke-dasharray: 4px !important;
  fill-opacity: 0.2 !important;
}
.highlight-todo.djs-shape .djs-visual > :nth-child(1) {
  fill: orange !important;
  stroke: orange !important;
  stroke-dasharray: 4px !important;
  fill-opacity: 0.2 !important;
}

/deep/.highlight-todo.djs-connection > .djs-visual > path {
  stroke: orange !important;
  stroke-dasharray: 4px !important;
  fill-opacity: 0.2 !important;
  marker-end: url(#sequenceflow-end-_E7DFDF-_E7DFDF-803g1kf6zwzmcig1y2ulm5egr);
}
/deep/.highlight-todo.djs-shape .djs-visual > :nth-child(1) {
  fill: orange !important;
  stroke: orange !important;
  stroke-dasharray: 4px !important;
  fill-opacity: 0.2 !important;
}

/** 通过 */
.highlight.djs-shape .djs-visual > :nth-child(1) {
  fill: green !important;
  stroke: green !important;
  fill-opacity: 0.2 !important;
}
.highlight.djs-shape .djs-visual > :nth-child(2) {
  fill: green !important;
}
.highlight.djs-shape .djs-visual > path {
  fill: green !important;
  fill-opacity: 0.2 !important;
  stroke: green !important;
}
.highlight.djs-connection > .djs-visual > path {
  stroke: green !important;
}

.highlight:not(.djs-connection) .djs-visual > :nth-child(1) {
  fill: green !important; /* color elements as green */
}

/deep/.highlight.djs-shape .djs-visual > :nth-child(1) {
  fill: green !important;
  stroke: green !important;
  fill-opacity: 0.2 !important;
}
/deep/.highlight.djs-shape .djs-visual > :nth-child(2) {
  fill: green !important;
}
/deep/.highlight.djs-shape .djs-visual > path {
  fill: green !important;
  fill-opacity: 0.2 !important;
  stroke: green !important;
}
/deep/.highlight.djs-connection > .djs-visual > path {
  stroke: green !important;
}

/** 不通过 */
.highlight-reject.djs-shape .djs-visual > :nth-child(1) {
  fill: red !important;
  stroke: red !important;
  fill-opacity: 0.2 !important;
}
.highlight-reject.djs-shape .djs-visual > :nth-child(2) {
  fill: red !important;
}
.highlight-reject.djs-shape .djs-visual > path {
  fill: red !important;
  fill-opacity: 0.2 !important;
  stroke: red !important;
}
.highlight-reject.djs-connection > .djs-visual > path {
  stroke: red !important;
}

.highlight-reject:not(.djs-connection) .djs-visual > :nth-child(1) {
  fill: red !important; /* color elements as green */
}

/deep/.highlight-reject.djs-shape .djs-visual > :nth-child(1) {
  fill: red !important;
  stroke: red !important;
  fill-opacity: 0.2 !important;
}
/deep/.highlight-reject.djs-shape .djs-visual > :nth-child(2) {
  fill: red !important;
}
/deep/.highlight-reject.djs-shape .djs-visual > path {
  fill: red !important;
  fill-opacity: 0.2 !important;
  stroke: red !important;
}
/deep/.highlight-reject.djs-connection > .djs-visual > path {
  stroke: red !important;
}

/** 已取消 */
.highlight-cancel.djs-shape .djs-visual > :nth-child(1) {
  fill: grey !important;
  stroke: grey !important;
  fill-opacity: 0.2 !important;
}
.highlight-cancel.djs-shape .djs-visual > :nth-child(2) {
  fill: grey !important;
}
.highlight-cancel.djs-shape .djs-visual > path {
  fill: grey !important;
  fill-opacity: 0.2 !important;
  stroke: grey !important;
}
.highlight-cancel.djs-connection > .djs-visual > path {
  stroke: grey !important;
}

.highlight-cancel:not(.djs-connection) .djs-visual > :nth-child(1) {
  fill: grey !important; /* color elements as green */
}

/deep/.highlight-cancel.djs-shape .djs-visual > :nth-child(1) {
  fill: grey !important;
  stroke: grey !important;
  fill-opacity: 0.2 !important;
}
/deep/.highlight-cancel.djs-shape .djs-visual > :nth-child(2) {
  fill: grey !important;
}
/deep/.highlight-cancel.djs-shape .djs-visual > path {
  fill: grey !important;
  fill-opacity: 0.2 !important;
  stroke: grey !important;
}
/deep/.highlight-cancel.djs-connection > .djs-visual > path {
  stroke: grey !important;
}
</style>
