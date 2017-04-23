import { API_URL } from '../config.js'

export const handleRequestErrors = (response) => {
	if (response.type == "opaqueredirect") {
		window.location.href = API_URL + "/users/login?redirectUrl=" + encodeURI(window.location.href).replace("#", "%23")  
		return null
	}
	console.log("RESPONSE", response)
	return response
}