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
    value: String, // xml 字符串
    prefix: {
      type: String,
      default: "camunda"
    },
    taskData: {
      type: Array,
      default: () => []
    }
  },
  data() {
    return {
      xml: '',
      tasks: [],
    };
  },
  mounted() {
    this.xml = this.value;
    this.tasks = this.taskData;
    // 初始化
    this.initBpmnModeler();
    this.createNewDiagram(this.xml);
    this.$once("hook:beforeDestroy", () => {
      if (this.bpmnModeler) this.bpmnModeler.destroy();
      this.$emit("destroy", this.bpmnModeler);
      this.bpmnModeler = null;
    });
  },
  watch: {
    value: function (newValue) { // 在 xmlString 发生变化时，重新创建，从而绘制流程图
      this.xml = newValue;
      this.createNewDiagram(this.xml);
    },
    taskData: function (newTaskData) {
      this.tasks = newTaskData;
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
    async highlightDiagram() {
      // let tasks = this.tasks.filter(task => {
      //   if (task.type !== 'sequenceFlow') { // 去除连线元素
      //     return true;
      //   }
      // });
      let tasks = this.tasks;
      if (tasks.length === 0) {
        return;
      }
      // 参考自 https://gitee.com/tony2y/RuoYi-flowable/blob/master/ruoyi-ui/src/components/Process/index.vue#L222 实现
      let canvas = this.bpmnModeler.get('canvas');
      this.bpmnModeler.getDefinitions().rootElements[0].flowElements?.forEach(n => {
        let completeTask = tasks.find(m => m.key === n.id)
        let todoTask = tasks.find(m => !m.endTime)
        let endTask = tasks[tasks.length - 1]
        if (n.$type === 'bpmn:UserTask') { // 用户任务
          if (completeTask) {
            canvas.addMarker(n.id, completeTask.endTime ? 'highlight' : 'highlight-todo');
            // console.log(n.id + ' : ' + (completeTask.endTime ? 'highlight' : 'highlight-todo'));
            n.outgoing?.forEach(nn => {
              let targetTask = tasks.find(m => m.key === nn.targetRef.id)
              if (targetTask) {
                canvas.addMarker(nn.id, targetTask.endTime ? 'highlight' : 'highlight-todo');
              } else if (nn.targetRef.$type === 'bpmn:ExclusiveGateway') {
                // canvas.addMarker(nn.id, 'highlight');
                canvas.addMarker(nn.id, completeTask.endTime ? 'highlight' : 'highlight-todo');
                canvas.addMarker(nn.targetRef.id, completeTask.endTime ? 'highlight' : 'highlight-todo');
              } else if (nn.targetRef.$type === 'bpmn:EndEvent') {
                if (!todoTask && endTask.key === n.id) {
                  canvas.addMarker(nn.id, 'highlight');
                  canvas.addMarker(nn.targetRef.id, 'highlight');
                }
                if (!completeTask.endTime) {
                  canvas.addMarker(nn.id, 'highlight-todo');
                  canvas.addMarker(nn.targetRef.id, 'highlight-todo');
                }
              }
            });
          }
        } else if (n.$type === 'bpmn:ExclusiveGateway') { // 排它网关
          n.outgoing?.forEach(nn => {
            let targetTask = tasks.find(m => m.key === nn.targetRef.id)
            if (targetTask) {
              canvas.addMarker(nn.id, targetTask.endTime ? 'highlight' : 'highlight-todo');
            }
          })
        } else if (n.$type === 'bpmn:ParallelGateway') { // 并行网关
          if (completeTask) {
            canvas.addMarker(n.id, completeTask.endTime ? 'highlight' : 'highlight-todo')
            n.outgoing?.forEach(nn => {
              const targetTask = this.taskList.find(m => m.key === nn.targetRef.id)
              if (targetTask) {
                canvas.addMarker(nn.id, targetTask.endTime ? 'highlight' : 'highlight-todo')
                canvas.addMarker(nn.targetRef.id, targetTask.endTime ? 'highlight' : 'highlight-todo')
              }
            })
          }
        } else if (n.$type === 'bpmn:StartEvent') { // 开始节点
          n.outgoing?.forEach(nn => {
            let completeTask = tasks.find(m => m.key === nn.targetRef.id)
            if (completeTask) {
              canvas.addMarker(nn.id, 'highlight');
              canvas.addMarker(n.id, 'highlight');
              return
            }
          });
        } else if (n.$type === 'bpmn:EndEvent') { // 结束节点
          if (endTask.key === n.id && endTask.endTime) {
            canvas.addMarker(n.id, 'highlight')
            return
          }
        }
      })
    }
  }
};
</script>

<style>

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

</style>
