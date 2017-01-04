import React, {Component} from 'react'
import { connect } from 'react-redux'
import Snackbar from 'material-ui/Snackbar'
import { toggleSnackbar } from '../actions/snackbar'

const SnackbarComponent = ({ message, open, dispatch }) => (
	<Snackbar
    open={open}
    message={message}
    autoHideDuration={5000}
    onRequestClose={() => dispatch(toggleSnackbar(false, ""))}
  />
 )

const mapStateToProps = state => {
  const { snackbar } = state

  const {
    open,
    message
  } = snackbar;

  return {
    open,
    message
  }
}

const SnackbarContainer = connect(
  mapStateToProps
)(SnackbarComponent)

export default SnackbarContainer
