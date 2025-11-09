import { useState, useEffect } from "react";
import { RefreshCw, Mail, User } from "lucide-react";
import axios from "axios";
import type { Quote, Avatar } from "@/types";

export default function ProfileCard() {
  const [quote, setQuote] = useState<Quote | null>(null);
  const [avatar, setAvatar] = useState<Avatar | null>(null);
  const [email, setEmail] = useState("user@example.com");
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    fetchQuote();
    fetchAvatar(email);
  }, []);

  const fetchQuote = async () => {
    setLoading(true);
    try {
      const resp = await axios.get<Quote>("http://localhost:8082/api/quote", {
        timeout: 10000,
      });
      setQuote(resp.data);
    } catch (error) {
      console.error("Failed to fetch quote:", error);
      setQuote({
        quote: "The only way to do great work is to love what you do.",
        author: "Steve Jobs",
      });
    } finally {
      setLoading(false);
    }
  };

  const fetchAvatar = async (emailAddress: string) => {
    try {
      const resp = await axios.get<Avatar>(
        `http://localhost:8082/api/avatar/${encodeURIComponent(emailAddress)}`,
        { timeout: 10000 }
      );
      setAvatar(resp.data);
    } catch (error) {
      console.error("Failed to fetch avatar:", error);
      setAvatar({
        avatarUrl: `https://www.gravatar.com/avatar/00000000000000000000000000000000?d=mp&s=200`,
        email: emailAddress,
      });
    }
  };

  const handleEmailChange = (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    fetchAvatar(email);
  };

  return (
    <div className="bg-white rounded-lg shadow-md p-6 space-y-6">
      <div className="flex items-center justify-between">
        <h3 className="text-lg font-semibold text-slate-900">Profile</h3>
        <User className="w-5 h-5 text-slate-400" />
      </div>

      <div className="flex flex-col items-center">
        <img
          src={
            avatar?.avatarUrl ||
            "https://www.gravatar.com/avatar/00000000000000000000000000000000?d=mp&s=200"
          }
          alt="Profile Avatar"
          className="w-24 h-24 rounded-full border-4 border-slate-100 shadow-md"
        />

        <form onSubmit={handleEmailChange} className="w-full mt-4">
          <div className="relative">
            <Mail className="absolute left-3 top-1/2 transform -translate-y-1/2 w-4 h-4 text-slate-400" />
            <input
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              className="w-full pl-10 pr-3 py-2 border border-slate-300 rounded-md text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500"
              placeholder="Enter email"
            />
          </div>
          <button
            type="submit"
            className="w-full mt-2 px-4 py-2 bg-slate-200 text-slate-700 rounded-md hover:bg-slate-300 transition-colors text-sm font-medium"
          >
            Update Avatar
          </button>
        </form>
      </div>

      <div className="border-t border-slate-200 pt-6">
        <div className="flex items-center justify-between mb-4">
          <h4 className="text-sm font-semibold text-slate-700">
            Daily Motivation
          </h4>
          <button
            onClick={fetchQuote}
            disabled={loading}
            className="p-1 text-slate-600 hover:text-slate-900 hover:bg-slate-100 rounded transition-colors disabled:opacity-50"
          >
            <RefreshCw className={`w-4 h-4 ${loading ? "animate-spin" : ""}`} />
          </button>
        </div>

        {quote ? (
          <div className="space-y-2">
            <p className="text-sm text-slate-700 italic leading-relaxed">
              "{quote.quote}"
            </p>
            <p className="text-xs text-slate-500 text-right">
              — {quote.author}
            </p>
          </div>
        ) : (
          <div className="text-center text-slate-400 text-sm py-4">
            Loading quote...
          </div>
        )}
      </div>
    </div>
  );
}
