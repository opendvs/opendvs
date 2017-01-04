import React, {Component} from 'react';
import { Grid, Row, Col } from 'react-flexbox-grid/lib/index'
import ComponentTable from './ComponentTable'
import ComponentVersionDialogContainer from '../../containers/ComponentVersionDialogContainer'
import { fetchComponentsIfNeeded, fetchComponents, selectPage } from '../../actions/component'
import { toggleVersionDialog } from '../../actions/component'

const  gridStyle = {width: "100%"};

class ComponentList extends Component {
	componentDidMount() {
	    const { dispatch, page } = this.props
	    dispatch(fetchComponentsIfNeeded(page))
	  }
	
	  componentWillReceiveProps(nextProps) {
		  if (this.props.page.current != nextProps.page.current) {
			  const { dispatch, page } = nextProps
			  dispatch(fetchComponents(page))
	  	  }
	  }

	  handleClick = newPage => {
	    this.props.dispatch(selectPage(newPage))
	  }
	  onComponentSelect = component => {
		  this.props.dispatch(toggleVersionDialog(true, component))
	  }
	render() {
		 const { items, page, dialog } = this.props

		 return (
		  <div>
		  	  <ComponentVersionDialogContainer />
		  	  <Grid style={gridStyle}>
		        <Row>
		          <h1>Fetched Components</h1>
			      <ComponentTable components={items} page={page} onPageChange={this.handleClick} onComponentSelect={this.onComponentSelect} />
			    </Row>
		      </Grid>
	      </div>
		)

	}
}

export default ComponentList