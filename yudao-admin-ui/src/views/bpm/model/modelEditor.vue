<template>
  <div class="app-container">

    <!-- 流程编辑器 -->
    <my-process-designer :key="`designer-${reloadIndex}`" v-model="xmlString" v-bind="controlForm"
      keyboard ref="processDesigner" @init-finished="initModeler"/>
    <!-- 右边工具栏 -->
    <my-properties-panel :key="`penal-${reloadIndex}`" :bpmn-modeler="modeler" :prefix="controlForm.prefix" class="process-panel" />

  </div>
</template>

<script>
import translations from "@/components/bpmnProcessDesigner/src/translations";
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
      xmlString: "", // BPMN XML
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
        prefix: "activiti",
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
    }
  }
};
</script>

<style lang="scss">
//body {
//  overflow: hidden;
//  margin: 0;
//  box-sizing: border-box;
//}
//.app {
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

// TODO 芋艿：去掉多余的 faq
//.info-tip {
//  position: fixed;
//  top: 40px;
//  right: 500px;
//  z-index: 10;
//  color: #999999;
//}

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

.my-process-designer {
  height: calc(100vh - 84px);
  //height: 800px !important; // TODO 芋艿：bjs 容器的高度不对，临时改下
  //z-index: 0 !important;
  //pointer-events: none !important;
}
.process-panel__container { // TODO 芋艿：右边的位置不对，临时改下
  //margin-top: -800px !important;
  //float: right;
  //margin-left: 800px !important;
  //height: 800px;
  //z-index: 2147483647 !important;
  //cursor:pointer !important;
  position: absolute;
  right: 0;
  top: 55px;
  height: calc(100vh - 84px);
}

</style>
