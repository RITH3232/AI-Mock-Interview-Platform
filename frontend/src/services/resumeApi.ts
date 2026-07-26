import { api } from './api';

export const resumeApi = {
  uploadResume: async (file: File) => {
    const formData = new FormData();
    formData.append('resume', file);
    const response = await api.post('/resume/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });
    return response.data;
  },
  getLatestResume: async () => {
    const response = await api.get('/resume/latest');
    return response.data;
  },
  getResumeStatus: async (id: string) => {
    const response = await api.get(`/resume/status/${id}`);
    return response.data;
  },
  getResumeReport: async (id: string) => {
    const response = await api.get(`/resume/report/${id}`);
    return response.data;
  },
  deleteResume: async (id: string) => {
    const response = await api.delete(`/resume/${id}`);
    return response.data;
  },
};
