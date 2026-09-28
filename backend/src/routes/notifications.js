const express = require('express');
const { body, query } = require('express-validator');
const { authenticate, requireApproved } = require('../middleware/auth');
const { validate } = require('../middleware/validate');
const {
  list,
  unreadCount,
  markRead,
  markAllRead,
  remove,
  getSettings,
  updateSettings,
  registerFcmToken,
  unregisterFcmToken,
  sendTestPush,
} = require('../controllers/notificationController');

const router = express.Router();

router.use(authenticate, requireApproved);

router.get('/notifications', list);
router.get('/notifications/unread-count', unreadCount);
router.patch('/notifications/read-all', markAllRead);
router.patch('/notifications/:id/read', markRead);
router.delete('/notifications/:id', remove);

router.get('/notifications/settings', getSettings);
router.put('/notifications/settings', updateSettings);

// Diagnostic: real push to the caller's own devices, returns FCM's verdict + backend->FCM
// latency. No Notification record is created, so the Notification Center stays clean.
router.post('/notifications/test-push', sendTestPush);

router.post(
  '/notifications/fcm-token',
  [body('token').trim().notEmpty().isLength({ min: 20, max: 4096 }).withMessage('token is malformed')
    .matches(/^[A-Za-z0-9_:.\-]+$/).withMessage('token contains invalid characters')],
  validate,
  registerFcmToken
);
router.delete(
  '/notifications/fcm-token',
  [query('token').trim().notEmpty().withMessage('token is required')],
  validate,
  unregisterFcmToken
);

module.exports = router;
