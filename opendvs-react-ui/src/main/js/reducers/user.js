import {
  RECEIVE_USER_INFO
} from '../actions/auth'


const user = (state = {
	  id: 'Anonymous'
	}, action) => {
	  switch (action.type) {
	    case RECEIVE_USER_INFO:
		      return {
		        ...state,
		        id: action.user.id
		      }
	    default:
	      return state
	  }
	}


export default user