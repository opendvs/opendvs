import { connect } from 'react-redux'
import ProjectGraph from '../components/project/ProjectGraph'

const mapStateToProps = state => {
  return state.project
}

const ProjectGraphContainer = connect(
  mapStateToProps
)(ProjectGraph)

export default ProjectGraphContainer
