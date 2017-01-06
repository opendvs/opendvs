import React, {Component} from 'react';
import CircularProgress from 'material-ui/CircularProgress';
import Divider from 'material-ui/Divider';
import FloatingActionButton from 'material-ui/FloatingActionButton';
import ContentAdd from 'material-ui/svg-icons/content/add';
import { Legend, PieChart, Tooltip, Pie, Sector, Cell } from 'recharts'
import { Grid, Row, Col } from 'react-flexbox-grid/lib/index'
import ComponentVersionDialog from '../component/ComponentVersionDialog'
import { fetchProject, selectArtifact, pageComponents, selectComponentPage, openComponentDialog, toggleComponentDialog, uploadArtifact } from '../../actions/project'
import ArtifactSelect from './ArtifactSelect'
import ArtifactComponentTable from './ArtifactComponentTable'
import ArtifactBadge from './ArtifactBadge'

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
			this.props.dispatch(selectComponentPage(1));
			this.props.dispatch(selectArtifact(this.props.params.projectId, value));
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
		  let file = event.target.files[0];
		  let fd = new FormData();
	      fd.append('artifact', file);

	      this.props.dispatch(uploadArtifact(fd, this.props.item.id));
	  }

	render() {
		const { item, artifacts, selectedArtifact, pagedComponents, page, componentDialog } = this.props

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
	
		if (selectedArtifact && selectedArtifact.state != 'FINISHED') {
	 	   icon = <CircularProgress style={{"marginRight": "10px"}} size={25}/>
	    }

	    var gridStyle = {width: "100%"};
		
		return (
			<div>

		  	  <ComponentVersionDialog dialog={componentDialog} onClose={this.onDialogClose} />
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
						<ArtifactComponentTable components={pagedComponents} page={page} onPageChange={this.onPageChange} onComponentClick={this.onComponentClick} />
					</Col>
		
					<Col xs={6} md={4}>
						<ArtifactBadge artifact={selectedArtifact} components={selectedArtifact.components} /> 
			        </Col>
		        </Row>
		    </Grid>
		   </div>
		)
	}
}


export default Project