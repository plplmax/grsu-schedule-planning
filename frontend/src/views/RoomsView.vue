<script setup lang="ts">
import { onMounted, ref } from 'vue'
import service from '@/rooms/rooms.service'
import type { Room } from '@/rooms/room'

const rooms = ref<Room[]>([])
const capacities = [...Array(6).keys()].splice(1)
const headers = [
  { title: 'Название', value: 'name' },
  { title: 'Вместимость', value: 'capacity' },
  { title: 'Действия', value: 'actions' }
]
const dialog = ref(false)
const dialogDelete = ref(false)
const defaultRoom: Room = {
  id: 0,
  name: '',
  capacity: 1
}
const activeRoom = ref<Room>({ ...defaultRoom })
const resetActiveRoom = () => (activeRoom.value = { ...defaultRoom })
const closeDialog = () => (dialog.value = false)
const closeDialogDelete = () => (dialogDelete.value = false)
const addRoom = () =>
  service
    .add(activeRoom.value)
    .then((value) => {
      rooms.value.push(value)
      closeDialog()
    })
    .catch((error) => console.error(error))
const editRoom = () =>
  service
    .edit(activeRoom.value)
    .then((value) => {
      const index = rooms.value.findIndex((item) => item.id === value.id)
      rooms.value[index] = value
      closeDialog()
    })
    .catch((error) => console.error(error))
const deleteRoom = () => {
  const id = activeRoom.value.id
  service
    .remove(id)
    .then(() => {
      const index = rooms.value.findIndex((item) => item.id === id)
      rooms.value.splice(index, 1)
      closeDialogDelete()
    })
    .catch((error) => console.error(error))
}
const startEditingRoom = (item: Room) => {
  activeRoom.value = { ...item }
  dialog.value = true
}
const startDeletingRoom = (item: Room) => {
  activeRoom.value = item
  dialogDelete.value = true
}

onMounted(() =>
  service
    .all()
    .then((value) => (rooms.value = value))
    .catch((error) => console.error(error))
)
</script>
<template>
  <v-data-table :items="rooms" :headers="headers" :sort-by="[{ key: 'name', order: 'asc' }]">
    <template v-slot:top>
      <v-toolbar flat>
        <v-toolbar-title>Кабинеты</v-toolbar-title>
        <v-divider class="mx-4" inset vertical></v-divider>
        <v-spacer></v-spacer>
        <v-dialog v-model="dialog" max-width="500px">
          <template v-slot:activator="{ props }">
            <v-btn class="mb-2" color="primary" dark v-bind="props" @click="resetActiveRoom">
              Добавить кабинет
            </v-btn>
          </template>
          <v-card>
            <v-card-title>
              <span class="text-h5"
                >{{ activeRoom.id === 0 ? 'Добавление' : 'Изменение' }} кабинета</span
              >
            </v-card-title>

            <v-card-text>
              <v-text-field
                label="Название кабинета"
                :model-value="activeRoom.name"
                @update:model-value="(value) => (activeRoom.name = value)"
              ></v-text-field>
              <v-select
                label="Вместимость классов"
                :items="capacities"
                v-model="activeRoom.capacity"
              ></v-select>
            </v-card-text>

            <v-card-actions>
              <v-spacer></v-spacer>
              <v-btn color="blue-darken-1" variant="text" @click="closeDialog()"> Отменить </v-btn>
              <v-btn
                color="blue-darken-1"
                variant="text"
                @click="activeRoom.id === 0 ? addRoom() : editRoom()"
              >
                {{ activeRoom.id === 0 ? 'Добавить' : 'Изменить' }}
              </v-btn>
            </v-card-actions>
          </v-card>
        </v-dialog>
        <v-dialog v-model="dialogDelete" max-width="520px">
          <v-card>
            <v-card-title class="text-h5">Вы уверены, что хотите удалить кабинет?</v-card-title>
            <v-card-actions>
              <v-spacer></v-spacer>
              <v-btn color="blue-darken-1" variant="text" @click="closeDialogDelete()"
                >Cancel</v-btn
              >
              <v-btn color="blue-darken-1" variant="text" @click="deleteRoom()">OK</v-btn>
              <v-spacer></v-spacer>
            </v-card-actions>
          </v-card>
        </v-dialog>
      </v-toolbar>
    </template>
    <template v-slot:[`item.actions`]="{ item }">
      <v-icon class="me-2" size="small" @click="startEditingRoom(item)"> mdi-pencil </v-icon>
      <v-icon size="small" @click="startDeletingRoom(item)"> mdi-delete </v-icon>
    </template>
    <template v-slot:no-data> Пока здесь нет кабинетов... </template>
  </v-data-table>
</template>
