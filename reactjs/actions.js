const actions = {
  LIST_PROJECTS: 'LIST_PROJECTS',
  LIST_PROJECTS_SUCCESS: 'LIST_PROJECTS_SUCCESS',
  LIST_LIVE_PROJECTS: 'LIST_LIVE_PROJECTS',
  LIST_LIVE_PROJECTS_SUCCESS: 'LIST_LIVE_PROJECTS_SUCCESS',
  LIST_CASTING_PENDING: 'LIST_CASTING_PENDING',
  LIST_CASTING_PENDING_SUCCESS: 'LIST_CASTING_PENDING_SUCCESS',
  LIST_PROJECTS_KINDS: 'LIST_PROJECTS_KINDS',
  LIST_PROJECTS_KINDS_SUCCESS: 'LIST_PROJECTS_KINDS_SUCCESS',
  LIST_USERS: 'LIST_USERS',
  LIST_USERS_SUCCESS: 'LIST_USERS_SUCCESS',
  CREATE_PROJECTS: 'CREATE_PROJECTS',
  UPDATE_PROJECT: 'UPDATE_PROJECT',
  UPDATE_PROJECT_SUCCESS: 'UPDATE_PROJECT_SUCCESS',
  LIST_NOTIFICATIONS: 'LIST_NOTIFICATIONS',
  LIST_NOTIFICATIONS_SUCCESS: 'LIST_NOTIFICATIONS_SUCCESS',
  ACCEPT_NOTIFICATION: 'ACCEPT_NOTIFICATION',
  REJECT_NOTIFICATION: 'REJECT_NOTIFICATION',
  READ_NOTIFICATION: 'READ_NOTIFICATION',
  ADD_CASTING: 'ADD_CASTING',
  FETCH_CASTING: 'FETCH_CASTING',
  ADD_FILE: 'ADD_FILE',
  SIGNED_URL_SUCCESS: 'SIGNED_URL_SUCCESS',
  SET_LANG: 'SET_LANG',
  PROJECT_FEEDBACK: 'PROJECT_FEEDBACK',
  PENDING_EXPORT: 'PENDING_EXPORT',
  PENDING_EXPORT_SUCCESS: 'PENDING_EXPORT_SUCCESS',
  PENDING_DUB: 'PENDING_DUB',
  PENDING_DUB_SUCCESS: 'PENDING_DUB_SUCCESS',
  INVI_PENDING: 'INVI_PENDING',
  INVI_PENDING_SUCCESS: 'INVI_PENDING_SUCCESS',
  SET_KEY: 'SET_KEY',
  CLEAR_ALL_NOTIFICATIONS: 'CLEAR_ALL_NOTIFICATIONS',
  listProjects: (payload) => ({
    type: actions.LIST_PROJECTS,
    payload: payload
  }),
  listLiveProjects: (payload) => ({
    type: actions.LIST_LIVE_PROJECTS,
    payload: payload
  }),
  listCastingPending: (payload) => ({
    type: actions.LIST_CASTING_PENDING,
    payload: payload
  }),
  addCasting: (payload, projectId) => ({
    type: actions.ADD_CASTING,
    payload,
    projectId,
  }),
  addFile:(file, fileType) => ({
    type: actions.ADD_FILE,
    file,
    fileType,
  }),
  listNotifications: () => ({
    type: actions.LIST_NOTIFICATIONS,
  }),
  listProjectKinds: (payload) => ({
    type: actions.LIST_PROJECTS_KINDS,
  }),
  listUsers: (payload) => ({
    type: actions.LIST_USERS,
  }),
  createProject: (payload, spinHandler) => ({
    type: actions.CREATE_PROJECTS,
    payload,
    spinHandler,
  }),
  editProject: (payload, pId, spinHandler) => ({
    type: actions.UPDATE_PROJECT,
    payload,
    pId,
    spinHandler,
  }),
  projectFeedback: (payload) => ({
    type: actions.PROJECT_FEEDBACK,
    payload: payload
  }),
  readNotification: (payload) => ({
    type: actions.READ_NOTIFICATION,
    payload: payload
  }),
  setLang: (langs) => ({
    type: actions.SET_LANG,
    langs,
  }),
  pendingExport: () => ({
    type: actions.PENDING_EXPORT,
  }),
  pendingDub: (payload) => ({
    type: actions.PENDING_DUB,
    payload,
  }),
  inviPending: () => ({
    type: actions.INVI_PENDING,
  }),
  updateSearchKey: (key) => ({
    type: actions.SET_KEY,
    key,
  }),
  clearAllNotifications: () => ({
    type: actions.CLEAR_ALL_NOTIFICATIONS,
  }),
};
export default actions;
