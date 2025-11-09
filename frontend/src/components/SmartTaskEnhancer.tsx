import React, { useState } from 'react';
import { 
  Sparkles, 
  Cloud, 
  Link2, 
  Globe,
  Loader2, 
  X,
  CheckCircle,
  AlertCircle
} from 'lucide-react';

interface SmartTaskEnhancerProps {
  onInsertMotivation?: (text: string) => void;
  onSetReminder?: (weatherInfo: string) => void;
  onAttachUrl?: (url: string, isValid: boolean) => void;
}

const SmartTaskEnhancer: React.FC<SmartTaskEnhancerProps> = ({ 
  onInsertMotivation,
  onSetReminder,
  onAttachUrl
}) => {
  const [activeFeature, setActiveFeature] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);
  const [result, setResult] = useState<any>(null);
  
  // Feature-specific inputs
  const [urlInput, setUrlInput] = useState('');
  const [cityInput, setCityInput] = useState('');
  const [apiUrlInput, setApiUrlInput] = useState('');

  const sendRequest = async (action: string, data?: any) => {
    try {
      const response = await fetch('http://localhost:3000/url-service', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
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

  // 1. Get Motivational Quote for Task Description
  const handleGetMotivation = async () => {
    setLoading(true);
    const response = await sendRequest('GET_QUOTE');
    if (response.status === 'success' && onInsertMotivation) {
      const motivationText = `💡 ${response.data.quote}\n   — ${response.data.author}`;
      onInsertMotivation(motivationText);
      setResult({ type: 'success', message: 'Motivation added to task!' });
      setTimeout(() => setActiveFeature(null), 1500);
    } else {
      setResult({ type: 'error', message: response.message });
    }
    setLoading(false);
  };

  // 2. Check Weather for Outdoor Tasks
  const handleCheckWeather = async () => {
    if (!cityInput.trim()) {
      setResult({ type: 'error', message: 'Please enter a city name' });
      return;
    }
    
    setLoading(true);
    const response = await sendRequest('GET_WEATHER', { city: cityInput });
    if (response.status === 'success') {
      const weatherInfo = `🌤️ ${response.data.city}: ${response.data.temperature} (${response.data.description})`;
      if (onSetReminder) {
        onSetReminder(weatherInfo);
      }
      setResult({ 
        type: 'success', 
        data: response.data,
        message: 'Weather info retrieved!' 
      });
    } else {
      setResult({ type: 'error', message: response.message });
    }
    setLoading(false);
  };

  // 3. Validate Task-Related URLs (meeting links, resources, etc.)
  const handleValidateUrl = async () => {
    if (!urlInput.trim()) {
      setResult({ type: 'error', message: 'Please enter a URL' });
      return;
    }

    setLoading(true);
    const response = await sendRequest('VALIDATE_URL', { url: urlInput });
    if (response.status === 'success') {
      const isValid = response.data.valid && response.data.accessible;
      if (onAttachUrl) {
        onAttachUrl(urlInput, isValid);
      }
      setResult({ 
        type: isValid ? 'success' : 'warning',
        data: response.data,
        message: isValid ? 'URL is valid and accessible!' : 'URL has issues' 
      });
    } else {
      setResult({ type: 'error', message: response.message });
    }
    setLoading(false);
  };

  // 4. Fetch Data from External API (e.g., project management APIs)
  const handleFetchApiData = async () => {
    if (!apiUrlInput.trim()) {
      setResult({ type: 'error', message: 'Please enter an API URL' });
      return;
    }

    setLoading(true);
    const response = await sendRequest('FETCH_API', { 
      url: apiUrlInput,
      method: 'GET' 
    });
    if (response.status === 'success') {
      setResult({ 
        type: 'success',
        data: response.data,
        message: 'API data fetched successfully!' 
      });
    } else {
      setResult({ type: 'error', message: response.message });
    }
    setLoading(false);
  };

  const features = [
    {
      id: 'motivation',
      icon: Sparkles,
      title: 'Add Motivation',
      description: 'Get an inspirational quote',
      color: 'from-amber-500 to-orange-500',
      action: handleGetMotivation,
    },
    {
      id: 'weather',
      icon: Cloud,
      title: 'Check Weather',
      description: 'For outdoor tasks',
      color: 'from-cyan-500 to-blue-500',
      action: handleCheckWeather,
      hasInput: true,
    },
    {
      id: 'validate',
      icon: Link2,
      title: 'Validate URL',
      description: 'Check meeting/resource links',
      color: 'from-green-500 to-emerald-500',
      action: handleValidateUrl,
      hasInput: true,
    },
    {
      id: 'api',
      icon: Globe,
      title: 'Fetch API Data',
      description: 'Import from external systems',
      color: 'from-purple-500 to-pink-500',
      action: handleFetchApiData,
      hasInput: true,
    },
  ];

  return (
    <div className="space-y-3">
      <div className="flex items-center gap-2 mb-2">
        <Sparkles className="w-4 h-4 text-indigo-600" />
        <h4 className="text-sm font-semibold text-slate-700">Smart Task Enhancers</h4>
      </div>

      {/* Feature Buttons */}
      <div className="grid grid-cols-2 gap-2">
        {features.map((feature) => {
          const Icon = feature.icon;
          return (
            <button
              key={feature.id}
              type="button"
              onClick={() => {
                if (!feature.hasInput) {
                  feature.action();
                } else {
                  setActiveFeature(activeFeature === feature.id ? null : feature.id);
                  setResult(null);
                }
              }}
              className={`flex items-center gap-2 p-3 rounded-xl border-2 transition-all ${
                activeFeature === feature.id
                  ? 'border-indigo-400 bg-indigo-50'
                  : 'border-slate-200 bg-white hover:border-indigo-200 hover:bg-slate-50'
              }`}
            >
              <div className={`p-2 bg-gradient-to-br ${feature.color} rounded-lg`}>
                <Icon className="w-4 h-4 text-white" />
              </div>
              <div className="text-left flex-1">
                <p className="text-xs font-semibold text-slate-800">{feature.title}</p>
                <p className="text-[10px] text-slate-500">{feature.description}</p>
              </div>
            </button>
          );
        })}
      </div>

      {/* Input Panel for Active Feature */}
      {activeFeature && (
        <div className="bg-gradient-to-br from-slate-50 to-blue-50 rounded-xl p-4 border border-slate-200 animate-fade-in">
          <div className="flex items-center justify-between mb-3">
            <h5 className="text-sm font-semibold text-slate-700">
              {features.find(f => f.id === activeFeature)?.title}
            </h5>
            <button
              onClick={() => {
                setActiveFeature(null);
                setResult(null);
              }}
              className="p-1 hover:bg-slate-200 rounded transition-colors"
            >
              <X className="w-4 h-4 text-slate-500" />
            </button>
          </div>

          {/* Weather Input */}
          {activeFeature === 'weather' && (
            <div className="space-y-2">
              <input
                type="text"
                value={cityInput}
                onChange={(e) => setCityInput(e.target.value)}
                placeholder="Enter city name (e.g., London)"
                className="w-full px-3 py-2 text-sm border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-cyan-500"
                onKeyPress={(e) => e.key === 'Enter' && handleCheckWeather()}
              />
              <button
                onClick={handleCheckWeather}
                disabled={loading}
                className="w-full py-2 px-4 bg-gradient-to-r from-cyan-500 to-blue-500 text-white text-sm rounded-lg hover:shadow-lg transition-all disabled:opacity-50 flex items-center justify-center gap-2"
              >
                {loading ? <Loader2 className="w-4 h-4 animate-spin" /> : 'Check Weather'}
              </button>
            </div>
          )}

          {/* URL Validation Input */}
          {activeFeature === 'validate' && (
            <div className="space-y-2">
              <input
                type="url"
                value={urlInput}
                onChange={(e) => setUrlInput(e.target.value)}
                placeholder="Enter URL (e.g., https://meet.google.com/xyz)"
                className="w-full px-3 py-2 text-sm border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-green-500"
                onKeyPress={(e) => e.key === 'Enter' && handleValidateUrl()}
              />
              <button
                onClick={handleValidateUrl}
                disabled={loading}
                className="w-full py-2 px-4 bg-gradient-to-r from-green-500 to-emerald-500 text-white text-sm rounded-lg hover:shadow-lg transition-all disabled:opacity-50 flex items-center justify-center gap-2"
              >
                {loading ? <Loader2 className="w-4 h-4 animate-spin" /> : 'Validate URL'}
              </button>
            </div>
          )}

          {/* API Fetch Input */}
          {activeFeature === 'api' && (
            <div className="space-y-2">
              <input
                type="url"
                value={apiUrlInput}
                onChange={(e) => setApiUrlInput(e.target.value)}
                placeholder="Enter API endpoint (e.g., https://api.example.com/data)"
                className="w-full px-3 py-2 text-sm border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-purple-500"
                onKeyPress={(e) => e.key === 'Enter' && handleFetchApiData()}
              />
              <button
                onClick={handleFetchApiData}
                disabled={loading}
                className="w-full py-2 px-4 bg-gradient-to-r from-purple-500 to-pink-500 text-white text-sm rounded-lg hover:shadow-lg transition-all disabled:opacity-50 flex items-center justify-center gap-2"
              >
                {loading ? <Loader2 className="w-4 h-4 animate-spin" /> : 'Fetch Data'}
              </button>
            </div>
          )}

          {/* Result Display */}
          {result && (
            <div className={`mt-3 p-3 rounded-lg border ${
              result.type === 'success' ? 'bg-green-50 border-green-200' :
              result.type === 'warning' ? 'bg-yellow-50 border-yellow-200' :
              'bg-red-50 border-red-200'
            }`}>
              <div className="flex items-start gap-2">
                {result.type === 'success' ? (
                  <CheckCircle className="w-4 h-4 text-green-600 mt-0.5" />
                ) : (
                  <AlertCircle className={`w-4 h-4 mt-0.5 ${
                    result.type === 'warning' ? 'text-yellow-600' : 'text-red-600'
                  }`} />
                )}
                <div className="flex-1">
                  <p className={`text-sm font-medium ${
                    result.type === 'success' ? 'text-green-800' :
                    result.type === 'warning' ? 'text-yellow-800' :
                    'text-red-800'
                  }`}>
                    {result.message}
                  </p>
                  
                  {/* Weather Data Display */}
                  {result.data && activeFeature === 'weather' && (
                    <div className="mt-2 grid grid-cols-2 gap-2 text-xs">
                      <div className="bg-white p-2 rounded">
                        <p className="text-slate-500">Temperature</p>
                        <p className="font-bold text-slate-800">{result.data.temperature}</p>
                      </div>
                      <div className="bg-white p-2 rounded">
                        <p className="text-slate-500">Humidity</p>
                        <p className="font-bold text-slate-800">{result.data.humidity}</p>
                      </div>
                    </div>
                  )}

                  {/* URL Validation Display */}
                  {result.data && activeFeature === 'validate' && (
                    <div className="mt-2 text-xs space-y-1">
                      <p>✓ Valid: {result.data.valid ? 'Yes' : 'No'}</p>
                      <p>✓ Accessible: {result.data.accessible ? 'Yes' : 'No'}</p>
                      {result.data.httpStatus && (
                        <p>✓ Status: {result.data.httpStatus}</p>
                      )}
                    </div>
                  )}

                  {/* API Data Display */}
                  {result.data && activeFeature === 'api' && (
                    <div className="mt-2 bg-white p-2 rounded max-h-32 overflow-y-auto">
                      <pre className="text-[10px] text-slate-700">
                        {JSON.stringify(JSON.parse(result.data.response), null, 2)}
                      </pre>
                    </div>
                  )}
                </div>
              </div>
            </div>
          )}
        </div>
      )}

      {/* Info Text */}
      <p className="text-xs text-slate-500 italic">
        💡 Enhance your tasks with smart features powered by external APIs
      </p>
    </div>
  );
};

export default SmartTaskEnhancer;
