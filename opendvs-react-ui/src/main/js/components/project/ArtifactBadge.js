import React, {Component} from 'react';
import { Legend, PieChart, Tooltip, Pie, Sector, Cell } from 'recharts'


export const COMPONENT_STATE_COLORS = {
		UP_TO_DATE: '#4CAF50',
		OUTDATED: '#FF9800',
		VULNERABLE: '#F44336',
		UNKNOWN: '#E0E0E0'
}

const chartColors = [COMPONENT_STATE_COLORS.UP_TO_DATE, COMPONENT_STATE_COLORS.OUTDATED, COMPONENT_STATE_COLORS.VULNERABLE, COMPONENT_STATE_COLORS.UNKNOWN];

const prepareChartData = (data) => {
    var dt = {UNKNOWN: 0, UP_TO_DATE: 0, OUTDATED: 0, VULNERABLE: 0};
      data.forEach((comp) => {
       dt[comp.state] += 1
     })
     var val = [
       {name: "Up-to-date", value: dt.UP_TO_DATE},
       {name: "Outdated", value: dt.OUTDATED},
       {name: "Vulnerable", value: dt.VULNERABLE},
       {name: "Unknown", value: dt.UNKNOWN},
     ];

     
     return val;
 }

class ArtifactBadge extends Component {
	constructor(props, context) {
	    super(props, context);
	    this.state = {
	    	chartData: undefined,
	    	currentSize: 0
	    }
	}

  componentDidMount() {
	const { artifact, components } = this.props
	if (components && components.length > 0) {
		this.setState({chartData: prepareChartData(components), currentSize: components.length});
	}
  }

	  componentWillReceiveProps(nextProps) {
		  const { artifact, components } = nextProps
			if (components && this.state.currentSize != components.length) {
				this.setState({chartData: prepareChartData(components), currentSize: components.length});
			}
	  }
	
	render() {
		const { artifact, components } = this.props

		if (artifact && artifact.initiated) {
			  if (components && components.length > 0 && this.state.chartData) {
			  var chart = (<PieChart width={400} height={200}>
	           <Pie  startAngle={180} endAngle={0} data={this.state.chartData} innerRadius={20} outerRadius={80}>
	             {
	             	this.state.chartData.map((entry, index) => <Cell key={index} fill={chartColors[index]}/>)
	             }
	           </Pie>
	           <Legend verticalAlign="bottom" height={80}/>
	           <Tooltip/>
	      </PieChart>);
			  }
		      var detail = (<div>
		        <h2>Artifact <i>{artifact.name}</i></h2>
		      <i><small>Initiated on {new Date(artifact.initiated).toString()}</small></i><br />
		      <i><small>{artifact.identity}</small></i><br />
		      <i><small>Analysis {artifact.probeAction && artifact.probeAction.state ? artifact.probeAction.state : '-'}</small></i><br />
		      <i><small>Overall state {artifact.state}</small></i>
		      <br />
		      <br />
		      {chart}
		      </div>);
		}
		return (
	
			<div>
		        {detail}
		    </div>
		)
	}
}

export default ArtifactBadge