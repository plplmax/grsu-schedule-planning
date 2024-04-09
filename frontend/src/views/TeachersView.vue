<script setup lang="ts">
import type { Subject } from '@/subjects/Subject'
import { onMounted, ref } from 'vue'
import teachersService from '@/teachers/teachers.service'
import type { Teacher } from '@/teachers/Teacher'
import subjectsService from '@/subjects/subjects.service'

const teachers = ref<Teacher[]>([])
const subjects = ref<Subject[]>([])
const headers = [
  { title: 'Фамилия', value: 'lastname' },
  { title: 'Имя', value: 'firstname' },
  { title: 'Предметы', value: 'subjects' },
  { title: 'Действия', value: 'actions' }
]
const dialog = ref(false)
const dialogDelete = ref(false)
const defaultTeacher: Teacher = {
  id: 0,
  firstname: '',
  lastname: '',
  subjects: []
}
const activeTeacher = ref<Teacher>({ ...defaultTeacher })
const resetActiveTeacher = () => (activeTeacher.value = { ...defaultTeacher })
const closeDialog = () => (dialog.value = false)
const closeDialogDelete = () => (dialogDelete.value = false)
const addTeacher = () =>
  teachersService
    .add(activeTeacher.value)
    .then((value) => {
      teachers.value.push(value)
      closeDialog()
    })
    .catch((error) => console.error(error))
const editTeacher = () =>
  teachersService
    .edit(activeTeacher.value)
    .then((value) => {
      const index = teachers.value.findIndex((item) => item.id === value.id)
      teachers.value[index] = value
      closeDialog()
    })
    .catch((error) => console.error(error))
const deleteTeacher = () => {
  const id = activeTeacher.value.id
  teachersService
    .remove(id)
    .then(() => {
      const index = teachers.value.findIndex((item) => item.id === id)
      teachers.value.splice(index, 1)
      closeDialogDelete()
    })
    .catch((error) => console.error(error))
}
const startEditingTeacher = (item: Teacher) => {
  activeTeacher.value = { ...item }
  dialog.value = true
}
const startDeletingTeacher = (item: Teacher) => {
  activeTeacher.value = item
  dialogDelete.value = true
}

onMounted(() => {
  teachersService
    .all()
    .then((value) => (teachers.value = value))
    .catch((error) => console.error(error))

  subjectsService
    .all()
    .then((value) => (subjects.value = value))
    .catch((error) => console.error(error))
})
</script>
<template>
  <v-data-table
    :items="teachers"
    :headers="headers"
    :sort-by="[
      { key: 'lastname', order: 'asc' },
      { key: 'firstname', order: 'asc' }
    ]"
    multi-sort
  >
    <template v-slot:top>
      <v-toolbar flat>
        <v-toolbar-title>Учителя</v-toolbar-title>
        <v-divider class="mx-4" inset vertical></v-divider>
        <v-spacer></v-spacer>
        <v-dialog v-model="dialog" max-width="500px">
          <template v-slot:activator="{ props }">
            <v-btn class="mb-2" color="primary" dark v-bind="props" @click="resetActiveTeacher">
              Добавить учителя
            </v-btn>
          </template>
          <v-card>
            <v-card-title>
              <span class="text-h5"
                >{{ activeTeacher.id === 0 ? 'Добавление' : 'Изменение' }} учителя</span
              >
            </v-card-title>

            <v-card-text>
              <v-text-field
                label="Фамилия"
                :model-value="activeTeacher.lastname"
                @update:model-value="(value) => (activeTeacher.lastname = value)"
              ></v-text-field>
              <v-text-field
                label="Имя"
                :model-value="activeTeacher.firstname"
                @update:model-value="(value) => (activeTeacher.firstname = value)"
              ></v-text-field>
              <v-select
                label="Предметы"
                :items="subjects"
                v-model="activeTeacher.subjects"
                :item-title="(value) => value.name"
                :item-value="(value) => value.id"
                return-object
                multiple
              ></v-select>
            </v-card-text>

            <v-card-actions>
              <v-spacer></v-spacer>
              <v-btn color="blue-darken-1" variant="text" @click="closeDialog()"> Отменить </v-btn>
              <v-btn
                color="blue-darken-1"
                variant="text"
                @click="activeTeacher.id === 0 ? addTeacher() : editTeacher()"
              >
                {{ activeTeacher.id === 0 ? 'Добавить' : 'Изменить' }}
              </v-btn>
            </v-card-actions>
          </v-card>
        </v-dialog>
        <v-dialog v-model="dialogDelete" max-width="520px">
          <v-card>
            <v-card-title class="text-h5">Вы уверены, что хотите удалить учителя?</v-card-title>
            <v-card-actions>
              <v-spacer></v-spacer>
              <v-btn color="blue-darken-1" variant="text" @click="closeDialogDelete()"
                >Cancel</v-btn
              >
              <v-btn color="blue-darken-1" variant="text" @click="deleteTeacher()">OK</v-btn>
              <v-spacer></v-spacer>
            </v-card-actions>
          </v-card>
        </v-dialog>
      </v-toolbar>
    </template>
    <template v-slot:[`item.subjects`]="{ item }">
      <v-chip class="ma-1" v-for="subject in item.subjects" :key="subject.id">{{
        subject.name
      }}</v-chip>
    </template>
    <template v-slot:[`item.actions`]="{ item }">
      <v-icon class="me-2" size="small" @click="startEditingTeacher(item)"> mdi-pencil </v-icon>
      <v-icon size="small" @click="startDeletingTeacher(item)"> mdi-delete </v-icon>
    </template>
    <template v-slot:no-data> Список учителей пока пуст... </template>
  </v-data-table>
</template>
