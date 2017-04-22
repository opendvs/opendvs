import {
  REQUEST_VULNERABILITIES, RECEIVE_VULNERABILITIES, SELECT_PAGE, TOGGLE_VULNERABILITY_DIALOG
} from '../actions/vulnerability'


const vulnerabilities = (state = {
	  isFetching: false,
	  didInvalidate: false,
	  items: [],
	  page: { current: 1, size: 10 },
	  dialog: {open: false, vulnerability: {}}
	}, action) => {
	  switch (action.type) {
	    case TOGGLE_VULNERABILITY_DIALOG:
		      return {
		        ...state,
		        dialog: {open: action.open, vulnerability: action.vulnerability}
		      }
		      
	    case REQUEST_VULNERABILITIES:
	      return {
	        ...state,
	        isFetching: true,
	        didInvalidate: false
	      }

	    case RECEIVE_VULNERABILITIES:
	      return {
	        ...state,
	        isFetching: false,
	        didInvalidate: false,
	        items: action.vulnerabilities,
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


export default vulnerabilities