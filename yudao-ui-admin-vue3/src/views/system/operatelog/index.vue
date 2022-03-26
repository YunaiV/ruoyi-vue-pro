<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="系统模块" prop="module">
        <el-input v-model="queryParams.module" placeholder="请输入系统模块" clearable style="width: 240px;"
                  @keyup.enter.native="handleQuery"/>
      </el-form-item>
      <el-form-item label="操作人员" prop="userNickname">
        <el-input v-model="queryParams.userNickname" placeholder="请输入操作人员" clearable style="width: 240px;"
                  @keyup.enter.native="handleQuery"/>
      </el-form-item>
      <el-form-item label="类型" prop="type">
        <el-select v-model="queryParams.type" placeholder="操作类型" clearable style="width: 240px">
          <el-option v-for="dict in proxy.getDictDatas(DICT_TYPE.SYSTEM_OPERATE_TYPE)" :key="parseInt(dict.value)"
                     :label="dict.label" :value="parseInt(dict.value)"/>
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.success" placeholder="操作状态" clearable style="width: 240px">
          <el-option :key="true" label="成功" :value="true"/>
          <el-option :key="false" label="失败" :value="false"/>
        </el-select>
      </el-form-item>
      <el-form-item label="操作时间">
        <el-date-picker v-model="dateRange" style="width: 240px" value-format="YYYY-MM-DD"
                        type="daterange" range-separator="-" start-placeholder="开始日期"
                        end-placeholder="结束日期"></el-date-picker>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="warning" icon="Download" @click="handleExport" :loading="exportLoading"
                   v-hasPermi="['system:operate-log:export']">导出
        </el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
    </el-row>

    <el-table v-loading="loading" :data="list">
      <el-table-column label="日志编号" align="center" prop="id"/>
      <el-table-column label="操作模块" align="center" prop="module"/>
      <el-table-column label="操作名" align="center" prop="name" width="180"/>
      <el-table-column label="操作类型" align="center" prop="type">
        <template #default="scope">
          <dict-tag :type="DICT_TYPE.SYSTEM_OPERATE_TYPE" :value="scope.row.type"/>
        </template>
      </el-table-column>
      <el-table-column label="操作人" align="center" prop="userNickname"/>
      <el-table-column label="操作结果" align="center" prop="status">
        <template #default="scope">
          <span>{{ scope.row.resultCode === 0 ? '成功' : '失败' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作日期" align="center" prop="startTime" width="180">
        <template #default="scope">
          <span>{{ parseTime(scope.row.startTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="执行时长" align="center" prop="startTime">
        <template #default="scope">
          <span>{{ scope.row.duration }}  ms</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button size="small" type="text" icon="View" @click="handleView(scope.row)"
                     v-hasPermi="['system:operate-log:query']">详细
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total>0" :total="total" v-model:page="queryParams.pageNo" v-model:limit="queryParams.pageSize"
                @pagination="getList"/>

    <!-- 操作日志详细 -->
    <el-dialog title="访问日志详细" v-model="open" width="700px" append-to-body>
      <el-form ref="form" :model="formData" label-width="100px" size="small">
        <el-row>
          <el-col :span="24">
            <el-form-item label="日志主键：">{{ formData.id }}</el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="链路追踪：">{{ formData.traceId }}</el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="用户信息：">{{ formData.userId }} | {{ formData.userNickname }} | {{ formData.userIp }} |
              {{ formData.userAgent }}
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="操作信息：">
              {{ formData.module }} | {{ formData.name }}
              <dict-tag :type="DICT_TYPE.SYSTEM_OPERATE_TYPE" :value="formData.type"/>
              <br/> {{ formData.content }}
              <br/> {{ formData.exts }}
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="请求信息：">{{ formData.requestMethod }} | {{ formData.requestUrl }}</el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="方法名：">{{ formData.javaMethod }}</el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="方法参数：">{{ formData.javaMethodArgs }}</el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="开始时间：">
              {{ proxy.parseTime(formData.startTime) }} | {{ formData.duration }} ms
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="操作结果：">
              <div v-if="formData.resultCode === 0">正常 | {{ formData.resultData }}</div>
              <div v-else-if="formData.resultCode > 0">失败 | {{ formData.resultCode }} || {{ formData.resultMsg }}</div>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="open = false">关 闭</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script setup name="Operlog">
import {listOperateLog, exportOperateLog} from "@/api/system/operatelog";

const {proxy} = getCurrentInstance();

const loading = ref(true);// 遮罩层
const exportLoading = ref(false);// 导出遮罩层
const showSearch = ref(true);// 显示搜索条件
const total = ref(0);// 总条数
const list = ref([]);// 表格数据
const open = ref(false);// 是否显示弹出层
const dateRange = ref([]);// 日期范围
const data = reactive({
  // 表单参数
  formData: {},
  // 查询参数
  queryParams: {
    pageNo: 1,
    pageSize: 10,
    module: undefined,
    userNickname: undefined,
    type: undefined,
    status: undefined
  }
});
const {formData, queryParams} = toRefs(data);

/** 查询登录日志 */
function getList() {
  loading.value = true;
  listOperateLog(proxy.addDateRange(queryParams.value, [
    dateRange.value[0] ? dateRange.value[0] + ' 00:00:00' : undefined,
    dateRange.value[1] ? dateRange.value[1] + ' 23:59:59' : undefined,
  ])).then(response => {
        list.value = response.data.list;
        total.value = response.data.total;
        loading.value = false;
      }
  );
}

/** 搜索按钮操作 */
function handleQuery() {
  queryParams.value.pageNo = 1;
  getList();
}

/** 重置按钮操作 */
function resetQuery() {
  dateRange.value = [];
  proxy.resetForm("queryForm");
  handleQuery();
}

/** 详细按钮操作 */
function handleView(row) {

  open.value = true;
  formData.value = row;
}

/** 导出按钮操作 */
function handleExport() {
  proxy.$modal.confirm('是否确认导出所有操作日志数据项?').then(() => {
    exportLoading.value = true;
    return exportOperateLog(proxy.addDateRange(queryParams.value, [
      dateRange.value[0] ? dateRange.value[0] + ' 00:00:00' : undefined,
      dateRange.value[1] ? dateRange.value[1] + ' 23:59:59' : undefined,
    ]));
  }).then(response => {
    proxy.$download.excel(response, '操作日志.xls');
    exportLoading.value = false;
  }).catch(() => {
  });
}

getList();
</script>

