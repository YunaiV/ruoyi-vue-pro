<template>
<!--  <div id="app">-->
  <div class="app-container">

    <!-- TODO 芋艿：去除测试任务 -->
<!--    <my-process-palette />-->
    <my-process-designer
      :key="`designer-${reloadIndex}`"
      v-model="xmlString"
      v-bind="controlForm"
      keyboard
      ref="processDesigner"
      @init-finished="initModeler"
    />
    <my-properties-panel :key="`penal-${reloadIndex}`" :bpmn-modeler="modeler" :prefix="controlForm.prefix" class="process-panel" />

    <!-- 右边的全局设置 -->
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

    <!-- TODO 芋艿：去掉多余的 faq -->
<!--    <div class="info-tip">-->
<!--      <p>注：activiti 好像不支持表单配置，控制台可能会报错</p>-->
<!--      <p>更多配置请查看源码：<a href="https://github.com/miyuesc/bpmn-process-designer">MiyueSC/bpmn-process-designer</a></p>-->
<!--      <p>疑问请在此留言：<a href="https://github.com/miyuesc/bpmn-process-designer/issues/16">MiyueSC/bpmn-process-designer/issues</a></p>-->
<!--    </div>-->
  </div>
</template>

<script>
import translations from "@/components/bpmnProcessDesigner/src/translations";
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
}

</style>
