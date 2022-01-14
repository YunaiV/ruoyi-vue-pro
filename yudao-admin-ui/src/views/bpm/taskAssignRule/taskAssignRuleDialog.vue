<template>
  <div>
    <!-- 列表弹窗 -->
    <el-dialog title="任务分配规则" :visible.sync="visible" width="800px" append-to-body>
      <el-table v-loading="loading" :data="list">
        <el-table-column label="任务名" align="center" prop="taskDefinitionName" width="120" fixed />
        <el-table-column label="任务标识" align="center" prop="taskDefinitionKey" width="120" show-tooltip-when-overflow />
        <el-table-column label="规则类型" align="center" prop="type" width="120">
          <template slot-scope="scope">
            <span>{{ getDictDataLabel(DICT_TYPE.BPM_TASK_ASSIGN_RULE_TYPE, scope.row.type) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="规则范围" align="center" prop="options" width="300px">
          <template slot-scope="scope">
            <el-tag size="medium" v-if="scope.row.options" v-for="option in scope.row.options">
              {{ getAssignRuleOptionName(scope.row.type, option) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center" width="80" fixed="right">
          <template slot-scope="scope">
            <!-- TODO 权限 -->
            <el-button size="mini" type="text" icon="el-icon-edit" @click="handleUpdateTaskAssignRule(scope.row)"
                       v-hasPermi="['bpm:model:update']">修改</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>
    <!-- 添加/修改弹窗 -->
    <el-dialog title="修改任务规则" :visible.sync="open" width="500px" append-to-body>
      <el-form ref="taskAssignRuleForm" :model="form" :rules="rules" label-width="110px">
        <el-form-item label="任务名称" prop="taskDefinitionName">
          <el-input v-model="form.taskDefinitionName" disabled />
        </el-form-item>
        <el-form-item label="任务标识" prop="taskDefinitionKey">
          <el-input v-model="form.taskDefinitionKey" disabled />
        </el-form-item>
        <el-form-item label="规则类型" prop="type">
          <el-select v-model="form.type" clearable style="width: 100%">
            <el-option v-for="dict in taskAssignRuleDictDatas" :key="parseInt(dict.value)" :label="dict.label" :value="parseInt(dict.value)"/>
          </el-select>
        </el-form-item>
        <el-form-item v-if="form.type === 10" label="指定角色" prop="roleIds">
          <el-select v-model="form.roleIds" multiple clearable style="width: 100%">
            <el-option v-for="item in roleOptions" :key="parseInt(item.id)" :label="item.name" :value="parseInt(item.id)" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="form.type === 20 || form.type === 21" label="指定部门" prop="deptIds">
          <treeselect v-model="form.deptIds" :options="deptTreeOptions" multiple flat :defaultExpandLevel="3"
                      placeholder="请选择指定部门" :normalizer="normalizer"/>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitAssignRuleForm">确 定</el-button>
        <el-button @click="cancelAssignRuleForm">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import {DICT_TYPE, getDictDatas} from "@/utils/dict";
import {createTaskAssignRule, getTaskAssignRuleList, updateTaskAssignRule} from "@/api/bpm/taskAssignRule";
import {listSimpleRoles} from "@/api/system/role";
import {listSimpleDepts} from "@/api/system/dept";

import Treeselect from "@riophae/vue-treeselect";
import "@riophae/vue-treeselect/dist/vue-treeselect.css";

export default {
  name: "taskAssignRuleDialog",
  components: {
    Treeselect
  },
  data() {
    return {
      // 如下参数，可传递
      modelId: undefined, // 流程模型的编号。如果 modelId 非空，则用于流程模型的查看与配置
      processDefinitionId: undefined, // 流程定义的编号。如果 processDefinitionId 非空，则用于流程定义的查看，不支持配置
      visible: false,

      // 任务分配规则表单
      row: undefined, // 选中的流程模型
      list: [], // 选中流程模型的任务分配规则们
      loading: false, // 加载中
      open: false, // 是否打开
      form: {}, // 表单
      rules: { // 表单校验规则
        type: [{ required: true, message: "规则类型不能为空", trigger: "change" }],
        roleIds: [{required: true, message: "指定角色不能为空", trigger: "change" }],
        deptIds: [{required: true, message: "指定部门不能为空", trigger: "change" }],
      },

      // 各种下拉框
      roleOptions: [],
      deptOptions: [],
      deptTreeOptions: [],

      // 数据字典
      modelFormTypeDictDatas: getDictDatas(DICT_TYPE.BPM_MODEL_FORM_TYPE),
      taskAssignRuleDictDatas: getDictDatas(DICT_TYPE.BPM_TASK_ASSIGN_RULE_TYPE),
    };
  },
  methods: {
    initModel(modelId) {
      this.modelId = modelId;
      this.processDefinitionId = undefined;

      // 初始化所有下拉框
      this.init0();
    },
    initProcessDefinition(processDefinitionId) {
      this.modelId = undefined;
      this.processDefinitionId = processDefinitionId;

      // 初始化所有下拉框
      this.init0();
    },
    /** 初始化 */
    init0() {
      // 设置可见
      this.visible = true;
      // 获得列表
      this.getList();

      // 获得角色列表
      this.roleOptions = [];
      listSimpleRoles().then(response => {
        this.roleOptions.push(...response.data);
      });
      // 获得部门列表
      this.deptOptions = [];
      this.deptTreeOptions = [];
      listSimpleDepts().then(response => {
        // 处理 roleOptions 参数
        this.deptOptions.push(...response.data);
        this.deptTreeOptions.push(...this.handleTree(response.data, "id"));
      });
    },
    /** 获得任务分配规则列表 */
    getList() {
      this.loading = true;
      getTaskAssignRuleList({
        modelId: this.modelId,
      }).then(response => {
        this.loading = false;
        this.list = response.data;
      })
    },
    /** 处理修改任务分配规则的按钮操作 */
    handleUpdateTaskAssignRule(row) {
      // 先重置标识
      this.resetAssignRuleForm();
      // 设置表单
      this.form = {
        ...row,
        options: [],
        roleIds: [],
        deptIds: [],
      };
      // 将 options 赋值到对应的 roleIds 等选项
      if (row.type === 10) {
        this.form.roleIds.push(...row.options);
      } else if (row.type === 20 || row.type === 21) {
        this.form.deptIds.push(...row.options);
      }
      this.open = true;
    },
    /** 提交任务分配规则的表单 */
    submitAssignRuleForm() {
      this.$refs["taskAssignRuleForm"].validate(valid => {
        if (valid) {
          // 构建表单
          let form = {
            ...this.form,
            taskDefinitionName: undefined,
          };
          // 将 roleIds 等选项赋值到 options 中
          if (form.type === 10) {
            form.options = form.roleIds;
          } else if (form.type === 20 || form.type === 21) {
            form.options = form.deptIds;
          }
          form.roleIds = undefined;
          form.deptIds = undefined;
          // 新增
          if (!form.id) {
            form.modelId = this.modelId; // 模型编号
            createTaskAssignRule(form).then(response => {
              this.msgSuccess("修改成功");
              this.open = false;
              this.getList();
            });
            // 修改
          } else {
            form.taskDefinitionKey = undefined; // 无法修改
            updateTaskAssignRule(form).then(response => {
              this.msgSuccess("修改成功");
              this.open = false;
              this.getList();
            });
          }
        }
      });
    },
    /** 取消任务分配规则的表单 */
    cancelAssignRuleForm() {
      this.open = false;
      this.resetAssignRuleForm();
    },
    /** 表单重置 */
    resetAssignRuleForm() {
      this.form = {};
      this.resetForm("taskAssignRuleForm");
    },
    getAssignRuleOptionName(type, option) {
      if (type === 10) {
        for (const roleOption of this.roleOptions) {
          if (roleOption.id === option) {
            return roleOption.name;
          }
        }
      } else if (type === 20 || type === 21) {
        for (const deptOption of this.deptOptions) {
          if (deptOption.id === option) {
            return deptOption.name;
          }
        }
      }
      return '未知(' + option + ')';
    },
    // 格式化部门的下拉框
    normalizer(node) {
      return {
        id: node.id,
        label: node.name,
        children: node.children
      }
    }
  }
};
</script>
