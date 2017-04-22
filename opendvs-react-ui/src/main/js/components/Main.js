import React, {Component} from 'react';
import { Router, Route, hashHistory } from 'react-router'
import ProjectListContainer from '../containers/ProjectListContainer'
import ComponentListContainer from '../containers/ComponentListContainer'
import VulnerabilityListContainer from '../containers/VulnerabilityListContainer'
import ProjectContainer from '../containers/ProjectContainer'
import ProjectGraphContainer from '../containers/ProjectGraphContainer'
import MainContainer from '../containers/MainContainer'

class Main extends Component {


  render() {
	  return (
	  <Router history={hashHistory}>
	  	<Route path="/" component={MainContainer}>
  		<Route path="projects" components={{content: ProjectListContainer}}/>
        <Route path="project/:projectId/details" components={{content: ProjectContainer}}/>
        <Route path="project/:projectId/graph" components={{content: ProjectGraphContainer}}/>
  		<Route path="components" components={{content: ComponentListContainer}}/>
  		<Route path="vulnerabilities" components={{content: VulnerabilityListContainer}}/>
        </Route>
      </Router>
    )
  }
}

export default Main;
