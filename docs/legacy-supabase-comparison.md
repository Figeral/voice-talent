# Legacy Flutter Prototype vs. Current Next.js Frontend

Comparing `/Users/katika/Downloads/voice-talents` (earlier Flutter Web + Supabase prototype) with `https://condidature-voice-talents.vercel.app/` (current Next.js App Router frontend) reveals what happens **after** candidate registration and what existing Supabase infrastructure/patterns were already designed.

## 1. Architectural Shift

| Aspect | Legacy Flutter App | Current Next.js Frontend |
| :--- | :--- | :--- |
| **Client-to-DB** | Direct Supabase SDK calls (`signInAnonymously`) | Backend API routes (`/api/time`, `/api/registration/*`) |
| **Storage Upload** | Direct `supabase.storage.from('saas_attachments').uploadBinary(...)` | Two-step signed upload: `POST /api/registration/uploads` → `PUT <signedUrl>` |
| **Bot Protection** | None (anonymous auth) | Cloudflare Turnstile + `website` honeypot |
| **Campaign Scoping** | Multi-tenant (`organizations` → `campaigns` → `applications`) | Single-event focused (Voice Talent Douala 2026) |

## 2. Field Evolution (Candidate Form)

| Legacy Flutter (`applications` table) | Current Next.js (`POST /api/registration`) | Notes |
| :--- | :--- | :--- |
| `nom_prenom` | `lastName`, `firstName`, `stageName` | Split + added stage name |
| `age` (int) | `birthDate` (`YYYY-MM-DD`) | Age 18–35 validated against `2026-11-27` |
| `sexe` | `gender` (`"female"` \| `"male"`) | Normalized enum |
| `whatsapp` | `phone` (`+2376XXXXXXXX`) + `email` | Email added; phone normalized |
| `quartier` | `city` + `district` | City separated from neighborhood |
| `telephone_proche` | `contactName` + `contactPhone` | Added contact person's name |
| `statut`, `reseau_actif`, `nombre_abonnes`, `lien_reseau` | `musicGenre`, `songTitle`, `musicBackground`, `socialLinks.{facebook,instagram,tiktok,youtube}` | Replaced generic influencer fields with musical profile |
| `application_files` (`photo`, `video`) | `photoUploadId`, `presentationUploadId` (UUIDs) | Pre-uploaded via signed URL |
| `consent_accepted` + `consent_records` | `acceptRules` (`true`) | Explicit consent for rules + privacy policy |
| `application_status` (`soumis`, ...) | `status` (`received`, `under_review`, `shortlisted`, `selected`, `rejected`) + `dossierNo` (`VT-2026-XXXX`) | Public tracker uses `dossierNo` + `phone` |

## 3. Post-Registration Workflows (From `AdminScreen` & Repository)

The legacy Flutter codebase shows the exact **back-office / post-submission** features expected after candidates register:

1. **Admin / Jury Authentication**: Email + password login (`supabase.auth.signInWithPassword`).
2. **Candidate Management Dashboard**:
   - List all applications with 1-hour signed URLs for portrait photos & audition videos.
   - Filter by `status`, `gender`, and search query; view analytics charts; export to **PDF** and **CSV**.
3. **Review & Status Lifecycle**:
   - Update candidate `status` (`received` → `under_review` → `shortlisted` → `selected` / `rejected`).
   - Record **audit trail** in `application_status_history`.
   - Add **private recruiter/jury comments** (`application_comments`).
   - Add **jury scores & evaluation notes** (`application_reviews`: `score`, `evaluation_notes`, `reviewer_id`).
4. **Notifications & Compliance**:
   - **WhatsApp logs** (`whatsapp_logs`) for candidate outreach/convocations.
   - **GDPR / Cameroon Law 2024/017 consent tracking** (`consent_records` with version & timestamp).
5. **Later Competition Phases** (documented on the Next.js site, not in Flutter):
   - **Public Voting**: 100 FCFA/vote via Mobile Money (MTN MoMo, Orange Money) during Phase 1 & Semi-final.
   - **QR Ticketing**: Single-use named QR tickets retrievable by phone number (opening Dec 5, 2026).
