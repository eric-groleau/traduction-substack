const statusElement = document.getElementById("status");
const button = document.getElementById("downloadBtn");
const outputDirField = document.getElementById("outputDir");

function setStatus(text, isError = false) {
  statusElement.textContent = text;
  statusElement.style.color = isError ? "#9b1c1c" : "#0f5132";
}

button.addEventListener("click", () => {
  setStatus("Lancement en cours...");

  chrome.runtime.sendMessage(
    {
      type: "START_DOWNLOAD",
      outputDir: outputDirField.value.trim()
    },
    (response) => {
      if (chrome.runtime.lastError) {
        setStatus(chrome.runtime.lastError.message, true);
        return;
      }

      if (!response?.ok) {
        setStatus(response?.error || "Erreur inconnue", true);
        return;
      }

      const message = response.nativeResponse?.message || "Téléchargement lancé.";
      setStatus(message);
    }
  );
});
