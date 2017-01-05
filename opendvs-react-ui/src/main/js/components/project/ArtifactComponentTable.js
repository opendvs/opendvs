import React, {Component} from 'react';
import MenuItem from 'material-ui/MenuItem';
import SelectField from 'material-ui/SelectField'
import {Table, TableFooter, TableBody, TableHeader, TableHeaderColumn, TableRow, TableRowColumn} from 'material-ui/Table'
import { Link } from 'react-router'
import FlatButton from 'material-ui/FlatButton'
import ChevronLeft from 'material-ui/svg-icons/navigation/chevron-left'
import ChevronRight from 'material-ui/svg-icons/navigation/chevron-right'
import {PAGINATION_OFFSET} from '../../config'
import ArtifactComponentRow from './ArtifactComponentRow'
import IconButton from 'material-ui/IconButton'

const ArtifactComponentTable = ({ components, page, onPageChange, onComponentClick }) => {
	var pageButtons = [];

    var start = page.current - PAGINATION_OFFSET;
    var end = page.current + PAGINATION_OFFSET;

    if (start < 1) {
  	  end = end + (1 - start);
  	  start = 1;
    } 
    if (end > page.total) {
  	  start = start - (end - page.total);
  	  end = page.total;
    }
    if (start < 1) {
  	  start = 1;
    }

    for (let i = start; i <= end; i++) {
		  pageButtons.push(<FlatButton onClick={() => onPageChange(i)} key={i} secondary={i == page.current}>{i}</FlatButton>)
    }

	return (
		<Table>
	        <TableHeader adjustForCheckbox={false} displaySelectAll={false}>
	          <TableRow>
	            <TableHeaderColumn>Name</TableHeaderColumn>
	            <TableHeaderColumn style={{width: '15%'}}>Version / Hash</TableHeaderColumn>
	            <TableHeaderColumn style={{width: '15%'}}>Scope TODO </TableHeaderColumn>
	            <TableHeaderColumn style={{width: '10%'}}>Group</TableHeaderColumn>
	            <TableHeaderColumn style={{width: '10%'}}>Occurrences</TableHeaderColumn>
	            <TableHeaderColumn style={{width: '15%'}}>State</TableHeaderColumn>
	          </TableRow>
	        </TableHeader>
	        <TableBody>{
	        	components.map(comp =>
	        		<ArtifactComponentRow handleClick={(e) => onComponentClick(comp)} component={comp} key={comp.id} />
	        	)
	        }</TableBody>
	        <TableFooter>
	          <TableRow> 
	          	<TableRowColumn style={{paddingTop: 20, textAlign: "center"}}>
	              <IconButton disabled={page.current == 1} style={{top: 8}} onClick={() => onPageChange(page.current - 1)}>
	                <ChevronLeft/>
	              </IconButton>
	              {pageButtons}
	              <IconButton disabled={page.current == page.total}  style={{top: 8}} onClick={() => onPageChange(page.current + 1)} >
	                <ChevronRight/>
	              </IconButton>
	              </TableRowColumn>
	          </TableRow>
	        </TableFooter>
	    </Table>
	)
}

export default ArtifactComponentTable