import React, {Component} from 'react';
import MenuItem from 'material-ui/MenuItem';
import SelectField from 'material-ui/SelectField';
import { Link } from 'react-router'

const ArtifactSelect = ({ artifacts, selectedArtifact, onArtifactSelect }) => (
		<div className="artifact-select">
	        <SelectField fullWidth={true}
	          onChange={onArtifactSelect}
	          value={selectedArtifact.id}
	          floatingLabelText="Artifact" style={{"overflow": "hidden"}}>
	          { artifacts.map(art => 
	          	<MenuItem key={art.id} value={art.id} primaryText={art.name} />)
	          }
	        </SelectField>
	    </div>
  
)

export default ArtifactSelect