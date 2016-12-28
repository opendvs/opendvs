import React, {Component} from 'react';
import IconButton from 'material-ui/IconButton';
import FlatButton from 'material-ui/FlatButton';
import RaisedButton from 'material-ui/RaisedButton';
import Dialog from 'material-ui/Dialog';
import { Link } from 'react-router'
import NavigationChevronRight from 'react-material-icons/icons/navigation/chevron-right';
import NavigationChevronLeft from 'react-material-icons/icons/navigation/chevron-left';
import {Table, TableFooter, TableBody, TableHeader, TableHeaderColumn, TableRow, TableRowColumn} from 'material-ui/Table';
import {List, ListItem} from 'material-ui/List';
import { Grid, Row, Col } from 'react-flexbox-grid/lib/index'
import * as JSOG from 'jsog'

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

class StepRow extends Component {
  render() {
    return (
      <TableRow>
      	<TableRowColumn>{new Date(this.props.step.started).toString()}</TableRowColumn>
  		<TableRowColumn>{(this.props.step.ended - this.props.step.started)/1000}s</TableRowColumn>
      	<TableRowColumn>{this.props.step.type}</TableRowColumn>
      	<TableRowColumn>{this.props.step.poller}</TableRowColumn>
        <TableRowColumn>{this.props.step.state}</TableRowColumn>
        <TableRowColumn><RaisedButton label="Show log" onTouchTap={this.props.handleClick} /></TableRowColumn>
      </TableRow>
    );
  }
}

class StepTable extends Component {
  constructor(props, context) {
	    super(props, context);
	    this.state = {
	    	open: false,
	    	offset: 0,
	    	limit: 10,
	    	total: 0,
	    	steps: [],
	    	stepOutput: ""
	    };

	    fetch(`http://localhost:8080/api/v1/pollers/action/${this.props.action}/steps?sort=started,desc`)
	        .then(result=>result.json())
	        .then(items=> {
		       var it = JSOG.decode(items);
		       this.setState({steps: it.content, limit: it.size, total: it.totalElements}) })
  }


  handleChange(offset) {
	    this.setState({offset: offset});

	  if (offset > 0 && this.state.limit > 0) {
		 var page = Math.floor(offset/this.state.limit);
	  } else {
		  var page = 0;
	  }

	    fetch(`http://localhost:8080/api/v1/pollers/action/${this.props.action}/steps?sort=started,desc&page=${page}`)
	        .then(result=>result.json())
	        .then(items=> {
		       var it = JSOG.decode(items);
		       this.setState({steps: it.content, limit: it.size, total: it.totalElements})})
  }

  
  render() {

  const actions = [
       <FlatButton
         label="Close"
         primary={true}
         onTouchTap={() => this.setState({open: false})}
       />
  ];
    var steps = [];
    this.state.steps.forEach((step) => {
	      steps.push(<StepRow step={step} key={step.id} handleClick={(e) => this.setState({open: true, stepOutput: step.output})} />);
	    });

    return (
    	<div>
    	 <Dialog
	      open={this.state.open}
	      title="Step log"
	      actions={actions}
         autoScrollBodyContent={true}
	      onRequestClose={() => this.setState({open: false})}>
		   	<pre style={{"wordWrap": "break-word", "whiteSpace": "pre-wrap", "color": "#000"}}>{this.state.stepOutput}</pre>
		  </Dialog>

      <Table>
          <TableHeader adjustForCheckbox={false} displaySelectAll={false}>
            <TableRow>
              <TableHeaderColumn>Started</TableHeaderColumn>
              <TableHeaderColumn>Duration</TableHeaderColumn>
              <TableHeaderColumn>Type</TableHeaderColumn>
              <TableHeaderColumn>Poller</TableHeaderColumn>
              <TableHeaderColumn>State</TableHeaderColumn>
              <TableHeaderColumn></TableHeaderColumn>
            </TableRow>
          </TableHeader>
          <TableBody>{steps}</TableBody>
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

class PollerActionStepList extends Component {
	  render() {

		var gridStyle = {width: "100%"};
	    return (
	      <div>
		  	  <Grid style={gridStyle}>
		        <Row>
		          <h1>Steps</h1>
			      <StepTable action={this.props.params.action} />
			    </Row>
		      </Grid>
	      </div>
	    );
	  }
	}

export {
   PollerActionStepList
}
