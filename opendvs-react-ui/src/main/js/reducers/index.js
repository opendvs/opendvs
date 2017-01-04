import { combineReducers } from 'redux'
import projects from './projects'
import components from './components'
import addProject from './addProject'
import snackbar from './snackbar'

const opendvsApp = combineReducers({
    projects,
    addProject,
    snackbar,
    components
})

export default opendvsApp
