import { all, call, takeEvery, put, fork } from 'redux-saga/effects';
import { push } from 'react-router-redux';
import actions from './actions';
import API from '../../helpers/API';
import Notification from '../../components/notification';

export function* getProjectAssetList() {
  yield takeEvery(actions.LIST_PROJECTS, function* (action) {
    try {
      const list = yield call(API.project.listProjectAssets, action.payload);
      if (list) {
        yield put({
          type: actions.LIST_PROJECTS_SUCCESS,
          projects: list
        });
      }
    } catch (err) {
      Notification(
        'error',
        'Invalid request',
        err.error
      );
    }
  });
}

export function* getLiveProjects() {
  yield takeEvery(actions.LIST_LIVE_PROJECTS, function* (action) {
    try {
      const list = yield call(API.project.listProjectAssets, action.payload);
      if (list) {
        yield put({
          type: actions.LIST_LIVE_PROJECTS_SUCCESS,
          projects: list
        });
      }
    } catch (err) {
      Notification(
        'error',
        'Invalid request',
        err.error
      );
    }
  });
}

export function* getCastingPending() {
  yield takeEvery(actions.LIST_CASTING_PENDING, function* (action) {
    try {
      const list = yield call(API.project.listProjectAssets, action.payload);
      if (list) {
        yield put({
          type: actions.LIST_CASTING_PENDING_SUCCESS,
          projects: list
        });
      }
    } catch (err) {
      Notification(
        'error',
        'Invalid request',
        err.error
      );
    }
  });
}

export function* createProject() {
  yield takeEvery(actions.CREATE_PROJECTS, function* (action) {
    try {
      const project = yield call(API.project.create, action.payload);
      Notification(
        'success',
        'Project created successfully',
      );
      yield put(push('activeProjects'));
    } catch (err) {
      yield call(action.spinHandler, false);
      Notification(
        'error',
        'Creating project failed',
        err.error
      );
    }
  });
}

export function* editProject() {
  yield takeEvery(actions.UPDATE_PROJECT, function* (action) {
    try {
      yield call(API.project.update, action.payload, action.pId);
      yield call(action.spinHandler, false);
      Notification(
        'success',
        'Project updated successfully',
      );
      yield put(push(`/dashboard/project/${action.pId}`));

    } catch (err) {
      yield call(action.spinHandler, false);
      Notification(
        'error',
        'Updating project failed',
        err.error
      );
    }
  });
}


export function* addCasting() {
  yield takeEvery(actions.ADD_CASTING, function* (action) {
    try {
      yield call(API.project.addCasting, action.payload, action.projectId);
    } catch (err) {
      Notification(
        'error',
        'Invalid request',
        err.error
      );
    }
  });
}

export function* fetchpendingExport() {
  yield takeEvery(actions.PENDING_EXPORT, function* (action) {
    try {
      const list = yield call(API.project.exportPendingList);
      if (list) {
        yield put({
          type: actions.PENDING_EXPORT_SUCCESS,
          projects: list
        });
      }
    } catch (err) {
      Notification(
        'error',
        'Invalid request',
        err.error
      );
    }
  });
}

export function* clearAllNotifications() {
  yield takeEvery(actions.CLEAR_ALL_NOTIFICATIONS, function* (action) {
    try {
      const list = yield call(API.common.clearAllNotifications);
      const notification_list = yield call(API.project.notifications);
      if (notification_list) {
        yield put({
          type: actions.LIST_NOTIFICATIONS_SUCCESS,
          notifications: notification_list.data,
          totalCount: notification_list.totalCount
        });
      }
    } catch (err) {
      Notification(
        'error',
        'Invalid request',
        err.error
      );
    }
  });
}

export default function* rootSaga() {
  yield all([
    fork(getProjectAssetList),
    fork(getLiveProjects),
    fork(getCastingPending),
    fork(createProject),
    fork(addCasting),
    fork(editProject),
    fork(readNotification),
    fork(fetchpendingExport),
    fork(clearAllNotifications),
  ]);
}