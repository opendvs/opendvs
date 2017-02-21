import React, {Component} from 'react';
import Divider from 'material-ui/Divider';
import ArtifactSelect from './ArtifactSelect'
import NavigationArrowBack from 'material-ui/svg-icons/navigation/arrow-back';
import { fetchProject, selectArtifact, toggleArtifactGroup } from '../../actions/project'
import Toggle from 'material-ui/Toggle';
import { Link } from 'react-router'
import { Grid, Row, Col } from 'react-flexbox-grid/lib/index'
import Graph from 'react-graph-vis'
import {COMPONENT_STATE_COLORS} from './Project'

class ProjectGraph extends Component {
	  componentDidMount() {
		const { dispatch, params } = this.props;
		if (!params.item && params.projectId) {
			dispatch(fetchProject(params.projectId))
		}
	  }

	  onArtifactSelect = (event, index, value) => {
			this.props.dispatch(selectArtifact(this.props.params.projectId, value));
	  }

	  onGroupToggle = (group, value) => {
		  this.props.dispatch(toggleArtifactGroup(group, value))
	  }

	  render() {
		const { item, artifacts, selectedArtifact, unselectedGroups } = this.props


	    var gridStyle = {width: "100%"};
		var detailsLink = `/project/${item.id}/details`;

		var graph = {
				  nodes: [],
				  edges: [],
		};
		var toggles = [];
		
		const toggleBlockStyle = {
			margin: '10px'
		};

		console.log(unselectedGroups);
		if (selectedArtifact && selectedArtifact.raw_components) {
			const components = selectedArtifact.raw_components.filter((entry) => !unselectedGroups.includes(entry.group));
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
			toggles = groups.map(group => <div key={group} style={toggleBlockStyle}><Toggle onToggle={(event, val) => this.onGroupToggle(group, val)} label={group} defaultToggled={true} /></div>);
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
				            enabled: true,
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
				    	enabled: false
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
		          	</Col>
		        </Row>
		        <Row>
		          {toggles}
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