import React, {Component} from 'react';
import CircularProgress from 'material-ui/CircularProgress';
import Divider from 'material-ui/Divider';
import FloatingActionButton from 'material-ui/FloatingActionButton';
import ContentAdd from 'material-ui/svg-icons/content/add';
import EditorShowChart from 'material-ui/svg-icons/editor/show-chart';
import { Legend, PieChart, Tooltip, Pie, Sector, Cell } from 'recharts'
import { Grid, Row, Col } from 'react-flexbox-grid/lib/index'
import ComponentVersionDialog from '../component/ComponentVersionDialog'
import { fetchProject, selectArtifact, selectComponentPage, openComponentDialog, toggleComponentDialog, uploadArtifact, toggleArtifactGroup, toggleArtifactScope } from '../../actions/project'
import ArtifactSelect from './ArtifactSelect'
import Checkbox from 'material-ui/Checkbox';
import ArtifactComponentTable from './ArtifactComponentTable'
import ArtifactBadge from './ArtifactBadge'
import { Link } from 'react-router'

export const COMPONENT_STATE_COLORS = {
		UP_TO_DATE: '#4CAF50',
		OUTDATED: '#FF9800',
		VULNERABLE: '#F44336',
		UNKNOWN: '#E0E0E0'
}

const chartColors = [COMPONENT_STATE_COLORS.UP_TO_DATE, COMPONENT_STATE_COLORS.OUTDATED, COMPONENT_STATE_COLORS.VULNERABLE, COMPONENT_STATE_COLORS.UNKNOWN];

class Project extends Component {
	  componentDidMount() {
		const { item, dispatch, params } = this.props;
		if ((!item || !item.id) && params.projectId) {
			dispatch(fetchProject(params.projectId))
		}
	  }

	  onArtifactSelect = (event, index, value) => {
			this.props.dispatch(selectArtifact(this.props.params.projectId, value));
	  }

	  onScopeToggle = (scope, value) => {
		  this.props.dispatch(toggleArtifactScope(scope, value));
	  }

	  onGroupToggle = (group, value) => {
		  this.props.dispatch(toggleArtifactGroup(group, value))
	  }

	  onPageChange = newPage => {
	    this.props.dispatch(selectComponentPage(newPage))
	  }

	  onComponentClick = (component) => {
		  this.props.dispatch(openComponentDialog(component))
	  }

	  onDialogClose = () => {
		  this.props.dispatch(toggleComponentDialog(false, {}))
	  }

	  onFile = (event) => {
		  event.preventDefault();
		  const file = event.target.files[0];
		  const fd = new FormData();
	      fd.append('artifact', file);

	      this.props.dispatch(uploadArtifact(fd, this.props.item.id));
	  }

	render() {
		const { item, artifacts, selectedArtifact, page, componentDialog,  unselectedGroups, unselectedScopes } = this.props

		var icon = '';
	    var typeProperties = [];
	    var uploadButton = '';

		if (item.type == 'local') {
			uploadButton = (
			  <Col xs={1} md={1}>
			    <FloatingActionButton style={{"marginTop": "20px"}} secondary={true} mini={true} containerElement='label'>
			    	<ContentAdd />
			    	<input type="file" style={{"display": "none"}} onChange={(event) => this.onFile(event)}/>
			    </FloatingActionButton>
			  </Col>
			)
		}

		if (selectedArtifact.state && selectedArtifact.state != 'FINISHED') {
	 	   icon = <CircularProgress style={{"marginRight": "10px"}} size={25}/>
	    }

		const toggleBlockStyle = {
			margin: '10px'
		};

		var toggles = [];
		var scopeToggles = [];
		var raw_components = [];
		var pagedComponents = [];
		var pagedPage = {};

		if (selectedArtifact && selectedArtifact.raw_components) {
			// page the details
			raw_components = selectedArtifact.raw_components.filter((entry) => !unselectedGroups.includes(entry.group) && !unselectedScopes.includes(entry.scope));
			var components = selectedArtifact.components.filter((entry) => !unselectedGroups.includes(entry.group) && !unselectedScopes.includes(entry.scope));

            // recalculate page
            pagedPage = { current: page.current, size: page.size, total: Math.ceil(components.length/page.size), totalElements: components.length };

            var offset = (pagedPage.current - 1) * pagedPage.size;
            pagedComponents = components.slice(offset, offset + page.size);
            


			// calculate groups / scopes
			var groups = [...new Set(selectedArtifact.components.map(entry => entry.group))];
			groups.sort();
			toggles = groups.map(group => <div key={group} style={toggleBlockStyle}><Checkbox onCheck={(event, val) => this.onGroupToggle(group, val)} label={group} checked={!unselectedGroups.includes(group)} /></div>);

			var scopes = [...new Set(selectedArtifact.components.filter((entry) => !unselectedGroups.includes(entry.group)).map(entry => entry.scope))];
			scopes.sort((a, b) => a === null ? 1 : b === null ? -1 : a.localeCompare(b));
			scopeToggles = scopes.map(scope => <div key={scope} style={toggleBlockStyle}><Checkbox onCheck={(event, val) => this.onScopeToggle(scope, val)} label={(scope)? scope: 'Undefined'} checked={!unselectedScopes.includes(scope)} /></div>);
		}

	    var gridStyle = {width: "100%"};
		var graphLink = `/project/${item.id}/graph`;
		return (
			<div>

		  	  <ComponentVersionDialog dialog={componentDialog} onClose={this.onDialogClose} />
			<Grid style={gridStyle}>
				<Row>
					<Col xs={6} md={8}>
						<h1 className="projectname">{icon}Project <i>{item.name}</i></h1>
						{typeProperties}

				    	<Link to={graphLink}>
							<EditorShowChart />
						</Link>
		          	</Col>
					<Col xs={5} md={3}>
						<ArtifactSelect artifacts={artifacts} selectedArtifact={selectedArtifact} onArtifactSelect={this.onArtifactSelect} />
		          	</Col>
						{uploadButton}
		        </Row>
		        <Row>
		          {toggles}
		        </Row>
		        <Row>
		          <Divider />
		        </Row>
		        <Row>
		          {scopeToggles}
		        </Row>
		        <Row>
		          <Divider />
		        </Row>
		        <Row>
					<Col xs={6} md={8}>
						<ArtifactComponentTable components={pagedComponents} page={pagedPage} onPageChange={this.onPageChange} onComponentClick={this.onComponentClick} />
					</Col>
		
					<Col xs={6} md={4}>
						<ArtifactBadge artifact={selectedArtifact} components={raw_components} /> 
			        </Col>
		        </Row>
		    </Grid>
		   </div>
		)
	}
}


export default Project