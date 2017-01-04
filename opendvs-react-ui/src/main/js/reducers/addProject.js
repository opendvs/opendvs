import {
  TOGGLE_PROJECT_ADD_DIALOG, RECEIVE_PROJECT_TYPES, SELECT_PROJECT_TYPE, UPDATE_PROJECT_ADD_FORM_FIELD, UPDATE_PROJECT_ADD_FORM_PROPERTY_FIELD, CREATE_PROJECT,  CREATE_PROJECT_REQUEST
} from '../actions/project'


const addProject = (state = {
	    project: {name: undefined, typeProperties: {}, majorVersionOffset: 0},
	    selectedType: {},
	    opened: false,
	    creating: false,
	    projectTypes: []
	  }, action) => {
	  switch (action.type) {
	  	case RECEIVE_PROJECT_TYPES:
	  		return {
	  			...state,
	  			projectTypes: action.types
	  		}
	    case TOGGLE_PROJECT_ADD_DIALOG:
	      return {
	        ...state,
	        opened: action.opened
	      }
	    case SELECT_PROJECT_TYPE:
	      var project = state.project;
	      project.type = action.projectType.id;
	      return {
	    	...state,
	    	selectedType: action.projectType,
	    	project: project
	      }
	    case UPDATE_PROJECT_ADD_FORM_FIELD:
	      var project = state.project;
	      if (action.field == "majorVersionOffset") {
	    	  project[action.field] = parseInt(action.value);
	      } else {
	    	  project[action.field] = action.value;
	      }

	      return {
	    	...state,
	    	project
	      }
	    case UPDATE_PROJECT_ADD_FORM_PROPERTY_FIELD:
	      var project = state.project;
	      project.typeProperties[action.field] = action.value;
	      return {
	    	...state,
	    	project
	      }
	    case CREATE_PROJECT_REQUEST:
	    	return {
	    		...state,
	    		creating: true
	    	}
	    case CREATE_PROJECT:
	    	var project = {name: undefined, typeProperties: {}, majorVersionOffset: 0};
	    	return {
	    		...state,
	    		project,
	    		selectedType: {},
	    		opened: false,
	    		creating: false
	    	}
	    default:
	      return state
	  }
	}


export default addProject