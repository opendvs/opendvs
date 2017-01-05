import React, {Component} from 'react';
import {Table, TableFooter, TableBody, TableHeader, TableHeaderColumn, TableRow, TableRowColumn} from 'material-ui/Table';
import { COMPONENT_STATE_COLORS } from './Project'
import FlatButton from 'material-ui/FlatButton';

const ArtifactComponentRow = ({ component, handleClick }) => (
	  <TableRow>
        <TableRowColumn>{component.name}</TableRowColumn>
        <TableRowColumn style={{width: '15%'}}>{component.version}{component.hash}</TableRowColumn>
        <TableRowColumn style={{width: '15%'}}>{component.scope}</TableRowColumn>
        <TableRowColumn style={{width: '10%'}}>{component.group}</TableRowColumn>
        <TableRowColumn style={{width: '10%'}}>{component.occurrences}</TableRowColumn>
        <TableRowColumn style={{width: '15%'}}><FlatButton style={{"color": COMPONENT_STATE_COLORS[component.state]}} onTouchTap={handleClick} label={component.state} /></TableRowColumn>
      </TableRow>
)

export default ArtifactComponentRow