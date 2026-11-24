# Voice Talent Douala 2026 — Frontend Analysis & Backend Requirements

Analysis of the deployed frontend at `https://condidature-voice-talents.vercel.app/` (inspected on 2026-11-22).

## Overview

**Voice Talent – Édition spéciale Douala 2026** is a singing competition in Douala, Cameroon (40 candidates aged 18–35, December 2026 – January 2027, 500,000 FCFA grand prize).

The current frontend is a **Next.js (App Router)** application deployed on Vercel. According to the legal/privacy pages and frontend bundle, the planned/expected stack includes:
- **Hosting**: Vercel (`condidature-voice-talents.vercel.app`)
- **Database & File Storage**: Supabase (evident from privacy/legal pages mentioning Supabase Inc. and the `PUT` signed-URL upload flow using `FormData` with `cacheControl: "3600"` and `x-upsert: "false"`).
- **Bot Protection**: Cloudflare Turnstile (`NEXT_PUBLIC_TURNSTILE_SITE_KEY`) + honeypot field (`website`).
- **Transactional Email**: Routed confirmation emails on registration submission.
- **Payments (Future Phases)**: Mobile Money (MTN MoMo, Orange Money) via a payment aggregator for public voting (100 FCFA/vote) and event ticketing (QR codes).

> **Important Note on Live Site State**: As of 2026-11-22, the `/inscription` page is gated by `RegistrationGate` (`opensAt: 2026-11-25T00:00:00+01:00`). Before Nov 25, the live UI shows *"Rideau encore fermé"*. All 4 registration steps, Zod schemas, and API contracts below were extracted directly from the deployed client JS chunks.

## Documentation Index

- [Pages & Business Flows](docs/pages-and-flows.md) — All 8 routes, competition phases, deadlines, voting/ticketing rules, and legal placeholders.
- [Forms & Validation Rules](docs/forms-and-validation.md) — Complete field-by-field breakdown of `/inscription` (4 steps) and `/suivre-mon-dossier`, including Zod schemas, media constraints, and error messages.
- [Backend API Contract](docs/backend-api-contract.md) — Exact endpoints, request/response payloads, status codes, upload flow, and data model requirements.
- [Legacy Flutter vs. Next.js Comparison](docs/legacy-supabase-comparison.md) — Field mapping, Supabase table/bucket conventions, and post-registration Admin/Jury/WhatsApp workflows from `/Users/katika/Downloads/voice-talents`.

## Spring Boot Backend (Phase 1)

Built with **Spring Boot 4.2.0** (Java 21, Maven Wrapper, Spring WebMVC, Spring Data JPA, Spring Security, Bean Validation, H2/PostgreSQL).

### Run Locally (H2 + Local Storage)

```bash
./mvnw spring-boot:run
```

### Run with Docker + PostgreSQL

```bash
cp .env.example .env
docker compose up --build
```

This starts:
- `postgres` (`postgres:16-alpine`) on port `5432` with persistent `pgdata` volume
- `backend` (Spring Boot) on `http://localhost:8080` connected to PostgreSQL, with persistent `uploads_data` volume (and `REGISTRATION_ENFORCE_WINDOW=false` by default in `.env.example` for local testing)

To point the backend to **Supabase PostgreSQL** instead of the local container, set `DATABASE_URL`, `DATABASE_USERNAME`, and `DATABASE_PASSWORD` in `.env`.

By default, it starts on `http://localhost:8080` with an in-memory H2 database (`MODE=PostgreSQL`), local signed file storage in `./uploads`, and HTTP Basic auth for `/api/admin/**` (`admin` / `changeme123`).

### Run Tests

```bash
./mvnw test
```

### Key Environment Variables (Production)

| Variable | Description | Default |
| :--- | :--- | :--- |
| `DATABASE_URL`, `DATABASE_USERNAME`, `DATABASE_PASSWORD` | PostgreSQL JDBC URL & credentials | H2 in-memory |
| `REGISTRATION_ENFORCE_WINDOW` | Enforce `2026-11-25` → `2026-11-27` window | `true` |
| `STORAGE_MODE` | `local` or `supabase` | `local` |
| `SUPABASE_URL`, `SUPABASE_SERVICE_KEY`, `SUPABASE_BUCKET` | Supabase Storage settings (when `STORAGE_MODE=supabase`) | — |
| `TURNSTILE_ENABLED`, `TURNSTILE_SECRET_KEY` | Cloudflare Turnstile verification | `false` |
| `ADMIN_USERNAME`, `ADMIN_PASSWORD` | Admin API Basic Auth credentials | `admin` / `changeme123` |
| `MAIL_ENABLED`, `MAIL_FROM`, `SPRING_MAIL_*` | Confirmation email SMTP settings | `false` |

## Items Requiring Human Input / Decisions

Before implementing the backend, please clarify:
1. **Backend Stack & Hosting**: Should we build this as Next.js API routes + Supabase, or a standalone backend service (e.g., Node.js/Express/Fastify/NestJS or Python/FastAPI) that the Vercel frontend calls?
2. **Supabase & Turnstile Credentials**: Do you already have a Supabase project (URL, service role key, storage buckets) and Cloudflare Turnstile secret key, plus an email provider (Resend, Postmark, SMTP)?
3. **Scope for Phase 1 vs. Later Phases**: Do you want only the **registration + dossier tracking** APIs right now (`/api/time`, `/api/registration/uploads`, `/api/registration`, `/api/registration/status`), or also the **admin/jury review**, **public voting (Mobile Money)**, and **QR ticketing** modules?
4. **Organization Legal Placeholders**: `/cgu`, `/confidentialite`, and `/mentions-legales` currently contain `[À compléter par l'organisation]` for organization name, address, RCCM/NIU, publication director, and contact email.
