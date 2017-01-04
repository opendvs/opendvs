import React, {Component} from 'react';
import {Table, TableFooter, TableBody, TableHeader, TableHeaderColumn, TableRow, TableRowColumn} from 'material-ui/Table';
import { Link } from 'react-router'

const ProjectRow = ({ project }) => {
	return (
	  <TableRow>
	    <TableRowColumn>
	        <Link to={"/project/" + project.id + "/details" }>{project.name}</Link>
	    </TableRowColumn>
	    <TableRowColumn>{project.type}</TableRowColumn>
	  </TableRow>
	)
}

export default ProjectRow