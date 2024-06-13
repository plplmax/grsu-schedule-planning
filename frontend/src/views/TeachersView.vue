<script setup lang="ts">
import type { Subject } from '@/subjects/Subject'
import { onMounted, ref, type Ref } from 'vue'
import teachersService from '@/teachers/teachers.service'
import type { Teacher, TeacherDetail, TeacherDetailUi } from '@/teachers/Teacher'
import subjectsService from '@/subjects/subjects.service'
import type { Group } from '@/groups/group'
import type { SubgroupDetail } from '@/divisions/subgroup'
import groupsService from '@/groups/groups.service'
import divisionsService from '@/divisions/divisions.service'
import type { Room } from '@/rooms/room'
import roomsService from '@/rooms/rooms.service'
import type { LessonUi } from '@/lessons/lesson'

const teachers = ref<Teacher[]>([]) as Ref<Teacher[]>
const subjects = ref<Subject[]>([])
const groups = ref<Group[]>([])
const subgroups = ref<SubgroupDetail[]>([])
const rooms = ref<Room[]>([])
const hours = [...Array(11).keys()].slice(1)
const headers = [
  { title: 'Фамилия', value: 'lastname' },
  { title: 'Имя', value: 'firstname' },
  { title: 'Предметы', value: 'subjects' },
  { title: 'Действия', value: 'actions' }
]
const dialog = ref(false)
const dialogDelete = ref(false)
const defaultTeacher: TeacherDetailUi = {
  id: 0,
  firstname: '',
  lastname: '',
  subjects: [],
  lessons: []
}
const activeTeacher = ref<TeacherDetailUi>({ ...defaultTeacher }) as Ref<TeacherDetailUi>
const resetActiveTeacher = () => (activeTeacher.value = { ...defaultTeacher })
const closeDialog = () => (dialog.value = false)
const closeDialogDelete = () => (dialogDelete.value = false)
const addTeacher = () =>
  teachersService
    .add(activeTeacher.value as TeacherDetail)
    .then((value) => {
      teachers.value.push(value)
      closeDialog()
    })
    .catch((error) => console.error(error))
const editTeacher = () =>
  teachersService
    .edit(activeTeacher.value as TeacherDetail)
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
  teachersService
    .findById(item.id)
    .then((value) => {
      activeTeacher.value = { ...value }
      dialog.value = true
    })
    .catch((error) => console.error(error))
}
const startDeletingTeacher = (item: Teacher) => {
  activeTeacher.value = { ...item, lessons: [] }
  dialogDelete.value = true
}

const updateHours = (hours: number, lesson: LessonUi) => {
  const lessons = activeTeacher.value.lessons
  const indexes = lessons.reduceRight((acc: number[], value, index) => {
    if (
      value.subject.id === lesson.subject.id &&
      value.group?.id === lesson.group?.id &&
      value.subgroup?.id === lesson.subgroup?.id &&
      value.room?.id === lesson.room?.id
    ) {
      acc.push(index)
    }
    return acc
  }, [])
  const old = indexes.length
  const diff = hours - old
  if (diff > 0) {
    lessons.push(
      ...Array.from(Array(diff), () => ({ ...lessons[indexes[0]], id: 0, timeslot: null }))
    )
  }

  if (diff < 0) {
    indexes.slice(0, Math.abs(diff)).forEach((value) => lessons.splice(value, 1))
  }
}

const updateSubjects = (subjects: Subject[]) => {
  const deleted = activeTeacher.value.subjects.filter(
    (oldSubject) => subjects.findIndex((newSubject) => newSubject.id === oldSubject.id) === -1
  )

  activeTeacher.value.lessons = activeTeacher.value.lessons.filter(
    (lesson) => deleted.findIndex((subject) => subject.id === lesson.subject.id) === -1
  )

  activeTeacher.value.subjects = subjects.sort((a, b) => a.name.localeCompare(b.name))
}

const addGroup = (subject: Subject) =>
  activeTeacher.value.lessons.push({
    id: 0,
    teacher: {
      id: activeTeacher.value.id,
      firstname: activeTeacher.value.firstname,
      lastname: activeTeacher.value.lastname
    },
    subject: subject,
    timeslot: null
  })

onMounted(() => {
  teachersService
    .all()
    .then((value) => (teachers.value = value))
    .catch((error) => console.error(error))

  subjectsService
    .all()
    .then((value) => (subjects.value = value))
    .catch((error) => console.error(error))

  groupsService
    .all()
    .then((value) => (groups.value = value))
    .catch((error) => console.error(error))

  divisionsService
    .allSubgroups()
    .then((value) => (subgroups.value = value))
    .catch((error) => console.error(error))

  roomsService
    .all()
    .then((value) => (rooms.value = value))
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
        <v-dialog v-model="dialog" max-width="1200px">
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
              <v-container>
                <v-row>
                  <v-col>
                    <v-text-field
                      label="Фамилия"
                      :model-value="activeTeacher.lastname"
                      @update:model-value="(value) => (activeTeacher.lastname = value)"
                    ></v-text-field>
                  </v-col>
                  <v-col>
                    <v-text-field
                      label="Имя"
                      :model-value="activeTeacher.firstname"
                      @update:model-value="(value) => (activeTeacher.firstname = value)"
                    ></v-text-field>
                  </v-col>
                </v-row>
                <v-row>
                  <v-col
                    ><v-select
                      label="Предметы"
                      :items="subjects"
                      :item-title="(value) => value.name"
                      :item-value="(value) => value.id"
                      :model-value="activeTeacher.subjects"
                      @update:model-value="updateSubjects"
                      return-object
                      multiple
                    ></v-select
                  ></v-col>
                </v-row>
              </v-container>
              <v-container v-for="subject in activeTeacher.subjects" :key="subject.id">
                <v-row>
                  <v-col cols="2">
                    <span>{{ subject.name }}:</span>
                  </v-col>
                  <v-col>
                    <v-row
                      v-for="lesson in activeTeacher.lessons
                        .filter((value) => value.subject.id === subject.id)
                        .filter(
                          (value, index, array) =>
                            array.findIndex(
                              (item) =>
                                item.group?.id === value.group?.id &&
                                item.subgroup?.id === value.subgroup?.id &&
                                item.room?.id === value.room?.id
                            ) === index
                        )"
                      :key="lesson.id"
                    >
                      <v-col>
                        <v-autocomplete
                          label="Класс"
                          :items="groups"
                          :model-value="lesson.group"
                          @update:model-value="
                            (group) =>
                              activeTeacher.lessons
                                .filter((value) => value.subject.id === subject.id)
                                .filter((value) => value.group?.id === lesson.group?.id)
                                .filter((value) => value.subgroup?.id === lesson.subgroup?.id)
                                .filter((value) => value.room?.id === lesson.room?.id)
                                .forEach((item) => (item.group = group))
                          "
                          :item-title="(value) => `${value.number} ${value.letter}`"
                          return-object
                          prepend-icon="mdi-delete"
                          @click:prepend="updateHours(0, lesson)"
                        ></v-autocomplete>
                      </v-col>
                      <v-col>
                        <v-select
                          label="Подгруппа"
                          :items="subgroups"
                          :model-value="lesson.subgroup"
                          @update:model-value="
                            (subgroup) =>
                              activeTeacher.lessons
                                .filter((value) => value.subject.id === subject.id)
                                .filter((value) => value.group?.id === lesson.group?.id)
                                .filter((value) => value.subgroup?.id === lesson.subgroup?.id)
                                .filter((value) => value.room?.id === lesson.room?.id)
                                .forEach((item) => (item.subgroup = subgroup))
                          "
                          :item-title="(value) => value.name"
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
                              activeTeacher.lessons
                                .filter((value) => value.subject.id === subject.id)
                                .filter((value) => value.group?.id === lesson.group?.id)
                                .filter((value) => value.subgroup?.id === lesson.subgroup?.id)
                                .filter((value) => value.room?.id === lesson.room?.id)
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
                            activeTeacher.lessons
                              .filter((value) => value.subject.id === subject.id)
                              .filter((value) => value.group?.id === lesson.group?.id)
                              .filter((value) => value.subgroup?.id === lesson.subgroup?.id)
                              .filter((value) => value.room?.id === lesson.room?.id).length
                          "
                          @update:model-value="(hours) => updateHours(hours, lesson)"
                        ></v-select>
                      </v-col>
                    </v-row>
                    <v-row>
                      <v-col>
                        <v-btn @click="addGroup(subject)">Добавить класс</v-btn>
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
