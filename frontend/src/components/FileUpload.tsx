import React, { useState } from "react";
import { uploadFile, downloadFile } from "../services/nioService";

const FileUpload: React.FC = () => {
    const [selectedFile, setSelectedFile] = useState<File | null>(null);
    const [progress, setProgress] = useState<number>(0);
    const [uploadedFileId, setUploadedFileId] = useState<string | null>(null);

    const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        if (e.target.files && e.target.files[0]) {
            setSelectedFile(e.target.files[0]);
        }
    };

    const handleUpload = async () => {
        console.log("🟢 Upload button clicked"); // <--- add this first line
        if (!selectedFile) {
            console.log("⚠️ No file selected");
            return;
        }

        try {
            const res = await uploadFile(selectedFile, setProgress);
            console.log("✅ Upload response:", res);
            setUploadedFileId(res.fileId);
        } catch (err) {
            console.error("❌ Upload error:", err);
        }
    };



    const handleDownload = async () => {
        if (!uploadedFileId) return;
        try {
            const res = await downloadFile(uploadedFileId);
            const url = window.URL.createObjectURL(new Blob([res.data]));
            const link = document.createElement("a");
            link.href = url;
            link.setAttribute("download", selectedFile?.name || "file");
            document.body.appendChild(link);
            link.click();
            link.remove();
        } catch (err) {
            console.error("Download error:", err);
        }
    };

    return (
        <div style={{ border: "2px dashed #ccc", padding: "20px", borderRadius: "8px" }}>
            <input
                type="file"
                onChange={(e) => {
                    const file = e.target.files?.[0];
                    console.log("File selected:", file);
                    if (file) setSelectedFile(file);
                }}
                style={{display: "block", marginBottom: "10px"}}
            />

            <button
                type="button"
                onClick={handleUpload}
                disabled={!selectedFile}
                style={{marginLeft: "10px"}}
            >
                Upload
            </button>

            {progress > 0 && (
                <div style={{marginTop: "10px", width: "100%", background: "#eee"}}>
                <div
                        style={{
                            width: `${progress}%`,
                            height: "10px",
                            background: "#4caf50",
                            transition: "width 0.2s",
                        }}
                    />
                </div>
            )}
            {uploadedFileId && (
                <button onClick={handleDownload} style={{ marginTop: "10px" }}>
                    Download Uploaded File
                </button>
            )}
        </div>
    );
};

export default FileUpload;
