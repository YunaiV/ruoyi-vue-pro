<template>
  <div style="margin-top: 16px">
    <el-form-item label="消息实例">
      <div style="display: flex; align-items: center; justify-content: space-between; flex-wrap: nowrap">
        <el-select v-model="bindMessageId" @change="updateTaskMessage">
          <el-option v-for="id in Object.keys(messageMap)" :value="id" :label="messageMap[id]" :key="id" />
        </el-select>
        <el-button size="mini" type="primary" icon="el-icon-plus" style="margin-left: 8px" @click="openMessageModel" />
      </div>
    </el-form-item>
    <el-dialog :visible.sync="messageModelVisible" :close-on-click-modal="false" title="创建新消息" width="400px" append-to-body destroy-on-close>
      <el-form :model="newMessageForm" size="mini" label-width="90px" @submit.native.prevent>
        <el-form-item label="消息ID">
          <el-input v-model="newMessageForm.id" clearable />
        </el-form-item>
        <el-form-item label="消息名称">
          <el-input v-model="newMessageForm.name" clearable />
        </el-form-item>
      </el-form>
      <template slot="footer">
        <el-button size="mini" type="primary" @click="createNewMessage">确 认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
export default {
  name: "ReceiveTask",
  props: {
    id: String,
    type: String
  },
  data() {
    return {
      bindMessageId: "",
      newMessageForm: {},
      messageMap: {},
      messageModelVisible: false
    };
  },
  watch: {
    id: {
      immediate: true,
      handler() {
        this.$nextTick(() => this.getBindMessage());
      }
    }
  },
  created() {
    this.bpmnMessageRefsMap = Object.create(null);
    this.bpmnRootElements = window.bpmnInstances.modeler.getDefinitions().rootElements;
    this.bpmnRootElements
      .filter(el => el.$type === "bpmn:Message")
      .forEach(m => {
        this.bpmnMessageRefsMap[m.id] = m;
        this.$set(this.messageMap, m.id, m.name);
      });
    this.$set(this.messageMap, "-1", "无"); // 添加一个空对象，保证可以取消原消息绑定
  },
  methods: {
    getBindMessage() {
      this.bpmnElement = window.bpmnInstances.bpmnElement;
      this.bindMessageId = this.bpmnElement.businessObject?.messageRef?.id || "-1";
    },
    openMessageModel() {
      this.messageModelVisible = true;
      this.newMessageForm = {};
    },
    createNewMessage() {
      if (this.messageMap[this.newMessageForm.id]) {
        this.$message.error("该消息已存在，请修改id后重新保存");
        return;
      }
      const newMessage = window.bpmnInstances.moddle.create("bpmn:Message", this.newMessageForm);
      this.bpmnRootElements.push(newMessage);
      this.$set(this.messageMap, this.newMessageForm.id, this.newMessageForm.name);
      this.bpmnMessageRefsMap[this.newMessageForm.id] = newMessage;
      this.messageModelVisible = false;
    },
    updateTaskMessage(messageId) {
      if (messageId === "-1") {
        window.bpmnInstances.modeling.updateProperties(this.bpmnElement, {
          messageRef: null
        });
      } else {
        window.bpmnInstances.modeling.updateProperties(this.bpmnElement, {
          messageRef: this.bpmnMessageRefsMap[messageId]
        });
      }
    }
  },
  beforeDestroy() {
    this.bpmnElement = null;
  }
};
</script>
