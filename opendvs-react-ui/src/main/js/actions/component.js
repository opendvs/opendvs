import * as JSOG from 'jsog'
import { toggleSnackbar } from '../actions/snackbar'
import { API_URL } from '../config.js'

export const REQUEST_COMPONENTS = 'REQUEST_COMPONENTS'
export const RECEIVE_COMPONENTS = 'RECEIVE_COMPONENTS'
export const SELECT_PAGE = 'SELECT_COMPONENTS_PAGE'
export const TOGGLE_VERSION_DIALOG = 'TOGGLE_COMPONENT_VERSION_DIALOG'
	

export const toggleVersionDialog = (open, component) =>  {	
  return ({
	type: TOGGLE_VERSION_DIALOG,
	open: open,
	component: component
  })
}

export const selectPage = (newPage) => ({
	type: SELECT_PAGE,
	newPage: newPage
})

export const requestComponents = () => ({
  type: REQUEST_COMPONENTS
})

export const receiveComponents = (data) => ({
  type: RECEIVE_COMPONENTS,
  components: data.content,
  page: {
	  total: data.totalPages,
	  size: data.size,
	  current: data.number + 1
  	}
})

export const fetchComponents = (page) => (dispatch) => {
  dispatch(requestComponents())
  return fetch(`${API_URL}/components?size=${page.size}&page=${page.current - 1}`)
    .then(result=>result.json())
    .then(items=> {
    	var data = JSOG.decode(items);
    	dispatch(receiveComponents(data));
    });
}

const shouldFetchComponents = (state, page) => {
  const components = state.components

  if (!components) {
    return true
  }

  if (components.isFetching) {
    return false
  }

  return components.page.total == undefined || components.page.current != page.current || components.page.size != page.size 
}

export const fetchComponentsIfNeeded = (page) => (dispatch, getState) => {
  if (shouldFetchComponents(getState(), page)) {
    return dispatch(fetchComponents(page))
  }
}
