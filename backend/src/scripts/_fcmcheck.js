// TEMP diagnostic: find the most recently-updated user (i.e. the account that was signed in
// on the device we just launched) so we know which FCM token belongs to the test device.
const mongoose = require('mongoose');
require('dotenv').config();

(async () => {
  await mongoose.connect(process.env.MONGODB_URI, { dbName: process.env.MONGODB_DB_NAME });
  const User = require('../models/User');
  const users = await User.find({ fcmTokens: { $exists: true, $ne: [] } })
    .select('email role fcmTokens updatedAt')
    .sort({ updatedAt: -1 })
    .limit(4)
    .lean();
  for (const u of users) {
    console.log(`- ${u.email} role=${u.role} updatedAt=${u.updatedAt}`);
    u.fcmTokens.forEach((t, i) => console.log(`    [${i}] ${t}`));
  }
  await mongoose.disconnect();
})().catch((e) => {
  console.error('ERR', e.message);
  process.exit(1);
});
