<template>
  <div class="app-container">
    <el-row>
      <el-col :span="24" class="card-box">
        <el-card>
          <div slot="header"><span>基本信息</span></div>
          <div class="el-table el-table--enable-row-hover el-table--medium">
            <table cellspacing="0" style="width: 100%">
              <tbody>
                <tr>
                  <td><div class="cell">Redis版本</div></td>
                  <td><div class="cell" v-if="cache.info">{{ cache.info.redis_version }}</div></td>
                  <td><div class="cell">运行模式</div></td>
                  <td><div class="cell" v-if="cache.info">{{ cache.info.redis_mode == "standalone" ? "单机" : "集群" }}</div></td>
                  <td><div class="cell">端口</div></td>
                  <td><div class="cell" v-if="cache.info">{{ cache.info.tcp_port }}</div></td>
                  <td><div class="cell">客户端数</div></td>
                  <td><div class="cell" v-if="cache.info">{{ cache.info.connected_clients }}</div></td>
                </tr>
                <tr>
                  <td><div class="cell">运行时间(天)</div></td>
                  <td><div class="cell" v-if="cache.info">{{ cache.info.uptime_in_days }}</div></td>
                  <td><div class="cell">使用内存</div></td>
                  <td><div class="cell" v-if="cache.info">{{ cache.info.used_memory_human }}</div></td>
                  <td><div class="cell">使用CPU</div></td>
                  <td><div class="cell" v-if="cache.info">{{ parseFloat(cache.info.used_cpu_user_children).toFixed(2) }}</div></td>
                  <td><div class="cell">内存配置</div></td>
                  <td><div class="cell" v-if="cache.info">{{ cache.info.maxmemory_human }}</div></td>
                </tr>
                <tr>
                  <td><div class="cell">AOF是否开启</div></td>
                  <td><div class="cell" v-if="cache.info">{{ cache.info.aof_enabled == "0" ? "否" : "是" }}</div></td>
                  <td><div class="cell">RDB是否成功</div></td>
                  <td><div class="cell" v-if="cache.info">{{ cache.info.rdb_last_bgsave_status }}</div></td>
                  <td><div class="cell">Key数量</div></td>
                  <td><div class="cell" v-if="cache.dbSize">{{ cache.dbSize }} </div></td>
                  <td><div class="cell">网络入口/出口</div></td>
                  <td><div class="cell" v-if="cache.info">{{ cache.info.instantaneous_input_kbps }}kps/{{cache.info.instantaneous_output_kbps}}kps</div></td>
                </tr>
              </tbody>
            </table>
          </div>
        </el-card>
      </el-col>

      <el-col :span="12" class="card-box">
        <el-card>
          <div slot="header"><span>命令统计</span></div>
          <div class="el-table el-table--enable-row-hover el-table--medium">
            <div ref="commandstats" style="height: 420px" />
          </div>
        </el-card>
      </el-col>

      <el-col :span="12" class="card-box">
        <el-card>
          <div slot="header">
            <span>内存信息</span>
          </div>
          <div class="el-table el-table--enable-row-hover el-table--medium">
            <div ref="usedmemory" style="height: 420px" />
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-table
        v-loading="keyListLoad"
        :data="keyList"
        row-key="id"
    >
      <el-table-column prop="keyTemplate" label="Key 模板" width="200" />
      <el-table-column prop="keyType" label="Key 类型" width="100" />
      <el-table-column prop="valueType" label="Value 类型" />
      <el-table-column prop="timeoutType" label="超时时间" width="150">
        <template slot-scope="scope">
          {{ getDictDataLabel(DICT_TYPE.INF_REDIS_TIMEOUT_TYPE, scope.row.timeoutType) }}
          <span v-if="scope.row.timeout > 0">({{ scope.row.timeout / 1000 }} 秒)</span>
        </template>
      </el-table-column>
      <el-table-column prop="memo" label="备注" />
    </el-table>
  </div>

</template>

<script>
import { getCache, getKeyList } from "@/api/infra/redis";
import echarts from "echarts";

export default {
  name: "Server",
  data() {
    return {
      // 加载层信息
      loading: [],
      // 统计命令信息
      commandstats: null,
      // 使用内存
      usedmemory: null,
      // cache 信息
      cache: [],
      // key 列表
      keyListLoad: true,
      keyList: [],
    };
  },
  created() {
    this.getList();
    this.openLoading();
  },
  methods: {
    /** 查缓存询信息 */
    getList() {
      // 查询 Redis 监控信息
      getCache().then((response) => {
        this.cache = response.data;
        this.loading.close();

        this.commandstats = echarts.init(this.$refs.commandstats, "macarons");
        const commandStats = [];
        response.data.commandStats.forEach(row => {
          commandStats.push({
            name: row.command,
            value: row.calls
          });
        })
        this.commandstats.setOption({
          tooltip: {
            trigger: "item",
            formatter: "{a} <br/>{b} : {c} ({d}%)",
          },
          series: [
            {
              name: "命令",
              type: "pie",
              roseType: "radius",
              radius: [15, 95],
              center: ["50%", "38%"],
              data: commandStats,
              animationEasing: "cubicInOut",
              animationDuration: 1000,
            },
          ],
        });
        this.usedmemory = echarts.init(this.$refs.usedmemory, "macarons");
        this.usedmemory.setOption({
          tooltip: {
            formatter: "{b} <br/>{a} : " + this.cache.info.used_memory_human,
          },
          series: [
            {
              name: "峰值",
              type: "gauge",
              min: 0,
              max: 1000,
              detail: {
                formatter: this.cache.info.used_memory_human,
              },
              data: [
                {
                  value: parseFloat(this.cache.info.used_memory_human),
                  name: "内存消耗",
                },
              ],
            },
          ],
        });
      });

      // 查询 Redis Key 列表
      getKeyList().then(response => {
        this.keyList = response.data;
        this.keyListLoad = false;
      });
    },
    // 打开加载层
    openLoading() {
      this.loading = this.$loading({
        lock: true,
        text: "拼命读取中",
        spinner: "el-icon-loading",
        background: "rgba(0, 0, 0, 0.7)",
      });
    },
  },
};
</script>
