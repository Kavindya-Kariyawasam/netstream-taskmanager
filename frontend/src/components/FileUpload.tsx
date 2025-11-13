import { useState, useRef } from "react";
import { Upload, File, X, Download, Loader2 } from "lucide-react";
import { nioService } from "@/services/nioService";

interface UploadedFile {
    id: string;
    name: string;
    size: number;
    uploadedAt: Date;
}

export default function FileUpload() {
    const [files, setFiles] = useState<UploadedFile[]>([]);
    const [uploading, setUploading] = useState(false);
    const [uploadProgress, setUploadProgress] = useState(0);
    const [dragActive, setDragActive] = useState(false);
    const fileInputRef = useRef<HTMLInputElement>(null);

    const handleDrag = (e: React.DragEvent) => {
        e.preventDefault();
        e.stopPropagation();
        if (e.type === "dragenter" || e.type === "dragover") {
            setDragActive(true);
        } else if (e.type === "dragleave") {
            setDragActive(false);
        }
    };

    const handleDrop = (e: React.DragEvent) => {
        e.preventDefault();
        e.stopPropagation();
        setDragActive(false);

        if (e.dataTransfer.files && e.dataTransfer.files[0]) {
            handleFile(e.dataTransfer.files[0]);
        }
    };

    const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        e.preventDefault();
        if (e.target.files && e.target.files[0]) {
            handleFile(e.target.files[0]);
        }
    };

    const handleFile = async (file: File) => {
        const maxSize = 50 * 1024 * 1024; // 50MB
        if (file.size > maxSize) {
            alert("File size must be less than 50MB");
            return;
        }

        setUploading(true);
        setUploadProgress(0);

        try {
            const response = await nioService.uploadFile(
                file,
                "task_default",
                (progress) => setUploadProgress(progress)
            );

            const newFile: UploadedFile = {
                id: response.fileId,
                name: response.fileName,
                size: response.size,
                uploadedAt: new Date(),
            };

            setFiles((prev) => [newFile, ...prev]);
            alert("File uploaded successfully!");
        } catch (error) {
            console.error("Upload failed:", error);
            alert("Failed to upload file. Please try again.");
        } finally {
            setUploading(false);
            setUploadProgress(0);
            if (fileInputRef.current) {
                fileInputRef.current.value = "";
            }
        }
    };

    const handleDownload = async (fileId: string, fileName: string) => {
        try {
            const blob = await nioService.downloadFile(fileId);
            const url = window.URL.createObjectURL(blob);
            const a = document.createElement("a");
            a.href = url;
            a.download = fileName;
            document.body.appendChild(a);
            a.click();
            window.URL.revokeObjectURL(url);
            document.body.removeChild(a);
        } catch (error) {
            console.error("Download failed:", error);
            alert("Failed to download file.");
        }
    };

    const handleRemove = async (fileId: string) => {
        try {
            await nioService.deleteFile(fileId); // <-- calls server
            setFiles((prev) => prev.filter((f) => f.id !== fileId)); // update UI
        } catch (error) {
            console.error("Delete failed:", error);
            alert("Delete failed");
        }
    };



    const formatFileSize = (bytes: number): string => {
        if (bytes === 0) return "0 Bytes";
        const k = 1024;
        const sizes = ["Bytes", "KB", "MB", "GB"];
        const i = Math.floor(Math.log(bytes) / Math.log(k));
        return Math.round((bytes / Math.pow(k, i)) * 100) / 100 + " " + sizes[i];
    };

    return (
        <div className="bg-white rounded-lg shadow-md p-6">
            <div className="flex items-center justify-between mb-4">
                <h3 className="text-lg font-semibold text-slate-900">
                    File Attachments
                </h3>
                <Upload className="w-5 h-5 text-slate-400" />
            </div>

            <div
                onDragEnter={handleDrag}
                onDragLeave={handleDrag}
                onDragOver={handleDrag}
                onDrop={handleDrop}
                className={`border-2 border-dashed rounded-lg p-8 text-center transition-colors ${
                    dragActive
                        ? "border-indigo-500 bg-indigo-50"
                        : "border-slate-300 hover:border-slate-400"
                }`}
            >
                <input
                    ref={fileInputRef}
                    type="file"
                    className="hidden"
                    onChange={handleChange}
                    disabled={uploading}
                />

                {uploading ? (
                    <div className="space-y-3">
                        <Loader2 className="w-10 h-10 mx-auto text-indigo-600 animate-spin" />
                        <div className="w-full bg-slate-200 rounded-full h-2">
                            <div
                                className="bg-indigo-600 h-2 rounded-full transition-all duration-300"
                                style={{ width: `${uploadProgress}%` }}
                            />
                        </div>
                        <p className="text-sm text-slate-600">{uploadProgress}% uploaded</p>
                    </div>
                ) : (
                    <>
                        <Upload className="w-12 h-12 mx-auto text-slate-400 mb-4" />
                        <p className="text-sm text-slate-600 mb-2">
                            Drag and drop file here, or click to select
                        </p>
                        <button
                            onClick={() => fileInputRef.current?.click()}
                            className="px-4 py-2 bg-gradient-to-r from-indigo-600 to-purple-600 text-white rounded-md hover:from-indigo-700 hover:to-purple-700 transition-colors text-sm font-medium shadow-sm"
                        >
                            Choose File
                        </button>
                        <p className="text-xs text-slate-500 mt-2">Max file size: 50MB</p>
                    </>
                )}
            </div>

            {files.length > 0 && (
                <div className="mt-6 space-y-3">
                    <h4 className="text-sm font-semibold text-slate-700">
                        Uploaded Files
                    </h4>
                    {files.map((file) => (
                        <div
                            key={file.id}
                            className="flex items-center justify-between p-3 bg-slate-50 rounded-lg hover:bg-slate-100 transition-colors"
                        >
                            <div className="flex items-center gap-3 flex-1 min-w-0">
                                <File className="w-5 h-5 text-slate-600 flex-shrink-0" />
                                <div className="flex-1 min-w-0">
                                    <p className="text-sm font-medium text-slate-900 truncate">
                                        {file.name}
                                    </p>
                                    <p className="text-xs text-slate-500">
                                        {formatFileSize(file.size)} •{" "}
                                        {file.uploadedAt.toLocaleTimeString()}
                                    </p>
                                </div>
                            </div>
                            <div className="flex gap-2">
                                <button
                                    onClick={() => handleDownload(file.id, file.name)}
                                    className="p-1 text-indigo-600 hover:text-indigo-800 hover:bg-indigo-50 rounded transition-colors"
                                    title="Download"
                                >
                                    <Download className="w-4 h-4" />
                                </button>
                                <button
                                    onClick={() => handleRemove(file.id)}
                                    className="p-1 text-rose-600 hover:text-rose-800 hover:bg-rose-50 rounded transition-colors"
                                    title="Remove"
                                >
                                    <X className="w-4 h-4" />
                                </button>
                            </div>
                        </div>
                    ))}
                </div>
            )}
        </div>
    );
}
