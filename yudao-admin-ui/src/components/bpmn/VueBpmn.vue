<template>
  <div class="">
    <ImportDialog :dialogVisibleBool="importXmlShow" @closeShowXmlDialog="closeShowXmlDialog"></ImportDialog>
    <el-row>
      <el-col :span="24">
        <vue-header class="bpmn-viewer-header" :processData="initData" :modeler="bpmnModeler" @restart="restart" @importXml="importXml"
                    @handleExportSvg="handleExportSvg" @handleExportBpmn="handleExportBpmn" @processSave="processSave"></vue-header>
      </el-col>
    </el-row>
    <el-row style="margin-left: 1%">
      <el-tabs v-model="activeName" type="card" >
        <el-tab-pane label="流程设计器" name="first">
          <el-col :span="19">
            <div class="bpmn-viewer">
              <div class="bpmn-viewer-content" ref="bpmnViewer" ></div>
            </div>
          </el-col>
          <el-col :span="5">
            <bpmn-panel  style="width: 92%"  @updateXml="updateXml" v-if="bpmnModeler" :modeler="bpmnModeler" :process="initData"></bpmn-panel>
          </el-col>
        </el-tab-pane>
        <el-tab-pane label="流程XML" name="second">
          <editor v-model="xmlShowWatch" @init="editorInit" lang="xml" theme="chrome" width="98%" height="calc(100vh - 100px)"></editor>
        </el-tab-pane>
      </el-tabs>
    </el-row>
  </div>
</template>

<script>
  import templateXml from "./data/template";
  import ImportDialog from "./dialog/ImportDialog";
  // import activitiCom from "../provider/activiti";
  // import BpmnModeler2 from 'bpmn-js/lib/Modeler';
  import BpmnModeler from 'jeeplus-bpmn/lib/Modeler'
  import customTranslate from "./data/translate/customTranslate";
  import VueHeader from "./Header";
  import BpmnPanel from "./panel/index";
  import activitiModule from './data/activiti.json'
  import flowableModule from './data/flowable.json'
  import './assets/css/vue-bmpn.css'
  import './assets/css/font-awesome.min.css'

  import 'bpmn-js/dist/assets/diagram-js.css'
  import 'bpmn-js/dist/assets/bpmn-font/css/bpmn.css'
  import 'bpmn-js/dist/assets/bpmn-font/css/bpmn-codes.css'
  import 'bpmn-js/dist/assets/bpmn-font/css/bpmn-embedded.css'
  import './assets/css/vue-bmpn.css'
  export default {
    name: "VueBpmn",
    data() {
      return {
        bpmnModeler: null,
        importXmlShow: false,
        xmlShowWatch: "",
        initTemplate: "",
        activeName: "first",
        initData: {},
        xmlShow: ""
      }
    },
    props: {
      product: String,
      bpmnXml: {
        type: String,
        required: false
      }
    },
    components: {
      editor: require('vue2-ace-editor'),
      VueHeader, BpmnPanel,ImportDialog
    },
    watch: {
      xmlShow: {
        handler(){
          this.xmlShowWatch = this.xmlShow
        }
      }
    },
    mounted() {
      let processId = new Date().getTime();
      this.initTemplate = templateXml.initTemplate(processId)
      this.initData = {key: "process" + processId, name: "流程" + processId, xml: this.initTemplate}
      if (this.bpmnXml != null) {
        this.initTemplate = this.bpmnXml
      }
      this.init();
    },
    methods: {
      editorInit: function (editor) {
        require('brace/mode/xml')
        require('brace/theme/chrome')
        editor.setOptions({
          fontSize: "15px"
        })
        editor.getSession().setUseWrapMode(true);
      },
      init() {
        const _this = this;
        // 支持activiti和flowable
        let _moddleExtensions = this.getModdleExtensions();
        // 获取画布 element
        this.canvas = this.$refs.bpmnViewer;
        // 创建Bpmn对象
        this.bpmnModeler = new BpmnModeler({
          container: this.canvas,
          additionalModules: [
            {
              translate: ['value', customTranslate]
            },
            // activitiCom
          ],
          moddleExtensions: _moddleExtensions
        });
        // 初始化建模器内容
        this.initDiagram(this.initTemplate);
        setTimeout(function () {
          _this.bpmnModeler.saveXML({format: true}, function (err, xml) {
            _this.updateXml(xml)
          });
          // console.log(_this.bpmnModeler);
          // const elementRegistry = _this.bpmnModeler.get('elementRegistry');
          // const startEvenList = elementRegistry.filter (
          //     (item) => item.type === 'bpmn:StartEvent'
          // );
          // // 初始化 开始节点
          // _this.bpmnModeler.get("modeling").updateProperties(startEvenList[0], {"activiti:initiator": "applyUserId"});
        }, 300)

      },
      initDiagram(xml) {
        this.bpmnModeler.importXML(xml, err => {
          if (err) {
            // this.$Message.error("打开模型出错,请确认该模型符合Bpmn2.0规范");
          }
          let _tag = document.getElementsByTagName("svg")[0];
          if (_tag) {
            _tag.style.width = "100%";
            _tag.style.height = "700px";
          }
        });

      },
      handleExportBpmn() {
        const _this = this;
        this.bpmnModeler.saveXML(function (err, xml) {
          if (err) {
            console.error(err)
          }
          let {filename, href} = _this.setEncoded('BPMN', xml);
          if (href && filename) {
            let a = document.createElement('a');
            a.download = filename; //指定下载的文件名
            a.href = href; //  URL对象
            a.click(); // 模拟点击
            URL.revokeObjectURL(a.href); // 释放URL 对象
          }
        });
      },
      updateXml(xmlShow) {
        this.xmlShow  = xmlShow
      },
      handleExportSvg() {
        const _this = this;
        this.bpmnModeler.saveSVG(function (err, svg) {
          if (err) {
            console.error(err)
          }
          let {filename, href} = _this.setEncoded('SVG', svg);
          if (href && filename) {
            let a = document.createElement('a');
            a.download = filename;
            a.href = href;
            a.click();
            URL.revokeObjectURL(a.href);
          }
        });
      },
      setEncoded(type, data) {
        const encodedData = encodeURIComponent(data);
        if (data) {
          if (type === 'XML') {
            return {
              filename: 'diagram.bpmn20.xml',
              href: "data:application/bpmn20-xml;charset=UTF-8," + encodedData,
              data: data
            }
          }
          if (type === 'BPMN') {
            return {
              filename: 'diagram.bpmn',
              href: "data:application/bpmn20-xml;charset=UTF-8," + encodedData,
              data: data
            }
          }
          if (type === 'SVG') {
            this.initData.svg = data;
            return {
              filename: 'diagram.svg',
              href: "data:application/text/xml;charset=UTF-8," + encodedData,
              data: data
            }
          }
        }
      },
      processSave(data){
        let initData = this.initData;
        data.procId = initData.key;
        data.name = initData.name;
        this.$emit("processSave",data);
      },
      restart() {
        let processId = new Date().getTime();
        this.initTemplate = templateXml.initTemplate(processId)
        this.initData = {key: "process" + processId, name: "流程" + processId, xml: this.initTemplate}
        this.initDiagram(this.initTemplate)
      },
      importXml() {
        this.importXmlShow = true
      },
      closeShowXmlDialog(xmlData) {
        this.importXmlShow = false
        const that =this;
        if (xmlData != null) {
          this.initDiagram(xmlData)
          // 刷新数据
          setTimeout(function () {
            that.bpmnModeler.saveXML({format: true}, function (err, xml) {
              that.updateXml(xml)
            })
          },500)
        }
      },
      getModdleExtensions() {
        let moddleExtensions = {};
        if (this.product === "flowable") {
          moddleExtensions = {
            flowable: flowableModule
          }
        }
        if (this.product === "activiti") {
          moddleExtensions = {
            activiti: activitiModule
          }
        }
        return moddleExtensions;
      }
    }
  }
</script>

<style scoped>
.djs-palette{
  top: 10px;
  left: 10px;
}
/deep/.el-tabs__header{
  margin: 0;
}
/deep/.el-tabs--card>.el-tabs__header{
  border-bottom: 0;
}

</style>
