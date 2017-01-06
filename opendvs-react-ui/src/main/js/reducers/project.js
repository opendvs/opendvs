import {
  REQUEST_PROJECT, RECEIVE_PROJECT, REQUEST_ARTIFACTS, RECEIVE_ARTIFACTS, REQUEST_ARTIFACT, RECEIVE_ARTIFACT, PAGE_COMPONENTS, SELECT_COMPONENT_PAGE, TOGGLE_COMPONENT_DIALOG, ARTIFACT_UPLOADED
} from '../actions/project'
import { PAGE_SIZE } from '../config.js'

const project = (state = {
	  item: {},
	  isFetchingProject: true,
	  isFetchingArtifacts: true,
	  isFetchingArtifactDetail: true,
	  artifacts: [],
	  selectedArtifact: {},
      pagedComponents: [],
	  page: { current: 1, size: PAGE_SIZE },
	  componentDialog: {open: false, component: {}, state: undefined, version: undefined}
	}, action) => {
	  switch (action.type) {
	    case REQUEST_PROJECT:
	      return {
	        ...state,
	        isFetchingProject: true,
	        artifacts: [],
	        selectedArtifact: {},
	        pagedComponents: []
	      }

	    case RECEIVE_PROJECT:
	      return {
	        ...state,
	        isFetchingProject: false,
	        item: action.project
	      }

	    case REQUEST_ARTIFACT:
		      return {
		        ...state,
		        isFetchingArtifactDetail: true,
		        selectedArtifact: {},
		        pagedComponents: []
		      }

	    case RECEIVE_ARTIFACT:
	    	var page = { current: state.page.current, size: state.page.size, total: Math.ceil(action.artifact.components.length/state.page.size) }
	      return {
	        ...state,
	        isFetchingArtifactDetail: false,
	        selectedArtifact: action.artifact,
	        page: page
	      }

	    case REQUEST_ARTIFACTS:
		      return {
		        ...state,
		        isFetchingArtifacts: true
		      }

	    case RECEIVE_ARTIFACTS:
	      return {
	        ...state,
	        isFetchingProject: false,
	        artifacts: action.artifacts
	      }

	    case ARTIFACT_UPLOADED:
	      var artifacts = state.artifacts
	      artifacts.unshift(action.artifact)

	      return {
	    	...state,
	    	artifacts: artifacts
	      }
	    case PAGE_COMPONENTS:
	      return {
	        ...state,
	        pagedComponents: action.components
	      }

	    case SELECT_COMPONENT_PAGE:
    	  var page = { current: action.page, size: state.page.size, total: state.page.total };

	      return {
	        ...state,
	        page: page
	      }		     
	    case TOGGLE_COMPONENT_DIALOG:
		      return {
		        ...state,
		        componentDialog: {open: action.open, component: action.component, version: action.version, state: action.state}
		      }
	    default:
	      return state
	  }
	}


export default project
