<script setup lang="ts">
import { onMounted, ref, type Ref } from 'vue'
import groupsService from '@/groups/groups.service'
import subjectsService from '@/subjects/subjects.service'
import type { GroupDetail, GroupDetailUi } from '@/groups/group'
import type { Subject } from '@/subjects/Subject'
import { computed } from 'vue'
import type { Teacher, TeacherDetail } from '@/teachers/Teacher'
import type { Room } from '@/rooms/room'
import teachersService from '@/teachers/teachers.service'
import roomsService from '@/rooms/rooms.service'

const groups = ref<GroupDetail[]>([]) as Ref<GroupDetail[]>
const subjects = ref<Subject[]>([])
const teachersDetail = ref<TeacherDetail[]>([])
const rooms = ref<Room[]>([])
const hours = [...Array(11).keys()].slice(1)
const headers = [
  { title: 'Название', value: 'name' },
  { title: 'Действия', value: 'actions' }
]
const ordinalNumbers = [...Array(12).keys()].slice(1)
const letters = ['А', 'Б', 'В', 'Г', 'Д', 'Е', 'Ж', 'З', 'И', 'К']
const dialog = ref(false)
const dialogDelete = ref(false)
const defaultGroup: GroupDetailUi = {
  id: 0,
  number: 1,
  letter: 'А',
  lessons: []
}
const activeGroup = ref<GroupDetailUi>({ ...defaultGroup }) as Ref<GroupDetailUi>
const activeSubjects = computed({
  get: () =>
    activeGroup.value.lessons
      .map((value) => value.subject)
      .filter((value, index, array) => array.findIndex((item) => item.id === value.id) === index)
      .sort((a, b) => a.name.localeCompare(b.name)),
  set: (newSubjects) => {
    const oldSubjects = activeGroup.value.lessons
      .map((value) => value.subject)
      .filter((value, index, array) => array.findIndex((item) => item.id === value.id) === index)
    const added = newSubjects.filter(
      (newSubject) => oldSubjects.findIndex((oldSubject) => oldSubject.id === newSubject.id) === -1
    )
    const deleted = oldSubjects.filter(
      (oldSubject) => newSubjects.findIndex((newSubject) => newSubject.id === oldSubject.id) === -1
    )
    activeGroup.value.lessons = activeGroup.value.lessons
      .filter((lesson) => deleted.findIndex((subject) => subject.id === lesson.subject.id) === -1)
      .concat(
        added.map((value) => ({
          id: 0,
          group: {
            id: activeGroup.value.id,
            number: activeGroup.value.number,
            letter: activeGroup.value.letter
          },
          subject: value,
          timeslot: null
        }))
      )
  }
})
const resetActiveGroup = () => (activeGroup.value = { ...defaultGroup })
const closeDialog = () => (dialog.value = false)
const closeDialogDelete = () => (dialogDelete.value = false)
const addGroup = () => {
  if (activeGroup.value.lessons.some((value) => !value.teacher || !value.room)) {
    console.error('Some teachers or rooms are not selected')
    return
  }
  groupsService
    .add(activeGroup.value as GroupDetail)
    .then((value) => {
      groups.value.push(value)
      closeDialog()
    })
    .catch((error) => console.error(error))
}
const editGroup = () => {
  if (activeGroup.value.lessons.some((value) => !value.teacher || !value.room)) {
    console.error('Some teachers or rooms are not selected')
    return
  }
  groupsService
    .edit(activeGroup.value as GroupDetail)
    .then((value) => {
      const index = groups.value.findIndex((item) => item.id === value.id)
      groups.value[index] = value
      closeDialog()
    })
    .catch((error) => console.error(error))
}
const deleteGroup = () => {
  const id = activeGroup.value.id
  groupsService
    .remove(id)
    .then(() => {
      const index = groups.value.findIndex((item) => item.id === id)
      groups.value.splice(index, 1)
      closeDialogDelete()
    })
    .catch((error) => console.error(error))
}
const startEditingGroup = (item: GroupDetail) => {
  activeGroup.value = { ...item }
  dialog.value = true
}
const startDeletingGroup = (item: GroupDetail) => {
  activeGroup.value = item
  dialogDelete.value = true
}
const updateHours = (hours: number, subject: Subject, teacher?: Teacher) => {
  const oldLessons = activeGroup.value.lessons
  const lessons = oldLessons
    .filter((value) => value.subject.id === subject.id)
    .filter((value) => value.teacher?.id === teacher?.id)
  const old = lessons.length
  const diff = hours - old
  if (diff > 0) {
    lessons.push(...Array.from(Array(diff), () => ({ ...lessons[0], id: 0 })))
  }

  if (diff < 0) {
    lessons.splice(0, Math.abs(diff))
  }

  activeGroup.value.lessons = oldLessons
    .filter((value) => !(value.subject.id === subject.id && value.teacher?.id === teacher?.id))
    .concat(lessons)
}

onMounted(() => {
  groupsService
    .all()
    .then((value) => (groups.value = value))
    .catch((error) => console.error(error))

  subjectsService
    .all()
    .then((value) => (subjects.value = value))
    .catch((error) => console.error(error))

  teachersService
    .all()
    .then((value) => (teachersDetail.value = value))
    .catch((error) => console.error(error))

  roomsService
    .all()
    .then((value) => (rooms.value = value))
    .catch((error) => console.error(error))
})
</script>
<template>
  <v-data-table :items="groups" :headers="headers" :sort-by="[{ key: 'name', order: 'asc' }]">
    <template v-slot:top>
      <v-toolbar flat>
        <v-toolbar-title>Классы</v-toolbar-title>
        <v-divider class="mx-4" inset vertical></v-divider>
        <v-spacer></v-spacer>
        <v-dialog v-model="dialog" max-width="800px">
          <template v-slot:activator="{ props }">
            <v-btn class="mb-2" color="primary" dark v-bind="props" @click="resetActiveGroup">
              Добавить класс
            </v-btn>
          </template>
          <v-card>
            <v-card-title>
              <span class="text-h5"
                >{{ activeGroup.id === 0 ? 'Добавление' : 'Изменение' }} класса</span
              >
            </v-card-title>

            <v-card-text>
              <v-container>
                <v-row>
                  <v-col>
                    <v-select
                      label="Номер"
                      :items="ordinalNumbers"
                      v-model="activeGroup.number"
                    ></v-select>
                  </v-col>
                  <v-col>
                    <v-select
                      label="Буква"
                      :items="letters"
                      v-model="activeGroup.letter"
                    ></v-select>
                  </v-col>
                </v-row>
                <v-row>
                  <v-col>
                    <v-select
                      label="Предметы"
                      :items="subjects"
                      v-model="activeSubjects"
                      :item-title="(value) => value.name"
                      :item-value="(value) => value.id"
                      return-object
                      multiple
                    ></v-select>
                  </v-col>
                </v-row>
              </v-container>
              <v-container v-for="subject in activeSubjects" :key="subject.id">
                <v-row>
                  <v-col cols="2">
                    <span>{{ subject.name }}:</span>
                  </v-col>
                  <v-col>
                    <v-row
                      v-for="lesson in activeGroup.lessons
                        .filter((value) => value.subject.id === subject.id)
                        .filter(
                          (value, index, array) =>
                            array.findIndex((item) => item.teacher?.id === value.teacher?.id) ===
                            index
                        )
                        .sort((a, b) =>
                          `${a.teacher?.lastname} ${a.teacher?.firstname}`.localeCompare(
                            `${b.teacher?.lastname} ${b.teacher?.firstname}`
                          )
                        )"
                      :key="lesson.id"
                    >
                      <v-col>
                        <v-select
                          label="Учитель"
                          :items="
                            teachersDetail
                              .filter((teacher) =>
                                teacher.subjects.some((value) => value.id === subject.id)
                              )
                              .map(({ subjects, ...value }) => value)
                          "
                          :model-value="lesson.teacher"
                          @update:model-value="
                            (teacher) =>
                              activeGroup.lessons
                                .filter((value) => value.subject.id === subject.id)
                                .filter((value) => value.teacher?.id === lesson.teacher?.id)
                                .forEach((item) => (item.teacher = teacher))
                          "
                          :item-title="(value) => `${value.lastname} ${value.firstname}`"
                          :item-value="(value) => value.id"
                          return-object
                        ></v-select>
                      </v-col>
                      <v-col>
                        <v-select
                          label="Кабинет"
                          :items="rooms"
                          :model-value="lesson.room"
                          @update:model-value="
                            (room) =>
                              activeGroup.lessons
                                .filter((value) => value.subject.id === subject.id)
                                .filter((value) => value.teacher?.id === lesson.teacher?.id)
                                .forEach((item) => (item.room = room))
                          "
                          :item-title="(value) => value.name"
                          :item-value="(value) => value.id"
                          return-object
                        ></v-select>
                      </v-col>
                      <v-col>
                        <v-select
                          label="Часов в неделю"
                          :items="hours"
                          :model-value="
                            activeGroup.lessons
                              .filter((value) => value.subject.id === subject.id)
                              .filter((value) => value.teacher?.id === lesson.teacher?.id).length
                          "
                          @update:model-value="
                            (hours) => updateHours(hours, subject, lesson.teacher)
                          "
                        ></v-select>
                      </v-col>
                    </v-row>
                  </v-col>
                </v-row>
              </v-container>
            </v-card-text>

            <v-card-actions>
              <v-spacer></v-spacer>
              <v-btn color="blue-darken-1" variant="text" @click="closeDialog()"> Отменить </v-btn>
              <v-btn
                color="blue-darken-1"
                variant="text"
                @click="activeGroup.id === 0 ? addGroup() : editGroup()"
              >
                {{ activeGroup.id === 0 ? 'Добавить' : 'Изменить' }}
              </v-btn>
            </v-card-actions>
          </v-card>
        </v-dialog>
        <v-dialog v-model="dialogDelete" max-width="520px">
          <v-card>
            <v-card-title class="text-h5">Вы уверены, что хотите удалить класс?</v-card-title>
            <v-card-actions>
              <v-spacer></v-spacer>
              <v-btn color="blue-darken-1" variant="text" @click="closeDialogDelete()"
                >Cancel</v-btn
              >
              <v-btn color="blue-darken-1" variant="text" @click="deleteGroup()">OK</v-btn>
              <v-spacer></v-spacer>
            </v-card-actions>
          </v-card>
        </v-dialog>
      </v-toolbar>
    </template>
    <template v-slot:[`item.name`]="{ item }">
      {{ `${item.number} ${item.letter}` }}
    </template>
    <template v-slot:[`item.actions`]="{ item }">
      <v-icon class="me-2" size="small" @click="startEditingGroup(item)"> mdi-pencil </v-icon>
      <v-icon size="small" @click="startDeletingGroup(item)"> mdi-delete </v-icon>
    </template>
    <template v-slot:no-data> Пока здесь нет классов... </template>
  </v-data-table>
</template>
