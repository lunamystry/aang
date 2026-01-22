package me.mandla.aang
package pages
package signin

import scalatags.Text
import scalatags.Text.all.*

object SignInPage:
  val clientId: String =
    System.getenv("GOOGLE_CLIENT_ID")

  def content() =
    div(
      script(
        src := "https://accounts.google.com/gsi/client",
        async := true,
      ),
      div(
        id := "g_id_onload",
        attr("data-client_id") := clientId,
        attr("data-callback") := "handleCredentialResponse",
        attr("data-auto_prompt") := "false",
      ),
      div(id := "google-signin-button"),
      img(id := "profile-picture"),
      script {
        raw {
          s"""
          // initialize (auto_prompt disabled so One Tap won't show)
          window.onload = () => {
            google.accounts.id.initialize({
              client_id: '$clientId',
              callback: handleCredentialResponse,
              auto_select: false    // don't auto-select an account
            });

            // render a button into the container
            google.accounts.id.renderButton(
              document.getElementById('google-signin-button'),
              {
                theme: 'outline',        // 'outline' | 'filled_blue' | 'filled_black'
                size: 'large',           // 'large' | 'medium' | 'small'
                width: '300',            // optional pixel width
                type: 'standard',        // 'standard' | 'icon'
                text: 'signin_with',     // 'signin_with' | 'signup_with' | 'continue_with'
                shape: 'rectangular'     // 'rectangular' | 'pill'
              }
            );

            // Optional: cancel One Tap if it might appear
            google.accounts.id.disableAutoSelect();
          };
          """
        }
      },
      script {
        raw {
          """
          async function handleCredentialResponse(response) {
            // response.credential is a JWT (ID token)
            const idToken = response?.credential;
            if (!idToken) {
              console.error('No credential received');
              return { success: false, error: 'no_credential' };
            }

            // 1) Decode JWT (no verification) to read claims client-side if needed
            function decodeJwt(token) {
              try {
                const parts = token.split('.');
                const payload = parts[1].replace(/-/g, '+').replace(/_/g, '/');
                const json = decodeURIComponent(
                  atob(payload)
                    .split('')
                    .map(c => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
                    .join('')
                );
                return JSON.parse(json);
              } catch (e) {
                return null;
              }
            }
            const claims = decodeJwt(idToken);
            console.info(claims);
            if (!claims) {
              console.error('Failed to decode ID token');
              return { success: false, error: 'decode_failed' };
            }

            // Optional quick client-side check (issuer, audience, expiry)
            const now = Math.floor(Date.now() / 1000);
            if (claims.iss !== 'https://accounts.google.com' && claims.iss !== 'accounts.google.com') {
              console.warn('Unexpected token issuer', claims.iss);
            }
            if (claims.exp && claims.exp < now) {
              console.error('ID token expired');
              return { success: false, error: 'token_expired' };
            }

            // 2) (Optional) Validate token against Google's tokeninfo endpoint
            // Note: This is not a substitute for server-side verification.
            try {
              const tokeninfoRes = await fetch(`https://oauth2.googleapis.com/tokeninfo?id_token=${encodeURIComponent(idToken)}`);
              if (!tokeninfoRes.ok) {
                console.warn('tokeninfo check failed', await tokeninfoRes.text());
              } else {
                const tokeninfo = await tokeninfoRes.json();
                // tokeninfo.aud should match your client ID
                // tokeninfo.email_verified === "true"
                // You can perform additional client-side checks here.
              }
            } catch (e) {
              console.warn('tokeninfo request error', e);
            }

            // 3) Send ID token to your backend for verification and sign-in/up
            try {
              const backendRes = await fetch('signin/google/callback', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                credentials: 'include', // if using cookies
                body: JSON.stringify({ id_token: idToken })
              });

              if (!backendRes.ok) {
                const text = await backendRes.text();
                console.error('Backend rejected token:', text);
                return { success: false, error: 'backend_reject', details: text };
              }
              const data = await backendRes.json(); // { picture, name, email }
              // display
              const img = document.getElementById('profile-picture');
              img.src = data.profile_picture;
              img.alt = data.name || data.email || 'Google profile';
              // Expect backend to verify token with Google's libs and create a session
              return { success: true, user: data };
            } catch (e) {
              console.error('Network error sending ID token to backend', e);
              return { success: false, error: 'network_error', details: e.message };
            }
          }
        """
        }
      },
    )
