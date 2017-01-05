import React, {Component} from 'react';
import { Router, Route, hashHistory } from 'react-router'
import {cyan500} from 'material-ui/styles/colors';
import AppBar from 'material-ui/AppBar';
import { Link } from 'react-router'
import IconButton from 'material-ui/IconButton';
import MoreVertIcon from 'material-ui/svg-icons/navigation/more-vert';
import ArrowDropRight from 'material-ui/svg-icons/navigation-arrow-drop-right';
import MenuItem from 'material-ui/MenuItem';
import Drawer from 'material-ui/Drawer';
import getMuiTheme from 'material-ui/styles/getMuiTheme';
import MuiThemeProvider from 'material-ui/styles/MuiThemeProvider';
import ProjectListContainer from '../containers/ProjectListContainer'
import ComponentListContainer from '../containers/ComponentListContainer'
import SnackbarContainer from '../containers/SnackbarContainer'
import ProjectContainer from '../containers/ProjectContainer'


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
					      		{this.props.content || <ProjectListContainer />}				     
					        </div>
					        <SnackbarContainer />
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
  		<Route path="projects" components={{content: ProjectListContainer}}/>
        <Route path="project/:projectId/details" components={{content: ProjectContainer}}/>
  		<Route path="components" components={{content: ComponentListContainer}}/>
        </Route>
      </Router>
    )
  }
}

export default Main;
