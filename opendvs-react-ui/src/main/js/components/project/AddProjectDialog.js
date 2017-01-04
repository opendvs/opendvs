import React, {Component} from 'react';
import FloatingActionButton from 'material-ui/FloatingActionButton'
import RaisedButton from 'material-ui/RaisedButton';
import ContentAdd from 'material-ui/svg-icons/content/add'
import Dialog from 'material-ui/Dialog'
import TextField from 'material-ui/TextField'
import NumberInput from 'material-ui-number-input'
import SelectField from 'material-ui/SelectField';
import MenuItem from 'material-ui/MenuItem'

import { toggleProjectDialog, fetchProjectTypes, selectProjectType, updateFormPropertyField, updateFormField, createNewProject } from '../../actions/project'


class AddProjectDialog extends Component {
	componentDidMount() {
	    const { dispatch } = this.props
	    dispatch(fetchProjectTypes())
	}

	handleClick = newPage => {
	    this.props.dispatch(selectPage(newPage))
	}

	render() {
		const { dispatch, project, selectedType, opened, projectTypes, creating } = this.props

	    const standardActions = (
	    		<RaisedButton label="Create project" primary={true} onTouchTap={() => dispatch(createNewProject(project))} disabled={creating} />
		    );
	
	    var typeItems = [];
	    projectTypes.forEach((it) => {
	        typeItems.push(<MenuItem key={it.id} value={it} primaryText={it.name} />)
	    });
	
	    var typeProperties = [];
	    if (selectedType.properties) {
	        selectedType.properties.forEach((it) => {
	            typeProperties.push(
	      		      <TextField key={it.name}
			      	  floatingLabelText={it.name}
			          hintText={it.description}
			          fullWidth={true}
	      		      defaultValue={project.typeProperties[it.key]}
	      		      onChange={(evt) => dispatch(updateFormPropertyField(it.key, evt.target.value)) }  />
	    		)
	        });    	
	    }
	
		return (
			<div>
				<FloatingActionButton mini={true} onTouchTap={() => dispatch(toggleProjectDialog(true))}>
		    		<ContentAdd />
		    	</FloatingActionButton>
	
				<Dialog
			      open={opened}
			      title="Create new project"
			      actions={standardActions}
			      onRequestClose={() => dispatch(toggleProjectDialog(false))}>
				      <TextField 
				      	  floatingLabelText="Name"
				          hintText="My example project"
				          fullWidth={true}
				      	  defaultValue={project.name}
			          	  onChange={(evt) => dispatch(updateFormField("name", evt.target.value)) } />
	
				      <SelectField
				      	  fullWidth={true}
			          	  floatingLabelText="Project type"
			          	  value={selectedType} onChange={(event, index, value) => dispatch(selectProjectType(value)) }>
				      	{typeItems}
			          </SelectField>
	
			          <NumberInput
			          defaultValue={project.majorVersionOffset}
			          min={0}
			      	  fullWidth={true}
		          	  floatingLabelText="Semantic versioning offset (days)"
		          	  hintText="Offset to treat dependencies with different major version as up-to-date"
		          	  onChange={(evt) => dispatch(updateFormField("majorVersionOffset", evt.target.value)) }  />
	
			          {typeProperties}
				  </Dialog>
			</div>
		)
	}
}

export default AddProjectDialog
