<script lang="ts" setup>
// import 'bpmn-js/dist/assets/diagram-js.css' // 左边工具栏以及编辑节点的样式
// import 'bpmn-js/dist/assets/bpmn-font/css/bpmn.css'
// import 'bpmn-js/dist/assets/bpmn-font/css/bpmn-codes.css'
// import 'bpmn-js/dist/assets/bpmn-font/css/bpmn-embedded.css'
// import 'bpmn-js-properties-panel/dist/assets/bpmn-js-properties-panel.css' // 右侧框样式
import { computed, h, onBeforeUnmount, onMounted, provide, ref } from 'vue';

import {
  AlignLeftOutlined,
  ApiOutlined,
  DownloadOutlined,
  EyeOutlined,
  FolderOpenOutlined,
  RedoOutlined,
  ReloadOutlined,
  UndoOutlined,
  WarningOutlined,
  ZoomInOutlined,
  ZoomOutOutlined,
} from '@vben/icons';

import { Button, ButtonGroup, message, Modal, Tooltip } from 'ant-design-vue';
// 模拟流转流程
// @ts-ignore
import tokenSimulation from 'bpmn-js-token-simulation';
import BpmnModeler from 'bpmn-js/lib/Modeler';
// 代码高亮插件
// import hljs from 'highlight.js/lib/highlight'
// import 'highlight.js/styles/github-gist.css'
// hljs.registerLanguage('xml', 'highlight.js/lib/languages/xml')
// hljs.registerLanguage('json', 'highlight.js/lib/languages/json')
// const eventName = reactive({
//   name: ''
// })
import hljs from 'highlight.js'; // 导入代码高亮文件
// 引入json转换与高亮
// import xml2js from 'xml-js'
// import xml2js from 'fast-xml-parser'
import { parseXmlString, XmlNode } from 'steady-xml';

import DefaultEmptyXML from './plugins/defaultEmpty';
import activitiModdleDescriptor from './plugins/descriptor/activitiDescriptor.json';
// 标签解析构建器
// import bpmnPropertiesProvider from "bpmn-js-properties-panel/lib/provider/bpmn";
// import propertiesPanelModule from 'bpmn-js-properties-panel'
// import propertiesProviderModule from 'bpmn-js-properties-panel/lib/provider/camunda'
// 标签解析 Moddle
import camundaModdleDescriptor from './plugins/descriptor/camundaDescriptor.json';
import flowableModdleDescriptor from './plugins/descriptor/flowableDescriptor.json';
import activitiModdleExtension from './plugins/extension-moddle/activiti';
// 标签解析 Extension
import camundaModdleExtension from './plugins/extension-moddle/camunda';
import flowableModdleExtension from './plugins/extension-moddle/flowable';
// 翻译方法
import customTranslate from './plugins/translate/customTranslate';
import translationsCN from './plugins/translate/zh';

import 'highlight.js/styles/github.css';

defineOptions({ name: 'MyProcessDesigner' });

const props = defineProps({
  value: { type: String, default: '' }, // xml 字符串
  // valueWatch: true, // xml 字符串的 watch 状态
  processId: { type: String, default: '' }, // 流程 key 标识
  processName: { type: String, default: '' }, // 流程 name 名字
  formId: { type: Number, default: undefined }, // 流程 form 表单编号
  translations: {
    // 自定义的翻译文件
    type: Object,
    default: () => {},
  },
  // eslint-disable-next-line vue/require-default-prop
  additionalModel: [Object, Array], // 自定义model
  moddleExtension: {
    // 自定义moddle
    type: Object,
    default: () => {},
  },
  onlyCustomizeAddi: {
    type: Boolean,
    default: false,
  },
  onlyCustomizeModdle: {
    type: Boolean,
    default: false,
  },
  simulation: {
    type: Boolean,
    default: true,
  },
  keyboard: {
    type: Boolean,
    default: true,
  },
  prefix: {
    type: String,
    default: 'camunda',
  },
  events: {
    type: Array,
    default: () => ['element.click'],
  },
  headerButtonSize: {
    type: String,
    default: 'small',
    validator: (value: string) =>
      ['default', 'medium', 'mini', 'small'].includes(value),
  },
  headerButtonType: {
    type: String,
    default: 'primary',
    validator: (value: string) =>
      ['danger', 'default', 'info', 'primary', 'success', 'warning'].includes(
        value,
      ),
  },
});

// 导入代码高亮样式

const emit = defineEmits([
  'destroy',
  'init-finished',
  'save',
  'commandStack-changed',
  'input',
  'change',
  'canvas-viewbox-changed',
  // eventName.name
  'element-click',
]);

const bpmnCanvas = ref();
const refFile = ref();

/**
 * 代码高亮
 */
const highlightedCode = (code: string) => {
  // 高亮
  if (previewType.value === 'json') {
    code = JSON.stringify(code, null, 2);
  }
  const result = hljs.highlight(code, {
    language: previewType.value,
    ignoreIllegals: true,
  });
  return result.value || '&nbsp;';
};

provide('configGlobal', props);
let bpmnModeler: any = null;
const defaultZoom = ref(1);
const previewModelVisible = ref(false);
const simulationStatus = ref(false);
const previewResult = ref('');
const previewType = ref('xml');
const recoverable = ref(false);
const revocable = ref(false);
const additionalModules = computed(() => {
  // console.log(props.additionalModel, 'additionalModel');
  const Modules: any[] = [];
  // 仅保留用户自定义扩展模块
  if (props.onlyCustomizeAddi) {
    if (
      Object.prototype.toString.call(props.additionalModel) === '[object Array]'
    ) {
      return props.additionalModel || [];
    }
    return [props.additionalModel];
  }

  // 插入用户自定义扩展模块
  if (
    Object.prototype.toString.call(props.additionalModel) === '[object Array]'
  ) {
    Modules.push(...(props.additionalModel as any[]));
  } else {
    props.additionalModel && Modules.push(props.additionalModel);
  }

  // 翻译模块
  const TranslateModule = {
    translate: ['value', customTranslate(props.translations || translationsCN)],
  };
  Modules.push(TranslateModule);

  // 模拟流转模块
  if (props.simulation) {
    Modules.push(tokenSimulation);
  }

  // 根据需要的流程类型设置扩展元素构建模块
  // if (this.prefix === "bpmn") {
  //   Modules.push(bpmnModdleExtension);
  // }
  // console.log(props.prefix, 'props.prefix ');
  if (props.prefix === 'camunda') {
    Modules.push(camundaModdleExtension);
  }
  if (props.prefix === 'flowable') {
    Modules.push(flowableModdleExtension);
  }
  if (props.prefix === 'activiti') {
    Modules.push(activitiModdleExtension);
  }

  return Modules;
});
const moddleExtensions = computed(() => {
  // console.log(props.onlyCustomizeModdle, 'props.onlyCustomizeModdle');
  // console.log(props.moddleExtension, 'props.moddleExtension');
  // console.log(props.prefix, 'props.prefix');
  const Extensions: any = {};
  // 仅使用用户自定义模块
  if (props.onlyCustomizeModdle) {
    return props.moddleExtension || null;
  }

  // 插入用户自定义模块
  if (props.moddleExtension) {
    for (const key in props.moddleExtension) {
      Extensions[key] = props.moddleExtension[key];
    }
  }

  // 根据需要的 "流程类型" 设置 对应的解析文件
  if (props.prefix === 'activiti') {
    Extensions.activiti = activitiModdleDescriptor;
  }
  if (props.prefix === 'flowable') {
    Extensions.flowable = flowableModdleDescriptor;
  }
  if (props.prefix === 'camunda') {
    Extensions.camunda = camundaModdleDescriptor;
  }
  return Extensions;
});
// console.log(additionalModules, 'additionalModules()');
// console.log(moddleExtensions, 'moddleExtensions()');
const initBpmnModeler = () => {
  if (bpmnModeler) return;
  const data: any = document.querySelector('#bpmnCanvas');
  // console.log(data, 'data');
  // console.log(props.keyboard, 'props.keyboard');
  // console.log(additionalModules, 'additionalModules()');
  // console.log(moddleExtensions, 'moddleExtensions()');

  bpmnModeler = new BpmnModeler({
    // container: this.$refs['bpmn-canvas'],
    // container: getCurrentInstance(),
    // container: needClass,
    // container: bpmnCanvas.value,
    container: data,
    // width: '100%',
    // 添加控制板
    // propertiesPanel: {
    // parent: '#js-properties-panel'
    // },
    keyboard: props.keyboard ? { bindTo: document } : null,
    // additionalModules: additionalModules.value,
    additionalModules: additionalModules.value as any[],
    moddleExtensions: moddleExtensions.value,

    // additionalModules: [
    // additionalModules.value
    // propertiesPanelModule,
    // propertiesProviderModule
    // propertiesProviderModule
    // ],
    // moddleExtensions: { camunda: moddleExtensions.value }
  });

  // bpmnModeler.createDiagram()

  // console.log(bpmnModeler, 'bpmnModeler111111')
  // eslint-disable-next-line vue/custom-event-name-casing
  emit('init-finished', bpmnModeler);
  initModelListeners();
};

const initModelListeners = () => {
  const EventBus = bpmnModeler.get('eventBus');
  // console.log(EventBus, 'EventBus');
  // 注册需要的监听事件, 将. 替换为 - , 避免解析异常
  props.events.forEach((event: any) => {
    EventBus.on(event, (eventObj: any) => {
      // const eventName = event.replaceAll('.', '-');
      // eventName.name = eventName
      const element = eventObj ? eventObj.element : null;
      // console.log(eventName, 'eventName');
      // console.log(element, 'element');
      // eslint-disable-next-line vue/custom-event-name-casing
      emit('element-click', element, eventObj);
      // emit(eventName, element, eventObj)
    });
  });
  // 监听图形改变返回xml
  EventBus.on('commandStack.changed', async (event: any) => {
    try {
      recoverable.value = bpmnModeler.get('commandStack').canRedo();
      revocable.value = bpmnModeler.get('commandStack').canUndo();
      const { xml } = await bpmnModeler.saveXML({ format: true });
      // eslint-disable-next-line vue/custom-event-name-casing
      emit('commandStack-changed', event);
      emit('input', xml);
      emit('change', xml);
      emit('save', xml);
    } catch {
      // console.error(`[Process Designer Warn]: ${e.message || e}`);
    }
  });
  // 监听视图缩放变化
  bpmnModeler.on('canvas.viewbox.changed', ({ viewbox }: { viewbox: any }) => {
    // eslint-disable-next-line vue/custom-event-name-casing
    emit('canvas-viewbox-changed', { viewbox });
    const { scale } = viewbox;
    defaultZoom.value = Math.floor(scale * 100) / 100;
  });
};
/* 创建新的流程图 */
const createNewDiagram = async (xml: any) => {
  // console.log(xml, 'xml');
  // 将字符串转换成图显示出来
  const newId = props.processId || `Process_${Date.now()}`;
  const newName = props.processName || `业务流程_${Date.now()}`;
  const xmlString = xml || DefaultEmptyXML(newId, newName, props.prefix);
  try {
    // console.log(xmlString, 'xmlString')
    // console.log(this.bpmnModeler.importXML);
    const { warnings } = await bpmnModeler.importXML(xmlString);
    // console.log(warnings, 'warnings');
    if (warnings && warnings.length > 0) {
      // warnings.forEach((warn: any) => console.warn(warn));
    }
  } catch {
    // console.error(`[Process Designer Warn]: ${e.message || e}`);
  }
};

// 下载流程图到本地
const downloadProcess = async (type: string) => {
  try {
    // 按需要类型创建文件并下载
    if (type === 'xml' || type === 'bpmn') {
      const { err, xml } = await bpmnModeler.saveXML();
      // 读取异常时抛出异常
      if (err) {
        // console.error(`[Process Designer Warn ]: ${err.message || err}`);
      }
      const { href, filename } = setEncoded(type.toUpperCase(), xml);
      downloadFunc(href, filename);
    } else {
      const { err, svg } = await bpmnModeler.saveSVG();
      // 读取异常时抛出异常
      if (err) {
        // return console.error(err);
      }
      const { href, filename } = setEncoded('SVG', svg);
      downloadFunc(href, filename);
    }
  } catch (error: any) {
    console.error(`[Process Designer Warn ]: ${error.message || error}`);
  }
  // 文件下载方法
  function downloadFunc(href: string, filename: string) {
    if (href && filename) {
      const a = document.createElement('a');
      a.download = filename; // 指定下载的文件名
      a.href = href; //  URL对象
      a.click(); // 模拟点击
      URL.revokeObjectURL(a.href); // 释放URL 对象
    }
  }
};

// 根据所需类型进行转码并返回下载地址
const setEncoded = (type: string, data: string) => {
  const filename = 'diagram';
  const encodedData = encodeURIComponent(data);
  return {
    filename: `${filename}.${type}`,
    href: `data:application/${
      type === 'svg' ? 'text/xml' : 'bpmn20-xml'
    };charset=UTF-8,${encodedData}`,
    data,
  };
};

// 加载本地文件
const importLocalFile = () => {
  const file = refFile.value.files[0];
  const reader = new FileReader();
  // eslint-disable-next-line unicorn/prefer-blob-reading-methods
  reader.readAsText(file);
  reader.addEventListener('load', function () {
    const xmlStr = this.result;
    createNewDiagram(xmlStr);
    emit('save', xmlStr);
  });
};
/* ------------------------------------------------ refs methods ------------------------------------------------------ */
const downloadProcessAsXml = () => {
  downloadProcess('xml');
};
const downloadProcessAsBpmn = () => {
  downloadProcess('bpmn');
};
const downloadProcessAsSvg = () => {
  downloadProcess('svg');
};
const processSimulation = () => {
  simulationStatus.value = !simulationStatus.value;
  // console.log(
  //   bpmnModeler.get('toggleMode', 'strict'),
  //   "bpmnModeler.get('toggleMode')",
  // );
  props.simulation && bpmnModeler.get('toggleMode', 'strict').toggleMode();
};
const processRedo = () => {
  bpmnModeler.get('commandStack').redo();
};
const processUndo = () => {
  bpmnModeler.get('commandStack').undo();
};
const processZoomIn = (zoomStep = 0.1) => {
  const newZoom = Math.floor(defaultZoom.value * 100 + zoomStep * 100) / 100;
  if (newZoom > 4) {
    throw new Error(
      '[Process Designer Warn ]: The zoom ratio cannot be greater than 4',
    );
  }
  defaultZoom.value = newZoom;
  bpmnModeler.get('canvas').zoom(defaultZoom.value);
};
const processZoomOut = (zoomStep = 0.1) => {
  const newZoom = Math.floor(defaultZoom.value * 100 - zoomStep * 100) / 100;
  if (newZoom < 0.2) {
    throw new Error(
      '[Process Designer Warn ]: The zoom ratio cannot be less than 0.2',
    );
  }
  defaultZoom.value = newZoom;
  bpmnModeler.get('canvas').zoom(defaultZoom.value);
};
const processReZoom = () => {
  defaultZoom.value = 1;
  bpmnModeler.get('canvas').zoom('fit-viewport', 'auto');
};
const processRestart = () => {
  recoverable.value = false;
  revocable.value = false;
  createNewDiagram(null);
};
const elementsAlign = (align: string) => {
  const Align = bpmnModeler.get('alignElements');
  const Selection = bpmnModeler.get('selection');
  const SelectedElements = Selection.get();
  if (!SelectedElements || SelectedElements.length <= 1) {
    message.warning('请按住 Shift 键选择多个元素对齐');
    // alert('请按住 Ctrl 键选择多个元素对齐
    return;
  }
  Modal.confirm({
    title: '警告',
    content: '自动对齐可能造成图形变形，是否继续？',
    okText: '确定',
    cancelText: '取消',
    icon: h(WarningOutlined) as any,
    onOk() {
      Align.trigger(SelectedElements, align);
    },
  });
};
/* -----------------------------    方法结束     ---------------------------------*/
const previewProcessXML = () => {
  // console.log(bpmnModeler.saveXML, 'bpmnModeler');
  bpmnModeler.saveXML({ format: true }).then(({ xml }: { xml: string }) => {
    // console.log(xml, 'xml111111')
    previewResult.value = xml;
    previewType.value = 'xml';
    previewModelVisible.value = true;
  });
};
const previewProcessJson = () => {
  bpmnModeler.saveXML({ format: true }).then(({ xml }: { xml: string }) => {
    const rootNodes = new XmlNode('root' as any, parseXmlString(xml));
    previewResult.value = rootNodes.parent?.toJSON() as unknown as string;
    previewType.value = 'json';
    previewModelVisible.value = true;
  });
};

/* ------------------------------------------------ 芋道源码 methods ------------------------------------------------------ */
onMounted(() => {
  initBpmnModeler();
  createNewDiagram(props.value);
});
onBeforeUnmount(() => {
  if (bpmnModeler) bpmnModeler.destroy();
  emit('destroy', bpmnModeler);
  bpmnModeler = null;
});
</script>

<template>
  <div class="my-process-designer">
    <div
      class="my-process-designer__header"
      style="z-index: 999; display: table-row-group"
    >
      <slot name="control-header"></slot>
      <template v-if="!$slots['control-header']">
        <ButtonGroup key="file-control">
          <Button
            :icon="h(FolderOpenOutlined)"
            title="打开文件"
            @click="refFile.click()"
          />
          <Tooltip placement="bottom">
            <template #title>
              <div>
                <Button type="link" @click="downloadProcessAsXml()">
                  下载为XML文件
                </Button>
                <br />
                <Button type="link" @click="downloadProcessAsSvg()">
                  下载为SVG文件
                </Button>
                <br />
                <Button type="link" @click="downloadProcessAsBpmn()">
                  下载为BPMN文件
                </Button>
              </div>
            </template>
            <Button :icon="h(DownloadOutlined)" title="下载文件" />
          </Tooltip>
          <Tooltip>
            <template #title>
              <Button type="link" @click="previewProcessXML">预览XML</Button>
              <br />
              <Button type="link" @click="previewProcessJson">预览JSON</Button>
            </template>
            <Button :icon="h(EyeOutlined)" title="浏览" />
          </Tooltip>
          <Tooltip
            v-if="props.simulation"
            :title="simulationStatus ? '退出模拟' : '开启模拟'"
          >
            <Button
              :icon="h(ApiOutlined)"
              title="模拟"
              @click="processSimulation"
            />
          </Tooltip>
        </ButtonGroup>
        <ButtonGroup key="align-control">
          <Tooltip title="向左对齐">
            <Button
              :icon="h(AlignLeftOutlined)"
              class="align align-bottom"
              @click="elementsAlign('left')"
            />
          </Tooltip>
          <Tooltip title="向右对齐">
            <Button
              :icon="h(AlignLeftOutlined)"
              class="align align-top"
              @click="elementsAlign('right')"
            />
          </Tooltip>
          <Tooltip title="向上对齐">
            <Button
              :icon="h(AlignLeftOutlined)"
              class="align align-left"
              @click="elementsAlign('top')"
            />
          </Tooltip>
          <Tooltip title="向下对齐">
            <Button
              :icon="h(AlignLeftOutlined)"
              class="align align-right"
              @click="elementsAlign('bottom')"
            />
          </Tooltip>
          <Tooltip title="水平居中">
            <Button
              :icon="h(AlignLeftOutlined)"
              class="align align-center"
              @click="elementsAlign('center')"
            />
          </Tooltip>
          <Tooltip title="垂直居中">
            <Button
              :icon="h(AlignLeftOutlined)"
              class="align align-middle"
              @click="elementsAlign('middle')"
            />
          </Tooltip>
        </ButtonGroup>
        <ButtonGroup key="scale-control">
          <Tooltip title="缩小视图">
            <Button
              :icon="h(ZoomOutOutlined)"
              @click="processZoomOut()"
              :disabled="defaultZoom < 0.2"
            />
          </Tooltip>
          <Button>{{ `${Math.floor(defaultZoom * 10 * 10)}%` }}</Button>
          <Tooltip title="放大视图">
            <Button
              :icon="h(ZoomInOutlined)"
              @click="processZoomIn()"
              :disabled="defaultZoom > 4"
            />
          </Tooltip>
          <Tooltip title="重置视图并居中">
            <Button :icon="h(ReloadOutlined)" @click="processReZoom()" />
          </Tooltip>
        </ButtonGroup>
        <ButtonGroup key="stack-control">
          <Tooltip title="撤销">
            <Button
              :icon="h(UndoOutlined)"
              @click="processUndo()"
              :disabled="!revocable"
            />
          </Tooltip>
          <Tooltip title="恢复">
            <Button
              :icon="h(RedoOutlined)"
              @click="processRedo()"
              :disabled="!recoverable"
            />
          </Tooltip>
          <Tooltip title="重新绘制">
            <Button :icon="h(ReloadOutlined)" @click="processRestart()" />
          </Tooltip>
        </ButtonGroup>
      </template>
      <!-- 用于打开本地文件-->
      <input
        type="file"
        id="files"
        ref="refFile"
        class="hidden"
        accept=".xml, .bpmn"
        @change="importLocalFile"
      />
    </div>
    <div class="my-process-designer__container">
      <div
        class="my-process-designer__canvas"
        ref="bpmnCanvas"
        id="bpmnCanvas"
        style="width: 1680px; height: 800px"
      ></div>
      <!-- <div id="js-properties-panel" class="panel"></div> -->
      <!-- <div class="my-process-designer__canvas" ref="bpmn-canvas"></div> -->
    </div>
    <Modal
      title="预览"
      v-model:open="previewModelVisible"
      class="max-h-[600px] w-4/5"
      :scroll="true"
    >
      <div>
        <pre><code v-dompurify-html="highlightedCode(previewResult)" class="hljs"></code></pre>
      </div>
    </Modal>
  </div>
</template>
