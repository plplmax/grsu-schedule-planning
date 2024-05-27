<script setup lang="ts">
import type { Subject } from '@/subjects/Subject'
import { onMounted, ref } from 'vue'
import pairedSubjectsService from '@/subjects/paired-subjects.service'
import type { PairedSubjects, PairedSubjectsUi } from '@/subjects/PairedSubjects'
import type { Group } from '@/groups/group'
import subjectsService from '@/subjects/subjects.service'
import groupsService from '@/groups/groups.service'

const pairedSubjects = ref<PairedSubjects[]>([])
const subjects = ref<Subject[]>([])
const groups = ref<Group[]>([])
const countList = [...Array(6).keys()].splice(1)
const headers = [
  { title: 'Первый предмет', value: 'firstSubject.name' },
  { title: 'Второй предмет', value: 'secondSubject.name' },
  { title: 'Действия', value: 'actions' }
]
const dialog = ref(false)
const dialogDelete = ref(false)
const defaultPairedSubjects: PairedSubjectsUi = {
  id: 0,
  firstSubject: null,
  secondSubject: null,
  count: 1,
  groups: []
}
const activePairedSubjects = ref<PairedSubjectsUi>({ ...defaultPairedSubjects })
const resetActivePairedSubjects = () => (activePairedSubjects.value = { ...defaultPairedSubjects })
const closeDialog = () => (dialog.value = false)
const closeDialogDelete = () => (dialogDelete.value = false)
const addPairedSubjects = () =>
  pairedSubjectsService
    .add(activePairedSubjects.value as PairedSubjects)
    .then((value) => {
      pairedSubjects.value.push(value)
      closeDialog()
    })
    .catch((error) => console.error(error))
const editPairedSubjects = () =>
  pairedSubjectsService
    .edit(activePairedSubjects.value as PairedSubjects)
    .then((value) => {
      const index = pairedSubjects.value.findIndex((item) => item.id === value.id)
      pairedSubjects.value[index] = value
      closeDialog()
    })
    .catch((error) => console.error(error))
const deletePairedSubjects = () => {
  const id = activePairedSubjects.value.id
  pairedSubjectsService
    .remove(id)
    .then(() => {
      const index = pairedSubjects.value.findIndex((item) => item.id === id)
      pairedSubjects.value.splice(index, 1)
      closeDialogDelete()
    })
    .catch((error) => console.error(error))
}
const startEditingPairedSubjects = (item: PairedSubjects) => {
  activePairedSubjects.value = { ...item }
  dialog.value = true
}
const startDeletingPairedSubjects = (item: PairedSubjects) => {
  activePairedSubjects.value = item
  dialogDelete.value = true
}

onMounted(() => {
  pairedSubjectsService
    .all()
    .then((value) => (pairedSubjects.value = value))
    .catch((error) => console.error(error))

  subjectsService
    .all()
    .then((value) => (subjects.value = value))
    .catch((error) => console.error(error))

  groupsService
    .all()
    .then((value) => (groups.value = value))
    .catch((error) => console.error(error))
})
</script>
<template>
  <v-data-table
    :items="pairedSubjects"
    :headers="headers"
    :sort-by="[
      { key: 'firstSubject', order: 'asc' },
      { key: 'secondSubject', order: 'asc' }
    ]"
    multi-sort
  >
    <template v-slot:top>
      <v-toolbar flat>
        <v-toolbar-title>Парные предметы</v-toolbar-title>
        <v-divider class="mx-4" inset vertical></v-divider>
        <v-spacer></v-spacer>
        <v-dialog v-model="dialog" max-width="500px">
          <template v-slot:activator="{ props }">
            <v-btn
              class="mb-2"
              color="primary"
              dark
              v-bind="props"
              @click="resetActivePairedSubjects"
            >
              Добавить парные предметы
            </v-btn>
          </template>
          <v-card>
            <v-card-title>
              <span class="text-h5"
                >{{ activePairedSubjects.id === 0 ? 'Добавление' : 'Изменение' }} парных
                предметов</span
              >
            </v-card-title>

            <v-card-text>
              <v-select
                label="Первый предмет"
                :items="subjects"
                :item-title="(item) => item.name"
                v-model="activePairedSubjects.firstSubject"
                return-object
              ></v-select>
              <v-select
                label="Второй предмет"
                :items="subjects"
                :item-title="(item) => item.name"
                v-model="activePairedSubjects.secondSubject"
                return-object
              ></v-select>
              <v-select
                label="Классы"
                :items="groups"
                :item-title="(item) => `${item.number} ${item.letter}`"
                v-model="activePairedSubjects.groups"
                multiple
                return-object
              ></v-select>
              <v-select
                label="Количество пар"
                :items="countList"
                v-model="activePairedSubjects.count"
              ></v-select>
            </v-card-text>

            <v-card-actions>
              <v-spacer></v-spacer>
              <v-btn color="blue-darken-1" variant="text" @click="closeDialog()"> Отменить </v-btn>
              <v-btn
                color="blue-darken-1"
                variant="text"
                @click="activePairedSubjects.id === 0 ? addPairedSubjects() : editPairedSubjects()"
              >
                {{ activePairedSubjects.id === 0 ? 'Добавить' : 'Изменить' }}
              </v-btn>
            </v-card-actions>
          </v-card>
        </v-dialog>
        <v-dialog v-model="dialogDelete" max-width="520px">
          <v-card>
            <v-card-title class="text-h5"
              >Вы уверены, что хотите удалить парные предметы?</v-card-title
            >
            <v-card-actions>
              <v-spacer></v-spacer>
              <v-btn color="blue-darken-1" variant="text" @click="closeDialogDelete()"
                >Cancel</v-btn
              >
              <v-btn color="blue-darken-1" variant="text" @click="deletePairedSubjects()">OK</v-btn>
              <v-spacer></v-spacer>
            </v-card-actions>
          </v-card>
        </v-dialog>
      </v-toolbar>
    </template>
    <template v-slot:[`item.actions`]="{ item }">
      <v-icon class="me-2" size="small" @click="startEditingPairedSubjects(item)">
        mdi-pencil
      </v-icon>
      <v-icon size="small" @click="startDeletingPairedSubjects(item)"> mdi-delete </v-icon>
    </template>
    <template v-slot:no-data> Пока здесь нет парных предметов... </template>
  </v-data-table>
</template>
