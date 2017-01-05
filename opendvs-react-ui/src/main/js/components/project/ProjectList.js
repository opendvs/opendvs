import React, {Component} from 'react';
import { Grid, Row, Col } from 'react-flexbox-grid/lib/index'
import ProjectTable from './ProjectTable'
import { fetchProjectsIfNeeded, fetchProjects, selectPage } from '../../actions/project'
import AddProjectContainer from '../../containers/AddProjectContainer'

const  gridStyle = {width: "100%"};

class ProjectList extends Component {
	componentDidMount() {
	    const { dispatch, page } = this.props
	    dispatch(fetchProjectsIfNeeded(page))
	  }
	
	  componentWillReceiveProps(nextProps) {
		  if (this.props.page.current != nextProps.page.current) {
			  const { dispatch, page } = nextProps
			  dispatch(fetchProjects(page))
	  	  }
	  }

	  handleClick = newPage => {
	    this.props.dispatch(selectPage(newPage))
	  }
	render() {
		 const { items, page } = this.props
		 
		 return (
		 <Grid style={gridStyle}>
			<Row>
				<Col xs={11} md={11}>
				  <h1>My projects</h1>
				</Col>
				<Col xs={1} md={1}>
					<AddProjectContainer />
				</Col>
		    </Row>
		    <Row>
		      <ProjectTable projects={items} page={page} onPageChange={this.handleClick} />
		    </Row>
		  </Grid>
		)

	}
}

export default ProjectList