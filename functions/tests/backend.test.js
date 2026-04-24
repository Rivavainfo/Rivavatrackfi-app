const functions = require('firebase-functions');
const admin = require('firebase-admin');

describe('Backend Functions Tests', () => {
  let userLifecycleFunctions;
  let emailService;

  beforeAll(() => {
    jest.spyOn(admin, 'initializeApp').mockImplementation(() => {});

    jest.mock('firebase-functions', () => {
        return {
            config: jest.fn().mockReturnValue({ sendgrid: { key: 'test-key' } }),
            https: { onRequest: jest.fn().mockReturnValue(jest.fn()) },
            firestore: {
                document: jest.fn().mockReturnValue({
                    onWrite: jest.fn((callback) => callback),
                    onCreate: jest.fn().mockReturnValue(jest.fn())
                })
            }
        };
    });

    jest.mock('../services/emailService', () => {
        return {
            sendWelcomeEmail: jest.fn()
        };
    });

    userLifecycleFunctions = require('../userLifecycleFunctions');
    emailService = require('../services/emailService');
  });

  afterAll(() => {
    jest.restoreAllMocks();
  });

  test('onUserWrite triggers welcome email properly', async () => {
      emailService.sendWelcomeEmail.mockResolvedValue(true);

      const change = {
          after: {
              exists: true,
              data: () => ({ email: 'test@example.com', isVerified: true }),
              ref: { update: jest.fn() }
          }
      };
      const context = {};

      await userLifecycleFunctions.onUserWrite(change, context);

      expect(emailService.sendWelcomeEmail).toHaveBeenCalledWith('test@example.com');
      expect(change.after.ref.update).toHaveBeenCalled();
  });

  test('onUserWrite skips welcome email if welcomeEmailSentAt exists', async () => {
      emailService.sendWelcomeEmail.mockClear();

      const change = {
          after: {
              exists: true,
              data: () => ({ email: 'test@example.com', isVerified: true, welcomeEmailSentAt: 'sometime' }),
              ref: { update: jest.fn() }
          }
      };
      const context = {};

      await userLifecycleFunctions.onUserWrite(change, context);

      expect(emailService.sendWelcomeEmail).not.toHaveBeenCalled();
      expect(change.after.ref.update).not.toHaveBeenCalled();
  });
});
