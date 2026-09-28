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
            },
            auth: {
                user: jest.fn().mockReturnValue({
                    onCreate: jest.fn((callback) => callback)
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

  test('onUserWrite triggers welcome email properly with name', async () => {
      emailService.sendWelcomeEmail.mockResolvedValue(true);

      const change = {
          after: {
              exists: true,
              data: () => ({ email: 'test@example.com', full_name: 'John Doe', isVerified: true }),
              ref: { set: jest.fn() }
          }
      };
      const context = { params: { userId: '123' } };

      await userLifecycleFunctions.onUserWrite(change, context);

      expect(emailService.sendWelcomeEmail).toHaveBeenCalledWith('test@example.com', 'John Doe');
      expect(change.after.ref.set).toHaveBeenCalled();
  });

  test('onUserWrite skips welcome email if welcomeEmailSentAt exists', async () => {
      emailService.sendWelcomeEmail.mockClear();

      const change = {
          after: {
              exists: true,
              data: () => ({ email: 'test@example.com', isVerified: true, welcomeEmailSentAt: 'sometime' }),
              ref: { set: jest.fn() }
          }
      };
      const context = { params: { userId: '123' } };

      await userLifecycleFunctions.onUserWrite(change, context);

      expect(emailService.sendWelcomeEmail).not.toHaveBeenCalled();
      expect(change.after.ref.set).not.toHaveBeenCalled();
  });
});

