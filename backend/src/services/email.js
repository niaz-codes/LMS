const nodemailer = require('nodemailer');
const { Resend } = require('resend');

const BREVO_SEND_URL = 'https://api.brevo.com/v3/smtp/email';
const SEND_TIMEOUT_MS = 15000;

let transporter = null;
let resendClient = null;

function getResend() {
  if (!resendClient) {
    resendClient = new Resend(process.env.RESEND_API_KEY);
  }
  return resendClient;
}

function getTransporter() {
  if (!transporter) {
    transporter = nodemailer.createTransport({
      host: process.env.SMTP_HOST,
      port: Number(process.env.SMTP_PORT) || 587,
      secure: process.env.SMTP_SECURE === 'true',
      auth: process.env.SMTP_USER
        ? { user: process.env.SMTP_USER, pass: process.env.SMTP_PASS }
        : undefined,
      connectionTimeout: 15000,
      greetingTimeout: 15000,
      socketTimeout: 25000,
    });
  }
  return transporter;
}

/** Brevo over its REST API.
 *
 *  The account is on Brevo's free plan with no sending domain added, and the only credential that
 *  works is the xkeysib-* API key - which authenticates against the REST API only. Brevo's SMTP
 *  relay needs a separate xsmtpsib-* key, so SMTP is not an option for this account and talking
 *  HTTPS also sidesteps hosts that block outbound SMTP (as Railway did, until this path existed).
 *
 *  Uses global fetch (Node >= 18, per package.json engines) so this adds no dependency. */
async function sendViaBrevo({ to, subject, text, html }) {
  if (!process.env.BREVO_FROM) {
    throw new Error('BREVO_API_KEY is set but BREVO_FROM (a verified sender address) is not');
  }
  const response = await fetch(BREVO_SEND_URL, {
    method: 'POST',
    headers: {
      'api-key': process.env.BREVO_API_KEY,
      accept: 'application/json',
      'content-type': 'application/json',
    },
    body: JSON.stringify({
      sender: { name: process.env.BREVO_FROM_NAME || 'UOS', email: process.env.BREVO_FROM },
      to: [{ email: to }],
      subject,
      htmlContent: html,
      textContent: text,
    }),
    // Without this a hung connection would hold the request open past the client's own timeout.
    signal: AbortSignal.timeout(SEND_TIMEOUT_MS),
  });
  const body = await response.json().catch(() => ({}));
  if (!response.ok) {
    // Brevo's useful detail (unverified sender, bad key, blocked recipient) is in `message`;
    // surface it instead of a bare status code so the log says what actually went wrong.
    const detail = body && body.message ? body.message : `HTTP ${response.status}`;
    throw new Error(`Brevo send failed: ${detail}`);
  }
  return { id: body.messageId };
}

async function sendMail({ to, subject, text, html }) {
  // Brevo first: it's the provider this deployment is configured for. Resend and Nodemailer remain
  // as fallbacks for other environments (Resend is unusable here - its onboarding sender can only
  // mail the account owner - and SMTP needs a dedicated relay key).
  if (process.env.BREVO_API_KEY) {
    return sendViaBrevo({ to, subject, text, html });
  }

  if (process.env.RESEND_API_KEY) {
    const { data, error } = await getResend().emails.send({
      from: process.env.MAIL_FROM || 'onboarding@resend.dev',
      to,
      subject,
      text,
      html,
    });
    if (error) {
      throw new Error(`Resend send failed: ${error.message}`);
    }
    return { id: data.id };
  }

  if (!process.env.SMTP_HOST) {
    throw new Error('No email provider configured - set BREVO_API_KEY, RESEND_API_KEY or SMTP_HOST');
  }
  await getTransporter().sendMail({
    from: process.env.SMTP_FROM || process.env.SMTP_USER,
    to,
    subject,
    text,
    html,
  });
  return {};
}

module.exports = { sendMail };
