import React, {Component} from 'react';
import { Router, Route, hashHistory } from 'react-router'
import ProjectListContainer from '../containers/ProjectListContainer'
import ComponentListContainer from '../containers/ComponentListContainer'
import ProjectContainer from '../containers/ProjectContainer'
import MainContainer from '../containers/MainContainer'

class Main extends Component {


  render() {
	  return (
	  <Router history={hashHistory}>
	  	<Route path="/" component={MainContainer}>
  		<Route path="projects" components={{content: ProjectListContainer}}/>
        <Route path="project/:projectId/details" components={{content: ProjectContainer}}/>
  		<Route path="components" components={{content: ComponentListContainer}}/>
        </Route>
      </Router>
    )
  }
}

export default Main;
