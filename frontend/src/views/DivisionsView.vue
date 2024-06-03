<script setup lang="ts">
import { onMounted, ref, type Ref } from 'vue'
import type { DivisionDetail } from '@/divisions/division'
import divisionsService from '@/divisions/divisions.service'

const divisions = ref<DivisionDetail[]>([]) as Ref<DivisionDetail[]>
const systemDivisionId = 1
const headers = [
  { title: 'Идентификатор', value: 'id' },
  { title: 'Подгруппы', value: 'subgroups' },
  { title: 'Действия', value: 'actions' }
]
const dialog = ref(false)
const dialogDelete = ref(false)
const defaultDivision: DivisionDetail = {
  id: 0,
  subgroups: []
}
const activeDivision = ref<DivisionDetail>({ ...defaultDivision }) as Ref<DivisionDetail>
const resetActiveDivision = () =>
  (activeDivision.value = {
    ...defaultDivision,
    subgroups: [
      { id: 0, name: '' },
      { id: 0, name: '' }
    ]
  })
const closeDialog = () => (dialog.value = false)
const closeDialogDelete = () => (dialogDelete.value = false)
const addDivision = () =>
  divisionsService
    .add(activeDivision.value)
    .then((value) => {
      divisions.value.push(value)
      closeDialog()
    })
    .catch((error) => console.error(error))
const editDivision = () =>
  divisionsService
    .edit(activeDivision.value)
    .then((value) => {
      const index = divisions.value.findIndex((item) => item.id === value.id)
      divisions.value[index] = value
      closeDialog()
    })
    .catch((error) => console.error(error))
const deleteDivision = () => {
  const id = activeDivision.value.id
  divisionsService
    .remove(id)
    .then(() => {
      const index = divisions.value.findIndex((item) => item.id === id)
      divisions.value.splice(index, 1)
      closeDialogDelete()
    })
    .catch((error) => console.error(error))
}
const startEditingDivision = (item: DivisionDetail) => {
  activeDivision.value = { ...item, subgroups: item.subgroups.map((value) => ({ ...value })) }
  dialog.value = true
}
const startDeletingDivision = (item: DivisionDetail) => {
  activeDivision.value = item
  dialogDelete.value = true
}
const addSubgroup = () => activeDivision.value.subgroups.push({ id: 0, name: '' })
const deleteSubgroup = (index: number) => activeDivision.value.subgroups.splice(index, 1)

onMounted(() => {
  divisionsService
    .all()
    .then((value) => (divisions.value = value))
    .catch((error) => console.error(error))
})
</script>
<template>
  <v-data-table :items="divisions" :headers="headers" :sort-by="[{ key: 'id', order: 'asc' }]">
    <template v-slot:top>
      <v-toolbar flat>
        <v-toolbar-title>Разделения</v-toolbar-title>
        <v-divider class="mx-4" inset vertical></v-divider>
        <v-spacer></v-spacer>
        <v-dialog v-model="dialog" max-width="500px">
          <template v-slot:activator="{ props }">
            <v-btn class="mb-2" color="primary" dark v-bind="props" @click="resetActiveDivision">
              Добавить разделение
            </v-btn>
          </template>
          <v-card>
            <v-card-title>
              <span class="text-h5"
                >{{ activeDivision.id === 0 ? 'Добавление' : 'Изменение' }} разделения</span
              >
            </v-card-title>

            <v-card-text>
              <v-text-field
                v-for="(subgroup, index) in activeDivision.subgroups"
                :key="subgroup.id"
                :label="`Название ${index + 1} подгруппы`"
                v-model="subgroup.name"
                :prepend-icon="activeDivision.subgroups.length > 1 ? 'mdi-delete' : undefined"
                @click:prepend="deleteSubgroup(index)"
              ></v-text-field>
              <v-btn @click="addSubgroup()">Добавить подгруппу</v-btn>
            </v-card-text>

            <v-card-actions>
              <v-spacer></v-spacer>
              <v-btn color="blue-darken-1" variant="text" @click="closeDialog()"> Отменить </v-btn>
              <v-btn
                color="blue-darken-1"
                variant="text"
                @click="activeDivision.id === 0 ? addDivision() : editDivision()"
              >
                {{ activeDivision.id === 0 ? 'Добавить' : 'Изменить' }}
              </v-btn>
            </v-card-actions>
          </v-card>
        </v-dialog>
        <v-dialog v-model="dialogDelete" max-width="520px">
          <v-card>
            <v-card-title class="text-h5">Вы уверены, что хотите удалить разделение?</v-card-title>
            <v-card-actions>
              <v-spacer></v-spacer>
              <v-btn color="blue-darken-1" variant="text" @click="closeDialogDelete()"
                >Cancel</v-btn
              >
              <v-btn color="blue-darken-1" variant="text" @click="deleteDivision()">OK</v-btn>
              <v-spacer></v-spacer>
            </v-card-actions>
          </v-card>
        </v-dialog>
      </v-toolbar>
    </template>
    <template v-slot:[`item.subgroups`]="{ item }">
      <span v-for="(subgroup, index) in item.subgroups.slice(0, 4)" :key="subgroup.id" class="mr-2">
        <v-chip v-if="index < 3">
          <span>{{ subgroup.name }}</span>
        </v-chip>
        <span v-if="index === 3" class="text-grey text-caption align-self-center">
          (+{{ item.subgroups.length - 3 }} других)
        </span>
      </span>
    </template>
    <template v-slot:[`item.actions`]="{ item }">
      <v-icon
        v-if="item.id !== systemDivisionId"
        class="me-2"
        size="small"
        @click="startEditingDivision(item)"
      >
        mdi-pencil
      </v-icon>
      <v-icon v-if="item.id !== systemDivisionId" size="small" @click="startDeletingDivision(item)">
        mdi-delete
      </v-icon>
    </template>
    <template v-slot:no-data> Пока здесь нет разделений... </template>
  </v-data-table>
</template>
