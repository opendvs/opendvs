import React, {Component} from 'react'
import * as Colors from 'material-ui/styles/colors';
import {TableRow, TableRowColumn} from 'material-ui/Table'


const VersionRow = ({ version, latest, upToDate, outdated, vulnerable }) => {
	var style = {};
	if (upToDate) {
		style["backgroundColor"] = Colors.lightGreen100
		style["color"] = Colors.grey900
	} else if (outdated) {
		style["backgroundColor"] = Colors.orange100
		style["color"] = Colors.grey900
	} else if (vulnerable) {
		style["backgroundColor"] = Colors.red100
		style["color"] = Colors.grey900
	} else if (latest) {
		style["backgroundColor"] = Colors.lightBlue100
		style["color"] = Colors.grey900
	}
    return (
      <TableRow style={style}>
        <TableRowColumn>{version.version}</TableRowColumn>
        <TableRowColumn>{version.hash}</TableRowColumn>
        <TableRowColumn>{new Date(version.published).toString()}</TableRowColumn>
        <TableRowColumn>{version.source}</TableRowColumn>
      </TableRow>
    )
  }
export default VersionRow