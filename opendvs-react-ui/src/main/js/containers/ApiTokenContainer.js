import { connect } from 'react-redux'
import ApiTokenList from '../components/user/ApiTokenList'

const mapStateToProps = state => {
  return state.user
}

const ApiTokenContainer = connect(
  mapStateToProps
)(ApiTokenList)

export default ApiTokenContainer
