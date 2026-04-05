const NATIVE_HOST = "com.example.brave_video_saver";

function getActiveTab() {
  return chrome.tabs.query({ active: true, currentWindow: true }).then((tabs) => tabs[0]);
}

function sendToNativeHost(payload) {
  return chrome.runtime.sendNativeMessage(NATIVE_HOST, payload);
}

chrome.runtime.onMessage.addListener((message, _sender, sendResponse) => {
  if (message?.type !== "START_DOWNLOAD") {
    return false;
  }

  getActiveTab()
    .then((tab) => {
      if (!tab?.url) {
        throw new Error("Aucune URL détectée dans l'onglet actif.");
      }

      return sendToNativeHost({
        action: "download",
        url: tab.url,
        outputDir: message.outputDir || ""
      });
    })
    .then((nativeResponse) => {
      sendResponse({ ok: true, nativeResponse });
    })
    .catch((error) => {
      sendResponse({ ok: false, error: error.message || String(error) });
    });

  return true;
});
