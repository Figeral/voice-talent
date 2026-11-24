# Forms & Validation Rules

## 1. Registration Form (`/inscription`)

### Gate Behavior (`RegistrationGate`)
- Syncs client clock with `GET /api/time`.
- `now < 2026-11-25T00:00:00+01:00` → shows `"upcoming"` (*Rideau encore fermé*) + countdown.
- `now >= 2026-11-27T23:59:59+01:00 + 1000ms` → shows `"closed"` (*Inscriptions closes*).
- Otherwise → renders the 4-step registration form.
- Draft state is persisted in `localStorage` under key `vt-registration-draft-v2` (`{ draft, step }`) until successful submission.
- Includes a hidden honeypot input `name="website"` (must be empty).

---

### Step 1: Identité

| Field | Label | Type | Required | Validation / Constraints |
| :--- | :--- | :--- | :--- | :--- |
| `lastName` | Nom | `text` | Yes | Trimmed, min 1, max 80 chars |
| `firstName` | Prénom | `text` | Yes | Trimmed, min 1, max 80 chars |
| `stageName` | Nom de scène | `text` | No | Trimmed, max 80 chars, empty string → `undefined` |
| `birthDate` | Date de naissance | `date` | Yes | ISO date (`YYYY-MM-DD`); UI `min="1990-11-28"`, `max="2008-11-27"` (18–35 years old at closing date) |
| `gender` | Sexe | `radio` | Yes | Enum: `"female"` (`Femme`) \| `"male"` (`Homme`) |

---

### Step 2: Coordonnées

| Field | Label | Type | Required | Validation / Constraints |
| :--- | :--- | :--- | :--- | :--- |
| `phone` | Téléphone (WhatsApp de préférence) | `tel` | Yes | Stripped of `[\s.\-()]`, must match `/^(?:\+237\|00237\|237)?(6\d{8})$/`, normalized to `+2376XXXXXXXX` |
| `email` | E-mail | `email` | Yes | Trimmed, lowercased, valid email, max 160 chars |
| `city` | Ville | `text` | Yes | Trimmed, min 1, max 80 chars |
| `district` | Quartier | `text` | Yes | Trimmed, min 1, max 80 chars |
| `contactName` | Personne à contacter | `text` | Yes | Trimmed, min 1, max 80 chars (emergency/backup contact) |
| `contactPhone` | Téléphone de la personne à contacter | `tel` | Yes | Same Cameroon mobile regex, normalized to `+2376XXXXXXXX` |
| `photoUploadId` | Photo de portrait | `file` (`capture="user"`) | Yes | UUID returned by `POST /api/registration/uploads`. Allowed MIME: `image/jpeg`, `image/png`, `image/webp`. Max size: **5 MB** (`5242880` bytes). Client auto-resizes/compresses to WebP (max 1600px, quality 0.82) if smaller. |

---

### Step 3: Profil musical

| Field | Label | Type | Required | Validation / Constraints |
| :--- | :--- | :--- | :--- | :--- |
| `musicGenre` | Genre musical | `text` + `datalist` | Yes | Trimmed, min 1, max 60 chars. Suggested options: `Afrobeat`, `Makossa`, `Bikutsi`, `Gospel`, `R&B`, `Pop`, `Soul`, `Jazz`, `Rap / Hip-hop`, `Zouk`, `Variété` |
| `songTitle` | Chanson envisagée pour la prestation | `text` | Yes | Trimmed, min 1, max 150 chars |
| `musicBackground` | Votre parcours musical | `textarea` | Yes | Trimmed, min 1, max 1000 chars |
| `presentationUploadId` | Votre prestation (casting en ligne) | `file` | Yes | UUID returned by `POST /api/registration/uploads`. Allowed MIME: `video/mp4`, `video/quicktime`, `video/webm`, `video/3gpp`. Max size: **40 MB** (`41943040` bytes). Duration: **15–30 seconds** (client allows ±2s tolerance: `13s`–`32s`). |
| `socialLinks.facebook` | Facebook | `url` | No | Trimmed, max 200 chars, must match `/^https:\/\/\S+$/` if provided |
| `socialLinks.instagram` | Instagram | `url` | No | Same |
| `socialLinks.tiktok` | TikTok | `url` | No | Same |
| `socialLinks.youtube` | Youtube | `url` | No | Same |

---

### Step 4: Validation

| Field | Label | Type | Required | Validation / Constraints |
| :--- | :--- | :--- | :--- | :--- |
| `acceptRules` | Acceptation règlement & confidentialité | `checkbox` | Yes | Literal `true` |
| `turnstileToken` | Cloudflare Turnstile | widget | Optional if `NEXT_PUBLIC_TURNSTILE_SITE_KEY` unset | Sent in final `POST /api/registration` body |

---

## 2. Application Status Tracker (`/suivre-mon-dossier`)

| Field | Label | Type | Required | Placeholder / Format |
| :--- | :--- | :--- | :--- | :--- |
| `dossierNo` | Numéro de dossier | `text` | Yes | Uppercase, e.g. `VT-2026-0001` |
| `phone` | Téléphone utilisé à l'inscription | `tel` | Yes | e.g. `6 71 23 45 67` |
