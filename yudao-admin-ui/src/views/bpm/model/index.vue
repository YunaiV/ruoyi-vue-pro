<template>
  <div class="app-container">

    <!-- 搜索工作栏 -->
    <el-form :model="queryParams" ref="queryForm" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="流程标识" prop="key">
        <el-input v-model="queryParams.key" placeholder="请输入流程标识" clearable style="width: 240px;" size="small"
                  @keyup.enter.native="handleQuery"/>
      </el-form-item>
      <el-form-item label="流程名称" prop="name">
        <el-input v-model="queryParams.name" placeholder="请输入流程名称" clearable style="width: 240px;" size="small"
                  @keyup.enter.native="handleQuery"/>
      </el-form-item>
      <el-form-item label="流程分类" prop="category">
        <el-select v-model="queryParams.category" placeholder="流程分类" clearable size="small" style="width: 240px">
          <el-option v-for="dict in categoryDictDatas" :key="parseInt(dict.value)" :label="dict.label" :value="parseInt(dict.value)"/>
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="cyan" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 操作工具栏 -->
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" icon="el-icon-plus" size="mini" @click="handleAdd"
                   v-hasPermi="['bpm:model:create']">新建流程模型</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="info" icon="el-icon-upload2" size="mini" @click="handleImport"
                   v-hasPermi="['bpm:model:import']">导入流程模型</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <!-- 列表 -->
    <el-table v-loading="loading" :data="list">
      <el-table-column label="流程标识" align="center" prop="key" />
      <el-table-column label="流程名称" align="center" prop="name" width="200">
        <template slot-scope="scope">
          <el-button type="text" @click="handleBpmnDetail(scope.row)">
            <span>{{ scope.row.name }}</span>
          </el-button>
        </template>
      </el-table-column>
      <el-table-column label="流程分类" align="center" prop="category" width="100">
        <template slot-scope="scope">
          <span>{{ getDictDataLabel(DICT_TYPE.BPM_MODEL_CATEGORY, scope.row.category) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="表单信息" align="center" prop="formId">
        <template slot-scope="scope">
          <el-button v-if="scope.row.formId" type="text" @click="handleFormDetail(scope.row)">
            <span>{{ scope.row.formName }}</span>
          </el-button>
          <label v-else>暂无表单</label>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" align="center" prop="createTime" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="最新部署的流程定义" align="center">
        <el-table-column label="流程版本" align="center" prop="processDefinition.version" width="80">
          <template slot-scope="scope">
            <el-tag size="medium" v-if="scope.row.processDefinition">v{{ scope.row.processDefinition.version }}</el-tag>
            <el-tag size="medium" type="warning" v-else>未部署</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="激活状态" align="center" prop="processDefinition.version" width="80">
          <template slot-scope="scope">
            <el-switch v-if="scope.row.processDefinition" v-model="scope.row.processDefinition.suspensionState"
                       :active-value="1" :inactive-value="2" @change="handleChangeState(scope.row)" />
          </template>
        </el-table-column>
        <el-table-column label="部署时间" align="center" prop="deploymentTime" width="180">
          <template slot-scope="scope">
            <span v-if="scope.row.processDefinition">{{ parseTime(scope.row.processDefinition.deploymentTime) }}</span>
          </template>
        </el-table-column>
      </el-table-column>
      <el-table-column label="操作" align="center" width="300">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-setting" @click="handleUpdate(scope.row)"
                     v-hasPermi="['bpm:model:update']">设计流程</el-button>
          <el-button size="mini" type="text" icon="el-icon-thumb" @click="handleDeploy(scope.row)"
                     v-hasPermi="['bpm:model:deploy']">发布流程</el-button>
          <el-button size="mini" type="text" icon="el-icon-ice-cream-round" @click="handleDefinitionList(scope.row)"
                     v-hasPermi="['bpm:model:query']">流程定义</el-button>
          <el-button size="mini" type="text" icon="el-icon-delete" @click="handleDelete(scope.row)"
                     v-hasPermi="['bpm:model:delete']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <!-- 分页组件 -->
    <pagination v-show="total>0" :total="total" :page.sync="queryParams.pageNo" :limit.sync="queryParams.pageSize"
                @pagination="getList"/>

    <!-- 流程表单配置详情 -->
    <el-dialog title="表单详情" :visible.sync="detailOpen" width="50%" append-to-body>
      <parser :key="new Date().getTime()" :form-conf="detailForm" />
    </el-dialog>

    <!-- 流程模型图的预览 -->
    <el-dialog title="流程图" :visible.sync="showBpmnOpen" width="80%" append-to-body>
      <my-process-viewer key="designer" v-model="bpmnXML" v-bind="bpmnControlForm" />
    </el-dialog>

    <!-- 对话框(添加) -->
    <el-dialog title="新建模型" :visible.sync="open" width="500px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="流程标识" prop="key">
          <el-input v-model="form.key" placeholder="请输入流标标识" style="width: 350px;" />
          <el-tooltip class="item" effect="light" content="新建后，流程标识不可修改！" placement="top">
            <i style="padding-left: 5px;" class="el-icon-question" />
          </el-tooltip>
        </el-form-item>
        <el-form-item label="流程名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入流程名称" clearable />
        </el-form-item>
        <el-form-item label="流程描述" prop="description">
          <el-input type="textarea" v-model="form.description" clearable />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>

    <!-- 用户导入对话框 -->
    <el-dialog title="导入流程模型" :visible.sync="upload.open" width="400px" append-to-body>
      <el-upload ref="upload" :limit="1" accept=".bpmn, .xml" :headers="upload.headers" :action="upload.url"
        :disabled="upload.isUploading" :on-progress="handleFileUploadProgress" :on-success="handleFileSuccess"
        :auto-upload="false" name="bpmnFile" :data="upload.form" drag>
        <i class="el-icon-upload"></i>
        <div class="el-upload__text">
          将文件拖到此处，或
          <em>点击上传</em>
        </div>
        <div class="el-upload__tip" style="color:red" slot="tip">提示：仅允许导入“bpm”或“xml”格式文件！</div>
        <div class="el-upload__tip" slot="tip">
          <el-form ref="uploadForm" size="mini" label-width="90px" :model="upload.form" :rules="upload.rules" @submit.native.prevent>
            <el-form-item label="流程标识" prop="key">
              <el-input v-model="upload.form.key" placeholder="请输入流标标识" style="width: 250px;" />
              <el-tooltip class="item" effect="light" content="新建后，流程标识不可修改！" placement="top">
                <i style="padding-left: 5px;" class="el-icon-question" />
              </el-tooltip>
            </el-form-item>
            <el-form-item label="流程名称" prop="name">
              <el-input v-model="upload.form.name" placeholder="请输入流程名称" clearable />
            </el-form-item>
            <el-form-item label="流程描述" prop="description">
              <el-input type="textarea" v-model="upload.form.description" clearable />
            </el-form-item>
          </el-form>
        </div>
      </el-upload>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitFileForm">确 定</el-button>
        <el-button @click="uploadClose">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import {deleteModel, deployModel, getModelPage, getModel, updateModelState, createModel} from "@/api/bpm/model";
import {DICT_TYPE, getDictDatas} from "@/utils/dict";
import {getForm, getSimpleForms} from "@/api/bpm/form";
import {decodeFields} from "@/utils/formGenerator";
import Parser from '@/components/parser/Parser'
import {getBaseHeader} from "@/utils/request";

export default {
  name: "model",
  components: {
    Parser
  },
  data() {
    return {
      // 遮罩层
      loading: true,
      // 显示搜索条件
      showSearch: true,
      // 总条数
      total: 0,
      // 表格数据
      list: [],
      // 查询参数
      queryParams: {
        pageNo: 1,
        pageSize: 10
      },

      // BPMN 数据
      showBpmnOpen: false,
      bpmnXML: null,
      bpmnControlForm: {
        prefix: "activiti"
      },

      // 流程表单详情
      detailOpen: false,
      detailForm: {
        fields: []
      },

      // 流程表单
      open: false,
      form: {},
      // 表单校验
      rules: {
        key: [{ required: true, message: "流程标识不能为空", trigger: "blur" }],
        name: [{ required: true, message: "流程名称不能为空", trigger: "blur" }],
      },

      // 流程导入参数
      upload: {
        // 是否显示弹出层（用户导入）
        open: false,
        // 是否禁用上传
        isUploading: false,
        // 设置上传的请求头部
        headers: getBaseHeader(),
        // 上传的地址
        url: process.env.VUE_APP_BASE_API + '/api/' + "/bpm/model/import",
        // 表单
        form: {},
        // 校验规则
        rules: {
          key: [{ required: true, message: "流程标识不能为空", trigger: "blur" }],
          name: [{ required: true, message: "流程名称不能为空", trigger: "blur" }],
        },
      },
      // 流程表单的下拉框的数据
      forms: [],

      // 数据字典
      categoryDictDatas: getDictDatas(DICT_TYPE.BPM_MODEL_CATEGORY),
    };
  },
  created() {
    this.getList();
    // 获得流程表单的下拉框的数据
    getSimpleForms().then(response => {
      this.forms = response.data
    })
  },
  methods: {
    /** 查询流程模型列表 */
    getList() {
      this.loading = true;
      getModelPage(this.queryParams).then(response => {
          this.list = response.data.list;
          this.total = response.data.total;
          this.loading = false;
        }
      );
    },
    /** 取消按钮 */
    cancel() {
      this.open = false;
      this.reset();
    },
    // 表单重置
    reset() {
      this.form = {
        key: undefined,
        name: undefined,
        description: undefined
      };
      this.resetForm("form");
    },
    /** 搜索按钮操作 */
    handleQuery() {
      this.queryParams.pageNo = 1;
      this.getList();
    },
    /** 重置按钮操作 */
    resetQuery() {
      this.dateRange = [];
      this.resetForm("queryForm");
      this.handleQuery();
    },
    /** 新增按钮操作 */
    handleAdd() {
      this.reset();
      this.open = true;
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.$router.push({
        path:"/bpm/manager/model/edit",
        query:{
          modelId: row.id
        }
      });
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (!valid) {
          return;
        }
        createModel(this.form).then(response => {
          this.msgSuccess("新建流程模型成功");
          this.open = false;
          this.getList();
        });
      });
    },
    /** 删除按钮操作 */
    handleDelete(row) {
      const that = this;
      this.$confirm('是否删除该流程！！', "警告", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning"
      }).then(function() {
        deleteModel(row.id).then(response => {
          that.getList();
          that.msgSuccess("删除成功");
        })
      })
    },
    /** 部署按钮操作 */
    handleDeploy(row) {
      const that = this;
      this.$confirm('是否部署该流程！！', "提示", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "success"
      }).then(function() {
        deployModel(row.id).then(response => {
          that.getList();
          that.msgSuccess("部署成功");
        })
      })
    },
    /** 流程表单的详情按钮操作 */
    handleFormDetail(row) {
      getForm(row.formId).then(response => {
        // 设置值
        const data = response.data
        this.detailForm = {
          ...JSON.parse(data.conf),
          fields: decodeFields(data.fields)
        }
        // 弹窗打开
        this.detailOpen = true
      })
    },
    /** 流程图的详情按钮操作 */
    handleBpmnDetail(row) {
      getModel(row.id).then(response => {
        this.bpmnXML = response.data.bpmnXml
        // 弹窗打开
        this.showBpmnOpen = true
      })
    },
    /** 跳转流程定义的列表 */
    handleDefinitionList(row) {
      this.$router.push({
        path:"/bpm/manager/definition",
        query:{
          key: row.key
        }
      });
    },
    /** 更新状态操作 */
    handleChangeState(row) {
      const id = row.id;
      let state = row.processDefinition.suspensionState;
      let statusState = state === 1 ? '激活' : '挂起';
      this.$confirm('是否确认' + statusState + '流程名字为"' + row.name + '"的数据项?', "警告", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning"
      }).then(function() {
        return updateModelState(id, state);
      }).then(() => {
        this.getList();
        this.msgSuccess(statusState + "成功");
      })
    },
    /** 导入按钮操作 */
    handleImport() {
      this.upload.open = true;
    },
    // 文件上传中处理
    handleFileUploadProgress(event, file, fileList) {
      this.upload.isUploading = true;
    },
    // 文件上传成功处理
    handleFileSuccess(response, file, fileList) {
      if (response.code !== 0) {
        this.msgError(response.msg)
        return;
      }
      // 重置表单
      this.uploadClose();
      // 提示，并刷新
      this.msgSuccess("导入流程模型成功！请点击【设计流程】按钮，进行编辑保存后，才可以进行【发布流程】");
      this.getList();
    },
    uploadClose() {
      // 关闭弹窗
      this.upload.open = false;
      // 重置上传状态和文件
      this.upload.isUploading = false;
      this.$refs.upload.clearFiles();
      // 重置表单
      this.upload.form = {};
      this.resetForm("uploadForm");
    },
    /** 提交上传文件 */
    submitFileForm() {
      this.$refs["uploadForm"].validate(valid => {
        if (!valid) {
          return;
        }
        this.$refs.upload.submit();
      })
    },
  }
};
</script>

<style lang="scss">
.my-process-designer {
  height: calc(100vh - 200px);
}
</style>
