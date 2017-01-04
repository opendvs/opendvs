import { connect } from 'react-redux'
import ComponentVersionDialog from '../components/component/ComponentVersionDialog'

const mapStateToProps = state => {
  return state.components
}

const ComponentVersionDialogContainer = connect(
  mapStateToProps
)(ComponentVersionDialog)

export default ComponentVersionDialogContainer
