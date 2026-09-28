const nodemailer = require('nodemailer');
const { Resend } = require('resend');

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

async function sendMail({ to, subject, text, html }) {
  // Resend talks HTTPS, so it works from hosts whose outbound SMTP ports are blocked
  // (Railway). Nodemailer stays as the fallback for local/dev SMTP.
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
    throw new Error('Neither RESEND_API_KEY nor SMTP_HOST is configured - cannot send email');
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
