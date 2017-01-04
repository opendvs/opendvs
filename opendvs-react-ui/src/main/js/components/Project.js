import React, {Component} from 'react';
import CircularProgress from 'material-ui/CircularProgress';
import Divider from 'material-ui/Divider';
import FloatingActionButton from 'material-ui/FloatingActionButton';
import ContentAdd from 'material-ui/svg-icons/content/add';
import { Legend, PieChart, Tooltip, Pie, Sector, Cell } from 'recharts'
import { Grid, Row, Col } from 'react-flexbox-grid/lib/index'
import ArtifactSelect from './ArtifactSelect'

const COMPONENT_STATE_COLORS = {
		UP_TO_DATE: '#4CAF50',
		OUTDATED: '#FF9800',
		VULNERABLE: '#F44336',
		UNKNOWN: '#E0E0E0'
}

const chartColors = [COMPONENT_STATE_COLORS.UP_TO_DATE, COMPONENT_STATE_COLORS.OUTDATED, COMPONENT_STATE_COLORS.VULNERABLE, COMPONENT_STATE_COLORS.UNKNOWN];

const Project = {{ project, selectedArtifact, artifacts, chartData, onFile, onArtifactSelect }} => {
	
	if (project.type == 'local') {
		var uploadButton = (
		  <Col xs={1} md={1}>
		    <FloatingActionButton style={{"marginTop": "20px"}} secondary={true} mini={true} containerElement='label'>
		    	<ContentAdd />
		    	<input type="file" style={{"display": "none"}} onChange={(event) => onFile(event)}/>
		    </FloatingActionButton>
		  </Col>
		)
	}


	if (selectedArtifact && selectedArtifact.probeAction) {
		var state = selectedArtifact.probeAction.state;
		if (selectedArtifact.probeAction.state == 'IN_PROGRESS' || selectedArtifact.probeAction.state == 'QUEUED') {
	 	   var icon = <CircularProgress style={{"marginRight": "10px"}} size={25}/>
		}
	} else {
		var state = "-"
	}


    var typeProperties = [];
    if (project.properties) {
        project.typeProperties.forEach((it) => {
            typeProperties.push(
      		      <h3 key={it[0]}>{it[0]}: {it[1]}</h3>
    		)
        });    	
    }

    if (selectedArtifact) {
    	var artDate = (new Date(selectedArtifact.initiated)).toString();
    	var artifactDetails = (
    		<div>
  	          <h2>Artifact <i>{selectedArtifact.name}</i></h2>
	          <i><small>Initiated on {artDate}</small></i><br />
	          <i><small>{selectedArtifact.identity}</small></i><br />
	          <i><small>Analysis {state}</small></i><br />
	          <i><small>Overall state {selectedArtifact.state}</small></i>
	          <br />
	          <br />
	          <PieChart width={400} height={200}>
		           <Pie startAngle={180} endAngle={0} data={chartData} innerRadius={20} outerRadius={80}>
		             {
		             	chartData.map((entry, index) => <Cell key={index} fill={chartColors[index]}/>)
		             }
		           </Pie>
		           <Legend verticalAlign="bottom" height={80}/>
		           <Tooltip/>
	          </PieChart>
	        </div>
    	);
  	} else {
  		var artifactDetails = (
  			<h3>No artifact available</h3>
  		);
  	}

    var gridStyle = {width: "100%"};
	
	return (
		<Grid style={gridStyle}>
			<Row>
				<Col xs={6} md={8}>
					<h1 className="projectname">{icon}Project <i>{project.name}</i></h1>
					{typeProperties}
	          	</Col>
				<Col xs={5} md={3}>
					<ArtifactSelect artifact={selectedArtifact} artifacts={artifacts} onChange={onArtifactChange} />
	          	</Col>
					{uploadButton}
	        </Row>
	        <Row>
	          <Divider />
	        </Row>
	        <Row>
				<Col xs={6} md={8}>
					<ArtifactComponentTable components={selectedArtifact.components} filterCallback={this.prepareChartData.bind(this)}/>
				</Col>
	
				<Col xs={6} md={4}>
					{artifactDetails}
		        </Col>
	        </Row>
	    </Grid>
	)
}


export default Project