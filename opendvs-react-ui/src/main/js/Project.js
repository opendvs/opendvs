import React, {Component} from 'react';
import {deepOrange500} from 'material-ui/styles/colors';
import {Table, TableFooter, TableBody, TableHeader, TableHeaderColumn, TableRow, TableRowColumn} from 'material-ui/Table';
import {List, ListItem} from 'material-ui/List';
import SelectField from 'material-ui/SelectField';
import MenuItem from 'material-ui/MenuItem';
import Subheader from 'material-ui/Subheader';
import FlatButton from 'material-ui/FlatButton';
import IconButton from 'material-ui/IconButton';
import CircularProgress from 'material-ui/CircularProgress';
import NavigationChevronRight from 'react-material-icons/icons/navigation/chevron-right';
import NavigationChevronLeft from 'react-material-icons/icons/navigation/chevron-left';
import Divider from 'material-ui/Divider';
import Dialog from 'material-ui/Dialog';
import FloatingActionButton from 'material-ui/FloatingActionButton';
import RaisedButton from 'material-ui/RaisedButton';
import ContentAdd from 'material-ui/svg-icons/content/add';
import TextField from 'material-ui/TextField';
import NumberInput from 'material-ui-number-input';
import { Link } from 'react-router'
import { Legend, PieChart, Tooltip, Pie, Sector, Cell } from 'recharts'
import { Grid, Row, Col } from 'react-flexbox-grid/lib/index'
import { ArtifactComponentDetail } from './ArtifactComponentDetail'
import * as JSOG from 'jsog'

const COMPONENT_STATE_COLORS = {
		UP_TO_DATE: '#4CAF50',
		OUTDATED: '#FF9800',
		VULNERABLE: '#F44336',
		UNKNOWN: '#E0E0E0'
}

const styles = {
  footerContent: {
    float: 'right'
  },
  footerText: {
    float: 'right',
    paddingTop: 16,
    height: 16
  }
};


class ArtifactSelect extends Component {
  render() {
    var artifacts = [];
    this.props.artifacts.forEach((art) => {
       artifacts.push(<MenuItem key={art.id} value={art} primaryText={art.name} />)
    });
    var chartOptions = {
        responsive: false
    }

    return (
    	<div className="artifact-select">
          <SelectField fullWidth={true}
            value={this.props.artifact}
            onChange={this.props.onChange}
            floatingLabelText="Artifact" style={{"overflow": "hidden"}}>
            {artifacts}
          </SelectField>
         </div>
    );
  }
}


class ArtifactComponentRow extends Component {
  render() {
    return (
      <TableRow>
        <TableRowColumn>{this.props.artifact.name}</TableRowColumn>
        <TableRowColumn style={{width: '15%'}}>{this.props.artifact.version}{this.props.artifact.hash}</TableRowColumn>
        <TableRowColumn style={{width: '15%'}}>{this.props.artifact.scope}</TableRowColumn>
        <TableRowColumn style={{width: '10%'}}>{this.props.artifact.group}</TableRowColumn>
        <TableRowColumn style={{width: '10%'}}>{this.props.artifact.occurrences}</TableRowColumn>
        <TableRowColumn style={{width: '15%'}}><FlatButton style={{"color": COMPONENT_STATE_COLORS[this.props.artifact.state]}} onTouchTap={this.props.handleClick} label={this.props.artifact.state} /></TableRowColumn>
      </TableRow>
    );
  }
}

class ProjectRow extends Component {
  render() {
    return (
      <TableRow>
        <TableRowColumn>
            <Link to={"/project/" + this.props.project.id + "/details" }>{this.props.project.name}</Link>
        </TableRowColumn>
        <TableRowColumn>{this.props.project.type}</TableRowColumn>
      </TableRow>
    );
  }
}

class ProjectTable extends Component {
	  render() {
	    var rows = [];
	    this.props.projects.forEach((project) => {
	      rows.push(<ProjectRow project={project} key={project.id} />);
	    });
	    return (
	        <Table selectable={false}>
	          <TableHeader adjustForCheckbox={false} displaySelectAll={false}>
	            <TableRow>
	              <TableHeaderColumn>Name</TableHeaderColumn>
	              <TableHeaderColumn>Type</TableHeaderColumn>
	            </TableRow>
	          </TableHeader>
	          <TableBody>{rows}</TableBody>
	        </Table>
	    );
	  }
	}

class ArtifactComponentTable extends Component {
	 constructor(props, context) {
		    super(props, context);
		    this.state = {
		    	offset: 0,
		    	limit: 10,
		    	total: 0,
		    	unfilteredTotal: 0,
		    	pagedComponents: [],
		    	filteredComponents: [],
		    	scope: undefined,
			    uniqueScopes: [],
			    open: false,
			    component: {}
		    };
		    
	  }

	 setFilteredComponents(scope) {
  	      if (!scope || scope == "") {
  	    	  this.setState({scope: scope, filteredComponents: this.props.components});
  	  	      this.props.filterCallback(this.props.components);
  	      } else {
  	    	  var dt = this.props.components.filter(c => c.scope == scope);
  	    	  this.setState({scope: scope, filteredComponents: dt});
  	  	      this.props.filterCallback(dt);
  	      }
	 }

	  render() {
		  if (this.state.unfilteredTotal != this.props.components.length) {
			  this.state.uniqueScopes = [];
			  this.state.filteredComponents = [];
			  this.state.scope = undefined;
		  }

   	      if (this.state.uniqueScopes.length == 0) {
   	    	  this.state.uniqueScopes = [...new Set(this.props.components.map(a => a.scope))];
   	      }
   	      if (this.state.filteredComponents.length == 0) {
   	    	  this.state.filteredComponents = this.props.components;
   	      }

		  this.state.total = this.state.filteredComponents.length;
		  this.state.unfilteredTotal = this.props.components.length;
		  if (this.state.offset > this.state.total) {
			  this.state.offset = 0;
		  }
		  this.state.pagedComponents = this.state.filteredComponents.slice(this.state.offset, this.state.offset + this.state.limit);


   	      var handleChange = (offset) => {
   		    this.setState({offset: offset});
		  }

	    var rows = [];
	    this.state.pagedComponents.forEach((art) => {
	      rows.push(<ArtifactComponentRow handleClick={(e) => this.setState({open: true, component: art})} artifact={art} key={art.id} />);
	    });

	    var scopes = [];
	    this.state.uniqueScopes.forEach((scope) => {
	        scopes.push(<MenuItem key={scope} value={scope} primaryText={scope} />)
	    });
	    
	    var dialogTitle = 'Component ' + this.state.component.name + ' details';

	    return (
	    	<div>
		    	<Dialog
	            open={this.state.open}
		        autoScrollBodyContent={true}
		    	className= 'dialog-root'
	            title={dialogTitle}
	            onRequestClose={() => this.setState({open: false})}>
	    			<ArtifactComponentDetail component={this.state.component} />
		    	</Dialog>

		        <Table>
		          <TableHeader adjustForCheckbox={false} displaySelectAll={false}>
		            <TableRow>
		              <TableHeaderColumn>Name</TableHeaderColumn>
		              <TableHeaderColumn style={{width: '15%'}}>Version / Hash</TableHeaderColumn>
		              <TableHeaderColumn style={{width: '15%'}}><SelectField
		              	  value={this.state.scope}
				      	  fullWidth={true}
			          	  floatingLabelText="Scope" onChange={(event, index, value) => { this.setFilteredComponents(value)}}>
		              	  {scopes}
			          </SelectField>
		              </TableHeaderColumn>
		              <TableHeaderColumn style={{width: '10%'}}>Group</TableHeaderColumn>
		              <TableHeaderColumn style={{width: '10%'}}>Occurrences</TableHeaderColumn>
		              <TableHeaderColumn style={{width: '15%'}}>State</TableHeaderColumn>
		            </TableRow>
		          </TableHeader>
		          <TableBody>{rows}</TableBody>
		          <TableFooter adjustForCheckbox={false}>
		          <TableRow>
		            <TableRowColumn style={styles.footerContent}>
		              <IconButton disabled={this.state.offset === 0} onClick={handleChange.bind(null, this.state.offset - this.state.limit)}>
		                <NavigationChevronLeft />
		              </IconButton>
		              <IconButton disabled={this.state.offset + this.state.limit >= this.state.total} onClick={handleChange.bind(null, this.state.offset + this.state.limit)}>
		                <NavigationChevronRight />
		              </IconButton>
		            </TableRowColumn>
		            <TableRowColumn style={styles.footerText}>
		              {Math.min((this.state.offset + 1), this.state.total) + '-' + Math.min((this.state.offset + this.state.limit), this.state.total) + ' of ' + this.state.total}
		            </TableRowColumn>
		          </TableRow>
		        </TableFooter>
		        </Table>
	        </div>
	    );
	  }
	}

class ArtifactComponentListItem extends Component {
	render() {
	    var children= [];
	    if (this.props.item.children) {
		    this.props.item.children.forEach((it) => {
		      children.push(<ArtifactComponentListItem opened={false} item={it} key={it.name} />);
		    });
	    }
	    return (
	    		<ListItem
	            primaryText={this.props.item.name}
	            initiallyOpen={this.props.opened}
	            primaryTogglesNestedList={this.props.opened}
	            nestedItems={children} />
	    )
	}
}
class ArtifactComponentList extends Component {

	render() {
		return (
	    		 <List>
		            <Subheader>Component list</Subheader>
		            <ArtifactComponentListItem opened={true} item={this.props.component} />
		          </List>
	    );
	  }
	}

class ProjectDetail extends Component {
  constructor(props, context) {
    super(props, context);
    this.state = {
      project: {},
      artifact: {},
      artifactComponents: [],
      artifacts: [],
      artifactDetail: {},
      treeData: {},
      chartData: [],
      handleChange: (event, index, value) => {
	    this.setState({artifact: value})

	    fetch("http://localhost:8080/api/v1/project/" + this.props.params.projectId + "/artifact/" + value.id)
	    .then(result=>result.json())
	    .then(item=> {
	       var it = JSOG.decode(item);
	       
	       var comps = this.removeDuplicateUIDs(it.components);
	       comps.sort(function (a,b) {return a.name.localeCompare(b.name)});
	       this.setState({artifactDetail: value, artifactComponents: comps});

	       this.prepareChartData(comps);
	    })
	  }

    };

    fetch("http://localhost:8080/api/v1/project/" + props.params.projectId)
    .then(result=>result.json())
    .then(item=>this.setState({project: item}))

    fetch("http://localhost:8080/api/v1/project/" + props.params.projectId + "/artifacts")
    .then(result=>result.json())
    .then(items=> { 
    	this.setState({artifacts: items.content});
    	this.state.handleChange(null, 0, items.content[0]);
  
	})
  }
  
  removeDuplicateUIDs(components) {
	  var keys = {};
	  var newarr = [];
	  for (var i in components) {
		  var obj = components[i];
		  var k = obj.group + "|" + obj.name + "|" + obj.version;
		  if (!(k in keys)) {
			  keys[k] = [];
		  }

		  keys[k].push(obj);
	  }

	  for (var k in keys) {
		  var e = keys[k][0];
		  e.occurrences = keys[k].length;
		  newarr.push(e);
	  }

	  return newarr;
  }

  _handleFileChange(e) {
	  e.preventDefault();
	  let file = e.target.files[0];
	  let projectid = this.state.project.id;
	  new Promise((resolve, reject) => {
	    let fd = new FormData();

	    fd.append('artifact', file);
	    
	    var xhr = new XMLHttpRequest();
	    
	    xhr.open('post', 'http://localhost:8080/api/v1/project/' + projectid + '/upload', true);
	    
	    xhr.onload = function () {
	      if (this.status == 200) {
	        resolve(this.response);
	      } else {
	        reject(this.statusText);
	      }
	    };
	    
	    xhr.send(fd);

	  }).then((response) => {
		var obj = JSON.parse(response);
		this.setState({artifacts: this.state.artifacts.concat(obj)});
    	this.state.handleChange(null, 0, obj);
	  })
  }

  prepareChartData(data) {
      var dt = {UNKNOWN: 0, UP_TO_DATE: 0, OUTDATED: 0, VULNERABLE: 0};
     data.forEach((comp) => {
        dt[comp.state] += 1
      })
      var val = [
        {name: "Up-to-date", value: dt.UP_TO_DATE},
        {name: "Outdated", value: dt.OUTDATED},
        {name: "Vulnerable", value: dt.VULNERABLE},
        {name: "Unknown", value: dt.UNKNOWN},
      ];

      
      this.setState({chartData: val, treeData: data})
  }

  render() {
	if (this.state.project.type == 'local') {
		var uploadButton = (
		  <Col xs={1} md={1}>
		    <FloatingActionButton style={{"marginTop": "20px"}} secondary={true} mini={true} containerElement='label'>
		    	<ContentAdd />
		    	<input type="file" style={{"display": "none"}} onChange={(event) => this._handleFileChange(event)}/>
		    </FloatingActionButton>
		  </Col>
		)
	}


	if (this.state.artifact && this.state.artifact.probeAction) {
		var state = this.state.artifact.probeAction.state;
	} else {
		var state = "-"
	}
    if (this.state.artifact && this.state.artifact.probeAction && (this.state.artifact.probeAction.state == 'IN_PROGRESS' || this.state.artifact.probeAction.state == 'QUEUED')) {
 	   var icon = <CircularProgress style={{"marginRight": "10px"}} size={25}/>
    }

	var chartColors = [COMPONENT_STATE_COLORS.UP_TO_DATE, COMPONENT_STATE_COLORS.OUTDATED, COMPONENT_STATE_COLORS.VULNERABLE, COMPONENT_STATE_COLORS.UNKNOWN];

    var typeProperties = [];
    if (this.state.project.properties) {
        this.state.project.typeProperties.forEach((it) => {
            typeProperties.push(
      		      <h3 key={it[0]}>{it[0]}: {it[1]}</h3>
    		)
        });    	
    }

    if (this.state.artifact) {
    	var artDate = (new Date(this.state.artifact.initiated)).toString();
    	var artifactDetails = (
    		<div>
  	          <h2>Artifact <i>{this.state.artifact.name}</i></h2>
	          <i><small>Initiated on {artDate}</small></i><br />
	          <i><small>{this.state.artifact.identity}</small></i><br />
	          <i><small>Analysis {state}</small></i><br />
	          <i><small>Overall state {this.state.artifact.state}</small></i>
	          <br />
	          <br />
	          <PieChart width={400} height={200}>
		           <Pie  startAngle={180} endAngle={0} data={this.state.chartData} innerRadius={20} outerRadius={80}>
		             {
		             	this.state.chartData.map((entry, index) => <Cell key={index} fill={chartColors[index]}/>)
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
	//<ArtifactComponentList component={this.state.treeData} />
    return (
	<Grid style={gridStyle}>
		<Row>
			<Col xs={6} md={8}>
				<h1 className="projectname">{icon}Project <i>{this.state.project.name}</i></h1>
				{typeProperties}
          	</Col>
			<Col xs={5} md={3}>
				<ArtifactSelect artifact={this.state.artifact} artifacts={this.state.artifacts} onChange={this.state.handleChange} />
          	</Col>
				{uploadButton}
        </Row>
        <Row>
          <Divider />
        </Row>
        <Row>
			<Col xs={6} md={8}>
				<ArtifactComponentTable components={this.state.artifactComponents} filterCallback={this.prepareChartData.bind(this)}/>
			</Col>

			<Col xs={6} md={4}>
				{artifactDetails}
	        </Col>
        </Row>
	</Grid>
    );
  }
}

class ProjectList extends Component {
  constructor(props, context) {
    super(props, context);

    this.state = {
      opened: false,
      project: {typeProperties: {}, majorVersionOffset: "0"},
      selectedType: {},
      projects: [],
      projectTypes: []
    }

    fetch(`http://localhost:8080/api/v1/projects`)
    .then(result=>result.json())
    .then(items=>this.setState({projects: items.content}))

    fetch(`http://localhost:8080/api/v1/project/types`)
        .then(result=>result.json())
        .then(items=>this.setState({projectTypes: items}))
  }

  createProject() {
	  // TODO: validation


	  fetch('http://localhost:8080/api/v1/projects', {
		  method: 'POST',
		  headers: {
		    'Accept': 'application/json',
		    'Content-Type': 'application/json',
		  },
		  body: JSON.stringify(this.state.project)
		}).then(result => result.json()).then((project) => {
			this.setState({project: {}, selectedType: {}, projects: this.state.projects.concat(project), opened: false});
		})
  }

  render() {
    const standardActions = (
    		<RaisedButton label="Create project" primary={true} onTouchTap={() => this.createProject()} />
	    );

    var typeItems = [];
    this.state.projectTypes.forEach((it) => {
        typeItems.push(<MenuItem key={it.id} value={it} primaryText={it.name} />)
    });
    var typeProperties = [];
    if (this.state.selectedType.properties) {
        this.state.selectedType.properties.forEach((it) => {
            typeProperties.push(
      		      <TextField key={it.name}
		      	  floatingLabelText={it.name}
		          hintText={it.description}
		          fullWidth={true}
      		      value={this.state.project.typeProperties[it.key]}
      		      onChange={(evt) => { var p = this.state.project; p.typeProperties[it.key] = evt.target.value; this.setState({project: p})} } />
    		)
        });    	
    }


    var gridStyle = {width: "100%"};
    return (
      <div>
	      <Dialog
	      open={this.state.opened}
	      title="Create new project"
	      actions={standardActions}
	      onRequestClose={() => this.setState({opened: false})}>
		      <TextField 
		      	  floatingLabelText="Name"
		          hintText="My example project"
		          fullWidth={true}
		      	  value={this.state.project.name}
	          	  onChange={(evt) => { var p = this.state.project; p.name = evt.target.value; this.setState({project: p})} } />

		      <SelectField
		      	  fullWidth={true}
	          	  floatingLabelText="Project type"
	          	  value={this.state.selectedType} onChange={(event, index, value) => { var p = this.state.project; p.type = value.id; this.setState({project: p, selectedType: value})} }>
		      	{typeItems}
	          </SelectField>

	          <NumberInput
	          value={this.state.project.majorVersionOffset}
	          min={0}
	      	  fullWidth={true}
          	  floatingLabelText="Semantic versioning offset (days)"
          	  hintText="Offset to treat dependencies with different major version as up-to-date"
          	  onChange={(evt) => { var p = this.state.project; p.majorVersionOffset = evt.target.value; this.setState({project: p})} } />

	          {typeProperties}
		  </Dialog>

	  	  <Grid style={gridStyle}>
	    	<Row>
	    		<Col xs={11} md={11}>
	    		  <h1>My projects</h1>
	    		</Col>
	    		<Col xs={1} md={1}>
		    	  <FloatingActionButton mini={true} onTouchTap={() => this.setState({opened: true})}>
			    	<ContentAdd />
			      </FloatingActionButton>
	    		</Col>
	        </Row>
	        <Row>
		      <ProjectTable projects={this.state.projects} />
		    </Row>
	      </Grid>
      </div>
    );
  }
}

export {
   ProjectList,
   ProjectDetail
}
