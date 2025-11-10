import React, { useState } from 'react';
import { Sparkles, Loader2, X } from 'lucide-react';

interface QuickURLActionsProps {
  onInsertText?: (text: string) => void;
}

const QuickURLActions: React.FC<QuickURLActionsProps> = ({ onInsertText }) => {
  const [loading, setLoading] = useState<string | null>(null);
  const [showQuote, setShowQuote] = useState(false);
  const [quote, setQuote] = useState<any>(null);

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

  const handleGetQuote = async () => {
    setLoading('quote');
    const result = await sendRequest('GET_QUOTE');
    if (result.status === 'success') {
      setQuote(result.data);
      setShowQuote(true);
    }
    setLoading(null);
  };

  const handleUseQuote = () => {
    if (quote && onInsertText) {
      onInsertText(`${quote.quote}\n— ${quote.author}`);
      setShowQuote(false);
    }
  };

  return (
    <div className="relative">
      <div className="flex gap-2 mb-3">
        <button
          type="button"
          onClick={handleGetQuote}
          disabled={loading === 'quote'}
          className="flex items-center gap-2 px-3 py-1.5 text-sm bg-gradient-to-r from-amber-500 to-orange-500 text-white rounded-lg hover:shadow-md transition-all disabled:opacity-50"
        >
          {loading === 'quote' ? (
            <Loader2 className="w-4 h-4 animate-spin" />
          ) : (
            <Sparkles className="w-4 h-4" />
          )}
          Get Motivational Quote
        </button>
      </div>

      {showQuote && quote && (
        <div className="mb-3 p-4 bg-gradient-to-br from-amber-50 to-orange-50 rounded-xl border border-amber-200 relative">
          <button
            onClick={() => setShowQuote(false)}
            className="absolute top-2 right-2 p-1 hover:bg-amber-100 rounded transition-colors"
          >
            <X className="w-4 h-4 text-slate-600" />
          </button>
          <p className="text-slate-700 italic mb-2">"{quote.quote}"</p>
          <p className="text-slate-600 text-sm font-medium mb-3">— {quote.author}</p>
          <button
            type="button"
            onClick={handleUseQuote}
            className="px-4 py-1.5 text-sm bg-amber-600 text-white rounded-lg hover:bg-amber-700 transition-colors"
          >
            Use This Quote
          </button>
        </div>
      )}
    </div>
  );
};

export default QuickURLActions;
