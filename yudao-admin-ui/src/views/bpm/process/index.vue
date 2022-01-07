<template>
  <div class="app-container">

    <!-- 搜索工作栏 -->
    <el-form :model="queryParams" ref="queryForm" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="流程名" prop="name">
        <el-input v-model="queryParams.name" placeholder="请输入流程名" clearable size="small" @keyup.enter.native="handleQuery"/>
      </el-form-item>
      <el-form-item label="所属流程" prop="processDefinitionId">
        <el-input v-model="queryParams.processDefinitionId" placeholder="请输入流程定义的编号" clearable size="small" @keyup.enter.native="handleQuery"/>
      </el-form-item>
      <el-form-item label="流程分类" prop="category">
        <el-select v-model="queryParams.category" placeholder="请选择流程分类" clearable size="small">
          <el-option v-for="dict in this.getDictDatas(DICT_TYPE.BPM_MODEL_CATEGORY)"
                     :key="dict.value" :label="dict.label" :value="dict.value"/>
        </el-select>
      </el-form-item>
      <el-form-item label="提交时间">
        <el-date-picker v-model="dateRangeCreateTime" size="small" style="width: 240px" value-format="yyyy-MM-dd"
                        type="daterange" range-separator="-" start-placeholder="开始日期" end-placeholder="结束日期" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择状态" clearable size="small">
          <el-option v-for="dict in this.getDictDatas(DICT_TYPE.BPM_PROCESS_INSTANCE_STATUS)"
                     :key="dict.value" :label="dict.label" :value="dict.value"/>
        </el-select>
      </el-form-item>
      <el-form-item label="结果" prop="result">
        <el-select v-model="queryParams.result" placeholder="请选择流结果" clearable size="small">
          <el-option v-for="dict in this.getDictDatas(DICT_TYPE.BPM_PROCESS_INSTANCE_RESULT)"
                     :key="dict.value" :label="dict.label" :value="dict.value"/>
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 操作工具栏 -->
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="el-icon-plus" size="mini" @click="handleAdd">发起流程</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <!-- 列表 -->
    <el-table v-loading="loading" :data="list">
      <el-table-column label="编号" align="center" prop="id" width="300" />
      <el-table-column label="流程名" align="center" prop="name" />
      <el-table-column label="流程分类" align="center" prop="category">
        <template slot-scope="scope">
          <span>{{ getDictDataLabel(DICT_TYPE.BPM_MODEL_CATEGORY, scope.row.category) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="当前审批任务" align="center" prop="tasks" /> <!-- TODO 芋艿：待完善 -->
      <el-table-column label="状态" align="center" prop="status">
        <template slot-scope="scope">
          <span>
            <el-tag type="primary" v-if="scope.row.result === 1"> <!-- 进行中 -->
              {{ getDictDataLabel(DICT_TYPE.BPM_PROCESS_INSTANCE_STATUS, scope.row.result) }}
            </el-tag>
             <el-tag type="success" v-if="scope.row.result === 2"> <!-- 已结束 -->
              {{ getDictDataLabel(DICT_TYPE.BPM_PROCESS_INSTANCE_STATUS, scope.row.result) }}
            </el-tag>
          </span>
        </template>
      </el-table-column>
      <el-table-column label="结果" align="center" prop="result">
        <template slot-scope="scope">
          <span>
            <el-tag type="primary" v-if="scope.row.result === 1"> <!-- 进行中 -->
              {{ getDictDataLabel(DICT_TYPE.BPM_PROCESS_INSTANCE_RESULT, scope.row.result) }}
            </el-tag>
             <el-tag type="success" v-if="scope.row.result === 2"> <!-- 通过 -->
              {{ getDictDataLabel(DICT_TYPE.BPM_PROCESS_INSTANCE_RESULT, scope.row.result) }}
            </el-tag>
             <el-tag type="danger" v-if="scope.row.result === 3"> <!-- 不通过 -->
              {{ getDictDataLabel(DICT_TYPE.BPM_PROCESS_INSTANCE_RESULT, scope.row.result) }}
            </el-tag>
             <el-tag type="info" v-if="scope.row.result === 4"> <!-- 撤回 -->
              {{ getDictDataLabel(DICT_TYPE.BPM_PROCESS_INSTANCE_RESULT, scope.row.result) }}
            </el-tag>
          </span>
        </template>
      </el-table-column>
      <el-table-column label="提交时间" align="center" prop="createTime" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="结束时间" align="center" prop="createTime" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.endTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template slot-scope="scope">
<!--          <el-button size="mini" type="text" icon="el-icon-edit" @click="handleUpdate(scope.row)"-->
<!--                     v-hasPermi="['bpm:process-instance-ext:update']">修改</el-button>-->
        </template>
      </el-table-column>
    </el-table>
    <!-- 分页组件 -->
    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNo" :limit.sync="queryParams.pageSize"
                @pagination="getList"/>

    <!-- 对话框(添加 / 修改) -->
    <el-dialog :title="title" :visible.sync="open" width="500px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="发起流程的用户编号" prop="userId">
          <el-input v-model="form.userId" placeholder="请输入发起流程的用户编号" />
        </el-form-item>
        <el-form-item label="流程实例的名字" prop="name">
          <el-input v-model="form.name" placeholder="请输入流程实例的名字" />
        </el-form-item>
        <el-form-item label="流程实例的编号" prop="processInstanceId">
          <el-input v-model="form.processInstanceId" placeholder="请输入流程实例的编号" />
        </el-form-item>
        <el-form-item label="流程定义的编号" prop="processDefinitionId">
          <el-input v-model="form.processDefinitionId" placeholder="请输入流程定义的编号" />
        </el-form-item>
        <el-form-item label="流程分类" prop="category">
          <el-select v-model="form.category" placeholder="请选择流程分类">
            <el-option v-for="dict in this.getDictDatas(DICT_TYPE.BPM_MODEL_CATEGORY)"
                       :key="dict.value" :label="dict.label" :value="dict.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="流程实例的名字" prop="status">
          <el-select v-model="form.status" placeholder="请选择流程实例的名字">
            <el-option v-for="dict in this.getDictDatas(DICT_TYPE.BPM_PROCESS_INSTANCE_STATUS)"
                       :key="dict.value" :label="dict.label" :value="parseInt(dict.value)" />
          </el-select>
        </el-form-item>
        <el-form-item label="流程实例的结果" prop="result">
          <el-select v-model="form.result" placeholder="请选择流程实例的结果">
            <el-option v-for="dict in this.getDictDatas(DICT_TYPE.BPM_PROCESS_INSTANCE_RESULT)"
                       :key="dict.value" :label="dict.label" :value="parseInt(dict.value)" />
          </el-select>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import {
  getMyProcessInstancePage,
  createProcessInstanceExt,
  updateProcessInstanceExt,
  deleteProcessInstanceExt,
  getProcessInstanceExt,
  getProcessInstanceExtPage,
  exportProcessInstanceExtExcel
} from "@/api/bpm/processInstance";

export default {
  name: "ProcessInstanceExt",
  components: {
  },
  data() {
    return {
      // 遮罩层
      loading: true,
      // 显示搜索条件
      showSearch: true,
      // 总条数
      total: 0,
      // 工作流的流程实例的拓展列表
      list: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      dateRangeCreateTime: [],
      // 查询参数
      queryParams: {
        pageNo: 1,
        pageSize: 10,
        name: null,
        processDefinitionId: null,
        category: null,
        status: null,
        result: null,
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        userId: [{ required: true, message: "发起流程的用户编号不能为空", trigger: "blur" }],
        name: [{ required: true, message: "流程实例的名字不能为空", trigger: "blur" }],
        processInstanceId: [{ required: true, message: "流程实例的编号不能为空", trigger: "blur" }],
        processDefinitionId: [{ required: true, message: "流程定义的编号不能为空", trigger: "blur" }],
        category: [{ required: true, message: "流程分类不能为空", trigger: "change" }],
        status: [{ required: true, message: "流程实例的名字不能为空", trigger: "change" }],
        result: [{ required: true, message: "流程实例的结果不能为空", trigger: "change" }],
      }
    };
  },
  created() {
    this.getList();
  },
  methods: {
    /** 查询列表 */
    getList() {
      this.loading = true;
      // 处理查询参数
      let params = {...this.queryParams};
      this.addBeginAndEndTime(params, this.dateRangeCreateTime, 'createTime');
      // 执行查询
      getMyProcessInstancePage(params).then(response => {
        this.list = response.data.list;
        this.total = response.data.total;
        this.loading = false;
      });
    },
    /** 取消按钮 */
    cancel() {
      this.open = false;
      this.reset();
    },
    /** 表单重置 */
    reset() {
      this.form = {
        id: undefined,
        userId: undefined,
        name: undefined,
        processInstanceId: undefined,
        processDefinitionId: undefined,
        category: undefined,
        status: undefined,
        result: undefined,
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
      this.dateRangeCreateTime = [];
      this.resetForm("queryForm");
      this.handleQuery();
    },
    /** 新增按钮操作 */
    handleAdd() {
      this.reset();
      this.open = true;
      this.title = "添加工作流的流程实例的拓展";
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (!valid) {
          return;
        }
        // 修改的提交
        if (this.form.id != null) {
          updateProcessInstanceExt(this.form).then(response => {
            this.msgSuccess("修改成功");
            this.open = false;
            this.getList();
          });
          return;
        }
        // 添加的提交
        createProcessInstanceExt(this.form).then(response => {
          this.msgSuccess("新增成功");
          this.open = false;
          this.getList();
        });
      });
    }
  }
};
</script>
