<script setup lang="ts">
import type { GroupDetail } from '@/groups/group'
import groupsService from '@/groups/groups.service'
import { TimetableStatus } from '@/timetable/TimetableStatus'
import { dayOfWeeks, type Timeslot } from '@/timeslots/Timeslot'
import timeslotsService from '@/timeslots/timeslots.service'
import timetableService from '@/timetable/timetable.service'
import { onUnmounted } from 'vue'
import { watch } from 'vue'
import { computed } from 'vue'
import { onMounted, ref } from 'vue'

const groups = ref<GroupDetail[]>([])
const timeslots = ref<Timeslot[]>([])
const status = ref<TimetableStatus>(TimetableStatus.NOT_SOLVING)
const solving = computed(() => status.value !== TimetableStatus.NOT_SOLVING)
watch(solving, (value) => (value ? startUpdating() : stopUpdating()))
const timeslotsHeaders = computed(() => {
  const map = timeslots.value
    .reduce((map, item) => {
      const tmslts = map.get(item.dayOfWeek) ?? []
      map.set(item.dayOfWeek, [...tmslts, item as Timeslot])
      return map
    }, new Map<number, Timeslot[]>())
    .entries()
  const timeslotsHeaders = []
  for (const [key, value] of map) {
    timeslotsHeaders.push({
      title: dayOfWeeks.find((item) => item.id === key)?.label ?? '',
      align: 'center',
      headerProps: { class: 'border' },
      children: value.map((item) => ({
        title: `${item.start} - ${item.end}`,
        headerProps: { class: 'border' },
        minWidth: '200px',
        align: 'center'
      }))
    })
  }
  return timeslotsHeaders
})
const headers = computed<any>(() => [
  { title: 'Класс', value: 'group', headerProps: { class: 'border' } },
  {
    title: 'Без времени',
    value: 'no-time',
    headerProps: { class: 'border' },
    minWidth: '200px',
    align: 'center'
  },
  ...timeslotsHeaders.value
])

const getSubjectColor = (subject: string) => {
  const hash = new TextEncoder()
    .encode(subject)
    .reduce((h, b) => ((h << 5) - h + b) & 0xffffffff, 0)

  const red = (hash & 0xff0000) >> 16
  const green = (hash & 0x00ff00) >> 8
  const blue = hash & 0x0000ff

  const clampedRed = Math.min(Math.max(red, 0), 255)
  const clampedGreen = Math.min(Math.max(green, 0), 255)
  const clampedBlue = Math.min(Math.max(blue, 0), 255)

  return `#${clampedRed.toString(16).padStart(2, '0')}${clampedGreen
    .toString(16)
    .padStart(2, '0')}${clampedBlue.toString(16).padStart(2, '0')}`
}

const update = () => {
  groupsService
    .allDetails()
    .then((value) => (groups.value = value))
    .catch((error) => console.error(error))

  timeslotsService
    .all()
    .then((value) => (timeslots.value = value))
    .catch((error) => console.error(error))

  timetableService
    .status()
    .then((value) => (status.value = value))
    .catch((error) => console.error(error))
}

const updateInterval = ref<number>()
const startUpdating = () => (updateInterval.value = setInterval(update, 2000))
const stopUpdating = () => clearInterval(updateInterval.value)

const start = () =>
  timetableService
    .start()
    .then(() => update())
    .catch((error) => console.error(error))

const stop = () =>
  timetableService
    .stop()
    .then(() => update())
    .catch((error) => console.error(error))

onMounted(() => update())
onUnmounted(() => stopUpdating())
</script>
<template>
  <v-data-table
    :items="groups"
    :headers="headers"
    :cell-props="() => ({ class: 'border' })"
    fixed-header
    height="650px"
  >
    <template v-slot:top>
      <v-toolbar flat>
        <v-toolbar-title>Расписание</v-toolbar-title>
        <v-divider class="mx-4" inset vertical></v-divider>
        <v-spacer></v-spacer>
        <v-btn
          :color="solving ? 'error' : 'success'"
          variant="outlined"
          dark
          @click="solving ? stop() : start()"
        >
          {{ `${solving ? 'Остановить' : 'Начать'} решение` }}
        </v-btn>
      </v-toolbar>
    </template>
    <template v-slot:item="{ item }">
      <tr>
        <td class="pinned border">{{ `${item.number} ${item.letter}` }}</td>
        <td class="border">
          <div
            v-for="lesson in item.lessons.filter((value) => !value.timeslot)"
            :key="lesson.id"
            class="mb-2"
          >
            <v-card :color="getSubjectColor(lesson.subject.name)" class="px-2 py-2">
              <div>{{ `${lesson.subject.name}` }}</div>
              <div>{{ `${lesson.teacher.lastname} ${lesson.teacher.firstname}` }}</div>
              <div>{{ `${lesson.room.name}` }}</div>
            </v-card>
          </div>
        </td>
        <td v-for="timeslot in timeslots" :key="timeslot.id" class="border">
          <div
            v-for="lesson in item.lessons.filter((value) => value.timeslot?.id === timeslot.id)"
            :key="lesson.id"
            class="mb-2"
          >
            <v-card :color="getSubjectColor(lesson.subject.name)" class="px-2 py-2 my-2">
              <div>{{ `${lesson.subject.name}` }}</div>
              <div>{{ `${lesson.teacher.lastname} ${lesson.teacher.firstname.charAt(0)}.` }}</div>
              <div>{{ `${lesson.room.name}` }}</div>
            </v-card>
          </div>
        </td>
      </tr>
    </template>
    <template v-slot:no-data> Пока здесь нет расписания... </template>
  </v-data-table>
</template>
<style scoped>
.pinned {
  position: sticky;
  left: 0;
  z-index: 1;
  background-color: white;
}
.pinned::after {
  content: '';
  position: absolute;
  width: 100%;
  height: 100%;
  right: 0;
  top: 0;
  box-shadow: 0 5px 10px rgba(0, 0, 0, 0.8);
}
</style>
