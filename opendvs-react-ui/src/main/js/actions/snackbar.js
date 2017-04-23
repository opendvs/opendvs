export const TOGGLE_SNACKBAR = 'TOGGLE_SNACKBAR'
export const TOGGLE_SNACKBAR_ERROR = 'TOGGLE_SNACKBAR_ERROR'

export const toggleSnackbar = (open, message) => ({
		type: TOGGLE_SNACKBAR,
		open: open,
		message: message
})
export const toggleSnackbarError = (open, message) => ({
		type: TOGGLE_SNACKBAR_ERROR,
		open: open,
		message: message
})