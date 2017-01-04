import React from 'react'
import {render} from 'react-dom'
import injectTapEventPlugin from 'react-tap-event-plugin'
import { Provider } from 'react-redux'
import { createStore,applyMiddleware } from 'redux'
import thunk from 'redux-thunk'
import Main from './components/Main'
import opendvsApp from './reducers'

// Needed for onTouchTap
// http://stackoverflow.com/a/34015469/988941
injectTapEventPlugin();

const middleware = [ thunk ]

let store = createStore(opendvsApp,  applyMiddleware(...middleware))

render(
  <Provider store={store}>
  	<Main />
  </Provider>,
  document.getElementById('app')
);
