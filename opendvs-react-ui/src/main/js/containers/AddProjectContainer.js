import { connect } from 'react-redux'
import AddProjectDialog from '../components/project/AddProjectDialog'

const mapStateToProps = state => {
  const { addProject } = state

  const {
    project: project,
    selectedType: selectedType,
    opened: opened,
    projectTypes: projectTypes,
    creating: creating
  } = addProject

  return {
    project,
    selectedType,
    opened,
    projectTypes,
    creating
  }
}

const AddProjectContainer = connect(
  mapStateToProps
)(AddProjectDialog)

export default AddProjectContainer
