import {
  RECEIVE_USER_INFO,
  RECEIVE_USER_TOKENS
} from '../actions/auth'


const user = (state = {
	  id: 'Anonymous',
	  tokens: []
	}, action) => {
	  switch (action.type) {
	  case RECEIVE_USER_INFO:
	      return {
	        ...state,
	        id: action.user.id
	      }
	  case RECEIVE_USER_TOKENS:
	      return {
	        ...state,
	        tokens: action.tokens
	      }
	    default:
	      return state
	  }
	}


export default user