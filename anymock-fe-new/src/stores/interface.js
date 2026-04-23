import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useInterfaceStore = defineStore('interface', () => {
  const list = ref([])
  const total = ref(0)
  const loading = ref(false)
  const pagination = ref({ page: 1, pageSize: 10 })

  const setList = (data) => {
    list.value = data
  }

  const setTotal = (n) => {
    total.value = n
  }

  return { list, total, loading, pagination, setList, setTotal }
})
