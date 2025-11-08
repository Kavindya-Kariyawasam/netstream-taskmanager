import axios from "axios";

const BASE_URL = "http://localhost:8081";

/**
 * Uploads a file to the backend and returns the file info
 */
export const uploadFile = async (file, setProgress) => {
    try {
        const formData = new FormData();
        formData.append("file", file);

        const response = await axios.post(`${BASE_URL}/upload`, formData, {
            headers: { "Content-Type": "multipart/form-data" },
            onUploadProgress: (e) => {
                if (e.total) {
                    const percent = Math.round((e.loaded * 100) / e.total);
                    setProgress(percent);
                }
            },
        });

        // ✅ Expect backend to return something like:
        // { fileId: "file_1762530193702_5b6b0b08_catscooty.jpg", filePath: "uploads/..." }

        if (response.data && response.data.fileId) {
            console.log("✅ Upload successful:", response.data);
            return response.data; // example: { fileId, filePath }
        } else {
            throw new Error("No fileId returned from backend");
        }
    } catch (error) {
        console.error("❌ Upload failed:", error);
        throw error;
    }
};

/**
 * Downloads a file by ID from the backend
 */
export const downloadFile = async (fileId) => {
    try {
        const response = await axios.get(`${BASE_URL}/download/${fileId}`, {
            responseType: "blob",
        });

        // Extract filename from Content-Disposition
        const contentDisposition = response.headers["content-disposition"];
        const filenameMatch = contentDisposition?.match(/filename="?([^"]+)"?/);
        const filename = filenameMatch ? filenameMatch[1] : fileId;

        // Create a blob URL and trigger download
        const url = window.URL.createObjectURL(new Blob([response.data]));
        const link = document.createElement("a");
        link.href = url;
        link.setAttribute("download", filename);
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
        window.URL.revokeObjectURL(url);

        console.log("📥 File downloaded:", filename);
    } catch (error) {
        console.error("❌ Download failed:", error);
        throw error;
    }
};
