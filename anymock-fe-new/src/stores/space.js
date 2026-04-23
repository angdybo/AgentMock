import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useSpaceStore = defineStore('space', () => {
  const spaceList = ref([])
  const loading = ref(false)

  const setSpaceList = (list) => {
    spaceList.value = list
  }

  return { spaceList, loading, setSpaceList }
})
