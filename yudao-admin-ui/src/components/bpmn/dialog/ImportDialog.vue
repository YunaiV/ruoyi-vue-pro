<template>
  <el-dialog title="导入BPMN" :visible.sync="dialogFormVisible" :before-close="close">
    <el-form>
      <editor v-model="xmlData" @init="editorInit" lang="xml" theme="chrome" width="98%" height="calc(60vh - 100px)"></editor>
    </el-form>
    <div slot="footer" class="dialog-footer">
      <el-button type="primary"  @click="commitForm()">确 定</el-button>
    </div>
  </el-dialog>
</template>

<script>

export default {
  name: "ImportDialog",
  data() {
    return {
      activeNames: ['1'],
      formLabelWidth: "200px",
      xmlData: "类",
      value: "类",
      options: [
        {
          value: "类",
          label: "类"
        }
      ]
    }
  },
  components: {
    editor: require('vue2-ace-editor'),
  },
  props: {
    dialogVisibleBool:{
      type: Boolean,
      required: true
    }
  },
  computed:{
    dialogFormVisible:{
      get(){
        this.xmlData = null
        return this.dialogVisibleBool
      }
    }
  },
  mounted() {
    this.xmlData = null
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
    commitForm(){
      this.$emit('closeShowXmlDialog', this.xmlData);
    },
    close(){
      this.$emit('closeShowXmlDialog', null);
    }
  }
}
</script>

<style scoped>

</style>
