# Pages & Business Flows

All routes discovered on `https://condidature-voice-talents.vercel.app/`:

| Route | Title | Purpose | Interactive / Backend Dependency |
| :--- | :--- | :--- | :--- |
| `/` | Accueil | Landing page, hero, live countdown, competition phases, prize (500,000 FCFA), voting & ticketing overview, timeline, previous finalists (Andy, Grace, Cheera) | Calls `GET /api/time` for `Countdown` sync |
| `/concours` | Le concours | Detailed phases (`#phases`), jury & coach info (`#jury`), vote/scoring table (`#vote`), and 10-point rules (`#reglement`) | Static content |
| `/inscription` | Inscription | Gated 4-step candidate registration wizard | Calls `GET /api/time`, `POST /api/registration/uploads`, `PUT <signedUrl>`, `POST /api/registration` |
| `/suivre-mon-dossier` | Suivre mon dossier | Candidate application status lookup | Calls `POST /api/registration/status` |
| `/contact` | Contact | Contact page (currently shows Douala, Cameroun + link to `/suivre-mon-dossier`; no contact form yet) | Static content |
| `/cgu` | Conditions générales | Terms of use & sale (free registration, paid votes, Mobile Money, QR tickets) | Static content |
| `/confidentialite` | Confidentialité | Privacy policy (Cameroon Law n° 2024/017 of 23 Dec 2024, data retention rules) | Static content |
| `/mentions-legales` | Mentions légales | Legal notices (Vercel hosting, Supabase DB/storage) | Static content |

## Competition Timeline (Douala Time, `UTC+1`)

| Milestone | Date / Window | Details |
| :--- | :--- | :--- |
| Registration Opens | `2026-11-25T00:00:00+01:00` | Free registration, 1 dossier per person (unique phone & email) |
| Registration Closes | `2026-11-27T23:59:59+01:00` | Hard cutoff enforced by `RegistrationGate` and backend |
| Online Casting | `2026-11-28T00:00:00+01:00` | Jury reviews submitted videos and selects **40 candidates** |
| Launch Ceremony & Ticketing Open | `2026-12-05T18:00:00+01:00` | Jury (2 members) & coach revealed; ticket sales open |
| Phase 1: Prestations libres | `2026-12-12` – `2026-12-27` | 4 shows × 10 candidates; **24 qualify** (60% Public / 40% Jury) |
| Phase 2: Battles | `2027-01-03` – `2027-01-09` | 12 duels on imposed songs across 2 shows; **12 qualify** (100% Jury) |
| Phase 3: Demi-finale | `2027-01-17` | 12 candidates on imposed song; **6 finalists** (60% Public / 40% Jury) |
| Phase 4: Grande finale | `2027-01-30` | 6 finalists; **1 winner** (100% Jury), 500,000 FCFA + partner prizes |

## Future / Documented Platform Features (Not Yet Live on Frontend)

1. **Public Voting** (Phase 1 & Demi-finale):
   - Unit price: **100 FCFA** per vote via Mobile Money (**MTN MoMo**, **Orange Money**).
   - Vote counted only after payment confirmation by the Mobile Money aggregator; timestamped and tied to a confirmed transaction.
   - Tie-breaker: Jury score first, then number of public votes.
   - Publicly displayed candidate info: only portrait photo, stage name, and presentation of the 40 selected candidates.
2. **Ticketing** (Opens `2026-12-05`):
   - Named digital tickets with **single-use QR codes** (invalidated on first scan), retrievable by phone number.
   - Tiers:
     - Première phase · Standard: 1,000 FCFA
     - Battles · Standard: 1,000 FCFA
     - Demi-finale · Standard: 2,000 FCFA | VIP: 5,000 FCFA
     - Grande finale · Standard: 3,000 FCFA | VIP: 5,000 FCFA | VIP Premium: 10,000 FCFA
3. **Data Retention Policy** (`/confidentialite`):
   - Rejected/non-selected dossiers: deleted **6 months** after competition ends.
   - Selected dossiers: retained during competition + **1 year**.
   - Vote & ticket transactions: retained for statutory accounting period.
