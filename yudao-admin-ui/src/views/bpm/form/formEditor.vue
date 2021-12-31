<template>
  <div class="container">
    <div class="left-board">
      <div class="logo-wrapper">
        <div class="logo">流程表单</div>
      </div>
      <el-scrollbar class="left-scrollbar">
        <!-- 左边：表单项 -->
        <div class="components-list">
          <div class="components-title">
            <svg-icon icon-class="component" />输入型组件
          </div>
          <draggable class="components-draggable" :list="inputComponents" :group="{ name: 'componentsGroup', pull: 'clone', put: false }"
            :clone="cloneComponent" draggable=".components-item" :sort="false" @end="onEnd">
            <div v-for="(element, index) in inputComponents" :key="index" class="components-item" @click="addComponent(element)">
              <div class="components-body">
                <svg-icon :icon-class="element.tagIcon" />
                {{ element.label }}
              </div>
            </div>
          </draggable>
          <div class="components-title">
            <svg-icon icon-class="component" />选择型组件
          </div>
          <draggable class="components-draggable" :list="selectComponents" :group="{ name: 'componentsGroup', pull: 'clone', put: false }"
            :clone="cloneComponent" draggable=".components-item" :sort="false" @end="onEnd">
            <div v-for="(element, index) in selectComponents" :key="index" class="components-item" @click="addComponent(element)">
              <div class="components-body">
                <svg-icon :icon-class="element.tagIcon" />
                {{ element.label }}
              </div>
            </div>
          </draggable>
          <div class="components-title">
            <svg-icon icon-class="component" />布局型组件
          </div>
          <draggable class="components-draggable" :list="layoutComponents" :group="{ name: 'componentsGroup', pull: 'clone', put: false }"
                     :clone="cloneComponent" draggable=".components-item" :sort="false" @end="onEnd">
            <div v-for="(element, index) in layoutComponents" :key="index" class="components-item" @click="addComponent(element)">
              <div class="components-body">
                <svg-icon :icon-class="element.tagIcon" />
                {{ element.label }}
              </div>
            </div>
          </draggable>

          <!-- 左边：动态表单 -->
          <el-form ref="form" :model="form" :rules="rules" label-width="80px">
            <el-form-item label="表单名" prop="name">
              <el-input v-model="form.name" placeholder="请输入表单名" />
            </el-form-item>
            <el-form-item label="开启状态" prop="status">
              <el-radio-group v-model="form.status">
                <el-radio v-for="dict in this.getDictDatas(DICT_TYPE.SYS_COMMON_STATUS)"
                          :key="dict.value" :label="parseInt(dict.value)">{{dict.label}}</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item label="备注" prop="remark">
              <el-input type="textarea" v-model="form.remark" placeholder="请输入备注" />
            </el-form-item>
          </el-form>
        </div>
      </el-scrollbar>
    </div>

    <div class="center-board">
      <!-- 上面：操作按钮 -->
      <div class="action-bar">
        <el-button icon="el-icon-check" type="text" @click="save">保存</el-button>
        <el-button class="delete-btn" icon="el-icon-delete" type="text" @click="empty">清空</el-button>
      </div>
      <!-- 中间，表单项 -->
      <el-scrollbar class="center-scrollbar">
        <el-row class="center-board-row" :gutter="formConf.gutter">
          <el-form :size="formConf.size" :label-position="formConf.labelPosition" :disabled="formConf.disabled"
            :label-width="formConf.labelWidth + 'px'">
            <draggable class="drawing-board" :list="drawingList" :animation="340" group="componentsGroup">
              <draggable-item v-for="(element, index) in drawingList" :key="element.renderKey" :drawing-list="drawingList"
                :element="element" :index="index" :active-id="activeId" :form-conf="formConf"
                @activeItem="activeFormItem" @copyItem="drawingItemCopy" @deleteItem="drawingItemDelete"/>
            </draggable>
            <div v-show="!drawingList.length" class="empty-info">
              从左侧拖入或点选组件进行表单设计
            </div>
          </el-form>
        </el-row>
      </el-scrollbar>
    </div>

    <!-- 右边：组件属性/表单属性 -->
    <right-panel :active-data="activeData" :form-conf="formConf" :show-field="!!drawingList.length" @tag-change="tagChange"/>

  </div>
</template>

<script>
import draggable from 'vuedraggable'
import render from '@/utils/generator/render'
import { inputComponents, selectComponents, layoutComponents, formConf} from '@/utils/generator/config'
import drawingDefalut from '@/utils/generator/drawingDefalut'
// import logo from '@/assets/logo/logo.png'
import DraggableItem from './../../tool/build/DraggableItem'
import RightPanel from './../../tool/build/RightPanel'
import {createForm, getForm, updateForm} from "@/api/bpm/form";
import {SysCommonStatusEnum} from "@/utils/constants";

// const emptyActiveData = { style: {}, autosize: {} }
let oldActiveId
let tempActiveData

export default {
  components: {
    draggable,
    render,
    RightPanel,
    DraggableItem
  },
  data() {
    return {
      // logo,
      idGlobal: 100, // 自动 ID 生成时，全局递增。例如说 field100
      formConf, // 表单格式
      inputComponents,
      selectComponents,
      layoutComponents,
      labelWidth: 100,

      drawingData: {}, // 生成后的表单数据

      drawingList: [], // 表单项的数组
      activeId: undefined,
      activeData: {},
      // drawerVisible: false,
      // formData: {},
      // dialogVisible: false,
      // showFileName: false,

      // 表单参数
      form: {
        status: SysCommonStatusEnum.ENABLE,
      },
      // 表单校验
      rules: {
        name: [{ required: true, message: "表单名不能为空", trigger: "blur" }],
        status: [{ required: true, message: "开启状态不能为空", trigger: "blur" }],
      }
    }
  },
  watch: {
    // eslint-disable-next-line func-names
    'activeData.label': function (val, oldVal) {
      if (
        this.activeData.placeholder === undefined
        || !this.activeData.tag
        || oldActiveId !== this.activeId
      ) {
        return
      }
      this.activeData.placeholder = this.activeData.placeholder.replace(oldVal, '') + val
    },
    activeId: {
      handler(val) {
        oldActiveId = val
      },
      immediate: true
    }
  },
  created() {
    // 读取表单配置
    const formId = this.$route.query && this.$route.query.formId
    if (formId) {
      getForm(formId).then(response => {
        const data = response.data
        this.form = {
          id: data.id,
          name: data.name,
          status: data.status,
          remark: data.remark
        }
        this.formConf = JSON.parse(data.conf)
        this.drawingList = this.decodeFields(data.fields)
        // 设置 idGlobal，避免重复
        this.idGlobal += this.drawingList.length
      });
    }
  },
  methods: {
    activeFormItem(element) {
      this.activeData = element
      this.activeId = element.formId
    },
    onEnd(obj, a) {
      if (obj.from !== obj.to) {
        this.activeData = tempActiveData
        this.activeId = this.idGlobal
      }
    },
    addComponent(item) {
      const clone = this.cloneComponent(item)
      this.drawingList.push(clone)
      this.activeFormItem(clone)
    },
    cloneComponent(origin) {
      const clone = JSON.parse(JSON.stringify(origin))
      clone.formId = ++this.idGlobal
      clone.span = formConf.span
      clone.renderKey = +new Date() // 改变renderKey后可以实现强制更新组件
      if (!clone.layout) clone.layout = 'colFormItem'
      if (clone.layout === 'colFormItem') {
        clone.vModel = `field${this.idGlobal}`
        clone.placeholder !== undefined && (clone.placeholder += clone.label)
        tempActiveData = clone
      } else if (clone.layout === 'rowFormItem') {
        delete clone.label
        clone.componentName = `row${this.idGlobal}`
        clone.gutter = this.formConf.gutter
        tempActiveData = clone
      }
      return tempActiveData
    },
    // 获得表单数据
    AssembleFormData() {
      this.formData = {
        fields: JSON.parse(JSON.stringify(this.drawingList)),
        ...this.formConf
      }
    },
    save() {
      // this.AssembleFormData()
      // console.log(this.formData)
      this.$refs["form"].validate(valid => {
        if (!valid) {
          return;
        }
        const form = {
          conf: JSON.stringify(this.formConf), // 表单配置
          // fields: JSON.stringify(this.drawingList), // 表单项的数组
          fields: this.encodeFields(), // 表单项的数组
          ...this.form // 表单名等
        }
        // 修改的提交
        if (this.form.id != null) {
          updateForm(form).then(response => {
            this.msgSuccess("修改成功");
            this.close()
          });
          return;
        }
        // 添加的提交
        createForm(form).then(response => {
          this.msgSuccess("新增成功");
          this.close()
        });
      });
    },
    /** 关闭按钮 */
    close() {
      this.$store.dispatch("tagsView/delView", this.$route);
      this.$router.push({ path: "/bpm/manager/form", query: { t: Date.now()}})
    },
    encodeFields() {
      const fields = []
      this.drawingList.forEach(item => {
        fields.push(JSON.stringify(item))
      })
      return fields
    },
    decodeFields(fields) {
      const drawingList = []
      fields.forEach(item => {
        drawingList.push(JSON.parse(item))
      })
      return drawingList
    },
    empty() {
      this.$confirm('确定要清空所有组件吗？', '提示', { type: 'warning' }).then(
        () => {
          this.drawingList = []
        }
      )
    },
    drawingItemCopy(item, parent) {
      let clone = JSON.parse(JSON.stringify(item))
      clone = this.createIdAndKey(clone)
      parent.push(clone)
      this.activeFormItem(clone)
    },
    createIdAndKey(item) {
      item.formId = ++this.idGlobal
      item.renderKey = +new Date()
      if (item.layout === 'colFormItem') {
        item.vModel = `field${this.idGlobal}`
      } else if (item.layout === 'rowFormItem') {
        item.componentName = `row${this.idGlobal}`
      }
      if (Array.isArray(item.children)) {
        item.children = item.children.map(childItem => this.createIdAndKey(childItem))
      }
      return item
    },
    drawingItemDelete(index, parent) {
      parent.splice(index, 1)
      this.$nextTick(() => {
        const len = this.drawingList.length
        if (len) {
          this.activeFormItem(this.drawingList[len - 1])
        }
      })
    },
    tagChange(newTag) {
      newTag = this.cloneComponent(newTag)
      newTag.vModel = this.activeData.vModel
      newTag.formId = this.activeId
      newTag.span = this.activeData.span
      delete this.activeData.tag
      delete this.activeData.tagIcon
      delete this.activeData.document
      Object.keys(newTag).forEach(key => {
        if (this.activeData[key] !== undefined
          && typeof this.activeData[key] === typeof newTag[key]) {
          newTag[key] = this.activeData[key]
        }
      })
      this.activeData = newTag
      this.updateDrawingList(newTag, this.drawingList)
    },
    updateDrawingList(newTag, list) {
      const index = list.findIndex(item => item.formId === this.activeId)
      if (index > -1) {
        list.splice(index, 1, newTag)
      } else {
        list.forEach(item => {
          if (Array.isArray(item.children)) this.updateDrawingList(newTag, item.children)
        })
      }
    }
  }
}
</script>

<style lang='scss'>
body, html{
  margin: 0;
  padding: 0;
  background: #fff;
  -moz-osx-font-smoothing: grayscale;
  -webkit-font-smoothing: antialiased;
  text-rendering: optimizeLegibility;
  font-family: -apple-system,BlinkMacSystemFont,Segoe UI,Helvetica,Arial,sans-serif,Apple Color Emoji,Segoe UI Emoji;
}

input, textarea{
  font-family: -apple-system,BlinkMacSystemFont,Segoe UI,Helvetica,Arial,sans-serif,Apple Color Emoji,Segoe UI Emoji;
}

.editor-tabs{
  background: #121315;
  .el-tabs__header{
    margin: 0;
    border-bottom-color: #121315;
    .el-tabs__nav{
      border-color: #121315;
    }
  }
  .el-tabs__item{
    height: 32px;
    line-height: 32px;
    color: #888a8e;
    border-left: 1px solid #121315 !important;
    background: #363636;
    margin-right: 5px;
    user-select: none;
  }
  .el-tabs__item.is-active{
    background: #1e1e1e;
    border-bottom-color: #1e1e1e!important;
    color: #fff;
  }
  .el-icon-edit{
    color: #f1fa8c;
  }
  .el-icon-document{
    color: #a95812;
  }
}

// home
.right-scrollbar {
  .el-scrollbar__view {
    padding: 12px 18px 15px 15px;
  }
}
.left-scrollbar .el-scrollbar__wrap {
  box-sizing: border-box;
  overflow-x: hidden !important;
  margin-bottom: 0 !important;
}
.center-tabs{
  .el-tabs__header{
    margin-bottom: 0!important;
  }
  .el-tabs__item{
    width: 50%;
    text-align: center;
  }
  .el-tabs__nav{
    width: 100%;
  }
}
.reg-item{
  padding: 12px 6px;
  background: #f8f8f8;
  position: relative;
  border-radius: 4px;
  .close-btn{
    position: absolute;
    right: -6px;
    top: -6px;
    display: block;
    width: 16px;
    height: 16px;
    line-height: 16px;
    background: rgba(0, 0, 0, 0.2);
    border-radius: 50%;
    color: #fff;
    text-align: center;
    z-index: 1;
    cursor: pointer;
    font-size: 12px;
    &:hover{
      background: rgba(210, 23, 23, 0.5)
    }
  }
  & + .reg-item{
    margin-top: 18px;
  }
}
.action-bar{
  & .el-button+.el-button {
    margin-left: 15px;
  }
  & i {
    font-size: 20px;
    vertical-align: middle;
    position: relative;
    top: -1px;
  }
}

.custom-tree-node{
  width: 100%;
  font-size: 14px;
  .node-operation{
    float: right;
  }
  i[class*="el-icon"] + i[class*="el-icon"]{
    margin-left: 6px;
  }
  .el-icon-plus{
    color: #409EFF;
  }
  .el-icon-delete{
    color: #157a0c;
  }
}

.left-scrollbar .el-scrollbar__view{
  overflow-x: hidden;
}

.el-rate{
  display: inline-block;
  vertical-align: text-top;
}
.el-upload__tip{
  line-height: 1.2;
}

$selectedColor: #f6f7ff;
$lighterBlue: #409EFF;

.container {
  position: relative;
  width: 100%;
  height: 100%;
}

.components-list {
  padding: 8px;
  box-sizing: border-box;
  height: 100%;
  .components-item {
    display: inline-block;
    width: 48%;
    margin: 1%;
    transition: transform 0ms !important;
  }
}
.components-draggable{
  padding-bottom: 20px;
}
.components-title{
  font-size: 14px;
  color: #222;
  margin: 6px 2px;
  .svg-icon{
    color: #666;
    font-size: 18px;
  }
}

.components-body {
  padding: 8px 10px;
  background: $selectedColor;
  font-size: 12px;
  cursor: move;
  border: 1px dashed $selectedColor;
  border-radius: 3px;
  .svg-icon{
    color: #777;
    font-size: 15px;
  }
  &:hover {
    border: 1px dashed #787be8;
    color: #787be8;
    .svg-icon {
      color: #787be8;
    }
  }
}

.left-board {
  width: 260px;
  position: absolute;
  left: 0;
  top: 0;
  height: 100vh;
}
.left-scrollbar{
  height: calc(100vh - 42px);
  overflow: hidden;
}
.center-scrollbar {
  height: calc(100vh - 42px);
  overflow: hidden;
  border-left: 1px solid #f1e8e8;
  border-right: 1px solid #f1e8e8;
  box-sizing: border-box;
}
.center-board {
  height: 100vh;
  width: auto;
  margin: 0 350px 0 260px;
  box-sizing: border-box;
}
.empty-info{
  position: absolute;
  top: 46%;
  left: 0;
  right: 0;
  text-align: center;
  font-size: 18px;
  color: #ccb1ea;
  letter-spacing: 4px;
}
.action-bar{
  position: relative;
  height: 42px;
  text-align: right;
  padding: 0 15px;
  box-sizing: border-box;;
  border: 1px solid #f1e8e8;
  border-top: none;
  border-left: none;
  .delete-btn{
    color: #F56C6C;
  }
}
.logo-wrapper{
  position: relative;
  height: 42px;
  background: #fff;
  border-bottom: 1px solid #f1e8e8;
  box-sizing: border-box;
}
.logo{
  position: absolute;
  left: 12px;
  top: 6px;
  line-height: 30px;
  color: #00afff;
  font-weight: 600;
  font-size: 17px;
  white-space: nowrap;
}

.center-board-row {
  padding: 12px 12px 15px 12px;
  box-sizing: border-box;
  & > .el-form {
    // 69 = 12+15+42
    height: calc(100vh - 69px);
  }
}
.drawing-board {
  height: 100%;
  position: relative;
  .components-body {
    padding: 0;
    margin: 0;
    font-size: 0;
  }
  .sortable-ghost {
    position: relative;
    display: block;
    overflow: hidden;
    &::before {
      content: " ";
      position: absolute;
      left: 0;
      right: 0;
      top: 0;
      height: 3px;
      background: rgb(89, 89, 223);
      z-index: 2;
    }
  }
  .components-item.sortable-ghost {
    width: 100%;
    height: 60px;
    background-color: $selectedColor;
  }
  .active-from-item {
    & > .el-form-item{
      background: $selectedColor;
      border-radius: 6px;
    }
    & > .drawing-item-copy, & > .drawing-item-delete{
      display: initial;
    }
    & > .component-name{
      color: $lighterBlue;
    }
  }
  .el-form-item{
    margin-bottom: 15px;
  }
}
.drawing-item{
  position: relative;
  cursor: move;
  &.unfocus-bordered:not(.activeFromItem) > div:first-child  {
    border: 1px dashed #ccc;
  }
  .el-form-item{
    padding: 12px 10px;
  }
}
.drawing-row-item{
  position: relative;
  cursor: move;
  box-sizing: border-box;
  border: 1px dashed #ccc;
  border-radius: 3px;
  padding: 0 2px;
  margin-bottom: 15px;
  .drawing-row-item {
    margin-bottom: 2px;
  }
  .el-col{
    margin-top: 22px;
  }
  .el-form-item{
    margin-bottom: 0;
  }
  .drag-wrapper{
    min-height: 80px;
  }
  &.active-from-item{
    border: 1px dashed $lighterBlue;
  }
  .component-name{
    position: absolute;
    top: 0;
    left: 0;
    font-size: 12px;
    color: #bbb;
    display: inline-block;
    padding: 0 6px;
  }
}
.drawing-item, .drawing-row-item{
  &:hover {
    & > .el-form-item{
      background: $selectedColor;
      border-radius: 6px;
    }
    & > .drawing-item-copy, & > .drawing-item-delete{
      display: initial;
    }
  }
  & > .drawing-item-copy, & > .drawing-item-delete{
    display: none;
    position: absolute;
    top: -10px;
    width: 22px;
    height: 22px;
    line-height: 22px;
    text-align: center;
    border-radius: 50%;
    font-size: 12px;
    border: 1px solid;
    cursor: pointer;
    z-index: 1;
  }
  & > .drawing-item-copy{
    right: 56px;
    border-color: $lighterBlue;
    color: $lighterBlue;
    background: #fff;
    &:hover{
      background: $lighterBlue;
      color: #fff;
    }
  }
  & > .drawing-item-delete{
    right: 24px;
    border-color: #F56C6C;
    color: #F56C6C;
    background: #fff;
    &:hover{
      background: #F56C6C;
      color: #fff;
    }
  }
}

</style>
