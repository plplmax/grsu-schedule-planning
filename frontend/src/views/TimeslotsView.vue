<script setup lang="ts">
import type { DayOfWeek, Timeslot } from '@/timeslots/Timeslot'
import { ref } from 'vue'
import { LocalTime } from '@js-joda/core'
import { onMounted } from 'vue'
import service from '@/timeslots/timeslots.service'

const timeslots = ref<Timeslot[]>([])
const dialog = ref(false)
const dialogDelete = ref(false)
const dayOfWeeks: DayOfWeek[] = [
  {
    id: 1,
    label: 'Понедельник'
  },
  {
    id: 2,
    label: 'Вторник'
  },
  {
    id: 3,
    label: 'Среда'
  },
  {
    id: 4,
    label: 'Четверг'
  },
  {
    id: 5,
    label: 'Пятница'
  },
  {
    id: 6,
    label: 'Суббота'
  },
  {
    id: 7,
    label: 'Воскресенье'
  }
]
const hours = [...Array(24).keys()]
const minutes = [...Array(60).keys()]
const defaultTimeslot: Timeslot = {
  id: 0,
  dayOfWeek: 0,
  start: LocalTime.of(),
  end: LocalTime.of()
}
const activeTimeslot = ref<Timeslot>({ ...defaultTimeslot })
const resetActiveTimeslot = () => (activeTimeslot.value = { ...defaultTimeslot })
const computeActiveDayOfWeek = () => {
  const dayOfWeek = activeTimeslot.value.dayOfWeek
  return dayOfWeek === 0 ? null : dayOfWeek
}
const headers = [
  { title: 'День недели', value: 'dayOfWeek' },
  { title: 'Время начала', value: 'start' },
  { title: 'Время конца', value: 'end' },
  { title: 'Действия', key: 'actions' }
]
const startEditingTimeslot = (item: Timeslot) => {
  activeTimeslot.value = item
  dialog.value = true
}
const startDeletingTimeslot = (item: Timeslot) => {
  activeTimeslot.value = item
  dialogDelete.value = true
}
const closeDialog = () => (dialog.value = false)
const closeDialogDelete = () => (dialogDelete.value = false)
const addTimeslot = () =>
  service
    .add(activeTimeslot.value as Timeslot)
    .then((value) => {
      timeslots.value.push(value)
      closeDialog()
    })
    .catch((error) => console.error(error))
const editTimeslot = () =>
  service
    .edit(activeTimeslot.value as Timeslot)
    .then((value) => {
      const index = timeslots.value.findIndex((item) => item.id === value.id)
      timeslots.value[index] = value
      closeDialog()
    })
    .catch((error) => console.error(error))
const deleteTimeslot = () => {
  const id = activeTimeslot.value.id
  service
    .remove(id)
    .then(() => {
      const index = timeslots.value.findIndex((item) => item.id === id)
      timeslots.value.splice(index, 1)
      closeDialogDelete()
    })
    .catch((error) => console.error(error))
}
onMounted(() =>
  service
    .all()
    .then((value) => (timeslots.value = value))
    .catch((error) => console.error(error))
)
</script>
<template>
  <v-data-table :items="timeslots" :headers="headers" :sort-by="[
    { key: 'dayOfWeek', order: 'asc' },
    { key: 'start', order: 'asc' },
    { key: 'end', order: 'asc' }
  ]" multi-sort>
    <template v-slot:top>
      <v-toolbar flat>
        <v-toolbar-title>Время</v-toolbar-title>
        <v-divider class="mx-4" inset vertical></v-divider>
        <v-spacer></v-spacer>
        <v-dialog v-model="dialog" max-width="500px">
          <template v-slot:activator="{ props }">
            <v-btn class="mb-2" color="primary" dark v-bind="props" @click="resetActiveTimeslot">
              Добавить время
            </v-btn>
          </template>
          <v-card>
            <v-card-title>
              <span class="text-h5">{{ activeTimeslot.id === 0 ? 'Добавление' : 'Изменение' }} времени</span>
            </v-card-title>

            <v-card-text>
              <v-container>
                <v-row>
                  <v-col>
                    <v-select label="День недели" :items="dayOfWeeks" :item-title="(day) => day.label"
                      :item-value="(day) => day.id" :model-value="computeActiveDayOfWeek()"
                      @update:model-value="(value) => (activeTimeslot.dayOfWeek = value)" />
                  </v-col>
                </v-row>
                <v-row>
                  <v-col>
                    <v-select label="Часы (начало)" :items="hours" :model-value="activeTimeslot.start.hour()"
                      @update:model-value="(hour) => (activeTimeslot.start = activeTimeslot.start.withHour(hour))
    " />
                  </v-col>
                  <v-col>
                    <v-select label="Минуты (начало)" :items="minutes" :model-value="activeTimeslot.start.minute()"
                      @update:model-value="(minute) => (activeTimeslot.start = activeTimeslot.start.withMinute(minute))
    " />
                  </v-col>
                </v-row>
                <v-row>
                  <v-col>
                    <v-select label="Часы (конец)" :items="hours" :model-value="activeTimeslot.end.hour()"
                      @update:model-value="(hour) => (activeTimeslot.end = activeTimeslot.end.withHour(hour))
    " />
                  </v-col>
                  <v-col>
                    <v-select label="Минуты (конец)" :items="minutes" :model-value="activeTimeslot.end.minute()"
                      @update:model-value="(minute) => (activeTimeslot.end = activeTimeslot.end.withMinute(minute))
    " />
                  </v-col>
                </v-row>
              </v-container>
            </v-card-text>

            <v-card-actions>
              <v-spacer></v-spacer>
              <v-btn color="blue-darken-1" variant="text" @click="closeDialog()"> Отменить </v-btn>
              <v-btn color="blue-darken-1" variant="text"
                @click="activeTimeslot.id === 0 ? addTimeslot() : editTimeslot()">
                {{ activeTimeslot.id === 0 ? 'Добавить' : 'Изменить' }}
              </v-btn>
            </v-card-actions>
          </v-card>
        </v-dialog>
        <v-dialog v-model="dialogDelete" max-width="500px">
          <v-card>
            <v-card-title class="text-h5">Вы уверены, что хотите удалить время?</v-card-title>
            <v-card-actions>
              <v-spacer></v-spacer>
              <v-btn color="blue-darken-1" variant="text" @click="closeDialogDelete()">Cancel</v-btn>
              <v-btn color="blue-darken-1" variant="text" @click="deleteTimeslot()">OK</v-btn>
              <v-spacer></v-spacer>
            </v-card-actions>
          </v-card>
        </v-dialog>
      </v-toolbar>
    </template>
    <template v-slot:[`item.dayOfWeek`]="{ item }">
      {{ dayOfWeeks.find((value) => value.id === item.dayOfWeek)?.label }}
    </template>
    <template v-slot:[`item.actions`]="{ item }">
      <v-icon class="me-2" size="small" @click="startEditingTimeslot(item as Timeslot)">
        mdi-pencil
      </v-icon>
      <v-icon size="small" @click="startDeletingTimeslot(item as Timeslot)"> mdi-delete </v-icon>
    </template>
    <template v-slot:no-data>
      <v-btn color="primary"> Reset </v-btn>
    </template>
  </v-data-table>
</template>
