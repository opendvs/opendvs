import React, {Component} from 'react';
import {Table, TableFooter, TableBody, TableHeader, TableHeaderColumn, TableRow, TableRowColumn} from 'material-ui/Table';
import ProjectRow from './ProjectRow'
import ChevronLeft from 'material-ui/svg-icons/navigation/chevron-left'
import ChevronRight from 'material-ui/svg-icons/navigation/chevron-right'
import IconButton from 'material-ui/IconButton'
import FlatButton from 'material-ui/FlatButton';

const PAGINATION_OFFSET = 2;
const ProjectTable = ({ projects, page, onPageChange }) => {
    var rows = [];

    projects.forEach((project) => {
      rows.push(<ProjectRow project={project} key={project.id} />);
    });

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
        <Table selectable={false}>
          <TableHeader adjustForCheckbox={false} displaySelectAll={false}>
            <TableRow>
              <TableHeaderColumn>Name</TableHeaderColumn>
              <TableHeaderColumn>Type</TableHeaderColumn>
            </TableRow>
          </TableHeader>
          <TableBody>{rows}</TableBody>
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

export default ProjectTable