# Brave Video Saver (prototype)

Oui, c'est possible, **mais pas directement depuis l'extension seule**.

Une extension Brave/Chrome ne peut pas lancer `ffmpeg` toute seule pour des raisons de sécurité. Il faut utiliser **Native Messaging** :

1. l'extension envoie l'URL de l'onglet actif ;
2. un hôte natif local (script Python) reçoit le message ;
3. ce script lance `ffmpeg` sur ta machine.

## Contenu

- `manifest.json`, `popup.*`, `background.js` : extension Brave MV3.
- `host/native_host.py` : processus local qui exécute `ffmpeg`.
- `host/com.example.brave_video_saver.json` : manifeste Native Messaging (à adapter).

## Pré-requis

- Brave
- Python 3
- ffmpeg installé dans le `PATH`

## Installation (Linux/macOS)

### 1) Charger l'extension en mode développeur

- Ouvre `brave://extensions`
- Active **Mode développeur**
- Clique **Charger l'extension non empaquetée**
- Sélectionne le dossier `brave-video-saver-extension`
- Note l'ID de l'extension (ex: `abcdefghijklmnopabcdefghijklmnop`)

### 2) Installer l'hôte natif

Rends le script exécutable :

```bash
chmod +x /workspace/traduction-substack/brave-video-saver-extension/host/native_host.py
```

Crée un manifeste réel à partir du template :

```bash
cp /workspace/traduction-substack/brave-video-saver-extension/host/com.example.brave_video_saver.json \
  ~/.config/BraveSoftware/Brave-Browser/NativeMessagingHosts/com.example.brave_video_saver.json
```

Édite le fichier copié et remplace :

- `path` par le chemin absolu vers `native_host.py`
- `__EXTENSION_ID__` par l'ID réel de ton extension

Exemple de `path` :

`/workspace/traduction-substack/brave-video-saver-extension/host/native_host.py`

### 3) Utiliser

- Ouvre la page qui contient la vidéo / flux
- Clique l'icône de l'extension
- (Optionnel) saisis un dossier de sortie
- Clique **Lancer ffmpeg**

Le script crée un fichier MP4 horodaté (copie flux via `-c copy`) dans `~/Videos` par défaut.

## Limites importantes

- Cette approche prend l'URL de la barre d'adresse, qui **n'est pas toujours** l'URL du flux vidéo direct.
- Pour des plateformes comme YouTube, DRM, HLS protégés, etc., cette méthode peut échouer.
- En pratique, il faut parfois détecter l'URL `m3u8`/`mpd` réelle via APIs réseau ou outil spécialisé.

## Sécurité

- N'installe ce host natif que sur ta machine de confiance.
- Laisse `allowed_origins` limité à ton extension uniquement.
