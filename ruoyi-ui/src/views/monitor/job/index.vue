<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" :inline="true" v-show="showSearch" label-width="100px">
      <el-form-item label="任务名称" prop="name">
        <el-input v-model="queryParams.name" placeholder="请输入任务名称" clearable size="small" @keyup.enter.native="handleQuery"/>
      </el-form-item>
      <el-form-item label="任务状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择任务状态" clearable size="small">
          <el-option v-for="dict in this.getDictDatas(DICT_TYPE.INF_JOB_STATUS)"
                     :key="dict.value" :label="dict.label" :value="dict.value"/>
        </el-select>
      </el-form-item>
      <el-form-item label="处理器的名字" prop="handlerName">
        <el-input v-model="queryParams.handlerName" placeholder="请输入处理器的名字" clearable size="small" @keyup.enter.native="handleQuery"/>
      </el-form-item>
      <el-form-item>
        <el-button type="cyan" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" icon="el-icon-plus" size="mini" @click="handleAdd"
                   v-hasPermi="['monitor:job:add']">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" icon="el-icon-download" size="mini" @click="handleExport"
                   v-hasPermi="['monitor:job:export']">导出</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="info" icon="el-icon-s-operation" size="mini" @click="handleJobLog"
                   v-hasPermi="['monitor:job:query']">日志</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="jobList">
      <el-table-column label="任务编号" align="center" prop="id" />
      <el-table-column label="任务名称" align="center" prop="name" />
      <el-table-column label="任务状态" align="center" prop="status">
        <template slot-scope="scope">
          <span>{{ getDictDataLabel(DICT_TYPE.INF_JOB_STATUS, scope.row.status) }}</span>
        </template>
      </el-table-column>>
      <el-table-column label="处理器的名字" align="center" prop="handlerName" />
      <el-table-column label="处理器的参数" align="center" prop="handlerParam" />
      <el-table-column label="CRON 表达式" align="center" prop="cronExpression" />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-view" @click="handleView(scope.row)"
                     v-hasPermi="['monitor:job:query']">详细</el-button>
          <el-button size="mini" type="text" icon="el-icon-view" @click="handleUpdate(scope.row)"
                     v-hasPermi="['monitor:job:query']">修改</el-button>
          <el-button
              size="mini"
              type="text"
              icon="el-icon-caret-right"
              @click="handleRun(scope.row)"
              v-hasPermi="['monitor:job:changeStatus']"
          >执行一次</el-button>
        </template>
      </el-table-column>
    </el-table>
    <!-- 分页组件 -->
    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNo" :limit.sync="queryParams.pageSize"
                @pagination="getList"/>

    <!-- 添加或修改定时任务对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="500px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="120px">
        <el-form-item label="任务名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入任务名称" />
        </el-form-item>
        <el-form-item label="处理器的名字" prop="handlerName">
          <el-input v-model="form.handlerName" placeholder="请输入处理器的名字" v-bind:readonly="form.id !== undefined" />
        </el-form-item>
        <el-form-item label="处理器的参数" prop="handlerParam">
          <el-input v-model="form.handlerParam" placeholder="请输入处理器的参数" />
        </el-form-item>
        <el-form-item label="CRON 表达式" prop="cronExpression">
          <el-input v-model="form.cronExpression" placeholder="请输入CRON 表达式" />
        </el-form-item>
        <el-form-item label="监控超时时间" prop="monitorTimeout">
          <el-input v-model="form.monitorTimeout" placeholder="请输入监控超时时间，单位：毫秒" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>

    <!-- 任务日志详细 -->
    <el-dialog title="任务详细" :visible.sync="openView" width="700px" append-to-body>
      <el-form ref="form" :model="form" label-width="200px" size="mini">
        <el-row>
          <el-col :span="24">
            <el-form-item label="任务编号：">{{ form.id }}</el-form-item>
            <el-form-item label="任务名称：">{{ form.name }}</el-form-item>
            <el-form-item label="任务名称：">{{ getDictDataLabel(DICT_TYPE.INF_JOB_STATUS, form.status) }}</el-form-item>
            <el-form-item label="处理器的名字：">{{ form.handlerName }}</el-form-item>
            <el-form-item label="处理器的参数：">{{ form.handlerParam }}</el-form-item>
            <el-form-item label="cron表达式：">{{ form.cronExpression }}</el-form-item>
            <el-form-item label="最后一次执行的开始时间：">{{ parseTime(form.executeBeginTime) }}</el-form-item>
            <el-form-item label="最后一次执行的开始时间：">{{ parseTime(form.executeEndTime) }}</el-form-item>
            <el-form-item label="上一次触发时间：">{{ parseTime(form.firePrevTime) }}</el-form-item>
            <el-form-item label="下一次触发时间：">{{ parseTime(form.fireNextTime) }}</el-form-item>
            <el-form-item label="监控超时时间：">{{ form.monitorTimeout > 0 ? form.monitorTimeout + " 毫秒" : "未开启" }}</el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="openView = false">关 闭</el-button>
      </div>
    </el-dialog>

  </div>
</template>

<script>
import { listJob, getJob, delJob, addJob, updateJob, exportJob, runJob, changeJobStatus } from "@/api/monitor/job";

export default {
  name: "Job",
  data() {
    return {
      // 遮罩层
      loading: true,
      // 显示搜索条件
      showSearch: true,
      // 总条数
      total: 0,
      // 定时任务表格数据
      jobList: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 是否显示详细弹出层
      openView: false,
      // 状态字典
      statusOptions: [],
      // 查询参数
      queryParams: {
        pageNo: 1,
        pageSize: 10,
        name: undefined,
        status: undefined,
        handlerName: undefined
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        name: [{ required: true, message: "任务名称不能为空", trigger: "blur" }],
        handlerName: [{ required: true, message: "处理器的名字不能为空", trigger: "blur" }],
        cronExpression: [{ required: true, message: "CRON 表达式不能为空", trigger: "blur" }],
      }
    };
  },
  created() {
    this.getList();
  },
  methods: {
    /** 查询定时任务列表 */
    getList() {
      this.loading = true;
      listJob(this.queryParams).then(response => {
        this.jobList = response.data.list;
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
        name: undefined,
        handlerName: undefined,
        handlerParam: undefined,
        cronExpression: undefined,
        monitorTimeout: undefined,
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
      this.resetForm("queryForm");
      this.handleQuery();
    },
    // 任务状态修改
    handleStatusChange(row) {
      let text = row.status === "0" ? "启用" : "停用";
      this.$confirm('确认要"' + text + '""' + row.name + '"任务吗?', "警告", {
          confirmButtonText: "确定",
          cancelButtonText: "取消",
          type: "warning"
        }).then(function() {
          return changeJobStatus(row.id, row.status);
        }).then(() => {
          this.msgSuccess(text + "成功");
        }).catch(function() {
          row.status = row.status === "0" ? "1" : "0";
        });
    },
    /* 立即执行一次 */
    handleRun(row) {
      this.$confirm('确认要立即执行一次"' + row.name + '"任务吗?', "警告", {
          confirmButtonText: "确定",
          cancelButtonText: "取消",
          type: "warning"
        }).then(function() {
          return runJob(row.id, row.jobGroup);
        }).then(() => {
          this.msgSuccess("执行成功");
        })
    },
    /** 任务详细信息 */
    handleView(row) {
      getJob(row.id).then(response => {
        this.form = response.data;
        this.openView = true;
      });
    },
    /** 任务日志列表查询 */
    handleJobLog() {
      this.$router.push("/job/log");
    },
    /** 新增按钮操作 */
    handleAdd() {
      this.reset();
      this.open = true;
      this.title = "添加任务";
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset();
      const id = row.id;
      getJob(id).then(response => {
        this.form = response.data;
        this.open = true;
        this.title = "修改任务";
      });
    },
    /** 提交按钮 */
    submitForm: function() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.id !== undefined) {
            updateJob(this.form).then(response => {
              this.msgSuccess("修改成功");
              this.open = false;
              this.getList();
            });
          } else {
            addJob(this.form).then(response => {
              this.msgSuccess("新增成功");
              this.open = false;
              this.getList();
            });
          }
        }
      });
    },
    /** 删除按钮操作 */
    handleDelete(row) {
      const ids = row.id || this.ids;
      this.$confirm('是否确认删除定时任务编号为"' + ids + '"的数据项?', "警告", {
          confirmButtonText: "确定",
          cancelButtonText: "取消",
          type: "warning"
        }).then(function() {
          return delJob(ids);
        }).then(() => {
          this.getList();
          this.msgSuccess("删除成功");
        })
    },
    /** 导出按钮操作 */
    handleExport() {
      const queryParams = this.queryParams;
      this.$confirm("是否确认导出所有定时任务数据项?", "警告", {
          confirmButtonText: "确定",
          cancelButtonText: "取消",
          type: "warning"
        }).then(function() {
          return exportJob(queryParams);
        }).then(response => {
          this.download(response.msg);
        })
    }
  }
};
</script>
