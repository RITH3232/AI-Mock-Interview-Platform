import { api } from './api';

export const interviewRoomApi = {
  startSession: async (id: string) => {
    const response = await api.post('/room/start', { id });
    return response.data;
  },
  submitAnswer: async (payload: FormData) => {
    // FormData allows sending audio Buffer (Blob) along with fields
    const response = await api.post('/room/answer', payload, {
      headers: { 'Content-Type': 'multipart/form-data' },
    });
    return response.data;
  },
  getProgress: async (id: string) => {
    const response = await api.get(`/room/progress/${id}`);
    return response.data;
  },
  generateReport: async (id: string) => {
    const response = await api.post(`/room/report/generate/${id}`);
    return response.data;
  },
  fetchReport: async (id: string) => {
    const response = await api.get(`/room/report/${id}`);
    return response.data;
  },
  downloadPDF: async (id: string) => {
    const response = await api.get(`/room/export/${id}`, { responseType: 'blob' });
    const url = window.URL.createObjectURL(new Blob([response.data]));
    const link = document.createElement('a');
    link.href = url;
    link.setAttribute('download', `Interview_Report_${id}.pdf`);
    document.body.appendChild(link);
    link.click();
  }
};
