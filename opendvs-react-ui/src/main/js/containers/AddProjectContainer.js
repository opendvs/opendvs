import { connect } from 'react-redux'
import AddProjectDialog from '../components/project/AddProjectDialog'

const mapStateToProps = state => {
  return state.addProject
}

const AddProjectContainer = connect(
  mapStateToProps
)(AddProjectDialog)

export default AddProjectContainer
