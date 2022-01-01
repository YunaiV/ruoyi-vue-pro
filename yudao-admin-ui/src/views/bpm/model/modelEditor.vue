<template>
<!--  <div id="app">-->
  <div class="app-container">

    <my-process-palette />
    <my-process-designer
      :key="`designer-${reloadIndex}`"
      v-model="xmlString"
      v-bind="controlForm"
      keyboard
      ref="processDesigner"
      @element-click="elementClick"
      @init-finished="initModeler"
    />
    <my-properties-panel :key="`penal-${reloadIndex}`" :bpmn-modeler="modeler" :prefix="controlForm.prefix" class="process-panel" />

    <!-- demo config -->
    <div class="demo-control-bar">
      <div class="open-control-dialog" @click="controlDrawerVisible = true"><i class="el-icon-setting"></i></div>
    </div>
    <el-drawer :visible.sync="controlDrawerVisible" size="400px" title="偏好设置" append-to-body destroy-on-close>
      <el-form :model="controlForm" size="small" label-width="100px" class="control-form" @submit.native.prevent>
        <el-form-item label="流程ID">
          <el-input v-model="controlForm.processId" @change="reloadProcessDesigner" />
        </el-form-item>
        <el-form-item label="流程名称">
          <el-input v-model="controlForm.processName" @change="reloadProcessDesigner" />
        </el-form-item>
        <el-form-item label="流转模拟">
          <el-switch v-model="controlForm.simulation" inactive-text="停用" active-text="启用" @change="reloadProcessDesigner" />
        </el-form-item>
        <el-form-item label="禁用双击">
          <el-switch v-model="controlForm.labelEditing" inactive-text="停用" active-text="启用" @change="changeLabelEditingStatus" />
        </el-form-item>
        <!-- TODO 芋艿：custom render 依赖报错 -->
<!--        <el-form-item label="隐藏label">-->
<!--          <el-switch v-model="controlForm.labelVisible" inactive-text="停用" active-text="启用" @change="changeLabelVisibleStatus" />-->
<!--        </el-form-item>-->
        <el-form-item label="流程引擎">
          <el-radio-group v-model="controlForm.prefix" @change="reloadProcessDesigner(true)">
            <el-radio label="camunda">camunda</el-radio>
            <el-radio label="flowable">flowable</el-radio>
            <el-radio label="activiti">activiti</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="工具栏">
          <el-radio-group v-model="controlForm.headerButtonSize">
            <el-radio label="mini">mini</el-radio>
            <el-radio label="small">small</el-radio>
            <el-radio label="medium">medium</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
    </el-drawer>

    <div class="info-tip">
      <p>注：activiti 好像不支持表单配置，控制台可能会报错</p>
      <p>更多配置请查看源码：<a href="https://github.com/miyuesc/bpmn-process-designer">MiyueSC/bpmn-process-designer</a></p>
      <p>疑问请在此留言：<a href="https://github.com/miyuesc/bpmn-process-designer/issues/16">MiyueSC/bpmn-process-designer/issues</a></p>
    </div>
  </div>
</template>

<script>
import translations from "./translations";
// 自定义渲染（隐藏了 label 标签）
// TODO 芋艿：custom render 依赖报错
// import CustomRenderer from "@/modules/custom-renderer";
// 自定义元素选中时的弹出菜单（修改 默认任务 为 用户任务）
import CustomContentPadProvider from "@/components/bpmnProcessDesigner/package/designer/plugins/content-pad";
// 自定义左侧菜单（修改 默认任务 为 用户任务）
import CustomPaletteProvider from "@/components/bpmnProcessDesigner/package/designer/plugins/palette";
// import xmlObj2json from "./utils/xml2json";
import MyProcessPalette from "@/components/bpmnProcessDesigner/package/palette/ProcessPalette";
// 自定义侧边栏
// import MyProcessPanel from "../package/process-panel/ProcessPanel";


export default {
  name: "App",
  components: { MyProcessPalette },
  data() {
    return {
      xmlString: "",
      modeler: null,
      reloadIndex: 0,
      controlDrawerVisible: false,
      translationsSelf: translations,
      controlForm: {
        processId: "",
        processName: "",
        simulation: true,
        labelEditing: false,
        labelVisible: false,
        prefix: "flowable",
        headerButtonSize: "mini",
        // additionalModel: []
        additionalModel: [CustomContentPadProvider, CustomPaletteProvider]
      },
      addis: {
        CustomContentPadProvider,
        CustomPaletteProvider
      }
    };
  },
  created() {},
  methods: {
    initModeler(modeler) {
      setTimeout(() => {
        this.modeler = modeler;
        console.log(modeler);
      }, 10);
    },
    reloadProcessDesigner(deep) {
      this.controlForm.additionalModel = [];
      for (let key in this.addis) {
        if (this.addis[key]) {
          this.controlForm.additionalModel.push(this.addis[key]);
        }
      }
      deep && (this.xmlString = undefined);
      this.reloadIndex += 1;
      this.modeler = null; // 避免 panel 异常
      // if (deep) {
      //   this.xmlString = undefined;
      //   this.$refs.processDesigner.processRestart();
      // }
    },
    changeLabelEditingStatus(status) {
      this.addis.labelEditing = status ? { labelEditingProvider: ["value", ""] } : false;
      this.reloadProcessDesigner();
    },
    // TODO 芋艿：custom render 依赖报错
    // changeLabelVisibleStatus(status) {
    //   this.addis.customRenderer = status ? CustomRenderer : false;
    //   this.reloadProcessDesigner();
    // },
    elementClick(element) {
      this.element = element;

      !this.elementOverlayIds && (this.elementOverlayIds = {});

      !this.overlays && (this.overlays = this.modeler.get("overlays"));
      !this.contextPad && (this.contextPad = this.modeler.get("contextPad"));

      this.modeler.on("element.hover", ({ element }) => {
        if (!this.elementOverlayIds[element.id] && element.type !== "bpmn:Process") {
          this.elementOverlayIds[element.id] = this.overlays.add(element, {
            position: { left: 0, bottom: 0 },
            html: `<div class="element-overlays">
            <p>Elemet id: ${element.id}</p>
            <p>Elemet type: ${element.type}</p>
          </div>`
          });
        }
      });

      this.modeler.on("element.out", ({ element }) => {
        if (element) {
          this.overlays.remove({ element });
          this.elementOverlayIds[element.id] = null;
        }
      });
    }
  }
};
</script>

<style lang="scss">
body {
  overflow: hidden;
  margin: 0;
  box-sizing: border-box;
}
//#app {
//  width: 100%;
//  height: 100%;
//  box-sizing: border-box;
//  display: inline-grid;
//  grid-template-columns: 100px auto max-content;
//}
.demo-control-bar {
  position: fixed;
  right: 8px;
  bottom: 8px;
  z-index: 1;
  .open-control-dialog {
    width: 48px;
    height: 48px;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: 4px;
    font-size: 32px;
    background: rgba(64, 158, 255, 1);
    color: #ffffff;
    cursor: pointer;
  }
}
.info-tip {
  position: fixed;
  top: 40px;
  right: 500px;
  z-index: 10;
  color: #999999;
}
.control-form {
  .el-radio {
    width: 100%;
    line-height: 32px;
  }
}
.element-overlays {
  box-sizing: border-box;
  padding: 8px;
  background: rgba(0, 0, 0, 0.6);
  border-radius: 4px;
  color: #fafafa;
}

body,
body * {
  /* 滚动条 */
  &::-webkit-scrollbar-track-piece {
    background-color: #fff; /*滚动条的背景颜色*/
    -webkit-border-radius: 0; /*滚动条的圆角宽度*/
  }
  &::-webkit-scrollbar {
    width: 10px; /*滚动条的宽度*/
    height: 8px; /*滚动条的高度*/
  }
  &::-webkit-scrollbar-thumb:vertical {
    /*垂直滚动条的样式*/
    height: 50px;
    background-color: rgba(153, 153, 153, 0.5);
    -webkit-border-radius: 4px;
    outline: 2px solid #fff;
    outline-offset: -2px;
    border: 2px solid #fff;
  }
  &::-webkit-scrollbar-thumb {
    /*滚动条的hover样式*/
    background-color: rgba(159, 159, 159, 0.3);
    -webkit-border-radius: 4px;
  }
  &::-webkit-scrollbar-thumb:hover {
    /*滚动条的hover样式*/
    background-color: rgba(159, 159, 159, 0.5);
    -webkit-border-radius: 4px;
  }
}
</style>
