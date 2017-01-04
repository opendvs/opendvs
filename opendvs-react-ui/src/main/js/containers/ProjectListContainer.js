import { connect } from 'react-redux'
import ProjectList from '../components/project/ProjectList'

const mapStateToProps = state => {
  return state.projects
}

const ProjectListContainer = connect(
  mapStateToProps
)(ProjectList)

export default ProjectListContainer
