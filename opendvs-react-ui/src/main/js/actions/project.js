import * as JSOG from 'jsog'
import { toggleSnackbar } from '../actions/snackbar'
import { API_URL } from '../config.js'

export const REQUEST_PROJECTS = 'REQUEST_PROJECTS'
export const RECEIVE_PROJECTS = 'RECEIVE_PROJECTS'
export const SELECT_PAGE = 'SELECT_PAGE'
	
export const TOGGLE_PROJECT_ADD_DIALOG = 'TOGGLE_PROJECT_ADD_DIALOG'
export const REQUEST_PROJECT_TYPES = 'REQUEST_PROJECT_TYPES'
export const RECEIVE_PROJECT_TYPES = 'RECEIVE_PROJECT_TYPES'
export const SELECT_PROJECT_TYPE = 'SELECT_PROJECT_TYPE'

export const UPDATE_PROJECT_ADD_FORM_PROPERTY_FIELD = 'UPDATE_PROJECT_ADD_FORM_PROPERTY_FIELD'
export const UPDATE_PROJECT_ADD_FORM_FIELD = 'UPDATE_PROJECT_ADD_FORM_FIELD'
export const CREATE_PROJECT_REQUEST = 'CREATE_PROJECT_REQUEST'
export const CREATE_PROJECT = 'CREATE_PROJECT'

export const selectPage = (newPage) => ({
	type: SELECT_PAGE,
	newPage: newPage
})

export const requestProjects = () => ({
  type: REQUEST_PROJECTS
})

export const requestProjectTypes = () => ({
  type: REQUEST_PROJECT_TYPES
})

export const receiveProjectTypes = (data) => ({
  type: RECEIVE_PROJECT_TYPES,
  types: data
})

export const receiveProjects = (data) => ({
  type: RECEIVE_PROJECTS,
  projects: data.content,
  page: {
	  total: data.totalPages,
	  size: data.size,
	  current: data.number + 1
  	}
})

export const fetchProjects = (page) => (dispatch) => {
  dispatch(requestProjects())
  return fetch(`${API_URL}/projects?size=${page.size}&page=${page.current - 1}`)
    .then(result=>result.json())
    .then(items=> {
    	var data = JSOG.decode(items);
    	dispatch(receiveProjects(data));
    });
}

const shouldFetchProjects = (state, page) => {
  const projects = state.projects

  if (!projects) {
    return true
  }

  if (projects.isFetching) {
    return false
  }

  return projects.page.total == undefined || projects.page.current != page.current || projects.page.size != page.size 
}

export const fetchProjectsIfNeeded = (page) => (dispatch, getState) => {
  if (shouldFetchProjects(getState(), page)) {
    return dispatch(fetchProjects(page))
  }
}


export const toggleProjectDialog = (opened) =>  {	
  return ({
	type: TOGGLE_PROJECT_ADD_DIALOG,
	opened: opened
  })
}

export const fetchProjectTypes = () => (dispatch) => {
  return fetch(`${API_URL}/project/types`)
    .then(result=>result.json())
    .then(items=> {
    	var data = JSOG.decode(items);
    	dispatch(receiveProjectTypes(data));
    });
}

export const selectProjectType = (type) => {
	return ({
		type: SELECT_PROJECT_TYPE,
		projectType: type
	})
}

export const updateFormField = (field, value) => {
	return ({
		type: UPDATE_PROJECT_ADD_FORM_FIELD,
		field: field,
		value: value
	})
}

export const updateFormPropertyField = (field, value) => {
	return ({
		type: UPDATE_PROJECT_ADD_FORM_PROPERTY_FIELD,
		field: field,
		value: value
	})
}

export const newProjectCreated = (project) => {
	return ({
		type: CREATE_PROJECT,
		project: project
	})
}

export const createProjectRequested = (project) => {
	
	return ({
		type: CREATE_PROJECT_REQUEST,
		project: project
	})
}

export const createNewProject = (project) => (dispatch, state) => {
	return fetch(`${API_URL}/projects`, {
	  method: 'POST',
	  headers: {
	    'Accept': 'application/json',
	    'Content-Type': 'application/json',
	  },
	  body: JSON.stringify(project)
	})
	.then(result=>result.json())
    .then(item => {
    	var proj = JSOG.decode(item);
    	dispatch(newProjectCreated(proj));
    	dispatch(toggleSnackbar(true, `Project ${proj.name} successfully created`));
	})
}