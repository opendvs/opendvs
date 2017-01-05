import React, {Component} from 'react';
import CircularProgress from 'material-ui/CircularProgress';
import Divider from 'material-ui/Divider';
import FloatingActionButton from 'material-ui/FloatingActionButton';
import ContentAdd from 'material-ui/svg-icons/content/add';
import { Legend, PieChart, Tooltip, Pie, Sector, Cell } from 'recharts'
import { Grid, Row, Col } from 'react-flexbox-grid/lib/index'
import { fetchProject, selectArtifact, pageComponents, selectComponentPage } from '../../actions/project'
import ArtifactSelect from './ArtifactSelect'
import ArtifactComponentTable from './ArtifactComponentTable'

export const COMPONENT_STATE_COLORS = {
		UP_TO_DATE: '#4CAF50',
		OUTDATED: '#FF9800',
		VULNERABLE: '#F44336',
		UNKNOWN: '#E0E0E0'
}

const chartColors = [COMPONENT_STATE_COLORS.UP_TO_DATE, COMPONENT_STATE_COLORS.OUTDATED, COMPONENT_STATE_COLORS.VULNERABLE, COMPONENT_STATE_COLORS.UNKNOWN];

class Project extends Component {
	  componentDidMount() {
		const { dispatch, params, filteredComponents } = this.props;
		if (params.projectId) {
			dispatch(fetchProject(params.projectId))
		}
	  }

	  componentWillReceiveProps(nextProps) {
		  if (this.props.page.current != nextProps.page.current) {
			  this.props.dispatch(pageComponents())
	  	  }
	  }

	  onArtifactSelect = (event, index, value) => {
		this.props.dispatch(selectArtifact(this.props.params.projectId, value));
	  }


	  onPageChange = newPage => {
	    this.props.dispatch(selectComponentPage(newPage))
	  }

	render() {
		const { item, artifacts, selectedArtifact, pagedComponents, page } = this.props

		var icon = '';
	    var typeProperties = [];
	    var uploadButton = '';

		if (item.type == 'local') {
			uploadButton = (
			  <Col xs={1} md={1}>
			    <FloatingActionButton style={{"marginTop": "20px"}} secondary={true} mini={true} containerElement='label'>
			    	<ContentAdd />
			    	<input type="file" style={{"display": "none"}} onChange={(event) => onFile(event)}/>
			    </FloatingActionButton>
			  </Col>
			)
		}
	
	
	    var gridStyle = {width: "100%"};
		
		return (
			<Grid style={gridStyle}>
				<Row>
					<Col xs={6} md={8}>
						<h1 className="projectname">{icon}Project <i>{item.name}</i></h1>
						{typeProperties}
		          	</Col>
					<Col xs={5} md={3}>
						<ArtifactSelect artifacts={artifacts} selectedArtifact={selectedArtifact} onArtifactSelect={this.onArtifactSelect} />
		          	</Col>
						{uploadButton}
		        </Row>
		        <Row>
		          <Divider />
		        </Row>
		        <Row>
					<Col xs={6} md={8}>
						<ArtifactComponentTable components={pagedComponents}page={page} onPageChange={this.onPageChange} />
					</Col>
		
					<Col xs={6} md={4}>
			        </Col>
		        </Row>
		    </Grid>
		)
	}
}


export default Project