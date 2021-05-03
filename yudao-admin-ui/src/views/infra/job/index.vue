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
                   v-hasPermi="['infra:job:create']">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" icon="el-icon-download" size="mini" @click="handleExport"
                   v-hasPermi="['infra:job:export']">导出</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="info" icon="el-icon-s-operation" size="mini" @click="handleJobLog"
                   v-hasPermi="['infra:job:query']">执行日志</el-button>
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
                     v-hasPermi="['infra:job:query']">详细</el-button>
          <el-button size="mini" icon="el-icon-s-operation" @click="handleJobLog(scope.row)"
                     v-hasPermi="['infra:job:query']">执行日志</el-button>
          <el-button size="mini" type="text" icon="el-icon-edit" @click="handleUpdate(scope.row)"
                     v-hasPermi="['infra:job:update']">修改</el-button>
          <el-button size="mini" type="text" icon="el-icon-check" @click="handleChangeStatus(scope.row, true)"
                     v-if="scope.row.status === InfJobStatusEnum.STOP" v-hasPermi="['infra:job:update']">开启</el-button>
          <el-button size="mini" type="text" icon="el-icon-close" @click="handleChangeStatus(scope.row, false)"
                     v-if="scope.row.status === InfJobStatusEnum.NORMAL" v-hasPermi="['infra:job:update']">暂停</el-button>
          <el-button size="mini" type="text" icon="el-icon-caret-right" @click="handleRun(scope.row)"
                     v-hasPermi="['infra:job:trigger']">执行一次</el-button>
          <el-button size="mini" type="text" icon="el-icon-delete" @click="handleDelete(scope.row)"
                     v-hasPermi="['infra:job:delete']">删除</el-button>
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
        <el-form-item label="重试次数" prop="retryCount">
          <el-input v-model="form.retryCount" placeholder="请输入重试次数。设置为 0 时，不进行重试" />
        </el-form-item>
        <el-form-item label="重试间隔" prop="retryInterval">
          <el-input v-model="form.retryInterval" placeholder="请输入重试间隔，单位：毫秒。设置为 0 时，无需间隔" />
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
            <el-form-item label="重试次数：">{{ form.retryCount }}</el-form-item>
            <el-form-item label="重试间隔：">{{ form.retryInterval + " 毫秒" }}</el-form-item>
            <el-form-item label="监控超时时间：">{{ form.monitorTimeout > 0 ? form.monitorTimeout + " 毫秒" : "未开启" }}</el-form-item>
            <el-form-item label="后续执行时间：">{{ Array.from(nextTimes, x => parseTime(x)).join('; ')}}</el-form-item>
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
import { listJob, getJob, delJob, addJob, updateJob, exportJob, runJob, updateJobStatus, getJobNextTimes } from "@/api/infra/job";
import { InfJobStatusEnum } from "@/utils/constants";

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
        retryCount: [{ required: true, message: "重试次数不能为空", trigger: "blur" }],
        retryInterval: [{ required: true, message: "重试间隔不能为空", trigger: "blur" }],
      },
      nextTimes: [], // 后续执行时间

      // 枚举
      InfJobStatusEnum: InfJobStatusEnum
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
        retryCount: undefined,
        retryInterval: undefined,
        monitorTimeout: undefined,
      };
      this.nextTimes = [];
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
    /** 立即执行一次 **/
    handleRun(row) {
      this.$confirm('确认要立即执行一次"' + row.name + '"任务吗?', "警告", {
          confirmButtonText: "确定",
          cancelButtonText: "取消",
          type: "warning"
        }).then(function() {
          return runJob(row.id);
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
      // 获取下一次执行时间
      getJobNextTimes(row.id).then(response => {
        this.nextTimes = response.data;
      });
    },
    /** 任务日志列表查询 */
    handleJobLog(row) {
      if (row.id) {
        this.$router.push({
          path:"/job/log",
          query:{
            jobId: row.id
          }
        });
      } else {
        this.$router.push("/job/log");
      }
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
      const ids = row.id;
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
    /** 更新状态操作 */
    handleChangeStatus(row, open) {
      const id = row.id;
      let status = open ? InfJobStatusEnum.NORMAL : InfJobStatusEnum.STOP;
      let statusStr = open ? '开启' : '关闭';
      this.$confirm('是否确认' + statusStr + '定时任务编号为"' + id + '"的数据项?', "警告", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning"
      }).then(function() {
        return updateJobStatus(id, status);
      }).then(() => {
        this.getList();
        this.msgSuccess(statusStr + "成功");
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
          this.downloadExcel(response, '定时任务.xls');
        })
    }
  }
};
</script>
