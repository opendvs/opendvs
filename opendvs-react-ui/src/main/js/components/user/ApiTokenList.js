import React, {Component} from 'react';
import { Grid, Row, Col } from 'react-flexbox-grid/lib/index'
import {Table, TableFooter, TableBody, TableHeader, TableHeaderColumn, TableRow, TableRowColumn} from 'material-ui/Table';
import { fetchUserTokens, deleteUserToken, createToken } from '../../actions/auth'
import { toggleVulnerabilityDialog } from '../../actions/vulnerability'
import RaisedButton from 'material-ui/RaisedButton';
import FloatingActionButton from 'material-ui/FloatingActionButton';
import ContentAdd from 'material-ui/svg-icons/content/add';


const  gridStyle = {width: "100%"};

class VulnerabilityList extends Component {
	componentDidMount() {
	    this.props.dispatch(fetchUserTokens())
	  }

	  deleteToken = (token) => {
		  this.props.dispatch(deleteUserToken(token))
	  }

	  createToken = () => {
		  this.props.dispatch(createToken())
	  }

	  handleClick = newPage => {
	    this.props.dispatch(selectPage(newPage))
	  }

	  onDialogClose = () => {
		  this.props.dispatch(toggleVulnerabilityDialog(false, {}))
	  }

	render() {
		 const { tokens, id } = this.props

		 const rows = tokens.map((t) => (<TableRow key={ t.token }>
	   <TableRowColumn>{ t.token }</TableRowColumn>
	   <TableRowColumn><RaisedButton label="Delete" secondary={true} onClick={() => this.deleteToken(t.token)} /></TableRowColumn>
   </TableRow>));
		 return (
		  <div>
		  	  <Grid style={gridStyle}>
		  	  	<Row>
				  	<Col xs={10} md={10}>
			          <h1>API tokens for <small>{ id }</small></h1>
			      	</Col>
					<Col xs={1} md={1}>
					    <FloatingActionButton mini={true} onClick={() => this.createToken()}>
					      <ContentAdd />
					    </FloatingActionButton>
			      	</Col>
			    </Row>
		        <Row>
		          <Table>
			          <TableHeader adjustForCheckbox={false} displaySelectAll={false}>
			            <TableRow>
			              <TableHeaderColumn>Token</TableHeaderColumn>
			              <TableHeaderColumn>Action</TableHeaderColumn>
			            </TableRow>
			          </TableHeader>
			          <TableBody displayRowCheckbox={false}>{rows}</TableBody>
			        </Table>
			    </Row>
		      </Grid>
	      </div>
		)

	}
}

export default VulnerabilityList