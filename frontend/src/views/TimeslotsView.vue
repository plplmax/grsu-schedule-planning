<script setup lang="ts">
import { onMounted, ref } from 'vue'
import axios from 'axios'
import type { Timeslots } from '@/timeslots/Timeslots'
import type { Timeslot } from '@/timeslots/Timeslot'
import type { TimeslotsResponse } from '@/timeslots/TimeslotsResponse'


const timeslots = ref<Timeslots>({})

const timeOptions: Intl.DateTimeFormatOptions = { hour: 'numeric', minute: 'numeric' }

const loadTimeslots = () => {
  axios
    .get<TimeslotsResponse>('/api/timeslots')
    .then((response) => {
      const tmslts: TimeslotsResponse = response.data
      const result: Timeslots = {}
      for (const tmslt in tmslts) {
        result[tmslt] = tmslts[tmslt].map(
          (value: any) =>
          ({
            id: value.id,
            start: new Date(value.start),
            end: new Date(value.end)
          })
        )
      }
      timeslots.value = result
    })
    .catch((error) => console.error(error))
}

const deleteById = (id: number) => {
  axios
    .delete(`/api/timeslots/${id}`)
    .then(() => loadTimeslots())
    .catch((error) => console.error(error))
}

const deleteByDate = (date: string) => {
  const ids = timeslots.value[date].map((item: Timeslot) => item.id)
  axios
    .delete('/api/timeslots', { params: { ids: ids }, paramsSerializer: { indexes: null } })
    .then(() => loadTimeslots())
    .catch((error) => console.error(error))
}

onMounted(() => loadTimeslots())
</script>

<template>
  <div v-if="timeslots" class="grid grid-cols-6 gap-4 items-start">
    <div v-for="(value, key) in timeslots" :key="key">
      <div class="border-solid border-2">
        <div class="flex items-center">
          <p>{{ new Date(key).toLocaleDateString() }}</p>
          <font-awesome-icon :icon="['fas', 'xmark']" @click="deleteByDate(key as string)"
            class="hover:cursor-pointer ml-2" />
        </div>
        <div v-for="item in value" :key="item.id">
          <div class="bg-slate-200 p-1 flex items-center">
            <p>
              {{ item.start.toLocaleTimeString(undefined, timeOptions) }} -
              {{ item.end.toLocaleTimeString(undefined, timeOptions) }}
            </p>
            <font-awesome-icon :icon="['fas', 'xmark']" @click="deleteById(item.id)"
              class="hover:cursor-pointer ml-2" />
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
