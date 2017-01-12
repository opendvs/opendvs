import * as JSOG from 'jsog'
import { toggleSnackbar } from '../actions/snackbar'
import { API_URL } from '../config.js'

export const REQUEST_PROJECTS = 'REQUEST_PROJECTS'
export const RECEIVE_PROJECTS = 'RECEIVE_PROJECTS'
export const SELECT_PAGE = 'SELECT_PROJECTS_PAGE'
	
export const TOGGLE_PROJECT_ADD_DIALOG = 'TOGGLE_PROJECT_ADD_DIALOG'
export const REQUEST_PROJECT_TYPES = 'REQUEST_PROJECT_TYPES'
export const RECEIVE_PROJECT_TYPES = 'RECEIVE_PROJECT_TYPES'
export const SELECT_PROJECT_TYPE = 'SELECT_PROJECT_TYPE'

export const UPDATE_PROJECT_ADD_FORM_PROPERTY_FIELD = 'UPDATE_PROJECT_ADD_FORM_PROPERTY_FIELD'
export const UPDATE_PROJECT_ADD_FORM_FIELD = 'UPDATE_PROJECT_ADD_FORM_FIELD'
export const CREATE_PROJECT_REQUEST = 'CREATE_PROJECT_REQUEST'
export const CREATE_PROJECT = 'CREATE_PROJECT'

export const REQUEST_PROJECT = 'REQUEST_PROJECT'
export const RECEIVE_PROJECT = 'RECEIVE_PROJECT'
export const REQUEST_ARTIFACTS = 'REQUEST_ARTIFACTS'
export const RECEIVE_ARTIFACTS = 'RECEIVE_ARTIFACTS'
export const REQUEST_ARTIFACT = 'REQUEST_ARTIFACT'
export const RECEIVE_ARTIFACT = 'RECEIVE_ARTIFACT'
export const PAGE_COMPONENTS = 'PAGE_ARTIFACT_COMPONENTS'
export const SELECT_COMPONENT_PAGE = 'SELECT_ARTIFACT_COMPONENT_PAGE'

export const TOGGLE_COMPONENT_DIALOG = 'TOGGLE_ARTIFACT_COMPONENT_DIALOG'

export const ARTIFACT_UPLOADED = 'ARTIFACT_UPLOADED'

export const selectPage = (newPage) => ({
	type: SELECT_PAGE,
	newPage: newPage
})

export const requestProject = () => ({
  type: REQUEST_PROJECT
})

export const receiveProject = (data) => ({
  type: RECEIVE_PROJECT,
  project: data
})


export const requestArtifacts = () => ({
  type: REQUEST_ARTIFACTS
})

export const receiveArtifacts = (data) => ({
  type: RECEIVE_ARTIFACTS,
  artifacts: data.content
})

export const requestArtifact = () => ({
  type: REQUEST_ARTIFACT
})

export const receiveArtifact = (data) => ({
  type: RECEIVE_ARTIFACT,
  artifact: data
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

const artifactUploaded = (item) => ({
  type: ARTIFACT_UPLOADED,
  artifact: item
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





export const fetchProject = (id) => (dispatch) => {
  dispatch(requestProject())
  return fetch(`${API_URL}/project/${id}`)
    .then(result=>result.json())
    .then(items=> {
    	var data = JSOG.decode(items);
    	dispatch(receiveProject(data));
    	dispatch(fetchArtifacts(id));
    });
}

export const selectArtifact = (projectId, artId) => (dispatch) => {
  dispatch(requestArtifact())
  return fetch(`${API_URL}/project/${projectId}/artifact/${artId}`)
    .then(result=>result.json())
    .then(items=> {
    	var data = JSOG.decode(items);
    	// TODO: move to separate field to be able to draw component graph in the future
    	data.components = removeDuplicateUIDs(data.components);
    	data.components.sort((a,b) => a.name.localeCompare(b.name));
    	dispatch(receiveArtifact(data));
		dispatch(pageComponents());
    });
}



const fetchArtifacts = (id) => (dispatch) => {
  dispatch(requestArtifacts())
  return fetch(`${API_URL}/project/${id}/artifacts`)
    .then(result=>result.json())
    .then(items=> {
    	var data = JSOG.decode(items);
    	dispatch(receiveArtifacts(data));
    	if (data.content.length > 0) {
    		dispatch(selectArtifact(id, data.content[0].id));
    	}
    });
}


export const selectComponentPage = (newPage) => ({	
	type: SELECT_COMPONENT_PAGE,
	page: newPage
})

export const pageComponents = () => (dispatch, getState) => {
	var selectedArtifact = getState().project.selectedArtifact;
	var page = getState().project.page;

	if (selectedArtifact && selectedArtifact.components) {
		var offset = (page.current - 1) * page.size
		var components = selectedArtifact.components.slice(offset, offset + page.size);
		dispatch(receivePagedComponents(components));
	}
}	


export const receivePagedComponents = (components) => ({
	type: PAGE_COMPONENTS,
	components: components
})

export const openComponentDialog = (component) => (dispatch) => {
	dispatch(toggleComponentDialog(true, {}, undefined, undefined));
	return fetch(`${API_URL}/components/${component.group}:${component.name}/detail`)
    .then(result=>result.json())
    .then(items=> {
    	var data = JSOG.decode(items);
    	dispatch(toggleComponentDialog(true, data, component.version, component.state));
    });
}
export const toggleComponentDialog = (open, component, version, state) => {	
  return ({
	type: TOGGLE_COMPONENT_DIALOG,
	open: open,
	component: component,
	version: version,
	state: state
  })
}

export const uploadArtifact = (formData, projectId) => (dispatch) => {
	// TODO: dispatch UPLOAD_STARTED action
	  return new Promise((resolve, reject) => {
		    var xhr = new XMLHttpRequest();
		    
		    xhr.open('post', `${API_URL}/project/${projectId}/upload`, true);
		    
		    xhr.onload = function () {
		      if (this.status >= 200 && this.status < 300) {
		        resolve(this.response);
		      } else {
		        reject(this.statusText);
		      }
		    };
		    // TODO: track progress
		    
		    xhr.send(formData);

		  }).then((response) => {
			var data = JSOG.decode(JSON.parse(response));
			dispatch(artifactUploaded(data));

			data.components = removeDuplicateUIDs(data.components);
	    	data.components.sort((a,b) => a.name.localeCompare(b.name));
	    	dispatch(receiveArtifact(data));
			dispatch(pageComponents());
			dispatch(toggleSnackbar(true, `Artifact ${data.name} was successfully uploaded`));
		  })
}

const removeDuplicateUIDs = (components) => {
	  var keys = {};
	  var newarr = [];
	  for (var i in components) {
		  var obj = components[i];
		  var k = obj.group + "|" + obj.name + "|" + obj.version;
		  if (!(k in keys)) {
			  keys[k] = [];
		  }

		  keys[k].push(obj);
	  }

	  for (var k in keys) {
		  var e = keys[k][0];
		  e.occurrences = keys[k].length;
		  newarr.push(e);
	  }

	  return newarr;
  }


export const handleArtifactUpdate = (event) => (dispatch, getState) => {
	var project = getState().project;

	if (project && project.item && project.item.id == event.project.id) {
	    if (!project.selectedArtifact || project.selectedArtifact.id == event.artifact.id) {
	    	dispatch(selectArtifact(event.project.id, event.artifact.id));
		}
	}

	dispatch(toggleSnackbar(true, `Artifact ${event.artifact.name} changed state to ${event.artifact.state}`));
}