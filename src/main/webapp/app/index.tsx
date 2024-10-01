import React from 'react'; 
import { createRoot } from 'react-dom/client'; 
import { Provider } from 'react-redux'; 
import { bindActionCreators } from 'redux'; 

import getStore from 'app/config/store'; 
import { registerLocale } from 'app/config/translation'; 
import setupAxiosInterceptors from 'app/config/axios-interceptor'; //for handling errors about authebtication
import { clearAuthentication } from 'app/shared/reducers/authentication'; //for clear authentication when user log out
import ErrorBoundary from 'app/shared/error/error-boundary'; // for error handing which occur in application
import AppComponent from 'app/app'; // import definition of appcomponent which is define in app.tsx
import { loadIcons } from 'app/config/icon-loader';

const store = getStore();
registerLocale(store);

const actions = bindActionCreators({ clearAuthentication }, store.dispatch);
setupAxiosInterceptors(() => actions.clearAuthentication('login.error.unauthorized'));

loadIcons();

const rootEl = document.getElementById('root');
const root = createRoot(rootEl);

const render = Component =>
  root.render(
    <ErrorBoundary>
      <Provider store={store}>
        <div>
          <Component />
        </div>
      </Provider>
    </ErrorBoundary>,
  );

render(AppComponent);
