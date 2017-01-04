import React, {Component} from 'react'
import FlatButton from 'material-ui/FlatButton'
import {TableRow, TableRowColumn} from 'material-ui/Table'


const ComponentRow = ({ component, onComponentSelect }) => (
   <TableRow>
     <TableRowColumn>{component.name}</TableRowColumn>
     <TableRowColumn>{component.latestVersion}</TableRowColumn>
     <TableRowColumn>{component.group}</TableRowColumn>
     <TableRowColumn><FlatButton label={component.versions.length} onTouchTap={onComponentSelect} /></TableRowColumn>
   </TableRow>
)

export default ComponentRow