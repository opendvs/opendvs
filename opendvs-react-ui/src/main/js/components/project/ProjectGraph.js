import React, {Component} from 'react';
import Divider from 'material-ui/Divider';
import ArtifactSelect from './ArtifactSelect'
import NavigationArrowBack from 'material-ui/svg-icons/navigation/arrow-back';
import { fetchProject, selectArtifact, toggleArtifactGroup, toggleArtifactScope, toggleArtifactGraphHierarchy } from '../../actions/project'
import Checkbox from 'material-ui/Checkbox';
import Toggle from 'material-ui/Toggle';
import SelectField from 'material-ui/SelectField';
import MenuItem from 'material-ui/MenuItem';
import { Link } from 'react-router'
import { Grid, Row, Col } from 'react-flexbox-grid/lib/index'
import Graph from 'react-graph-vis'
import {COMPONENT_STATE_COLORS} from './Project'

class ProjectGraph extends Component {
	  componentDidMount() {
		const { dispatch, params, item } = this.props;
		if ((!item || !item.id) && params.projectId) {
			dispatch(fetchProject(params.projectId))
		}
	  }

	  onGraphHierarchyToggle = (event, value) => {
		  this.props.dispatch(toggleArtifactGraphHierarchy());
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

	  render() {
		const { item, artifacts, selectedArtifact, unselectedGroups, unselectedScopes, graphHierarchy } = this.props


	    var gridStyle = {width: "100%"};
		var detailsLink = `/project/${item.id}/details`;

		var graph = {
				  nodes: [],
				  edges: [],
		};
		var toggles = [];
		var scopeToggles = [];
		
		const toggleBlockStyle = {
			margin: '10px'
		};

		if (selectedArtifact && selectedArtifact.raw_components) {
			const components = selectedArtifact.raw_components.filter((entry) => !unselectedGroups.includes(entry.group) && !unselectedScopes.includes(entry.scope))
			graph.nodes = components.map((entry) => ({
					id: entry.id,
					label: entry.uid,
					group: entry.group,
					color: {
						background: COMPONENT_STATE_COLORS[entry.state],
						highlight: {
							background: COMPONENT_STATE_COLORS[entry.state]
						}
					}
			}));
  		    graph.edges = components.filter((entry) => entry.parentId != null).map((entry) => ({from: entry.parentId, to: entry.id}));

			var groups = [...new Set(selectedArtifact.raw_components.map(entry => entry.group))];
			groups.sort();
			toggles = groups.map(group => <div key={group} style={toggleBlockStyle}><Checkbox onCheck={(event, val) => this.onGroupToggle(group, val)} label={group} checked={!unselectedGroups.includes(group)} /></div>);

			var scopes = [...new Set(selectedArtifact.raw_components.map(entry => entry.scope))];
			scopes.sort((a, b) => a === null ? 1 : b === null ? -1 : a.localeCompare(b));
			scopeToggles = scopes.map(scope => <div key={scope} style={toggleBlockStyle}><Checkbox onCheck={(event, val) => this.onScopeToggle(scope, val)} label={(scope)? scope: 'Undefined'} checked={!unselectedScopes.includes(scope)} /></div>);
			
		}

				var options = {
					groups: {
						maven: {
							color: {
								border: '#FFA500' 
							}
						}
					},
				    layout: {
				        hierarchical: {
				            enabled: graphHierarchy,
				            levelSeparation: 600,
				            nodeSpacing: 300,
				            treeSpacing: 100,
				            blockShifting: true,
				            edgeMinimization: false,
				            parentCentralization: false,
				            direction: 'UD',        // UD, DU, LR, RL
				            sortMethod: 'directed'   // hubsize, directed
				          }
				    },
				    physics: {
				    	enabled: !graphHierarchy
				    },
				    edges: {
				        color: {inherit: true},
				        smooth: {
				        	type: "continuous",
				        	roundness: 0.7
				        }
				    },
				    interaction: {
				    }
				};

				var events = {
				    select: function(event) {
				        var { nodes, edges } = event;
				    }
				}

				
		return (
			<div>

			<Grid style={gridStyle}>
				<Row>
					<Col xs={6} md={8}>
						<h1 className="projectname">Project <i>{item.name}</i></h1>

				    	<Link to={detailsLink}>
							<NavigationArrowBack />
						</Link>
		          	</Col>
					<Col xs={5} md={3}>
						<ArtifactSelect artifacts={artifacts} selectedArtifact={selectedArtifact} onArtifactSelect={this.onArtifactSelect} />
			          <div style={toggleBlockStyle}>
			  			<Toggle onToggle={this.onGraphHierarchyToggle} label="Hierarchical graph" toggled={graphHierarchy} />
			  	  	  </div>
		          	</Col>
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
					<Col xs={12} md={12}>
						<Graph graph={graph} options={options} events={events} style={{width: '100%', height: '100%'}}/>
					</Col>
		        </Row>
		    </Grid>
		   </div>
		)
	}
}


export default ProjectGraph