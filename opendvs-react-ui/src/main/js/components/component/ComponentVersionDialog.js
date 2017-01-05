import React, {Component} from 'react';
import FloatingActionButton from 'material-ui/FloatingActionButton'
import RaisedButton from 'material-ui/RaisedButton';
import ContentAdd from 'material-ui/svg-icons/content/add'
import Dialog from 'material-ui/Dialog'
import TextField from 'material-ui/TextField'
import NumberInput from 'material-ui-number-input'
import SelectField from 'material-ui/SelectField';
import MenuItem from 'material-ui/MenuItem'
import VersionRow from './VersionRow'
import {Table, TableFooter, TableBody, TableHeader, TableHeaderColumn, TableRow, TableRowColumn} from 'material-ui/Table';



const ComponentVersionDialog = ({ dialog, onClose }) => {
	var versions = [];
	if (dialog.component && dialog.component.versions) {
		dialog.component.versions.sort(function(a,b) {return b.published - a.published}); 

        dialog.component.versions.forEach((ver) => {
	      versions.push(<VersionRow key={ver.id} 
	      							version={ver}
	      							latest={ver.version == dialog.component.latestVersion}
	      							outdated={dialog.state == 'OUTDATED' && dialog.version == ver.version}
	      							vulnerable={dialog.state == 'VULNERABLE' && dialog.version == ver.version}
	      							upToDate={dialog.state == 'UP_TO_DATE' && dialog.version == ver.version} />);
	    });
	}

	return (
		<Dialog
            open={dialog.open}
	        autoScrollBodyContent={true}
            title={`Versions for ${dialog.component.id}`}
			onRequestClose={onClose}>
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
	)
}

export default ComponentVersionDialog