# aang: Design

## High level sequence

```mermaid
    sequenceDiagram
        User->>Browser: Click "Sign up with Google"
        Browser->>Google: Redirect to Google OAuth consent screen (include client_id, redirect_uri, scope, state)
        Google->>User: Display Google sign-in & consent
        User->>Google: Approve and authenticate
        Google->>Browser: Redirect back to redirect_uri with authorization code and state
        Browser->>Aang: POST /auth/google/callback (authorization code + state)
        Aang->>Google: Exchange code for tokens (authorization_code grant)
        Google-->>Aang: Return access_token + id_token (JWT) + refresh_token 
        Aang->>IdTokenVerifier: Verify id_token (signature, aud, exp, issuer)
        IdTokenVerifier-->>Aang: Valid / Invalid
        alt id_token valid
            Aang->>UserDB: Lookup user by Google sub (or email)
            alt user exists
                UserDB-->>Aang: Return existing user record
                Aang->>Aang: Create session / issue app JWT
                Aang-->>Browser: Set session cookie or return app token
                Browser->>User: Redirect to dashboard (signed in)
                Browser->>Cloudinary: Request profile image URL
                Cloudinary-->>Browser: Serve profile image from CDN
            else user not found
                Aang->>GoogleAPI: Fetch user details (given_name, family_name, email, picture) using access_token
                GoogleAPI-->>Aang: Return user profile data (given_name, family_name, email, picture_url)
                Aang->>Cloudinary: Upload profile picture (POST picture_url or fetch & upload) with transformations
                Cloudinary-->>Aang: Return hosted_image_url (cdn URL)
                Aang->>UserDB: Create new user record (store provider id, email, first_name, last_name, avatar_url = hosted_image_url, refresh_token?)
                UserDB-->>Aang: New user record
                Aang->>Aang: Create session / issue app JWT
                Aang-->>Browser: Set session cookie or return app token
                Browser->>User: Redirect to onboarding/dashboard (signed in)
                Browser->>Cloudinary: Request hosted_image_url
                Cloudinary-->>Browser: Serve profile image from CDN
            end
        else id_token invalid
            Aang-->>Browser: Return error (authentication failed)
            Browser->>User: Show error message
        end
```

## Stack

Design: Monolith

* Because it is easier
* Don't know how the scaling should work
* LLM suggested microservice
* Expecting 10s of users

Monitoring: out of scope

* There will be logs
* monolith does not need monitoring, everyone knows that

Secrets: git-crypt

* I'm familiar with it
* Easy to setup and use compared to something like harshicorp vault
* Not as feature rich as harshicorp vault
* Secrets are left exposed in deployed environment
* Probably puts too much trust in the dev machine

Docker/Podman: Yes

* Can probably use it for testcontainers
* Can use it for postgres
* Can use it for mockserver
* ~Can use it for harshicorp vault~ NO!

Language: Scala with ZIO

* I am more familiar with Scala than Clojure
* I don't want to learn and implement
* I have some code in scala that uses htmx and zio for something like this
* I can use the Java libraries for Google and Cloudinary if needed

Database: postgres

* I like it

## Layout

Uses the actual services in the DAO to encourage full use of the service without trying 
to be generic

```
      .
     ├──  pages -- uses dao and infra to show a page, each folder has a self contained mvc as needed
     │   ├──  landing 
     │   └──  login 
     ├──  dao -- accessing things that are outside of the application
     │   ├──  cloudinary
     │   ├──  google
     │   └──  postgres
     ├──  infra -- building blocks that are needed to build a page. These should be as generic as reasonable
     │   ├──  auditing -- a wrapper on the structured logging library which also handles hiding sensitive and provides ability to log requests and responses
     │   ├──  base -- so that there is a uniform style and metadata on pages
     │   ├──  config
     │   ├──  errors
     │   └──  security
     └──  application.scala - basically kicks off the application
```

## Data flow

```mermaid
flowchart LR
  subgraph Aang["aang"]
    style Aang fill:#f8f9fa,stroke:#333,stroke-width:1px
    subgraph Core["core"]
        Controller[["Controller"]]
        Service[[Service]]
    end
    Infra
    DAO
    DB
  end

  User(["<i class='fas fa-user-circle' style='font-size:36px;color:#0d6efd'></i> <span style='font-weight:600'></span>"])

  User -->|uses| Browser["Browser"]
  Browser --> Controller
  Controller --> Service

  Service --> Infra["Infra"]
  Service --> DAO["DAO"]

  DAO --> DB[(postgres)]
  DAO --> Google["Google"]
  DAO --> Cloudinary["Cloudinary"]

  classDef usercol fill:#e7f1ff,stroke:#0d6efd,color:#042a6b;
  classDef browsercol fill:#cff4fc,stroke:#0a6b74,color:#04464a;
  classDef corecol fill:#fff3cd,stroke:#8a6d00,color:#5a3e00;
  classDef servicecol fill:#e7d6ff,stroke:#5b2fa6,color:#32115a;
  classDef daocol fill:#f8d7da,stroke:#a61d2d,color:#4a0b0f;
  classDef extcol fill:#e2e3e5,stroke:#6c757d,color:#343a40;
  classDef infracol fill:#d1ecf1,stroke:#0b7285,color:#063a46;
  classDef dbcol fill:#fff0d6,stroke:#8b5e00,color:#5a3500;

  class User usercol;
  class Browser browsercol;
  class Controller,Service corecol;
  class Service servicecol;
  class DAO daocol;
  class External extcol;
  class Infra infracol;
  class DB dbcol;
```

## Database

```sql
    CREATE TABLE users (
      id UUID PRIMARY KEY DEFAULT gen_random_uuid(), 
      email CITEXT NOT NULL UNIQUE, -- case insentitive text
      first_name TEXT,
      last_name TEXT,
      avatar_image_url TEXT,
      is_active BOOLEAN NOT NULL DEFAULT TRUE,
      created_at TIMESTAMPTZ NOT NULL DEFAULT now(), -- light auditability
      updated_at TIMESTAMPTZ NOT NULL DEFAULT now()  -- light auditability
    );
```

* No indexes yet, but probably on email if db is slow
* Adding auditing triggers or a table feels like a lot for this thing
