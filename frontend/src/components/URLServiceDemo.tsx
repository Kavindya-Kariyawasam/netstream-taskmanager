import React, { useState } from 'react';
import {
  Sparkles,
  User,
  CheckCircle,
  XCircle,
  Link2,
  Download,
  Upload,
  Cloud,
  Globe,
  Loader2,
  Copy,
  Check
} from 'lucide-react';

interface ApiResponse {
  status: 'success' | 'error';
  data?: any;
  message?: string;
}

const URLServiceDemo: React.FC = () => {
  const [loading, setLoading] = useState<string | null>(null);
  const [results, setResults] = useState<Record<string, any>>({});
  const [copiedUrl, setCopiedUrl] = useState<string | null>(null);

  // Input states
  const [email, setEmail] = useState('demo@example.com');
  const [urlToValidate, setUrlToValidate] = useState('https://www.google.com');
  const [urlToParse, setUrlToParse] = useState('https://example.com:8080/path?param1=value1#section');
  const [city, setCity] = useState('London');
  const [downloadUrl, setDownloadUrl] = useState('https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf');
  const [apiUrl, setApiUrl] = useState('https://jsonplaceholder.typicode.com/todos/1');

  const sendRequest = async (action: string, data?: any): Promise<ApiResponse> => {
    try {
      const response = await fetch('http://localhost:3000/url-service', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ action, data }),
      });
      return await response.json();
    } catch (error) {
      return {
        status: 'error',
        message: error instanceof Error ? error.message : 'Network error'
      };
    }
  };

  const handleGetQuote = async () => {
    setLoading('quote');
    const result = await sendRequest('GET_QUOTE');
    setResults({ ...results, quote: result });
    setLoading(null);
  };

  const handleGetAvatar = async () => {
    setLoading('avatar');
    const result = await sendRequest('GET_AVATAR', { email });
    setResults({ ...results, avatar: result });
    setLoading(null);
  };

  const handleValidateUrl = async () => {
    setLoading('validate');
    const result = await sendRequest('VALIDATE_URL', { url: urlToValidate });
    setResults({ ...results, validate: result });
    setLoading(null);
  };

  const handleParseUrl = async () => {
    setLoading('parse');
    const result = await sendRequest('PARSE_URL', { url: urlToParse });
    setResults({ ...results, parse: result });
    setLoading(null);
  };

  const handleGetWeather = async () => {
    setLoading('weather');
    const result = await sendRequest('GET_WEATHER', { city });
    setResults({ ...results, weather: result });
    setLoading(null);
  };

  const handleDownloadFile = async () => {
    setLoading('download');
    const result = await sendRequest('DOWNLOAD_FILE', { 
      url: downloadUrl,
      fileName: 'downloaded_file.pdf'
    });
    setResults({ ...results, download: result });
    setLoading(null);
  };

  const handleUploadFile = async () => {
    setLoading('upload');
    const result = await sendRequest('UPLOAD_FILE', {
      fileData: 'Sample file content from frontend demo',
      fileName: 'frontend_upload.txt'
    });
    setResults({ ...results, upload: result });
    setLoading(null);
  };

  const handleFetchApi = async () => {
    setLoading('api');
    const result = await sendRequest('FETCH_API', {
      url: apiUrl,
      method: 'GET'
    });
    setResults({ ...results, api: result });
    setLoading(null);
  };

  const copyToClipboard = (text: string, key: string) => {
    navigator.clipboard.writeText(text);
    setCopiedUrl(key);
    setTimeout(() => setCopiedUrl(null), 2000);
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-50 via-blue-50 to-indigo-50 p-8">
      <div className="max-w-7xl mx-auto">
        {/* Header */}
        <div className="text-center mb-12">
          <h1 className="text-5xl font-bold bg-gradient-to-r from-indigo-600 to-purple-600 bg-clip-text text-transparent mb-4">
            URL Integration Service Demo
          </h1>
          <p className="text-lg text-slate-600">
            Member 3 - URLs/URIs & URLConnection
          </p>
          <div className="mt-4 flex items-center justify-center gap-2 text-sm text-slate-500">
            <div className="h-2 w-2 bg-green-500 rounded-full animate-pulse"></div>
            <span>Connected to URL Service (Port 8082)</span>
          </div>
        </div>

        {/* Features Grid */}
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
          
          {/* 1. GET_QUOTE */}
          <div className="bg-white rounded-2xl shadow-xl p-6 border border-slate-200 hover:shadow-2xl transition-shadow">
            <div className="flex items-center gap-3 mb-4">
              <div className="p-3 bg-gradient-to-br from-amber-400 to-orange-500 rounded-xl">
                <Sparkles className="w-6 h-6 text-white" />
              </div>
              <div>
                <h3 className="text-xl font-bold text-slate-800">Motivational Quote</h3>
                <p className="text-sm text-slate-500">Fetch from ZenQuotes API</p>
              </div>
            </div>

            <button
              onClick={handleGetQuote}
              disabled={loading === 'quote'}
              className="w-full py-3 px-4 bg-gradient-to-r from-amber-500 to-orange-500 text-white rounded-xl font-semibold hover:shadow-lg transition-all disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center gap-2"
            >
              {loading === 'quote' ? (
                <>
                  <Loader2 className="w-5 h-5 animate-spin" />
                  Fetching...
                </>
              ) : (
                'Get Quote'
              )}
            </button>

            {results.quote && (
              <div className="mt-4 p-4 bg-gradient-to-br from-amber-50 to-orange-50 rounded-xl border border-amber-200">
                {results.quote.status === 'success' ? (
                  <>
                    <p className="text-slate-700 italic text-lg mb-2">
                      "{results.quote.data.quote}"
                    </p>
                    <p className="text-slate-600 text-sm font-medium">
                      — {results.quote.data.author}
                    </p>
                    <p className="text-xs text-slate-500 mt-2">
                      Source: {results.quote.data.source}
                    </p>
                  </>
                ) : (
                  <p className="text-red-600 text-sm">{results.quote.message}</p>
                )}
              </div>
            )}
          </div>

          {/* 2. GET_AVATAR */}
          <div className="bg-white rounded-2xl shadow-xl p-6 border border-slate-200 hover:shadow-2xl transition-shadow">
            <div className="flex items-center gap-3 mb-4">
              <div className="p-3 bg-gradient-to-br from-blue-400 to-indigo-500 rounded-xl">
                <User className="w-6 h-6 text-white" />
              </div>
              <div>
                <h3 className="text-xl font-bold text-slate-800">Gravatar Avatar</h3>
                <p className="text-sm text-slate-500">Generate avatar URL</p>
              </div>
            </div>

            <input
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              placeholder="Enter email"
              className="w-full px-4 py-2 border border-slate-300 rounded-xl mb-3 focus:outline-none focus:ring-2 focus:ring-blue-500"
            />

            <button
              onClick={handleGetAvatar}
              disabled={loading === 'avatar'}
              className="w-full py-3 px-4 bg-gradient-to-r from-blue-500 to-indigo-500 text-white rounded-xl font-semibold hover:shadow-lg transition-all disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center gap-2"
            >
              {loading === 'avatar' ? (
                <>
                  <Loader2 className="w-5 h-5 animate-spin" />
                  Generating...
                </>
              ) : (
                'Generate Avatar URL'
              )}
            </button>

            {results.avatar && results.avatar.status === 'success' && (
              <div className="mt-4 p-4 bg-gradient-to-br from-blue-50 to-indigo-50 rounded-xl border border-blue-200">
                <div className="flex items-center gap-4">
                  <img
                    src={results.avatar.data.avatarUrl}
                    alt="Avatar"
                    className="w-16 h-16 rounded-full border-2 border-blue-300"
                  />
                  <div className="flex-1">
                    <p className="text-xs text-slate-500 mb-1">Avatar URL:</p>
                    <div className="flex items-center gap-2">
                      <p className="text-xs text-slate-700 break-all flex-1">
                        {results.avatar.data.avatarUrl.substring(0, 50)}...
                      </p>
                      <button
                        onClick={() => copyToClipboard(results.avatar.data.avatarUrl, 'avatar')}
                        className="p-2 hover:bg-blue-100 rounded-lg transition-colors"
                      >
                        {copiedUrl === 'avatar' ? (
                          <Check className="w-4 h-4 text-green-600" />
                        ) : (
                          <Copy className="w-4 h-4 text-slate-600" />
                        )}
                      </button>
                    </div>
                  </div>
                </div>
              </div>
            )}
          </div>

          {/* 3. VALIDATE_URL */}
          <div className="bg-white rounded-2xl shadow-xl p-6 border border-slate-200 hover:shadow-2xl transition-shadow">
            <div className="flex items-center gap-3 mb-4">
              <div className="p-3 bg-gradient-to-br from-green-400 to-emerald-500 rounded-xl">
                <CheckCircle className="w-6 h-6 text-white" />
              </div>
              <div>
                <h3 className="text-xl font-bold text-slate-800">URL Validator</h3>
                <p className="text-sm text-slate-500">Check URL validity & accessibility</p>
              </div>
            </div>

            <input
              type="text"
              value={urlToValidate}
              onChange={(e) => setUrlToValidate(e.target.value)}
              placeholder="Enter URL to validate"
              className="w-full px-4 py-2 border border-slate-300 rounded-xl mb-3 focus:outline-none focus:ring-2 focus:ring-green-500"
            />

            <button
              onClick={handleValidateUrl}
              disabled={loading === 'validate'}
              className="w-full py-3 px-4 bg-gradient-to-r from-green-500 to-emerald-500 text-white rounded-xl font-semibold hover:shadow-lg transition-all disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center gap-2"
            >
              {loading === 'validate' ? (
                <>
                  <Loader2 className="w-5 h-5 animate-spin" />
                  Validating...
                </>
              ) : (
                'Validate URL'
              )}
            </button>

            {results.validate && results.validate.status === 'success' && (
              <div className="mt-4 p-4 bg-gradient-to-br from-green-50 to-emerald-50 rounded-xl border border-green-200">
                <div className="space-y-2 text-sm">
                  <div className="flex items-center gap-2">
                    {results.validate.data.valid ? (
                      <CheckCircle className="w-4 h-4 text-green-600" />
                    ) : (
                      <XCircle className="w-4 h-4 text-red-600" />
                    )}
                    <span className="font-medium">
                      Valid: {results.validate.data.valid ? 'Yes' : 'No'}
                    </span>
                  </div>
                  {results.validate.data.valid && (
                    <>
                      <div className="flex items-center gap-2">
                        {results.validate.data.accessible ? (
                          <CheckCircle className="w-4 h-4 text-green-600" />
                        ) : (
                          <XCircle className="w-4 h-4 text-red-600" />
                        )}
                        <span className="font-medium">
                          Accessible: {results.validate.data.accessible ? 'Yes' : 'No'}
                        </span>
                      </div>
                      <p className="text-slate-600">
                        Protocol: {results.validate.data.protocol} | 
                        Host: {results.validate.data.host} | 
                        Status: {results.validate.data.httpStatus}
                      </p>
                    </>
                  )}
                </div>
              </div>
            )}
          </div>

          {/* 4. PARSE_URL */}
          <div className="bg-white rounded-2xl shadow-xl p-6 border border-slate-200 hover:shadow-2xl transition-shadow">
            <div className="flex items-center gap-3 mb-4">
              <div className="p-3 bg-gradient-to-br from-purple-400 to-pink-500 rounded-xl">
                <Link2 className="w-6 h-6 text-white" />
              </div>
              <div>
                <h3 className="text-xl font-bold text-slate-800">URL Parser</h3>
                <p className="text-sm text-slate-500">Extract URL components</p>
              </div>
            </div>

            <input
              type="text"
              value={urlToParse}
              onChange={(e) => setUrlToParse(e.target.value)}
              placeholder="Enter URL to parse"
              className="w-full px-4 py-2 border border-slate-300 rounded-xl mb-3 focus:outline-none focus:ring-2 focus:ring-purple-500"
            />

            <button
              onClick={handleParseUrl}
              disabled={loading === 'parse'}
              className="w-full py-3 px-4 bg-gradient-to-r from-purple-500 to-pink-500 text-white rounded-xl font-semibold hover:shadow-lg transition-all disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center gap-2"
            >
              {loading === 'parse' ? (
                <>
                  <Loader2 className="w-5 h-5 animate-spin" />
                  Parsing...
                </>
              ) : (
                'Parse URL'
              )}
            </button>

            {results.parse && results.parse.status === 'success' && (
              <div className="mt-4 p-4 bg-gradient-to-br from-purple-50 to-pink-50 rounded-xl border border-purple-200 max-h-64 overflow-y-auto">
                <div className="space-y-1 text-xs font-mono">
                  <p><span className="font-bold text-purple-700">Protocol:</span> {results.parse.data.protocol}</p>
                  <p><span className="font-bold text-purple-700">Host:</span> {results.parse.data.host}</p>
                  <p><span className="font-bold text-purple-700">Port:</span> {results.parse.data.port}</p>
                  <p><span className="font-bold text-purple-700">Path:</span> {results.parse.data.path}</p>
                  {results.parse.data.query && (
                    <p><span className="font-bold text-purple-700">Query:</span> {results.parse.data.query}</p>
                  )}
                  {results.parse.data.ref && (
                    <p><span className="font-bold text-purple-700">Fragment:</span> {results.parse.data.ref}</p>
                  )}
                  {results.parse.data.queryParams && (
                    <div className="mt-2">
                      <p className="font-bold text-purple-700 mb-1">Query Parameters:</p>
                      {Object.entries(results.parse.data.queryParams).map(([key, value]) => (
                        <p key={key} className="ml-4">• {key}: {value as string}</p>
                      ))}
                    </div>
                  )}
                </div>
              </div>
            )}
          </div>

          {/* 5. GET_WEATHER */}
          <div className="bg-white rounded-2xl shadow-xl p-6 border border-slate-200 hover:shadow-2xl transition-shadow">
            <div className="flex items-center gap-3 mb-4">
              <div className="p-3 bg-gradient-to-br from-cyan-400 to-blue-500 rounded-xl">
                <Cloud className="w-6 h-6 text-white" />
              </div>
              <div>
                <h3 className="text-xl font-bold text-slate-800">Weather Info</h3>
                <p className="text-sm text-slate-500">Get current weather</p>
              </div>
            </div>

            <input
              type="text"
              value={city}
              onChange={(e) => setCity(e.target.value)}
              placeholder="Enter city name"
              className="w-full px-4 py-2 border border-slate-300 rounded-xl mb-3 focus:outline-none focus:ring-2 focus:ring-cyan-500"
            />

            <button
              onClick={handleGetWeather}
              disabled={loading === 'weather'}
              className="w-full py-3 px-4 bg-gradient-to-r from-cyan-500 to-blue-500 text-white rounded-xl font-semibold hover:shadow-lg transition-all disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center gap-2"
            >
              {loading === 'weather' ? (
                <>
                  <Loader2 className="w-5 h-5 animate-spin" />
                  Fetching...
                </>
              ) : (
                'Get Weather'
              )}
            </button>

            {results.weather && results.weather.status === 'success' && (
              <div className="mt-4 p-4 bg-gradient-to-br from-cyan-50 to-blue-50 rounded-xl border border-cyan-200">
                <h4 className="font-bold text-slate-800 mb-2">{results.weather.data.city}</h4>
                <div className="grid grid-cols-2 gap-2 text-sm">
                  <div>
                    <p className="text-slate-500 text-xs">Temperature</p>
                    <p className="font-bold text-lg text-slate-800">{results.weather.data.temperature}</p>
                  </div>
                  <div>
                    <p className="text-slate-500 text-xs">Feels Like</p>
                    <p className="font-bold text-lg text-slate-800">{results.weather.data.feelsLike}</p>
                  </div>
                  <div>
                    <p className="text-slate-500 text-xs">Humidity</p>
                    <p className="font-semibold text-slate-700">{results.weather.data.humidity}</p>
                  </div>
                  <div>
                    <p className="text-slate-500 text-xs">Wind Speed</p>
                    <p className="font-semibold text-slate-700">{results.weather.data.windSpeed}</p>
                  </div>
                </div>
                <p className="mt-2 text-slate-600 italic">{results.weather.data.description}</p>
              </div>
            )}
          </div>

          {/* 6. DOWNLOAD_FILE */}
          <div className="bg-white rounded-2xl shadow-xl p-6 border border-slate-200 hover:shadow-2xl transition-shadow">
            <div className="flex items-center gap-3 mb-4">
              <div className="p-3 bg-gradient-to-br from-teal-400 to-green-500 rounded-xl">
                <Download className="w-6 h-6 text-white" />
              </div>
              <div>
                <h3 className="text-xl font-bold text-slate-800">File Download</h3>
                <p className="text-sm text-slate-500">Download from URL</p>
              </div>
            </div>

            <input
              type="text"
              value={downloadUrl}
              onChange={(e) => setDownloadUrl(e.target.value)}
              placeholder="Enter file URL"
              className="w-full px-4 py-2 border border-slate-300 rounded-xl mb-3 focus:outline-none focus:ring-2 focus:ring-teal-500 text-sm"
            />

            <button
              onClick={handleDownloadFile}
              disabled={loading === 'download'}
              className="w-full py-3 px-4 bg-gradient-to-r from-teal-500 to-green-500 text-white rounded-xl font-semibold hover:shadow-lg transition-all disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center gap-2"
            >
              {loading === 'download' ? (
                <>
                  <Loader2 className="w-5 h-5 animate-spin" />
                  Downloading...
                </>
              ) : (
                'Download File'
              )}
            </button>

            {results.download && results.download.status === 'success' && (
              <div className="mt-4 p-4 bg-gradient-to-br from-teal-50 to-green-50 rounded-xl border border-teal-200">
                <div className="space-y-2 text-sm">
                  <p><span className="font-bold text-teal-700">File:</span> {results.download.data.fileName}</p>
                  <p><span className="font-bold text-teal-700">Size:</span> {(results.download.data.fileSize / 1024).toFixed(2)} KB</p>
                  <p><span className="font-bold text-teal-700">Type:</span> {results.download.data.contentType}</p>
                  <p className="text-xs text-slate-600 break-all">
                    <span className="font-bold text-teal-700">Path:</span> {results.download.data.filePath}
                  </p>
                </div>
              </div>
            )}
          </div>

          {/* 7. UPLOAD_FILE */}
          <div className="bg-white rounded-2xl shadow-xl p-6 border border-slate-200 hover:shadow-2xl transition-shadow">
            <div className="flex items-center gap-3 mb-4">
              <div className="p-3 bg-gradient-to-br from-rose-400 to-red-500 rounded-xl">
                <Upload className="w-6 h-6 text-white" />
              </div>
              <div>
                <h3 className="text-xl font-bold text-slate-800">File Upload</h3>
                <p className="text-sm text-slate-500">Upload to server</p>
              </div>
            </div>

            <button
              onClick={handleUploadFile}
              disabled={loading === 'upload'}
              className="w-full py-3 px-4 bg-gradient-to-r from-rose-500 to-red-500 text-white rounded-xl font-semibold hover:shadow-lg transition-all disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center gap-2"
            >
              {loading === 'upload' ? (
                <>
                  <Loader2 className="w-5 h-5 animate-spin" />
                  Uploading...
                </>
              ) : (
                'Upload Sample File'
              )}
            </button>

            {results.upload && results.upload.status === 'success' && (
              <div className="mt-4 p-4 bg-gradient-to-br from-rose-50 to-red-50 rounded-xl border border-rose-200">
                <div className="space-y-2 text-sm">
                  <p><span className="font-bold text-rose-700">File:</span> {results.upload.data.fileName}</p>
                  <p><span className="font-bold text-rose-700">Size:</span> {results.upload.data.fileSize} bytes</p>
                  <p className="text-xs text-slate-600 break-all">
                    <span className="font-bold text-rose-700">Path:</span> {results.upload.data.filePath}
                  </p>
                  <p className="text-xs text-green-600 font-medium">✓ Upload successful!</p>
                </div>
              </div>
            )}
          </div>

          {/* 8. FETCH_API */}
          <div className="bg-white rounded-2xl shadow-xl p-6 border border-slate-200 hover:shadow-2xl transition-shadow">
            <div className="flex items-center gap-3 mb-4">
              <div className="p-3 bg-gradient-to-br from-violet-400 to-purple-500 rounded-xl">
                <Globe className="w-6 h-6 text-white" />
              </div>
              <div>
                <h3 className="text-xl font-bold text-slate-800">API Fetcher</h3>
                <p className="text-sm text-slate-500">Call any REST API</p>
              </div>
            </div>

            <input
              type="text"
              value={apiUrl}
              onChange={(e) => setApiUrl(e.target.value)}
              placeholder="Enter API URL"
              className="w-full px-4 py-2 border border-slate-300 rounded-xl mb-3 focus:outline-none focus:ring-2 focus:ring-violet-500 text-sm"
            />

            <button
              onClick={handleFetchApi}
              disabled={loading === 'api'}
              className="w-full py-3 px-4 bg-gradient-to-r from-violet-500 to-purple-500 text-white rounded-xl font-semibold hover:shadow-lg transition-all disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center gap-2"
            >
              {loading === 'api' ? (
                <>
                  <Loader2 className="w-5 h-5 animate-spin" />
                  Fetching...
                </>
              ) : (
                'Fetch API'
              )}
            </button>

            {results.api && results.api.status === 'success' && (
              <div className="mt-4 p-4 bg-gradient-to-br from-violet-50 to-purple-50 rounded-xl border border-violet-200 max-h-64 overflow-y-auto">
                <div className="space-y-2 text-sm">
                  <p><span className="font-bold text-violet-700">Status:</span> {results.api.data.statusCode}</p>
                  <p><span className="font-bold text-violet-700">Response:</span></p>
                  <pre className="text-xs bg-white p-2 rounded border border-violet-200 overflow-x-auto">
                    {JSON.stringify(JSON.parse(results.api.data.response), null, 2)}
                  </pre>
                </div>
              </div>
            )}
          </div>

        </div>

        {/* Info Footer */}
        <div className="mt-12 text-center">
          <div className="inline-block bg-white rounded-2xl shadow-lg p-6 border border-slate-200">
            <h3 className="text-lg font-bold text-slate-800 mb-2">
              Member 3 - URL/URI Integration Service
            </h3>
            <p className="text-sm text-slate-600 mb-4">
              Demonstrating URL, URI, URLConnection, and HttpURLConnection
            </p>
            <div className="flex flex-wrap justify-center gap-2 text-xs">
              <span className="px-3 py-1 bg-blue-100 text-blue-700 rounded-full font-medium">
                TCP Server (Port 8082)
              </span>
              <span className="px-3 py-1 bg-green-100 text-green-700 rounded-full font-medium">
                8 Features
              </span>
              <span className="px-3 py-1 bg-purple-100 text-purple-700 rounded-full font-medium">
                3 External APIs
              </span>
              <span className="px-3 py-1 bg-orange-100 text-orange-700 rounded-full font-medium">
                Exception Handling
              </span>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default URLServiceDemo;
