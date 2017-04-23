import {
	  TOGGLE_SNACKBAR,
	  TOGGLE_SNACKBAR_ERROR,
} from '../actions/snackbar'


const snackbar = (state = {
	    open: false,
	    message: "",
	    type: "normal"
	  }, action) => {
	  switch (action.type) {
	  case TOGGLE_SNACKBAR:
	  		return {
	  			...state,
	  			open: action.open,
	  			message: action.message,
	  			type: "normal"
	  		}
	  case TOGGLE_SNACKBAR_ERROR:
	  		return {
	  			...state,
	  			open: action.open,
	  			message: action.message,
	  			type: "error"
	  		}
	  	default:
	      return state
	  }
	}


export default snackbar