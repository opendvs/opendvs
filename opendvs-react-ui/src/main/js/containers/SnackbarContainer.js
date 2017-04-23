import React, {Component} from 'react'
import { connect } from 'react-redux'
import Snackbar from 'material-ui/Snackbar'
import { toggleSnackbar } from '../actions/snackbar'
import {red500} from 'material-ui/styles/colors'

const SnackbarComponent = ({ message, open, type, dispatch }) => {
	let duration = 5000
	let style = {}
	if (type == "error") {
		duration = 10000
		style={backgroundColor: red500}
	}
	
	return (
	<Snackbar
    open={open}
    message={message}
    autoHideDuration={duration}
	bodyStyle={style}
    onRequestClose={() => dispatch(toggleSnackbar(false, ""))}
  />
 )
}

const mapStateToProps = state => {
	return state.snackbar
}

const SnackbarContainer = connect(
  mapStateToProps
)(SnackbarComponent)

export default SnackbarContainer
