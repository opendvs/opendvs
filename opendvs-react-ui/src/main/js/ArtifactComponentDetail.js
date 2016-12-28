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
import { VersionRow } from './Component'
import * as JSOG from 'jsog'

class ArtifactComponentDetail extends Component {
	  constructor(props, context) {
		    super(props, context);
		    this.state = {
		    	artifactComponentId: undefined,
		    	component: {versions: []}
		    };
	  }

	  checkOutdated(ver) {
		  if (this.props.component.version.startsWith("^") || this.props.component.version.startsWith("~")) {
			  var version = this.props.component.version.substring(1);
		  } else {
			  var version = this.props.component.version;
		  }
		  return this.props.component.state == 'OUTDATED' && ver.version == version;
	  }

	  render() {
		if (this.props.component.id && this.state.artifactComponentId != this.props.component.id) {
			this.state.artifactComponentId = this.props.component.id;
			var compId = this.props.component.group + ":" + this.props.component.name;

		    fetch(`http://localhost:8080/api/v1/components/${compId}/detail`)
		        .then(result=>result.json())
		        .then(item=>this.setState({component: item}))
		}

		var versions = [];
		this.state.component.versions.sort(function(a,b) {return b.published - a.published}); 

        this.state.component.versions.forEach((ver) => {
	      versions.push(<VersionRow key={ver.id} version={ver} upToDate={this.props.component.state == 'UP_TO_DATE' && ver.version == this.state.component.latestVersion}  outdated={this.checkOutdated(ver)} latest={ver.version == this.state.component.latestVersion} />);
	    });

		return (
				
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
      );
	}
}

export {
   ArtifactComponentDetail
}
