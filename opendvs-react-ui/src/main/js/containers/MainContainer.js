import React, {Component} from 'react';
import { connect } from 'react-redux'
import {WS_URL} from '../config'
import { handleArtifactUpdate, handleResolvedComponents } from '../actions/project'
import {fetchUserInfo} from '../actions/auth'
import * as JSOG from 'jsog'
import {cyan500} from 'material-ui/styles/colors';
import AppBar from 'material-ui/AppBar';
import { Link } from 'react-router'
import IconButton from 'material-ui/IconButton';
import DropDownMenu from 'material-ui/DropDownMenu';
import FlatButton from 'material-ui/FlatButton';
import MoreVertIcon from 'material-ui/svg-icons/navigation/more-vert';
import ArrowDropRight from 'material-ui/svg-icons/navigation-arrow-drop-right';
import ActionExitToApp from 'material-ui/svg-icons/action/exit-to-app';
import MenuItem from 'material-ui/MenuItem';
import Drawer from 'material-ui/Drawer';
import getMuiTheme from 'material-ui/styles/getMuiTheme';
import MuiThemeProvider from 'material-ui/styles/MuiThemeProvider';
import SnackbarContainer from './SnackbarContainer'
import ProjectListContainer from '../containers/ProjectListContainer'


const muiTheme = getMuiTheme({
  palette: {
    textColor: cyan500,
  },
  appBar: {
    height: 50,
  }
});

// TODO: switch to separate file
export class MainWrapper extends Component {
	  constructor(props, context) {
		    super(props, context);
		    this.state = {
		    	open: false,
		    };

		    var socket = new SockJS(WS_URL);
		    var stompClient = Stomp.over(socket);

		    props.dispatch(fetchUserInfo())
		    stompClient.connect({}, function (frame) {
		        console.log('Connected: ' + frame);
		        stompClient.subscribe('/topic/event', function (event) {
		        	if (event.headers.eventType) {
		        		if (event.headers.eventType == "ArtifactUpdateEvent") {
		        			var object = JSOG.decode(JSON.parse(event.body));
		        			props.dispatch(handleArtifactUpdate(object))
		        		} else if (event.headers.eventType == "ComponentsResolvedEvent") {
		        			var object = JSOG.decode(JSON.parse(event.body));
		        			props.dispatch(handleResolvedComponents(object))
		        		}
		        	} else {
		        		console.log("Unexpected event obtained", event)
		        	}
		        });
		    });
		  }

	render() {

		const loggedIn = (
				<FlatButton label={this.props.user.id} href="/logout" labelPosition="before" icon={<ActionExitToApp />} />
		)
		 return (
			      <MuiThemeProvider muiTheme={muiTheme}>
			      	<div>
				        <AppBar
				        onLeftIconButtonTouchTap={() => this.setState({open: true})}
				        iconElementLeft={<IconButton><MoreVertIcon /></IconButton>}
				        iconElementRight={loggedIn}
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
				        <Link to="/vulnerabilities"><MenuItem>Vulnerabilities</MenuItem></Link>
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


const mapStateToProps = state => {
  return state
}

const MainContainer = connect(
  mapStateToProps
)(MainWrapper)

export default MainContainer
