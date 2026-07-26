import { api } from './api';

export const interviewApi = {
  generateQuestions: async (payload: any) => {
    const response = await api.post('/interview/generate', payload);
    return response.data;
  },
  generateResumeQuestions: async (payload: any) => {
    const response = await api.post('/interview/resume', payload);
    return response.data;
  },
  generateCompanyQuestions: async (payload: any) => {
    const response = await api.post('/interview/company', payload);
    return response.data;
  },
  generateFollowUp: async (payload: any) => {
    const response = await api.post('/interview/followup', payload);
    return response.data;
  },
  getSession: async (id: string) => {
    const response = await api.get(`/interview/session/${id}`);
    return response.data;
  },
};
