import { combineReducers } from 'redux'
import projects from './projects'
import components from './components'
import addProject from './addProject'
import snackbar from './snackbar'
import project from './project'

const opendvsApp = combineReducers({
    projects,
    addProject,
    snackbar,
    components,
    project
})

export default opendvsApp
