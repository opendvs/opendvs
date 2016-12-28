/**
 * In this file, we create a React component
 * which incorporates components provided by Material-UI.
 */
import React, {Component} from 'react';
import { Router, Route, hashHistory } from 'react-router'
import RaisedButton from 'material-ui/RaisedButton';
import Dialog from 'material-ui/Dialog';
import {cyan500} from 'material-ui/styles/colors';
import {GridList, GridTile} from 'material-ui/GridList';
import AppBar from 'material-ui/AppBar';
import { Link } from 'react-router'
import FlatButton from 'material-ui/FlatButton';
import IconButton from 'material-ui/IconButton';
import MoreVertIcon from 'material-ui/svg-icons/navigation/more-vert';
import ArrowDropRight from 'material-ui/svg-icons/navigation-arrow-drop-right';
import MenuItem from 'material-ui/MenuItem';
import Drawer from 'material-ui/Drawer';
import getMuiTheme from 'material-ui/styles/getMuiTheme';
import MuiThemeProvider from 'material-ui/styles/MuiThemeProvider';
import { ProjectList, ProjectDetail } from './Project'
import {ComponentList, PollerList} from './Component'
import {PollerActionStepList} from './PollerAction'


const muiTheme = getMuiTheme({
  palette: {
    textColor: cyan500,
  },
  appBar: {
    height: 50,
  }
});

class MainWrapper extends Component {
	  constructor(props, context) {
		    super(props, context);
		    this.state = {
		    	open: false,
		    };

		    var socket = new SockJS('http://localhost:8080/event');
		    var stompClient = Stomp.over(socket);
		    
		    stompClient.connect({}, function (frame) {
		        console.log('Connected: ' + frame);
		        stompClient.subscribe('/topic/event', function (event) {
		            console.log('Obtained event', event);
		        });
		    });
		  }

	render() {
		 return (
			      <MuiThemeProvider muiTheme={muiTheme}>
			      	<div>
				        <AppBar
				        onLeftIconButtonTouchTap={() => this.setState({open: true})}
				        iconElementLeft={<IconButton><MoreVertIcon /></IconButton>}
				      />
				        <Drawer
				        docked={false}
				        width={200}
				        open={this.state.open}
				        onRequestChange={(open) => this.setState({open})}
				      >
				        <AppBar title="OpenDVS" showMenuIconButton={false} />
				        <Link to="/"><MenuItem>Projects</MenuItem></Link>
				        <MenuItem
				            rightIcon={<ArrowDropRight />}
				        	menuItems={[
					        	<Link to="/components/"><MenuItem>List</MenuItem></Link>,
					        	<Link to="/components/actions"><MenuItem>Actions</MenuItem></Link>
				        ]}>Components</MenuItem>
				      </Drawer>

					      	<div className="container">
					      		{this.props.content || <ProjectList />}				     
					        </div>
				     </div>
			      </MuiThemeProvider>
			)
	}

}

class Main extends Component {


  render() {
	  return (
	  <Router history={hashHistory}>
	  	<Route path="/" component={MainWrapper}>
  		<Route path="projects" components={{content: ProjectList}}/>
  		<Route path="components" components={{content: ComponentList}}/>
  		<Route path="components/actions" components={{content: PollerList}}/>
  		<Route path="components/action/:action" components={{content: PollerActionStepList}}/>
	  	<Route path="project/:projectId/details" components={{content: ProjectDetail}}/>" +
        </Route>
      </Router>
    )
  }
}

export default Main;
