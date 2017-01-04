import { connect } from 'react-redux'
import ProjectList from '../components/project/ProjectList'

const mapStateToProps = state => {
  const { projects } = state

  const {
    isFetching,
    items: projectsItems,
    page: page
  } = projects;

  return {
    projects,
    isFetching,
    page
  }
}

const ProjectListContainer = connect(
  mapStateToProps
)(ProjectList)

export default ProjectListContainer
