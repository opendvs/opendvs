import { combineReducers } from 'redux'
import projects from './projects'
import components from './components'
import addProject from './addProject'
import snackbar from './snackbar'
import project from './project'
import vulnerabilities from './vulnerabilities'
import user from './user'

const opendvsApp = combineReducers({
    projects,
    addProject,
    snackbar,
    components,
    project,
    vulnerabilities,
    user
})

export default opendvsApp
