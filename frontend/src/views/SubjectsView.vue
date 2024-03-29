<script setup lang="ts">
import type { Subject } from '@/subjects/Subject'
import { onMounted, ref } from 'vue'
import service from '@/subjects/subjects.service'

const subjects = ref<Subject[]>([])
const headers = [
  { title: 'Название', value: 'name' },
  { title: 'Действия', value: 'actions' }
]
const dialog = ref(false)
const dialogDelete = ref(false)
const defaultSubject: Subject = {
  id: 0,
  name: ''
}
const activeSubject = ref<Subject>({ ...defaultSubject })
const resetActiveSubject = () => (activeSubject.value = { ...defaultSubject })
const closeDialog = () => (dialog.value = false)
const closeDialogDelete = () => (dialogDelete.value = false)
const addSubject = () =>
  service
    .add(activeSubject.value)
    .then((value) => {
      subjects.value.push(value)
      closeDialog()
    })
    .catch((error) => console.error(error))
const editSubject = () =>
  service
    .edit(activeSubject.value)
    .then((value) => {
      const index = subjects.value.findIndex((item) => item.id === value.id)
      subjects.value[index] = value
      closeDialog()
    })
    .catch((error) => console.error(error))
const deleteSubject = () => {
  const id = activeSubject.value.id
  service
    .remove(id)
    .then(() => {
      const index = subjects.value.findIndex((item) => item.id === id)
      subjects.value.splice(index, 1)
      closeDialogDelete()
    })
    .catch((error) => console.error(error))
}
const startEditingSubject = (item: Subject) => {
  activeSubject.value = { ...item }
  dialog.value = true
}
const startDeletingSubject = (item: Subject) => {
  activeSubject.value = item
  dialogDelete.value = true
}

onMounted(() =>
  service
    .all()
    .then((value) => (subjects.value = value))
    .catch((error) => console.error(error))
)
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
