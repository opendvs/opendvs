import { connect } from 'react-redux'
import ComponentList from '../components/component/ComponentList'

const mapStateToProps = state => {
  return state.components
}

const ComponentListContainer = connect(
  mapStateToProps
)(ComponentList)

export default ComponentListContainer
