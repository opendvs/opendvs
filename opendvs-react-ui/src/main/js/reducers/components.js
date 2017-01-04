import {
  REQUEST_COMPONENTS, RECEIVE_COMPONENTS, SELECT_PAGE, TOGGLE_VERSION_DIALOG
} from '../actions/component'


const components = (state = {
	  isFetching: false,
	  didInvalidate: false,
	  items: [],
	  page: { current: 1, size: 2 },
	  dialog: {open: false, component: {}}
	}, action) => {
	  switch (action.type) {

	    case TOGGLE_VERSION_DIALOG:
	      return {
	        ...state,
	        dialog: {open: action.open, component: action.component}
	      }
	    case REQUEST_COMPONENTS:
	      return {
	        ...state,
	        isFetching: true,
	        didInvalidate: false
	      }

	    case RECEIVE_COMPONENTS:
	      return {
	        ...state,
	        isFetching: false,
	        didInvalidate: false,
	        items: action.components,
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


export default components