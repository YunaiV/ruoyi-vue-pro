<template>
  <el-card>
    <el-tabs v-model="activeName">
      <el-tab-pane label="基本信息" name="basic">
        <basic-info-form ref="basicInfo" :info="table" />
      </el-tab-pane>
      <el-tab-pane label="字段信息" name="cloum">
        <el-table ref="dragTable" :data="columns" row-key="columnId" :max-height="tableHeight">
          <el-table-column
            label="字段列名"
            prop="columnName"
            min-width="10%"
            :show-overflow-tooltip="true"
          />
          <el-table-column label="字段描述" min-width="10%">
            <template v-slot="scope">
              <el-input v-model="scope.row.columnComment"></el-input>
            </template>
          </el-table-column>
          <el-table-column
            label="物理类型"
            prop="dataType"
            min-width="10%"
            :show-overflow-tooltip="true"
          />
          <el-table-column label="Java类型" min-width="11%">
            <template v-slot="scope">
              <el-select v-model="scope.row.javaType">
                <el-option label="Long" value="Long" />
                <el-option label="String" value="String" />
                <el-option label="Integer" value="Integer" />
                <el-option label="Double" value="Double" />
                <el-option label="BigDecimal" value="BigDecimal" />
                <el-option label="LocalDateTime" value="LocalDateTime" />
                <el-option label="Boolean" value="Boolean" />
              </el-select>
            </template>
          </el-table-column>
          <el-table-column label="java属性" min-width="10%">
            <template v-slot="scope">
              <el-input v-model="scope.row.javaField"></el-input>
            </template>
          </el-table-column>
          <el-table-column label="插入" min-width="4%">
            <template v-slot="scope">
              <el-checkbox true-label="true" false-label="false" v-model="scope.row.createOperation"></el-checkbox>
            </template>
          </el-table-column>
          <el-table-column label="编辑" min-width="4%">
            <template v-slot="scope">
              <el-checkbox true-label="true" false-label="false" v-model="scope.row.updateOperation"></el-checkbox>
            </template>
          </el-table-column>
          <el-table-column label="列表" min-width="4%">
            <template v-slot="scope">
              <el-checkbox true-label="true" false-label="false" v-model="scope.row.listOperationResult"></el-checkbox>
            </template>
          </el-table-column>
          <el-table-column label="查询" min-width="4%">
            <template v-slot="scope">
              <el-checkbox true-label="true" false-label="false" v-model="scope.row.listOperation"></el-checkbox>
            </template>
          </el-table-column>
          <el-table-column label="查询方式" min-width="10%">
            <template v-slot="scope">
              <el-select v-model="scope.row.listOperationCondition">
                <el-option label="=" value="=" />
                <el-option label="!=" value="!=" />
                <el-option label=">" value=">" />
                <el-option label=">=" value=">=" />
                <el-option label="<" value="<>" />
                <el-option label="<=" value="<=" />
                <el-option label="LIKE" value="LIKE" />
                <el-option label="BETWEEN" value="BETWEEN" />
              </el-select>
            </template>
          </el-table-column>
          <el-table-column label="允许空" min-width="5%">
            <template v-slot="scope">
              <el-checkbox true-label="true" false-label="false" v-model="scope.row.nullable"></el-checkbox>
            </template>
          </el-table-column>
          <el-table-column label="显示类型" min-width="12%">
            <template v-slot="scope">
              <el-select v-model="scope.row.htmlType">
                <el-option label="文本框" value="input" />
                <el-option label="文本域" value="textarea" />
                <el-option label="下拉框" value="select" />
                <el-option label="单选框" value="radio" />
                <el-option label="复选框" value="checkbox" />
                <el-option label="日期控件" value="datetime" />
                <el-option label="图片上传" value="imageUpload" />
                <el-option label="文件上传" value="fileUpload" />
                <el-option label="富文本控件" value="editor" />
              </el-select>
            </template>
          </el-table-column>
          <el-table-column label="字典类型" min-width="12%">
            <template v-slot="scope">
              <el-select v-model="scope.row.dictType" clearable filterable placeholder="请选择">
                <el-option
                    v-for="dict in dictOptions"
                    :key="dict.id"
                    :label="dict.name"
                    :value="dict.type"
                />
              </el-select>
            </template>
          </el-table-column>
          <el-table-column label="示例" min-width="10%">
            <template v-slot="scope">
              <el-input v-model="scope.row.example"></el-input>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
      <el-tab-pane label="生成信息" name="genInfo">
        <gen-info-form ref="genInfo" :info="table" :tables="tables" :menus="menus"/>
      </el-tab-pane>
    </el-tabs>
    <el-form label-width="100px">
      <el-form-item style="text-align: center;margin-left:-100px;margin-top:10px;">
        <el-button type="primary" @click="submitForm()">提交</el-button>
        <el-button @click="close()">返回</el-button>
      </el-form-item>
    </el-form>
  </el-card>
</template>
<script>
import { getCodegenDetail, updateCodegen } from "@/api/infra/codegen";
import { listAllSimple as listAllSimpleDictType } from "@/api/system/dict/type";
import { listSimpleMenus } from "@/api/system/menu";
import basicInfoForm from "./basicInfoForm";
import genInfoForm from "./genInfoForm";
import Sortable from 'sortablejs'

export default {
  name: "GenEdit",
  components: {
    basicInfoForm,
    genInfoForm
  },
  data() {
    return {
      // 选中选项卡的 name
      activeName: "cloum",
      // 表格的高度
      tableHeight: document.documentElement.scrollHeight - 245 + "px",
      // 表信息
      tables: [],
      // 表列信息
      columns: [],
      // 字典信息
      dictOptions: [],
      // 菜单信息
      menus: [],
      // 表详细信息
      table: {}
    };
  },
  created() {
    const tableId = this.$route.params && this.$route.params.tableId;
    if (tableId) {
      // 获取表详细信息
      getCodegenDetail(tableId).then(res => {
        this.table = res.data.table;
        this.columns = res.data.columns;
      });
      /** 查询字典下拉列表 */
      listAllSimpleDictType().then(response => {
        this.dictOptions = response.data;
      });
      /** 查询菜单下拉列表 */
      listSimpleMenus().then(response => {
        this.menus = [];
        this.menus.push(...this.handleTree(response.data, "id"));
      });
    }
  },
  methods: {
    /** 提交按钮 */
    submitForm() {
      const basicForm = this.$refs.basicInfo.$refs.basicInfoForm;
      const genForm = this.$refs.genInfo.$refs.genInfoForm;
      Promise.all([basicForm, genForm].map(this.getFormPromise)).then(res => {
        const validateResult = res.every(item => !!item);
        if (validateResult) {
          const genTable = {};
          genTable.table = Object.assign({}, basicForm.model, genForm.model);
          genTable.columns = this.columns;
          genTable.params = {
            treeCode: genTable.treeCode,
            treeName: genTable.treeName,
            treeParentCode: genTable.treeParentCode,
            parentMenuId: genTable.parentMenuId
          };
          updateCodegen(genTable).then(res => {
            this.$modal.msgSuccess("修改成功！");
            this.close();
          });
        } else {
          this.$modal.msgError("表单校验未通过，请重新检查提交内容");
        }
      });
    },
    getFormPromise(form) {
      return new Promise(resolve => {
        form.validate(res => {
          resolve(res);
        });
      });
    },
    /** 关闭按钮 */
    close() {
      this.$tab.closeOpenPage({
        path: "/infra/codegen",
        query: { t: Date.now(), pageNum: this.$route.query.pageNum } }
      );
    }
  },
  mounted() {
    const el = this.$refs.dragTable.$el.querySelectorAll(".el-table__body-wrapper > table > tbody")[0];
    const sortable = Sortable.create(el, {
      handle: ".allowDrag",
      onEnd: evt => {
        const targetRow = this.columns.splice(evt.oldIndex, 1)[0];
        this.columns.splice(evt.newIndex, 0, targetRow);
        for (let index in this.columns) {
          this.columns[index].sort = parseInt(index) + 1;
        }
      }
    });
  }
};
</script>
