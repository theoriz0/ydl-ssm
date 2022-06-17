<template>
  <el-form :inline="true" :model="formInline" class="demo-form-inline">
    <el-form-item label="Approved by">
      <el-input v-model="formInline.user" placeholder="Approved by" />
    </el-form-item>
    <el-form-item label="Activity zone">
      <el-select v-model="formInline.region" placeholder="Activity zone">
        <el-option label="Zone one" value="shanghai" />
        <el-option label="Zone two" value="beijing" />
      </el-select>
    </el-form-item>
    <el-form-item>
      <el-button type="primary" @click="onSubmit">Query</el-button>
    </el-form-item>
  </el-form>

  <el-table ref="tableRef" row-key="roleId" :data="tableData" style="width: 100%">
    <el-table-column prop="roleId" label="RoleId" width="180" />
    <el-table-column prop="roleName" label="RoleName" />
  </el-table>
  <el-pagination background layout="prev, pager, next" :total="total" @update:current-page="changePage"/>
</template>

<script setup>
import { ref } from 'vue'
import { ElTable } from 'element-plus'
import { listRole } from '@/api/role';
import { onMounted } from 'vue';
import { reactive } from 'vue'

const tableData = ref([])
const total = ref(0)
const currentPage = ref(1)

onMounted(() => {
    listRole({size: 10}).then(res => {
        tableData.value = res.data.content
        total.value = res.data.totalElements
    })}
)

const changePage = function(current){
    listRole({page: current - 1 ,size: 10}).then(res => {
        tableData.value = res.data.content
        total.value = res.data.totalElements
    })
}

const formInline = reactive({
  user: '',
  region: '',
})

const onSubmit = () => {
  console.log('submit!')
}
</script>
