<script setup lang="ts">
import type { SubjectDetail } from '@/subjects/Subject'
import { computed, onMounted, ref, type Ref } from 'vue'
import subjectsService from '@/subjects/subjects.service'
import { dayOfWeeks, type Timeslot } from '@/timeslots/Timeslot'
import timeslotsService from '@/timeslots/timeslots.service'

const subjects = ref<SubjectDetail[]>([]) as Ref<SubjectDetail[]>
const timeslots = ref<Timeslot[]>([])
const headers = [
  { title: 'Название', value: 'name' },
  { title: 'Действия', value: 'actions' }
]
const complexityPoints = [...Array(13).keys()]
const minDaysBetween = [...Array(5).keys()].splice(1)
const dialog = ref(false)
const dialogDelete = ref(false)
const defaultSubject: SubjectDetail = {
  id: 0,
  name: '',
  complexity: 0,
  disallowedTimeslots: [],
  minDaysBetween: 1,
  minDaysStrict: false,
  onceFirstOrLastTimeslot: false
}
const activeSubject = ref<SubjectDetail>({ ...defaultSubject }) as Ref<SubjectDetail>
const resetActiveSubject = () => (activeSubject.value = { ...defaultSubject })
const closeDialog = () => (dialog.value = false)
const closeDialogDelete = () => (dialogDelete.value = false)
const addSubject = () =>
  subjectsService
    .add(activeSubject.value)
    .then((value) => {
      subjects.value.push(value)
      closeDialog()
    })
    .catch((error) => console.error(error))
const editSubject = () =>
  subjectsService
    .edit(activeSubject.value)
    .then((value) => {
      const index = subjects.value.findIndex((item) => item.id === value.id)
      subjects.value[index] = value
      closeDialog()
    })
    .catch((error) => console.error(error))
const deleteSubject = () => {
  const id = activeSubject.value.id
  subjectsService
    .remove(id)
    .then(() => {
      const index = subjects.value.findIndex((item) => item.id === id)
      subjects.value.splice(index, 1)
      closeDialogDelete()
    })
    .catch((error) => console.error(error))
}
const startEditingSubject = (item: SubjectDetail) => {
  activeSubject.value = { ...item }
  dialog.value = true
}
const startDeletingSubject = (item: SubjectDetail) => {
  activeSubject.value = item
  dialogDelete.value = true
}
const selectedAllTimeslots = computed(
  () => activeSubject.value.disallowedTimeslots.length === timeslots.value.length
)
const selectedSomeTimeslots = computed(() => activeSubject.value.disallowedTimeslots.length > 0)
const toggleSelectAll = () => {
  if (selectedAllTimeslots.value) {
    activeSubject.value.disallowedTimeslots = []
  } else {
    activeSubject.value.disallowedTimeslots = timeslots.value as Timeslot[]
  }
}
onMounted(() => {
  subjectsService
    .allDetails()
    .then((value) => (subjects.value = value))
    .catch((error) => console.error(error))

  timeslotsService
    .all()
    .then((value) => (timeslots.value = value))
    .catch((error) => console.error(error))
})
</script>
<template>
  <v-data-table :items="subjects" :headers="headers" :sort-by="[{ key: 'name', order: 'asc' }]">
    <template v-slot:top>
      <v-toolbar flat>
        <v-toolbar-title>Предметы</v-toolbar-title>
        <v-divider class="mx-4" inset vertical></v-divider>
        <v-spacer></v-spacer>
        <v-dialog v-model="dialog" max-width="500px">
          <template v-slot:activator="{ props }">
            <v-btn class="mb-2" color="primary" dark v-bind="props" @click="resetActiveSubject">
              Добавить предмет
            </v-btn>
          </template>
          <v-card>
            <v-card-title>
              <span class="text-h5"
                >{{ activeSubject.id === 0 ? 'Добавление' : 'Изменение' }} предмета</span
              >
            </v-card-title>

            <v-card-text>
              <v-text-field
                label="Название предмета"
                :model-value="activeSubject.name"
                @update:model-value="(value) => (activeSubject.name = value)"
              ></v-text-field>
              <v-select
                label="Балл сложности"
                :items="complexityPoints"
                v-model="activeSubject.complexity"
              ></v-select>
              <v-select
                label="Запрещённое время"
                :items="timeslots"
                :item-title="
                  (item) =>
                    `${dayOfWeeks.find((value) => value.id === item.dayOfWeek)!.shortLabel} ${
                      item.start
                    } - ${item.end}`
                "
                v-model="activeSubject.disallowedTimeslots"
                return-object
                multiple
              >
                <template v-slot:prepend-item>
                  <v-list-item title="Выбрать всё" @click="toggleSelectAll">
                    <template v-slot:prepend>
                      <v-checkbox-btn
                        :color="selectedSomeTimeslots ? 'indigo-darken-4' : undefined"
                        :indeterminate="selectedSomeTimeslots && !selectedAllTimeslots"
                        :model-value="selectedAllTimeslots"
                      ></v-checkbox-btn>
                    </template>
                  </v-list-item>
                  <v-divider class="mt-2"></v-divider>
                </template>

                <template v-slot:selection="{ item, index }">
                  <v-chip v-if="index < 2">
                    <span>{{ item.title }}</span>
                  </v-chip>
                  <span v-if="index === 2" class="text-grey text-caption align-self-center">
                    (+{{ activeSubject.disallowedTimeslots.length - 2 }} других)
                  </span>
                </template>
              </v-select>
              <v-select
                label="Минимальное количество дней между уроками"
                :items="minDaysBetween"
                v-model="activeSubject.minDaysBetween"
                hint="1 - пн, вт, ср, чт, пт; 2 - пн, ср, пт; 3 - ..."
                persistent-hint
              ></v-select>
              <v-checkbox
                :label="`Обязательно минимум ${activeSubject.minDaysBetween} дней между уроками`"
                v-model="activeSubject.minDaysStrict"
              ></v-checkbox>
              <v-checkbox
                label="Обязательно минимум один раз или первым уроком, или последним"
                v-model="activeSubject.onceFirstOrLastTimeslot"
              ></v-checkbox>
            </v-card-text>

            <v-card-actions>
              <v-spacer></v-spacer>
              <v-btn color="blue-darken-1" variant="text" @click="closeDialog()"> Отменить </v-btn>
              <v-btn
                color="blue-darken-1"
                variant="text"
                @click="activeSubject.id === 0 ? addSubject() : editSubject()"
              >
                {{ activeSubject.id === 0 ? 'Добавить' : 'Изменить' }}
              </v-btn>
            </v-card-actions>
          </v-card>
        </v-dialog>
        <v-dialog v-model="dialogDelete" max-width="520px">
          <v-card>
            <v-card-title class="text-h5">Вы уверены, что хотите удалить предмет?</v-card-title>
            <v-card-actions>
              <v-spacer></v-spacer>
              <v-btn color="blue-darken-1" variant="text" @click="closeDialogDelete()"
                >Cancel</v-btn
              >
              <v-btn color="blue-darken-1" variant="text" @click="deleteSubject()">OK</v-btn>
              <v-spacer></v-spacer>
            </v-card-actions>
          </v-card>
        </v-dialog>
      </v-toolbar>
    </template>
    <template v-slot:[`item.actions`]="{ item }">
      <v-icon class="me-2" size="small" @click="startEditingSubject(item)"> mdi-pencil </v-icon>
      <v-icon size="small" @click="startDeletingSubject(item)"> mdi-delete </v-icon>
    </template>
    <template v-slot:no-data> Пока здесь нет предметов... </template>
  </v-data-table>
</template>
