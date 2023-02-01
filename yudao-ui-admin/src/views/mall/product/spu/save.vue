<template>
  <div class="container">
    <!-- TODO 样式优化：表单宽度、表单项对齐、hr 粗细； -->
    <el-tabs v-model="activeName" class="tabs">
      <!-- 基础设置 -->
      <!-- TODO @luowenfeng：基础设置，分成基础信息、配送信息 -->
      <el-tab-pane label="基础设置" name="basic">
        <el-form ref="basic" :model="baseForm" :rules="rules" label-width="100px" style="width: 95%">
          <el-form-item label="商品名称" prop="name">
            <el-input v-model="baseForm.name" placeholder="请输入商品名称" />
          </el-form-item>
          <el-form-item label="促销语">
            <el-input type="textarea" v-model="baseForm.sellPoint" placeholder="请输入促销语"/>
          </el-form-item>
          <el-form-item label="商品主图" prop="picUrls">
            <ImageUpload v-model="baseForm.picUrls" :value="baseForm.picUrls" :limit="10" class="mall-image"/>
          </el-form-item>
          <el-form-item label="商品视频" prop="videoUrl">
            <VideoUpload v-model="baseForm.videoUrl" :value="baseForm.videoUrl"/>
          </el-form-item>
          <el-form-item label="商品品牌" prop="brandId">
            <el-select v-model="baseForm.brandId" placeholder="请选择商品品牌">
              <el-option v-for="item in brandList" :key="item.id" :label="item.name" :value="item.id"/>
            </el-select>
          </el-form-item>
          <el-form-item label="商品分类" prop="categoryIds">
            <el-cascader v-model="baseForm.categoryIds" placeholder="商品分类" style="width: 100%"
                         :options="categoryList" :props="propName" clearable/>
          </el-form-item>
          <el-form-item label="是否上架" prop="status">
            <el-radio-group v-model="baseForm.status">
              <el-radio :label="1">立即上架</el-radio>
              <el-radio :label="0">放入仓库</el-radio>
            </el-radio-group>
          </el-form-item>
        </el-form>
      </el-tab-pane>

      <!-- 价格库存 -->
      <!-- TODO @luowenfeng：rates=》priceStack 会更好哈 -->
      <el-tab-pane label="价格库存" name="rates" class="rates">
        <el-form ref="rates" :model="ratesForm" :rules="rules">
          <el-form-item label="启用多规格">
            <el-switch v-model="specSwitch" @change="changeSpecSwitch"/>
          </el-form-item>
          <!-- 动态添加规格属性 -->
          <div v-show="ratesForm.spec === 2">
            <div v-for="(specs, index) in dynamicSpec" :key="index" class="dynamic-spec">
              <!-- 删除按钮 -->
              <el-button type="danger" icon="el-icon-delete" circle class="spec-delete" @click="removeSpec(index)"/>
              <div class="spec-header">
                规格项：
                <el-select v-model="specs.specId" filterable placeholder="请选择" @change="changeSpec">
                  <el-option v-for="item in propertyPageList" :key="item.id" :label="item.name" :value="item.id"/>
                </el-select>
              </div>
              <div class="spec-values">
                <template v-for="(v, i) in specs.specValue">
                  <el-input v-model="v.name" class="spec-value" :key="i" disabled/>
                </template>
              </div>
            </div>
            <el-button type="primary" @click="dynamicSpec.push({specValue: []}); ratesForm.rates = []">添加规格项目</el-button>
          </div>

          <!-- 规格明细 -->
          <el-form-item label="规格明细">
            <el-table :data="ratesForm.rates" border style="width: 100%" ref="ratesTable">
              <template v-if="this.specSwitch">
                <el-table-column :key="index" v-for="(item, index) in dynamicSpec.filter(v => v.specName !== undefined)"
                                 :label="item.specName">
                  <template v-slot="scope">
                    <el-input v-if="scope.row.spec" v-model="scope.row.spec[index]" disabled/>
                  </template>
                </el-table-column>
              </template>
              <el-table-column label="规格图片" width="120px" :render-header="addRedStar" key="90">
                <template v-slot="scope">
                  <ImageUpload v-model="scope.row.picUrl" :limit="1" :isShowTip="false" style="width: 100px; height: 50px"/>
                </template>
              </el-table-column>
              <el-table-column label="市场价(元)" :render-header="addRedStar" key="92">
                <template v-slot="scope">
                  <el-form-item :prop="'rates.'+ scope.$index + '.marketPrice'" :rules="[{required: true, trigger: 'change'}]">
                    <el-input v-model="scope.row.marketPrice"
                              oninput="value= value.match(/\d+(\.\d{0,2})?/) ? value.match(/\d+(\.\d{0,2})?/)[0] : ''"/>
                  </el-form-item>
                </template>
              </el-table-column>
              <el-table-column label="销售价(元)" :render-header="addRedStar" key="93">
                <template v-slot="scope">
                  <el-form-item :prop="'rates.'+ scope.$index + '.price'"
                                :rules="[{required: true, trigger: 'change'}]">
                    <el-input v-model="scope.row.price"
                              oninput="value= value.match(/\d+(\.\d{0,2})?/) ? value.match(/\d+(\.\d{0,2})?/)[0] : ''" />
                  </el-form-item>
                </template>
              </el-table-column>
              <el-table-column label="成本价" :render-header="addRedStar" key="94">
                <template v-slot="scope">
                  <el-form-item :prop="'rates.'+ scope.$index + '.costPrice'"
                                :rules="[{required: true, trigger: 'change'}]">
                    <el-input v-model="scope.row.costPrice"
                              oninput="value= value.match(/\d+(\.\d{0,2})?/) ? value.match(/\d+(\.\d{0,2})?/)[0] : ''" />
                  </el-form-item>
                </template>
              </el-table-column>
              <el-table-column label="库存" :render-header="addRedStar" key="95">
                <template v-slot="scope">
                  <el-form-item :prop="'rates.'+ scope.$index + '.stock'" :rules="[{required: true, trigger: 'change'}]">
                    <el-input v-model="scope.row.stock" oninput="value=value.replace(/^(0+)|[^\d]+/g,'')"></el-input>
                  </el-form-item>
                </template>
              </el-table-column>
              <el-table-column label="预警库存" key="96">
                <template v-slot="scope">
                  <el-input v-model="scope.row.warnStock" oninput="value=value.replace(/^(0+)|[^\d]+/g,'')"></el-input>
                </template>
              </el-table-column>
              <el-table-column label="体积" key="97">
                <template v-slot="scope">
                  <el-input v-model="scope.row.volume" />
                </template>
              </el-table-column>
              <el-table-column label="重量" key="98">
                <template v-slot="scope">
                  <el-input v-model="scope.row.weight" />
                </template>
              </el-table-column>
              <el-table-column label="条码" key="99">
                <template v-slot="scope">
                  <el-input v-model="scope.row.barCode" />
                </template>
              </el-table-column>
              <template v-if="this.specSwitch">
                <el-table-column fixed="right" label="操作" width="50" key="100">
                  <template v-slot="scope">
                    <el-button @click="scope.row.status = 1" type="text" size="small"
                               v-show="scope.row.status === undefined || scope.row.status === 0 ">禁用
                    </el-button>
                    <el-button @click="scope.row.status = 0" type="text" size="small" v-show="scope.row.status === 1">
                      启用
                    </el-button>
                  </template>
                </el-table-column>
              </template>
            </el-table>
          </el-form-item>
          <el-form-item label="虚拟销量" prop="virtualSalesCount">
            <el-input v-model="baseForm.virtualSalesCount" placeholder="请输入虚拟销量"
                      oninput="value=value.replace(/^(0+)|[^\d]+/g,'')"/>
          </el-form-item>
        </el-form>
      </el-tab-pane>

      <!-- 商品详情 -->
      <el-tab-pane label="商品详情" name="detail">
        <el-form ref="detail" :model="baseForm" :rules="rules">
          <el-form-item prop="description">
            <editor v-model="baseForm.description" :min-height="380"/>
          </el-form-item>
        </el-form>
      </el-tab-pane>

      <!-- 销售设置 -->
      <el-tab-pane label="高级设置" name="senior">
        <el-form ref="senior" :model="baseForm" :rules="rules" label-width="100px" style="width: 95%">
          <el-form-item label="排序字段">
            <el-input v-model="baseForm.sort" placeholder="请输入排序字段" oninput="value=value.replace(/^(0+)|[^\d]+/g,'')"/>
          </el-form-item>
          <el-form-item label="是否展示库存" prop="showStock">
            <el-radio-group v-model="baseForm.showStock">
              <el-radio :label="true">是</el-radio>
              <el-radio :label="false">否</el-radio>
            </el-radio-group>
          </el-form-item>
        </el-form>
      </el-tab-pane>
    </el-tabs>

    <div class="buttons">
      <el-button type="info" round @click="cancel">取消</el-button>
      <el-button type="success" round @click="submit">确认</el-button>
    </div>

  </div>
</template>

<script>

import {getBrandList} from "@/api/mall/product/brand";
import {getProductCategoryList} from "@/api/mall/product/category";
import {createSpu, getSpuDetail, updateSpu} from "@/api/mall/product/spu";
import {getPropertyListAndValue,} from "@/api/mall/product/property";
import Editor from "@/components/Editor";
import ImageUpload from "@/components/ImageUpload";
import VideoUpload from "@/components/VideoUpload";

export default {
  components: {
    Editor,
    ImageUpload,
    VideoUpload
  },
  data() {
    return {
      specSwitch: false,
      activeName: "basic",
      propName: {
        checkStrictly: true,
        label: "name",
        value: "id",
      },
      // 基础设置
      baseForm: {
        id: null,
        name: null,
        sellPoint: null,
        categoryIds: null,
        sort: null,
        description: null,
        picUrls: null,
        videoUrl: null,
        status: 0,
        virtualSalesCount: 0,
        showStock: true,
        brandId: null,
      },

      categoryList: [],
      // 价格库存
      ratesForm: {
        spec: 1,
        // 规格明细
        rates: [{}]
      },
      dynamicSpec: [
        // {
        //   specId: 86,
        //   specName: "颜色",
        //   specValue:[{
        //      name: "红色",
        //      id: 225,
        //   }]
        // },
      ],
      propertyPageList: [],
      brandList: [],
      specValue: null,

      // 表单校验
      rules: {
        name: [{required: true, message: "商品名称不能为空", trigger: "blur"},],
        description: [{required: true, message: "描述不能为空", trigger: "blur"},],
        categoryIds: [{required: true, message: "分类id不能为空", trigger: "blur"},],
        status: [{required: true, message: "商品状态不能为空", trigger: "blur"}],
        brandId: [{required: true, message: "商品品牌不能为空", trigger: "blur"}],
        picUrls: [{required: true, message: "商品轮播图地址不能为空", trigger: "blur"}],
      },
    };
  },
  created() {
    this.getListBrand();
    this.getListCategory();
    this.getPropertyPageList();
    const spuId = this.$route.params && this.$route.params.spuId;
    if (spuId != null) {
      this.updateType(spuId)
    }
  },
  methods: {
    removeSpec(index) {
      this.dynamicSpec.splice(index, 1);
      this.changeSpecSwitch()
    },
    // 必选标识
    addRedStar(h, {column}) {
      return [
        h('span', {style: 'color: #F56C6C'}, '*'),
        h('span', ' ' + column.label)
      ];
    },
    changeSpecSwitch() {
      this.specSwitch ? this.ratesForm.spec = 2 : this.ratesForm.spec = 1;
      this.$refs.ratesTable.doLayout();
      if (this.ratesForm.spec === 1) {
        this.ratesForm.rates = [{}]
      } else {
        this.ratesForm.rates = []
        if (this.dynamicSpec.length > 0) {
          this.buildRatesFormRates()
        }
      }
    },
    // 构建规格明细笛卡尔积
    buildRatesFormRates() {
      let rates = [];
      this.dynamicSpec.map(v => v.specValue.map(m => m.name))
        .reduce((last, current) => {
          const array = [];
          last.forEach(par1 => {
            current.forEach(par2 => {
              let v
              // 当两个对象合并时，需使用[1,2]方式生成数组，而当数组和对象合并时，需使用concat
              if (par1 instanceof Array) {
                v = par1.concat(par2)
              } else {
                v = [par1, par2];
              }
              array.push(v)
            });
          });
          return array;
        })
        .forEach(v => {
          let spec = v;
          // 当v为单个规格项时，会变成字符串。造成表格只截取第一个字符串，而不是数组的第一个元素
          if (typeof v == 'string') {
            spec = Array.of(v)
          }
          rates.push({spec: spec, status: 0, name: Array.of(v).join()})
        });
      this.ratesForm.rates = rates
    },
    /** 查询分类 */
    getListCategory() {
      // 执行查询
      getProductCategoryList().then((response) => {
        this.categoryList = this.handleTree(response.data, "id", "parentId");
      });
    },
    /** 查询品牌列表 */
    getListBrand() {
      // 执行查询
      getBrandList().then((response) => {
        this.brandList = response.data;
      });
    },
    // 取消按钮
    cancel() {
      var currentView = this.$store.state.tagsView.visitedViews[0]
      for (currentView of this.$store.state.tagsView.visitedViews) {
        if (currentView.path === this.$route.path) {
          break
        }
      }
      this.$store.dispatch('tagsView/delView', currentView)
        .then(() => {
          this.$router.push("/product/spu")
        })
    },
    submit() {
      this.$refs[this.activeName].validate((valid) => {
        if (!valid) {
          return;
        }
        let rates = JSON.parse(JSON.stringify(this.ratesForm.rates));

        // 价格元转分
        rates.forEach(r => {
          r.marketPrice = r.marketPrice * 100;
          r.price = r.price * 100;
          r.costPrice = r.costPrice * 100;
        })

        // 动态规格调整字段
        if (this.specSwitch) {
          rates.forEach(r => {
            let properties = []
            Array.of(r.spec).forEach(s => {
              let obj;
              if (s instanceof Array) {
                obj = s;
              } else {
                obj = Array.of(s);
              }
              obj.forEach((v, i) => {
                let specValue = this.dynamicSpec[i].specValue.find(o => o.name === v);
                let propertie = {};
                propertie.propertyId = this.dynamicSpec[i].specId;
                propertie.valueId = specValue.id;
                properties.push(propertie);
              })
            })
            r.properties = properties;
          })
        } else {
          rates[0].name = this.baseForm.name;
          rates[0].status = this.baseForm.status;
        }
        let form = this.baseForm
        if (form.picUrls instanceof Array) {
          form.picUrls = form.picUrls.flatMap(m => m.split(','))
        } else if (form.picUrls.split(',') instanceof Array) {
          form.picUrls = form.picUrls.split(',').flatMap(m => m.split(','))
        } else {
          form.picUrls = Array.of(form.picUrls)
        }
        form.skus = rates;
        form.specType = this.ratesForm.spec;

        let category = form.categoryIds instanceof Array ? form.categoryIds: Array.of(form.categoryIds)
        console.log(category)
        form.categoryId = category[category.length - 1];

        if (form.id == null) {
          createSpu(form).then(() => {
            this.$modal.msgSuccess("新增成功");
          }).then(()=>{
            this.cancel();
          })
        } else {
          updateSpu(form).then(() => {
            this.$modal.msgSuccess("修改成功");
          }).then(()=>{
            this.cancel();
          })
        }
      });

    },
    /** 查询规格 */
    getPropertyPageList() {
      // 执行查询
      getPropertyListAndValue().then((response) => {
        this.propertyPageList = response.data;
      });
    },
    // 添加规格项目
    changeSpec(val) {
      let obj = this.propertyPageList.find(o => o.id === val);
      let spec = this.dynamicSpec.find(o => o.specId === val)
      spec.specId = obj.id;
      spec.specName = obj.name;
      spec.specValue = obj.values;
      this.buildRatesFormRates();
    },
    updateType(id) {
      getSpuDetail(id).then((response) => {
        let data = response.data;
        this.baseForm.id = data.id;
        this.baseForm.name = data.name;
        this.baseForm.sellPoint = data.sellPoint;
        this.baseForm.categoryIds = data.categoryId;
        this.baseForm.videoUrl = data.videoUrl;
        this.baseForm.sort = data.sort;
        this.baseForm.description = data.description;
        this.baseForm.picUrls = data.picUrls;
        this.baseForm.status = data.status;
        this.baseForm.virtualSalesCount = data.virtualSalesCount;
        this.baseForm.showStock = data.showStock;
        this.baseForm.brandId = data.brandId;
        this.ratesForm.spec = data.specType;
        data.skus.forEach(r => {
          r.marketPrice = this.divide(r.marketPrice, 100)
          r.price = this.divide(r.price, 100)
          r.costPrice = this.divide(r.costPrice, 100)
        })
        if (this.ratesForm.spec === 2) {
          this.specSwitch = true;
          data.productPropertyViews.forEach(p => {
            let obj = {};
            obj.specId = p.propertyId;
            obj.specName = p.name;
            obj.specValue = p.propertyValues;
            this.dynamicSpec.push(obj);
          })
          data.skus.forEach(s => {
            s.spec = [];
            s.properties.forEach(sp => {
              let spec = data.productPropertyViews.find(o => o.propertyId === sp.propertyId).propertyValues.find(v => v.id === sp.valueId).name;
              s.spec.push(spec)
            })
          })
        }
        this.ratesForm.rates = data.skus
      })
    },

  },
};
</script>

<style lang="scss">
.container{
  padding: 20px;
}

.dynamic-spec {
  background-color: #f2f2f2;
  width: 85%;
  margin: auto;
  margin-bottom: 10px;

  .spec-header {
    padding: 30px;
    padding-bottom: 20px;

    .spec-name {
      display: inline;

      input {
        width: 30%;
      }
    }
  }

  .spec-values {
    width: 84%;
    padding: 25px;
    margin: auto;
    padding-top: 5px;

    .spec-value {
      display: inline-block;
      margin-right: 10px;
      margin-bottom: 10px;
      width: 13%;

    }
  }

  .spec-delete {
    float: right;
    margin-top: 10px;
    margin-right: 10px;
  }
}

.tabs {
  border-bottom: 2px solid #f2f2f2;

  .el-tab-pane {
    overflow-y: auto;
  }
}

// 库存价格图片样式修改
.rates {
  .component-upload-image {
    margin: auto;
  }

  .el-upload--picture-card {
    width: 100px;
    height: 50px;
    line-height: 60px;
    margin: auto;
  }

  .el-upload-list__item {
    width: 100px !important;
    height: 50px !important;
  }
}

.buttons {
  margin-top: 20px;
  height: 36px;

  button {
    float: right;
    margin-left: 15px;
  }
}

.mall-image {
  .el-upload--picture-card {
    width: 80px;
    height: 80px;
    line-height: 90px;
  }

  .el-upload-list__item {
    width: 80px;
    height: 80px;
  }
}

</style>
