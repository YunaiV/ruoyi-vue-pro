<template>
  <div class="my-process-designer">
    <div class="my-process-designer__container">
      <div class="my-process-designer__canvas" style="height: 760px" ref="bpmnCanvas"></div>
    </div>
  </div>
</template>

<script setup lang="ts" name="MyProcessViewer">
import BpmnViewer from 'bpmn-js/lib/Viewer'
import DefaultEmptyXML from './plugins/defaultEmpty'
import { DICT_TYPE, getIntDictOptions } from '@/utils/dict'
const props = defineProps({
  value: {
    // BPMN XML 字符串
    type: String,
    default: ''
  },
  prefix: {
    // 使用哪个引擎
    type: String,
    default: 'camunda'
  },
  activityData: {
    // 活动的数据。传递时，可高亮流程
    type: Array,
    default: () => []
  },
  processInstanceData: {
    // 流程实例的数据。传递时，可展示流程发起人等信息
    type: Object,
    default: () => {}
  },
  taskData: {
    // 任务实例的数据。传递时，可展示 UserTask 审核相关的信息
    type: Array,
    default: () => []
  }
})

provide('configGlobal', props)

const emit = defineEmits(['destroy'])

let bpmnModeler

const xml = ref('')
const activityLists = ref<any[]>([])
const processInstance = ref<any>(undefined)
const taskList = ref<any[]>([])
const bpmnCanvas = ref()
// const element = ref()
const elementOverlayIds = ref<any>(null)
const overlays = ref<any>(null)

const initBpmnModeler = () => {
  if (bpmnModeler) return
  bpmnModeler = new BpmnViewer({
    container: bpmnCanvas.value,
    bpmnRenderer: {}
  })
}

/* 创建新的流程图 */
const createNewDiagram = async (xml) => {
  // 将字符串转换成图显示出来
  let newId = `Process_${new Date().getTime()}`
  let newName = `业务流程_${new Date().getTime()}`
  let xmlString = xml || DefaultEmptyXML(newId, newName, props.prefix)
  try {
    let { warnings } = await bpmnModeler.importXML(xmlString)
    if (warnings && warnings.length) {
      warnings.forEach((warn) => console.warn(warn))
    }
    // 高亮流程图
    await highlightDiagram()
    const canvas = bpmnModeler.get('canvas')
    canvas.zoom('fit-viewport', 'auto')
  } catch (e) {
    console.error(e)
    // console.error(`[Process Designer Warn]: ${e?.message || e}`);
  }
}
/* 高亮流程图 */
// TODO 芋艿：如果多个 endActivity 的话，目前的逻辑可能有一定的问题。https://www.jdon.com/workflow/multi-events.html
const highlightDiagram = async () => {
  const activityList = activityLists.value
  if (activityList.length === 0) {
    return
  }
  // 参考自 https://gitee.com/tony2y/RuoYi-flowable/blob/master/ruoyi-ui/src/components/Process/index.vue#L222 实现
  // 再次基础上，增加不同审批结果的颜色等等
  let canvas = bpmnModeler.get('canvas')
  let todoActivity: any = activityList.find((m: any) => !m.endTime) // 找到待办的任务
  let endActivity: any = activityList[activityList.length - 1] // 获得最后一个任务
  // debugger
  bpmnModeler.getDefinitions().rootElements[0].flowElements?.forEach((n: any) => {
    let activity: any = activityList.find((m: any) => m.key === n.id) // 找到对应的活动
    if (!activity) {
      return
    }
    if (n.$type === 'bpmn:UserTask') {
      // 用户任务
      // 处理用户任务的高亮
      const task: any = taskList.value.find((m: any) => m.id === activity.taskId) // 找到活动对应的 taskId
      if (!task) {
        return
      }
      // 高亮任务
      canvas.addMarker(n.id, getResultCss(task.result))

      // 如果非通过，就不走后面的线条了
      if (task.result !== 2) {
        return
      }
      // 处理 outgoing 出线
      const outgoing = getActivityOutgoing(activity)
      outgoing?.forEach((nn: any) => {
        // debugger
        let targetActivity: any = activityList.find((m: any) => m.key === nn.targetRef.id)
        // 如果目标活动存在，则根据该活动是否结束，进行【bpmn:SequenceFlow】连线的高亮设置
        if (targetActivity) {
          canvas.addMarker(nn.id, targetActivity.endTime ? 'highlight' : 'highlight-todo')
        } else if (nn.targetRef.$type === 'bpmn:ExclusiveGateway') {
          // TODO 芋艿：这个流程，暂时没走到过
          canvas.addMarker(nn.id, activity.endTime ? 'highlight' : 'highlight-todo')
          canvas.addMarker(nn.targetRef.id, activity.endTime ? 'highlight' : 'highlight-todo')
        } else if (nn.targetRef.$type === 'bpmn:EndEvent') {
          // TODO 芋艿：这个流程，暂时没走到过
          if (!todoActivity && endActivity.key === n.id) {
            canvas.addMarker(nn.id, 'highlight')
            canvas.addMarker(nn.targetRef.id, 'highlight')
          }
          if (!activity.endTime) {
            canvas.addMarker(nn.id, 'highlight-todo')
            canvas.addMarker(nn.targetRef.id, 'highlight-todo')
          }
        }
      })
    } else if (n.$type === 'bpmn:ExclusiveGateway') {
      // 排它网关
      // 设置【bpmn:ExclusiveGateway】排它网关的高亮
      canvas.addMarker(n.id, getActivityHighlightCss(activity))
      // 查找需要高亮的连线
      let matchNN: any = undefined
      let matchActivity: any = undefined
      n.outgoing?.forEach((nn: any) => {
        let targetActivity = activityList.find((m: any) => m.key === nn.targetRef.id)
        if (!targetActivity) {
          return
        }
        // 特殊判断 endEvent 类型的原因，ExclusiveGateway 可能后续连有 2 个路径：
        //  1. 一个是 UserTask => EndEvent
        //  2. 一个是 EndEvent
        // 在选择路径 1 时，其实 EndEvent 可能也存在，导致 1 和 2 都高亮，显然是不正确的。
        // 所以，在 matchActivity 为 EndEvent 时，需要进行覆盖~~
        if (!matchActivity || matchActivity.type === 'endEvent') {
          matchNN = nn
          matchActivity = targetActivity
        }
      })
      if (matchNN && matchActivity) {
        canvas.addMarker(matchNN.id, getActivityHighlightCss(matchActivity))
      }
    } else if (n.$type === 'bpmn:ParallelGateway') {
      // 并行网关
      // 设置【bpmn:ParallelGateway】并行网关的高亮
      canvas.addMarker(n.id, getActivityHighlightCss(activity))
      n.outgoing?.forEach((nn: any) => {
        // 获得连线是否有指向目标。如果有，则进行高亮
        const targetActivity = activityList.find((m: any) => m.key === nn.targetRef.id)
        if (targetActivity) {
          canvas.addMarker(nn.id, getActivityHighlightCss(targetActivity)) // 高亮【bpmn:SequenceFlow】连线
          // 高亮【...】目标。其中 ... 可以是 bpm:UserTask、也可以是其它的。当然，如果是 bpm:UserTask 的话，其实不做高亮也没问题，因为上面有逻辑做了这块。
          canvas.addMarker(nn.targetRef.id, getActivityHighlightCss(targetActivity))
        }
      })
    } else if (n.$type === 'bpmn:StartEvent') {
      // 开始节点
      n.outgoing?.forEach((nn) => {
        // outgoing 例如说【bpmn:SequenceFlow】连线
        // 获得连线是否有指向目标。如果有，则进行高亮
        let targetActivity = activityList.find((m: any) => m.key === nn.targetRef.id)
        if (targetActivity) {
          canvas.addMarker(nn.id, 'highlight') // 高亮【bpmn:SequenceFlow】连线
          canvas.addMarker(n.id, 'highlight') // 高亮【bpmn:StartEvent】开始节点（自己）
        }
      })
    } else if (n.$type === 'bpmn:EndEvent') {
      // 结束节点
      if (!processInstance.value || processInstance.value.result === 1) {
        return
      }
      canvas.addMarker(n.id, getResultCss(processInstance.value.result))
    } else if (n.$type === 'bpmn:ServiceTask') {
      //服务任务
      if (activity.startTime > 0 && activity.endTime === 0) {
        //进入执行，标识进行色
        canvas.addMarker(n.id, getResultCss(1))
      }
      if (activity.endTime > 0) {
        // 执行完成，节点标识完成色, 所有outgoing标识完成色。
        canvas.addMarker(n.id, getResultCss(2))
        const outgoing = getActivityOutgoing(activity)
        outgoing?.forEach((out) => {
          canvas.addMarker(out.id, getResultCss(2))
        })
      }
    }
  })
}
const getActivityHighlightCss = (activity) => {
  return activity.endTime ? 'highlight' : 'highlight-todo'
}
const getResultCss = (result) => {
  if (result === 1) {
    // 审批中
    return 'highlight-todo'
  } else if (result === 2) {
    // 已通过
    return 'highlight'
  } else if (result === 3) {
    // 不通过
    return 'highlight-reject'
  } else if (result === 4) {
    // 已取消
    return 'highlight-cancel'
  }
  return ''
}

const getActivityOutgoing = (activity) => {
  // 如果有 outgoing，则直接使用它
  if (activity.outgoing && activity.outgoing.length > 0) {
    return activity.outgoing
  }
  // 如果没有，则遍历获得起点为它的【bpmn:SequenceFlow】节点们。原因是：bpmn-js 的 UserTask 拿不到 outgoing
  const flowElements = bpmnModeler.getDefinitions().rootElements[0].flowElements
  const outgoing: any[] = []
  flowElements.forEach((item: any) => {
    if (item.$type !== 'bpmn:SequenceFlow') {
      return
    }
    if (item.sourceRef.id === activity.key) {
      outgoing.push(item)
    }
  })
  return outgoing
}
const initModelListeners = () => {
  const EventBus = bpmnModeler.get('eventBus')
  // 注册需要的监听事件
  EventBus.on('element.hover', function (eventObj) {
    let element = eventObj ? eventObj.element : null
    elementHover(element)
  })
  EventBus.on('element.out', function (eventObj) {
    let element = eventObj ? eventObj.element : null
    elementOut(element)
  })
}
// 流程图的元素被 hover
const elementHover = (element) => {
  element.value = element
  !elementOverlayIds.value && (elementOverlayIds.value = {})
  !overlays.value && (overlays.value = bpmnModeler.get('overlays'))
  // 展示信息
  console.log(activityLists.value, 'activityLists.value')
  console.log(element.value, 'element.value')
  const activity = activityLists.value.find((m) => m.key === element.value.id)
  console.log(activity, 'activityactivityactivityactivity')
  // if (!activity) {
  //   return
  // }
  if (!elementOverlayIds.value[element.value.id] && element.value.type !== 'bpmn:Process') {
    let html = `<div class="element-overlays">
            <p>Elemet id: ${element.value.id}</p>
            <p>Elemet type: ${element.value.type}</p>
          </div>` // 默认值
    if (element.value.type === 'bpmn:StartEvent' && processInstance.value) {
      html = `<p>发起人：${processInstance.value.startUser.nickname}</p>
                  <p>部门：${processInstance.value.startUser.deptName}</p>
                  <p>创建时间：${parseTime(processInstance.value.createTime)}`
    } else if (element.value.type === 'bpmn:UserTask') {
      // debugger
      let task = taskList.value.find((m) => m.id === activity.taskId) // 找到活动对应的 taskId
      if (!task) {
        return
      }
      let optionData = getIntDictOptions(DICT_TYPE.BPM_PROCESS_INSTANCE_RESULT)
      let dataResult = ''
      optionData.forEach((element) => {
        if (element.value == task.result) {
          dataResult = element.label
        }
      })
      html = `<p>审批人：${task.assigneeUser.nickname}</p>
                  <p>部门：${task.assigneeUser.deptName}</p>
                  <p>结果：${dataResult}</p>
                  <p>创建时间：${parseTime(task.createTime)}</p>`
      // html = `<p>审批人：${task.assigneeUser.nickname}</p>
      //             <p>部门：${task.assigneeUser.deptName}</p>
      //             <p>结果：${getIntDictOptions(
      //               DICT_TYPE.BPM_PROCESS_INSTANCE_RESULT,
      //               task.result
      //             )}</p>
      //             <p>创建时间：${parseTime(task.createTime)}</p>`
      if (task.endTime) {
        html += `<p>结束时间：${parseTime(task.endTime)}</p>`
      }
      if (task.reason) {
        html += `<p>审批建议：${task.reason}</p>`
      }
    } else if (element.value.type === 'bpmn:ServiceTask' && processInstance.value) {
      if (activity.startTime > 0) {
        html = `<p>创建时间：${parseTime(activity.startTime)}</p>`
      }
      if (activity.endTime > 0) {
        html += `<p>结束时间：${parseTime(activity.endTime)}</p>`
      }
      console.log(html)
    } else if (element.value.type === 'bpmn:EndEvent' && processInstance.value) {
      let optionData = getIntDictOptions(DICT_TYPE.BPM_PROCESS_INSTANCE_RESULT)
      let dataResult = ''
      optionData.forEach((element) => {
        if (element.value == processInstance.value.result) {
          dataResult = element.label
        }
      })
      html = `<p>结果：${dataResult}</p>`
      // html = `<p>结果：${getIntDictOptions(
      //   DICT_TYPE.BPM_PROCESS_INSTANCE_RESULT,
      //   processInstance.value.result
      // )}</p>`
      if (processInstance.value.endTime) {
        html += `<p>结束时间：${parseTime(processInstance.value.endTime)}</p>`
      }
    }
    console.log(html, 'html111111111111111')
    elementOverlayIds.value[element.value.id] = toRaw(overlays.value).add(element.value, {
      position: { left: 0, bottom: 0 },
      html: `<div class="element-overlays">${html}</div>`
    })
  }
}
// 流程图的元素被 out
const elementOut = (element) => {
  toRaw(overlays.value).remove({ element })
  elementOverlayIds.value[element.id] = null
}
const parseTime = (time) => {
  if (!time) {
    return null
  }
  const format = '{y}-{m}-{d} {h}:{i}:{s}'
  let date
  if (typeof time === 'object') {
    date = time
  } else {
    if (typeof time === 'string' && /^[0-9]+$/.test(time)) {
      time = parseInt(time)
    } else if (typeof time === 'string') {
      time = time
        .replace(new RegExp(/-/gm), '/')
        .replace('T', ' ')
        .replace(new RegExp(/\.[\d]{3}/gm), '')
    }
    if (typeof time === 'number' && time.toString().length === 10) {
      time = time * 1000
    }
    date = new Date(time)
  }
  const formatObj = {
    y: date.getFullYear(),
    m: date.getMonth() + 1,
    d: date.getDate(),
    h: date.getHours(),
    i: date.getMinutes(),
    s: date.getSeconds(),
    a: date.getDay()
  }
  const time_str = format.replace(/{(y|m|d|h|i|s|a)+}/g, (result, key) => {
    let value = formatObj[key]
    // Note: getDay() returns 0 on Sunday
    if (key === 'a') {
      return ['日', '一', '二', '三', '四', '五', '六'][value]
    }
    if (result.length > 0 && value < 10) {
      value = '0' + value
    }
    return value || 0
  })
  return time_str
}

onMounted(() => {
  xml.value = props.value
  activityLists.value = props.activityData
  // 初始化
  initBpmnModeler()
  createNewDiagram(xml.value)
  // 初始模型的监听器
  initModelListeners()
})
onBeforeUnmount(() => {
  // this.$once('hook:beforeDestroy', () => {
  // })
  if (bpmnModeler) bpmnModeler.destroy()
  emit('destroy', bpmnModeler)
  bpmnModeler = null
})

watch(
  () => props.value,
  (newValue) => {
    xml.value = newValue
    createNewDiagram(xml.value)
  }
)
watch(
  () => props.activityData,
  (newActivityData) => {
    activityLists.value = newActivityData
    createNewDiagram(xml.value)
  }
)
watch(
  () => props.processInstanceData,
  (newProcessInstanceData) => {
    processInstance.value = newProcessInstanceData
    createNewDiagram(xml.value)
  }
)
watch(
  () => props.taskData,
  (newTaskListData) => {
    taskList.value = newTaskListData
    createNewDiagram(xml.value)
  }
)
</script>

<style>
/** 处理中 */
.highlight-todo.djs-connection > .djs-visual > path {
  stroke: #1890ff !important;
  stroke-dasharray: 4px !important;
  fill-opacity: 0.2 !important;
}
.highlight-todo.djs-shape .djs-visual > :nth-child(1) {
  fill: #1890ff !important;
  stroke: #1890ff !important;
  stroke-dasharray: 4px !important;
  fill-opacity: 0.2 !important;
}

:deep(.highlight-todo.djs-connection > .djs-visual > path) {
  stroke: #1890ff !important;
  stroke-dasharray: 4px !important;
  fill-opacity: 0.2 !important;
  marker-end: url(#sequenceflow-end-_E7DFDF-_E7DFDF-803g1kf6zwzmcig1y2ulm5egr);
}
:deep(.highlight-todo.djs-shape .djs-visual > :nth-child(1)) {
  fill: #1890ff !important;
  stroke: #1890ff !important;
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

:deep(.highlight.djs-shape .djs-visual > :nth-child(1)) {
  fill: green !important;
  stroke: green !important;
  fill-opacity: 0.2 !important;
}
:deep(.highlight.djs-shape .djs-visual > :nth-child(2)) {
  fill: green !important;
}
:deep(.highlight.djs-shape .djs-visual > path) {
  fill: green !important;
  fill-opacity: 0.2 !important;
  stroke: green !important;
}
:deep(.highlight.djs-connection > .djs-visual > path) {
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

:deep(.highlight-reject.djs-shape .djs-visual > :nth-child(1)) {
  fill: red !important;
  stroke: red !important;
  fill-opacity: 0.2 !important;
}
:deep(.highlight-reject.djs-shape .djs-visual > :nth-child(2)) {
  fill: red !important;
}
:deep(.highlight-reject.djs-shape .djs-visual > path) {
  fill: red !important;
  fill-opacity: 0.2 !important;
  stroke: red !important;
}
:deep(.highlight-reject.djs-connection > .djs-visual > path) {
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

:deep(.highlight-cancel.djs-shape .djs-visual > :nth-child(1)) {
  fill: grey !important;
  stroke: grey !important;
  fill-opacity: 0.2 !important;
}
:deep(.highlight-cancel.djs-shape .djs-visual > :nth-child(2)) {
  fill: grey !important;
}
:deep(.highlight-cancel.djs-shape .djs-visual > path) {
  fill: grey !important;
  fill-opacity: 0.2 !important;
  stroke: grey !important;
}
:deep(.highlight-cancel.djs-connection > .djs-visual > path) {
  stroke: grey !important;
}

.element-overlays {
  box-sizing: border-box;
  padding: 8px;
  background: rgba(0, 0, 0, 0.6);
  border-radius: 4px;
  color: #fafafa;
  width: 200px;
}
</style>
