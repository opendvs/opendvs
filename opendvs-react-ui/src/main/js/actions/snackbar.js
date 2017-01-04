export const TOGGLE_SNACKBAR = 'TOGGLE_SNACKBAR'

export const toggleSnackbar = (open, message) => ({
	type: TOGGLE_SNACKBAR,
	open: open,
	message: message
})