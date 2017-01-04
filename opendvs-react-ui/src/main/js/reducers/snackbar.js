import {
  TOGGLE_SNACKBAR
} from '../actions/snackbar'


const snackbar = (state = {
	    open: false,
	    message: ""
	  }, action) => {
	  switch (action.type) {
	  	case TOGGLE_SNACKBAR:
	  		return {
	  			...state,
	  			open: action.open,
	  			message: action.message
	  		}
	  	default:
	      return state
	  }
	}


export default snackbar