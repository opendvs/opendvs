import { API_URL } from '../config.js'
import { toggleSnackbarError } from '../actions/snackbar'

export const RECEIVE_USER_INFO = 'RECEIVE_USER_INFO'
	
export const handleRequestErrors = (response, dispatch) => {
	if (response.type == "opaqueredirect") {
		window.location.href = API_URL + "/users/login?redirectUrl=" + encodeURI(window.location.href).replace("#", "%23")  
		return null
	}

	if (response.status == 404) {
		window.location.href = '/#/errors/404'
		return null
	}
	if (response.status == 403) {
		window.location.href = '/#/errors/403'
		return null
	}
	
	if (response.status >= 400) {
		response.json().then(response => {
			console.log(response)
	    	dispatch(toggleSnackbarError(true, response.message));
	}).catch(error => 
	    	dispatch(toggleSnackbarError(true, "Internal server error, " + error)))
    	return null;
	}

	return response
}

export const receiveUserInfo = (data) => ({
  type: RECEIVE_USER_INFO,
  user: data
})


export const fetchUserInfo = (id) => (dispatch) => {
  return fetch(`${API_URL}/users/me`, {credentials: 'include', redirect: 'manual'})
	.then((res) => handleRequestErrors(res, dispatch))
    .then(result=>result.json())
    .then(items=> {
    	var data = JSOG.decode(items);
    	dispatch(receiveUserInfo(data));
    });
}