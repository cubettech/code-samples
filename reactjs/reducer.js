import { fromJS, Map } from 'immutable';
import actions from './actions';

const initState = fromJS({
  projects: null,
  projectsLive: null,
  castingPending: null,
  kinds: null,
  users: null,
  notifications: null,
  notificationCount: 0,
  project_status: null,
  accept_success: null,
  videoId: null,
  textFileId: null,
  fxFileId: null,
  appImgId: null,
  langs: null,
  exportList: null,
  key: null,
  dubPending: null,
});

export default function ProjectReducer(state = initState, action) {
  switch (action.type) {
    case actions.LIST_PROJECTS_SUCCESS:
          return state
            .set('projects', action.projects);
    case actions.LIST_LIVE_PROJECTS_SUCCESS:
          return state
            .set('projectsLive', action.projects);
    case actions.LIST_CASTING_PENDING_SUCCESS:
          return state
            .set('castingPending', action.projects);
    case actions.LIST_PROJECTS_KINDS_SUCCESS:
          return state
            .set('kinds', action.kinds);
    case actions.LIST_USERS_SUCCESS:
          return state
            .set('users', action.users);
    case actions.SIGNED_URL_SUCCESS:
          if(action.fileType == 'video'){
            return state
              .set('videoId', action.id);
          }
          else if(action.fileType == 'text'){
              return state
              .set('textFileId', action.id);}
          else if (action.fileType == 'fx'){
              return state
                .set('fxFileId', action.id);}
          else if(action.fileType == 'appImg'){
            return state
              .set('appImgId', action.id);}
    case actions.LIST_NOTIFICATIONS_SUCCESS:
          return state
            .set('notifications', action.notifications)
            .set('notificationCount', action.totalCount);
    case actions.ACCEPT_SUCCESS:
          return state
            .set('accept_success', true);
    case actions.PENDING_EXPORT_SUCCESS:
          return state
            .set('exportList', action.projects);
    case actions.PENDING_DUB_SUCCESS:
          return state
            .set('dubPending', action.projects);
    case actions.INVI_PENDING_SUCCESS:
          return state
            .set('dubPending', action.projects);
    case actions.SET_KEY:
          return state
            .set('key', action.key);
    case actions.SET_LANG:
          return state
            .set('langs', action.langs);
    default:
      return state;
  }
}
