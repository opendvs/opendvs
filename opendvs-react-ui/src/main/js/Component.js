import React, {Component} from 'react';
import IconButton from 'material-ui/IconButton';
import FlatButton from 'material-ui/FlatButton';
import Dialog from 'material-ui/Dialog';
import { Link } from 'react-router'
import RaisedButton from 'material-ui/RaisedButton';
import ContentAdd from 'material-ui/svg-icons/content/add';
import TextField from 'material-ui/TextField';
import FloatingActionButton from 'material-ui/FloatingActionButton';
import NavigationChevronRight from 'react-material-icons/icons/navigation/chevron-right';
import NavigationChevronLeft from 'react-material-icons/icons/navigation/chevron-left';
import {Table, TableFooter, TableBody, TableHeader, TableHeaderColumn, TableRow, TableRowColumn} from 'material-ui/Table';
import {List, ListItem} from 'material-ui/List';
import { Grid, Row, Col } from 'react-flexbox-grid/lib/index'
import * as JSOG from 'jsog'
import * as Colors from 'material-ui/styles/colors';

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

class ComponentRow extends Component {
  render() {
    return (
      <TableRow>
        <TableRowColumn>{this.props.component.name}</TableRowColumn>
        <TableRowColumn>{this.props.component.latestVersion}</TableRowColumn>
        <TableRowColumn>{this.props.component.group}</TableRowColumn>
        <TableRowColumn><FlatButton label={this.props.component.versions.length} onTouchTap={this.props.handleClick} /></TableRowColumn>
      </TableRow>
    );
  }
}

class PollerActionRow extends Component {
  render() {
	if (this.props.action.artifactId) {
		var artifactId = this.props.action.artifactId;
	} else {
		var artifactID = "-";
	}
	if (this.props.action.started > 0) {
		var started = new Date(this.props.action.started).toString();
	} else {
		var started = "-";
	}
	if (this.props.action.ended > 0) {
		var ended = new Date(this.props.action.ended).toString();
	} else {
		var ended = "-";
	}
	if (this.props.action.filter) {
		var filter = this.props.action.filter;
	} else {
		var filter = "-";
	}
    return (
      <TableRow>
      <TableRowColumn>{filter}</TableRowColumn>
      <TableRowColumn>{artifactId}</TableRowColumn>
      <TableRowColumn>{new Date(this.props.action.initiated).toString()}</TableRowColumn>
      <TableRowColumn>{started}</TableRowColumn>
      <TableRowColumn>{ended}</TableRowColumn>
      <TableRowColumn><Link to={`/components/action/${this.props.action.id}`}><FlatButton label={this.props.action.state} /></Link></TableRowColumn>
      </TableRow>
    );
  }
}
class VersionRow extends Component {
  render() {
	var style = {};
	if (this.props.upToDate) {
		style["backgroundColor"] = Colors.lightGreen100
		style["color"] = Colors.grey900
	} else if (this.props.outdated) {
		style["backgroundColor"] = Colors.orange100
		style["color"] = Colors.grey900
	} else if (this.props.vulnerable) {
		style["backgroundColor"] = Colors.red100
		style["color"] = Colors.grey900
	} else if (this.props.latest) {
		style["backgroundColor"] = Colors.lightBlue100
		style["color"] = Colors.grey900
	}
    return (
      <TableRow style={style}>
        <TableRowColumn>{this.props.version.version}</TableRowColumn>
        <TableRowColumn>{this.props.version.hash}</TableRowColumn>
        <TableRowColumn>{new Date(this.props.version.published).toString()}</TableRowColumn>
        <TableRowColumn>{this.props.version.source}</TableRowColumn>
      </TableRow>
    );
  }
}

class ComponentTable extends Component {
	 constructor(props, context) {
		    super(props, context);
		    this.state = {
		    	open: false,
		    	offset: 0,
		    	limit: 10,
		    	total: 0,
		    	components: [],
		    	component: {versions: []}
		    };

		    fetch(`http://localhost:8080/api/v1/components/`)
		        .then(result=>result.json())
		        .then(items=>this.setState({components: items.content, limit: items.size, total: items.totalElements}))
	  }


      handleChange(offset) {
		    this.setState({offset: offset});

 		  if (offset > 0 && this.state.limit > 0) {
 			 var page = Math.floor(offset/this.state.limit);
 		  } else {
 			  var page = 0;
 		  }
		    fetch(`http://localhost:8080/api/v1/components/?page=${page}`)
		        .then(result=>result.json())
		        .then(items=>this.setState({components: items.content, limit: items.size, total: items.totalElements}))
	  }

	  render() {

	    var rows = [];
	    this.state.components.forEach((art) => {
		      rows.push(<ComponentRow component={art} key={art.id} handleClick={(e) => this.setState({open: true, component: art})} />);
		    });
		
		var versions = [];
		this.state.component.versions.sort(function(a,b) {return b.published - a.published}); 

        this.state.component.versions.forEach((ver) => {
	      versions.push(<VersionRow key={ver.id} version={ver} latest={ver.version == this.state.component.latestVersion} />);
	    });
	

	    return (
	    	<div>
	    	<Dialog
            open={this.state.open}
	          autoScrollBodyContent={true}
            title="Versions"
            onRequestClose={() => this.setState({open: false})}
          >

	        <Table>
	          <TableHeader adjustForCheckbox={false} displaySelectAll={false}>
	            <TableRow>
	              <TableHeaderColumn>Version</TableHeaderColumn>
	              <TableHeaderColumn>Hash</TableHeaderColumn>
	              <TableHeaderColumn>Published</TableHeaderColumn>
	              <TableHeaderColumn>Source</TableHeaderColumn>
	            </TableRow>
	          </TableHeader>
	          <TableBody>{versions}</TableBody>
	        </Table>
          </Dialog>

          <Table>
	          <TableHeader adjustForCheckbox={false} displaySelectAll={false}>
	            <TableRow>
	              <TableHeaderColumn>Name</TableHeaderColumn>
	              <TableHeaderColumn>Latest Version</TableHeaderColumn>
	              <TableHeaderColumn>Group</TableHeaderColumn>
	              <TableHeaderColumn>Versions</TableHeaderColumn>
	            </TableRow>
	          </TableHeader>
	          <TableBody>{rows}</TableBody>
	          <TableFooter adjustForCheckbox={false}>
	          <TableRow>
	            <TableRowColumn style={styles.footerContent}>
	              <IconButton disabled={this.state.offset === 0} onClick={() => this.handleChange(this.state.offset - this.state.limit)}>
	                <NavigationChevronLeft />
	              </IconButton>
	              <IconButton disabled={this.state.offset + this.state.limit >= this.state.total} onClick={() => this.handleChange(this.state.offset + this.state.limit)}>
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

class ComponentList extends Component {

  render() {

	var gridStyle = {width: "100%"};
    return (
      <div>

	  	  <Grid style={gridStyle}>
	        <Row>
	          <h1>Fetched Components</h1>
		      <ComponentTable />
		    </Row>
	      </Grid>
      </div>
    );
  }
}

class PollerActionTable extends Component {
  constructor(props, context) {
	    super(props, context);
	    this.state = {
	    	offset: 0,
	    	limit: 10,
	    	total: 0,
	    	actions: []
	    };

	    fetch(`http://localhost:8080/api/v1/pollers/actions?sort=initiated,desc`)
	        .then(result=>result.json())
	        .then(items=>this.setState({actions: items.content, limit: items.size, total: items.totalElements}))
  }

  handleChange(offset) {
	  this.setState({offset: offset});

	  if (offset > 0 && this.state.limit > 0) {
		 var page = Math.floor(offset/this.state.limit);
	  } else {
		 var page = 0;
	  }

      fetch(`http://localhost:8080/api/v1/pollers/actions?sort=initiated,desc&page=${page}`)
	        .then(result=>result.json())
	        .then(items=>this.setState({actions: items.content, limit: items.size, total: items.totalElements}))
  }

  render() {
    var rows = [];
    this.state.actions.forEach((act) => {
      rows.push(<PollerActionRow action={act} key={act.id} />);
    });

    return (
    	<div>

      <Table>
          <TableHeader adjustForCheckbox={false} displaySelectAll={false}>
            <TableRow>
            	<TableHeaderColumn>Filter</TableHeaderColumn>
            	<TableHeaderColumn>Artifact</TableHeaderColumn>
	            <TableHeaderColumn>Initiated</TableHeaderColumn>
	            <TableHeaderColumn>Started</TableHeaderColumn>
	            <TableHeaderColumn>Ended</TableHeaderColumn>
	            <TableHeaderColumn>State</TableHeaderColumn>
            </TableRow>
          </TableHeader>
          <TableBody>{rows}</TableBody>
          <TableFooter adjustForCheckbox={false}>
          <TableRow>
            <TableRowColumn style={styles.footerContent}>
              <IconButton disabled={this.state.offset === 0} onClick={() => this.handleChange(this.state.offset - this.state.limit)}>
                <NavigationChevronLeft />
              </IconButton>
              <IconButton disabled={this.state.offset + this.state.limit >= this.state.total} onClick={() => this.handleChange(this.state.offset + this.state.limit)}>
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

class PollerList extends Component {
	  constructor(props, context) {
		    super(props, context);
		    this.state = {
		    	opened: false,
		    	filter: undefined
		    };
	  }

	  executePolling() {
		  var obj = {};
		  if (this.state.filter && this.state.filter != "") {
			  obj.filter = this.state.filter;
		  }

		  fetch('http://localhost:8080/api/v1/pollers/actions', {
			  method: 'POST',
			  headers: {
			    'Accept': 'application/json',
			    'Content-Type': 'application/json',
			  },
			  body: JSON.stringify(obj)
			}).then(result => result.json()).then((res) => this.setState({filter: undefined, opened: false}));
	  }
	  
	  render() {
		    const standardActions = (
		    		<RaisedButton label="Execute polling" primary={true} onTouchTap={() => this.executePolling()} />
			    );

		    var gridStyle = {width: "100%"};

	    return (
	      <div>
		      <Dialog
		      open={this.state.opened}
		      title="Create new polling action"
		      actions={standardActions}
		      onRequestClose={() => this.setState({opened: false})}>
			      <TextField 
			      	  floatingLabelText="Filter"
			          hintText="org.slf4j:slf4j-api"
			          fullWidth={true}
			      	  value={this.state.filter}
		          	  onChange={(evt) => this.setState({filter: evt.target.value})}/>
			  </Dialog>

			  <Grid  style={gridStyle}>
		    	<Row>
		    		<Col xs={11} md={11}>
		    		  <h1>Polling actions</h1>
		    		</Col>
		    		<Col xs={1} md={1}>
			    	  <FloatingActionButton mini={true} onTouchTap={() => this.setState({opened: true})}>
				    	<ContentAdd />
				      </FloatingActionButton>
		    		</Col>
		        </Row>
		        <Row>
			      <PollerActionTable />
			    </Row>
		      </Grid>
	      </div>
	    );
	  }
	}

export {
   ComponentList,
   PollerList,
   VersionRow
}
