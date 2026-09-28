const nodemailer = require('nodemailer');

let transporter = null;

function getTransporter() {
  if (!transporter) {
    transporter = nodemailer.createTransport({
      host: process.env.SMTP_HOST,
      port: Number(process.env.SMTP_PORT) || 587,
      secure: process.env.SMTP_SECURE === 'true',
      auth: process.env.SMTP_USER
        ? { user: process.env.SMTP_USER, pass: process.env.SMTP_PASS }
        : undefined,
      connectionOptions: { family: 4 },
      tls: { rejectUnauthorized: false },
      connectionTimeout: 20000,
      greetingTimeout: 20000,
      socketTimeout: 30000,
    });
  }
  return transporter;
}

async function sendMail({ to, subject, text, html }) {
  if (!process.env.SMTP_HOST) {
    throw new Error('SMTP_HOST is not configured - cannot send email');
  }
  try {
    await getTransporter().sendMail({
      from: process.env.SMTP_FROM || process.env.SMTP_USER,
      to,
      subject,
      text,
      html,
    });
  } catch (err) {
    // eslint-disable-next-line no-console
    console.error('SMTP sendMail failed:', {
      code: err.code,
      address: err.address,
      command: err.command,
      response: err.response,
      message: err.message,
    });
    throw err;
  }
}

module.exports = { sendMail };
