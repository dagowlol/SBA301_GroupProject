import { sessionApi } from '../../../api/sessionApi';

export const sessionService = {
  getSessions: async (params) => {
    try {
      return await sessionApi.getSessions(params);
    } catch (error) {
      console.error('Service: Error fetching sessions', error);
      throw error;
    }
  },

  getSessionById: async (id) => {
    try {
      return await sessionApi.getSessionById(id);
    } catch (error) {
      console.error('Service: Error fetching session by id', error);
      throw error;
    }
  },

  createSession: async (data, customIdempotencyKey) => {
    try {
      const idempotencyKey = customIdempotencyKey || crypto.randomUUID();
      return await sessionApi.createSession(data, idempotencyKey);
    } catch (error) {
      console.error('Service: Error creating session', error);
      throw error;
    }
  },

  updateSession: async (id, data) => {
    try {
      return await sessionApi.updateSession(id, data);
    } catch (error) {
      console.error('Service: Error updating session', error);
      throw error;
    }
  },

  deleteSession: async (id) => {
    try {
      return await sessionApi.deleteSession(id);
    } catch (error) {
      console.error('Service: Error deleting session', error);
      throw error;
    }
  },

  getDeletedSessions: async () => {
    try {
      return await sessionApi.getDeletedSessions();
    } catch (error) {
      console.error('Service: Error fetching deleted sessions', error);
      throw error;
    }
  },

  restoreSession: async (id) => {
    try {
      return await sessionApi.restoreSession(id);
    } catch (error) {
      console.error('Service: Error restoring session', error);
      throw error;
    }
  }
};
