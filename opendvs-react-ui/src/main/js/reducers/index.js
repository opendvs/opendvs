import { combineReducers } from 'redux'
import projects from './projects'
import addProject from './addProject'
import snackbar from './snackbar'

const opendvsApp = combineReducers({
    projects,
    addProject,
    snackbar
})

export default opendvsApp
