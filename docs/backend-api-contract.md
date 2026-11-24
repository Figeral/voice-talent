# Backend API Contract

These are the exact endpoints called by the frontend (`https://condidature-voice-talents.vercel.app/`).

---

## 1. `GET /api/time`

Used by `Countdown` and `RegistrationGate` to sync client clocks with the server.

- **Cache**: `cache: "no-store"`
- **Response `200 OK`**:
  ```json
  {
    "now": 1795339800000
  }
  ```
  - `now`: current server Unix timestamp in milliseconds.

---

## 2. `POST /api/registration/uploads`

Prepares a direct-to-storage upload (signed URL) for portrait photos and audition videos.

- **Request Body (`application/json`)**:
  ```json
  {
    "kind": "photo" | "presentation",
    "mime": "image/webp",
    "size": 123456,
    "durationSeconds": 22
  }
  ```
  - `durationSeconds` is `undefined` for `photo`, and an integer (rounded seconds) for `presentation` when browser metadata is readable.
- **Response `200 OK`**:
  ```json
  {
    "uploadId": "550e8400-e29b-41d4-a716-446655440000",
    "signedUrl": "https://<supabase-project>.supabase.co/storage/v1/upload/sign/..."
  }
  ```
- **Error Response (`4xx`/`5xx`)**:
  ```json
  {
    "error": "Human-readable French error message"
  }
  ```
- **Subsequent Client Upload**:
  The frontend immediately sends a `PUT` request to `signedUrl` with:
  - Header: `x-upsert: false`
  - Body: `FormData` containing `cacheControl: "3600"` and `""` (empty-key file entry) — standard Supabase Storage `createSignedUploadUrl` format.

---

## 3. `POST /api/registration`

Submits the complete candidate registration.

- **Request Body (`application/json`)**:
  ```json
  {
    "lastName": "Mbarga",
    "firstName": "Amina",
    "stageName": "Amina V",
    "birthDate": "2001-05-14",
    "gender": "female",
    "phone": "6 71 23 45 67",
    "email": "amina@example.com",
    "city": "Douala",
    "district": "Bonapriso",
    "contactName": "Paul Mbarga",
    "contactPhone": "6 99 00 11 22",
    "photoUploadId": "550e8400-e29b-41d4-a716-446655440000",
    "musicGenre": "Afrobeat",
    "songTitle": "Ma chanson",
    "musicBackground": "5 ans de chorale...",
    "socialLinks": {
      "facebook": "https://facebook.com/...",
      "instagram": "https://instagram.com/...",
      "tiktok": "https://tiktok.com/...",
      "youtube": "https://youtube.com/..."
    },
    "presentationUploadId": "660e8400-e29b-41d4-a716-446655440001",
    "acceptRules": true,
    "website": "",
    "turnstileToken": "..."
  }
  ```
- **Server-Side Checks Required**:
  1. Registration window open (`2026-11-25T00:00:00+01:00` ≤ now ≤ `2026-11-27T23:59:59+01:00`).
  2. Honeypot `website` must be empty.
  3. Verify `turnstileToken` with Cloudflare Turnstile if configured.
  4. Validate all fields with the same Zod rules (including age 18–35 at closing date `2026-11-27`, Cameroon mobile normalization `+2376XXXXXXXX`).
  5. Enforce uniqueness: 1 dossier per person (unique normalized `phone` and lowercased `email`).
  6. Verify `photoUploadId` and `presentationUploadId` exist in `uploads` table / storage and are not already claimed.
  7. Generate unique `dossierNo` (format `VT-2026-XXXX`), persist candidate with initial status `"received"`, and send confirmation email.
- **Response `200 OK` / `201 Created`**:
  ```json
  {
    "dossierNo": "VT-2026-0001"
  }
  ```
- **Error Response (`400`/`409`/`422`/`500`)**:
  ```json
  {
    "error": "Message d'erreur global",
    "fieldErrors": {
      "email": "Cette adresse e-mail est déjà utilisée.",
      "socialLinks.instagram": "Lien invalide (doit commencer par https://)"
    }
  }
  ```
  - Note: If `fieldErrors` keys match a step's fields, the frontend automatically navigates the user back to that step and highlights the field.

---

## 4. `POST /api/registration/status`

Looks up a candidate's dossier status.

- **Request Body (`application/json`)**:
  ```json
  {
    "dossierNo": "VT-2026-0001",
    "phone": "6 71 23 45 67"
  }
  ```
  - Backend should normalize `dossierNo` (trim/uppercase) and `phone` (`+2376XXXXXXXX`) before querying.
- **Response `200 OK`**:
  ```json
  {
    "dossierNo": "VT-2026-0001",
    "status": "received",
    "submittedAt": "2026-11-25T14:32:00.000Z",
    "firstName": "Amina"
  }
  ```
  - Allowed `status` values:
    - `"received"` — Reçu
    - `"under_review"` — En cours d'examen
    - `"shortlisted"` — Présélectionné pour le casting
    - `"selected"` — Retenu (parmi les 40)
    - `"rejected"` — Refusé
- **Error Response (`400`/`404`)**:
  ```json
  {
    "error": "Aucun dossier trouvé avec ces informations."
  }
  ```
