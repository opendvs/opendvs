import { connect } from 'react-redux'
import Project from '../components/project/Project'

const mapStateToProps = state => {
  return state.project
}

const ProjectContainer = connect(
  mapStateToProps
)(Project)

export default ProjectContainer
