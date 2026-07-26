import React, { useCallback, useState, useEffect } from 'react';
import { useDropzone } from 'react-dropzone';
import { motion } from 'framer-motion';
import { UploadCloud, File as FileIcon, X, CheckCircle2, Loader2 } from 'lucide-react';
import { useDispatch, useSelector } from 'react-redux';
import { useNavigate } from 'react-router-dom';
import { setResume, setError, setAnalyzing } from '../store/resumeSlice';
import { resumeApi } from '../services/resumeApi';
import { RootState } from '../store';

export const ResumeUpload: React.FC = () => {
  const [file, setFile] = useState<File | null>(null);
  const [uploadProgress, setUploadProgress] = useState(0);
  const [isUploading, setIsUploading] = useState(false);
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const { error } = useSelector((state: RootState) => state.resume);

  const onDrop = useCallback((acceptedFiles: File[]) => {
    if (acceptedFiles.length > 0) {
      setFile(acceptedFiles[0]);
    }
  }, []);

  const { getRootProps, getInputProps, isDragActive, isDragReject } = useDropzone({
    onDrop,
    accept: { 'application/pdf': ['.pdf'] },
    maxSize: 5 * 1024 * 1024, // 5MB
    multiple: false,
  });

  const handleUpload = async () => {
    if (!file) return;

    setIsUploading(true);
    setUploadProgress(10); // Fake initial progress

    try {
      const progressInterval = setInterval(() => {
        setUploadProgress((prev) => (prev >= 90 ? 90 : prev + 10));
      }, 500);

      const response = await resumeApi.uploadResume(file);
      clearInterval(progressInterval);
      setUploadProgress(100);

      dispatch(setResume(response.data.resume));
      dispatch(setAnalyzing(true));
      
      // Wait a moment so user sees 100% completion
      setTimeout(() => {
        navigate(`/resume/insights${window.location.search}`);
      }, 1000);

    } catch (error: any) {
      dispatch(setError(error.message));
      setUploadProgress(0);
      setIsUploading(false);
    }
  };

  return (
    <div className="flex flex-col items-center justify-center min-h-screen bg-background text-foreground p-4">
      <div className="w-full max-w-2xl text-center space-y-4 mb-12">
        <h1 className="text-4xl font-extrabold tracking-tight lg:text-5xl">Upload Your Resume</h1>
        <p className="text-lg text-muted-foreground">
          Let our AI analyze your resume and prepare you for your next big interview.
        </p>
      </div>

      {error && (
        <div className="w-full max-w-2xl p-4 mb-8 bg-destructive/10 text-destructive border border-destructive/20 rounded-xl text-center">
          <p className="font-medium">Analysis Failed</p>
          <p className="text-sm mt-1">{error}</p>
        </div>
      )}

      <motion.div
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        className="w-full max-w-2xl"
      >
        {!file ? (
          <div
            {...getRootProps()}
            className={`p-12 border-2 border-dashed rounded-2xl cursor-pointer transition-all duration-200 ease-in-out flex flex-col items-center justify-center space-y-4 bg-card ${
              isDragActive ? 'border-primary bg-primary/5 scale-[1.02]' : 'border-border hover:border-primary/50 hover:bg-accent/50'
            } ${isDragReject ? 'border-destructive bg-destructive/5' : ''}`}
          >
            <input {...getInputProps()} />
            <div className="p-4 bg-primary/10 rounded-full text-primary">
              <UploadCloud className="w-10 h-10" />
            </div>
            <div className="text-center">
              <p className="text-lg font-medium">
                {isDragActive ? 'Drop your resume here' : 'Drag & drop your resume here'}
              </p>
              <p className="text-sm text-muted-foreground mt-1">
                Supports PDF only (Max 5MB)
              </p>
            </div>
          </div>
        ) : (
          <div className="p-6 border border-border rounded-2xl bg-card shadow-sm space-y-6">
            <div className="flex items-center justify-between p-4 bg-accent/50 rounded-xl border border-border/50">
              <div className="flex items-center space-x-4">
                <div className="p-3 bg-primary/10 text-primary rounded-lg">
                  <FileIcon className="w-6 h-6" />
                </div>
                <div>
                  <p className="font-medium text-sm line-clamp-1">{file.name}</p>
                  <p className="text-xs text-muted-foreground">{(file.size / 1024 / 1024).toFixed(2)} MB</p>
                </div>
              </div>
              {!isUploading && (
                <button
                  onClick={() => setFile(null)}
                  className="p-2 text-muted-foreground hover:text-destructive hover:bg-destructive/10 rounded-full transition-colors"
                >
                  <X className="w-5 h-5" />
                </button>
              )}
            </div>

            {isUploading ? (
              <div className="space-y-2">
                <div className="flex justify-between text-sm font-medium">
                  <span>Uploading & Analyzing</span>
                  <span>{uploadProgress}%</span>
                </div>
                <div className="h-2 w-full bg-accent rounded-full overflow-hidden">
                  <motion.div
                    className="h-full bg-primary"
                    initial={{ width: 0 }}
                    animate={{ width: `${uploadProgress}%` }}
                    transition={{ ease: "easeOut" }}
                  />
                </div>
              </div>
            ) : (
              <button
                onClick={handleUpload}
                className="w-full py-3 px-4 bg-primary text-primary-foreground font-medium rounded-xl hover:bg-primary/90 transition-colors flex items-center justify-center space-x-2 shadow-md shadow-primary/20"
              >
                <span>Analyze Resume with AI</span>
              </button>
            )}
          </div>
        )}
      </motion.div>
    </div>
  );
};
