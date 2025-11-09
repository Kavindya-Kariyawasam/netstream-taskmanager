// urlService removed per repo cleanup. Use direct API calls from components
// if you need to call the backend URL service (http://localhost:8082).
// This stub prevents accidental imports; replace with direct axios calls.

export const urlService = {
  getQuote: async () => {
    throw new Error(
      "urlService removed. Call the backend directly from your component."
    );
  },
  getAvatar: async () => {
    throw new Error(
      "urlService removed. Call the backend directly from your component."
    );
  },
};
