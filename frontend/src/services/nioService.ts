//nioService.ts
import axios from "axios";
import type { FileUploadResponse } from "@/types";

const NIO_API = "http://localhost:8081";

export const nioService = {
    uploadFile: async (
        file: File,
        taskId: string,
        onProgress?: (progress: number) => void
    ): Promise<FileUploadResponse> => {
        const formData = new FormData();
        formData.append("file", file);
        formData.append("taskId", taskId);
        formData.append("description", `Attachment for task ${taskId}`);

        const response = await axios.post(`${NIO_API}/upload`, formData, {
            headers: {
                "Content-Type": "multipart/form-data",
            },
            onUploadProgress: (progressEvent) => {
                if (progressEvent.total && onProgress) {
                    const percentCompleted = Math.round(
                        (progressEvent.loaded * 100) / progressEvent.total
                    );
                    onProgress(percentCompleted);
                }
            },
        });

        return response.data;
    },

    downloadFile: async (fileId: string): Promise<Blob> => {
        const response = await axios.get(`${NIO_API}/download/${fileId}`, {
            responseType: "blob",
        });
        return response.data;
    },

    deleteFile: async (fileId: string) => {
        return axios.delete(`${NIO_API}/files/${fileId}`);
    },

    getFiles: async () => {
        return await axios.get(`${NIO_API}/files`);
    }

};
