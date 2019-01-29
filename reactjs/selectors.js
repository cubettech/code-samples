/**
 * Project selectors
 */

import { createSelector, createStructuredSelector } from 'reselect';

const selectProjects = state => state.Projects;
const selectApp = state => state.App;

const getDrawer = () => createSelector(
    selectApp,
    (projectState) => projectState.get('openDrawer')
);

const getCollapsed = () => createSelector(
    selectApp,
    (projectState) => projectState.get('collapsed')
);

const getProjects = () => createSelector(
    selectProjects,
    (projectState) => projectState.get('projects')
);

const getLiveProjects = () => createSelector(
    selectProjects,
    (projectState) => projectState.get('projectsLive')
);

const getCastingPending = () => createSelector(
    selectProjects,
    (projectState) => projectState.get('castingPending')
);

const getExportList = () => createSelector(
    selectProjects,
    (projectState) => projectState.get('exportList')
);
const getProjectStatus = () => createSelector(
    selectProjects,
    (projectState) => projectState.get('project_status')
);
const getUsers = () => createSelector(
    selectProjects,
    (userState) => userState.get('users')
);
const getSearchQuery = () => createSelector(
    selectProjects,
    (userState) => userState.get('key')
);
const getKinds = () => createSelector(
    selectProjects,
    (kindState) => kindState.get('kinds')
);
const getVideoId = () => createSelector(
    selectProjects,
    (projectState) => projectState.get('videoId')
);
const getTextFileId = () => createSelector(
    selectProjects,
    (projectState) => projectState.get('textFileId')
);
const getfxFileId = () => createSelector(
    selectProjects,
    (projectState) => projectState.get('fxFileId')
);
const getAppImgId = () => createSelector(
    selectProjects,
    (projectState) => projectState.get('appImgId')
);
const getNotifications = () => createSelector(
    selectProjects,
    (notificationState) => notificationState.get('notifications')
);
const getNotificationCount = () => createSelector(
    selectProjects,
    (notificationState) => notificationState.get('notificationCount')
);
const getAcceptSuccess = () => createSelector(
    selectProjects,
    (notificationState) => notificationState.get('accept_success')
);
const getProducers = () => createSelector(
    getUsers(),
    (users) => ((users) && (users.filter((user) => user && user.role && user.role.title == 'Producer')))
);
const getClients = () => createSelector(
    getUsers(),
    (users) => ((users) && (users.filter((user) => user && user.role && user.role.title == 'Client')))
);
const getDirectors = () => createSelector(
    getUsers(),
    (users) => ((users) && (users.filter((user) => user && user.role && user.role.title == 'Director')))
);
const getTranslators = () => createSelector(
    getUsers(),
    (users) => ((users) && (users.filter((user) => user && user.role && user.role.title == 'Translator')))
);
const getTechMixers = () => createSelector(
    getUsers(),
    (users) => ((users) && (users.filter((user) => user && user.role && user.role.title == 'Tech Mixer')))
);
const getDubbers = () => createSelector(
    getUsers(),
    (users) => ((users) && (users.filter((user) => user && user.role && user.role.title == 'Voice Actor')))
);

const PullDubPending = () => createSelector(
    selectProjects,
    (projectState) => projectState.get('dubPending')
);

const getDubPending = () => createSelector(
    PullDubPending(), getSearchQuery(),
    (characters, key) => ((characters) && (characters.filter((character) => 
        {
            key = key ? key.toLowerCase(): '';
            return key ? 
                character && (
                    character.title.toLowerCase().indexOf(key) > -1 ||
                    character.project && character.project.title.toLowerCase().indexOf(key) > -1
                )
            : true
        })
    ))
);

export {
    getProjects,
    getLiveProjects,
    getCastingPending,
    getDubPending,
    getKinds,
    getUsers,
    getVideoId,
    getTextFileId,
    getfxFileId,
    getAppImgId,
    getNotifications,
    getNotificationCount,
    getProjectStatus,
    getAcceptSuccess,
    getProducers,
    getClients,
    getDirectors,
    getTranslators,
    getTechMixers,
    getDubbers,
    getExportList,
    getDrawer,
    getCollapsed
};
