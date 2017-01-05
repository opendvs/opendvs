import {
  REQUEST_PROJECTS, RECEIVE_PROJECTS, SELECT_PAGE
} from '../actions/project'
import { PAGE_SIZE } from '../config.js'


const projects = (state = {
	  isFetching: false,
	  didInvalidate: false,
	  items: [],
	  page: { current: 1, size: PAGE_SIZE },
	  newProject: {}
	}, action) => {
	  switch (action.type) {
	    case REQUEST_PROJECTS:
	      return {
	        ...state,
	        isFetching: true,
	        didInvalidate: false
	      }

	    case RECEIVE_PROJECTS:
	      return {
	        ...state,
	        isFetching: false,
	        didInvalidate: false,
	        items: action.projects,
	        page: action.page
	      }

	    case SELECT_PAGE:
	      var page = { current: action.newPage, size: state.page.size, total: state.page.total };

	      return {
	        ...state,
	        page: page
	      }
	    default:
	      return state
	  }
	}


export default projects
