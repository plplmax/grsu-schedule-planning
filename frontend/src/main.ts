import './assets/main.css'

import { createApp } from 'vue'
import App from './App.vue'
import { library } from '@fortawesome/fontawesome-svg-core'
import router from './router'
import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome'
import { faXmark } from '@fortawesome/free-solid-svg-icons'

library.add(faXmark)

const app = createApp(App)

app.use(router)
app.component('font-awesome-icon', FontAwesomeIcon).mount('#app')
