import { SESClient, SendEmailCommand } from "@aws-sdk/client-ses";
import { buildEmail } from "./email-template.mjs";

const ses = new SESClient({ region: process.env.AWS_REGION });
const SENDER = process.env.SES_SENDER;

/**
 * SQS-triggered Lambda. For each message it parses the order-status-changed
 * event and emails the buyer through SES. Uses SQS partial batch responses
 * so only failed messages are retried / sent to the DLQ.
 */
export const handler = async (event) => {
  const records = event.Records ?? [];
  const batchItemFailures = [];

  for (const record of records) {
    try {
      await processRecord(record);
    } catch (err) {
      console.error(`Failed to process message ${record.messageId}:`, err);
      batchItemFailures.push({ itemIdentifier: record.messageId });
    }
  }

  return { batchItemFailures };
};

async function processRecord(record) {
  const payload = JSON.parse(record.body);

  if (!payload.buyerEmail) {
    console.warn(`Order ${payload.orderId} has no buyer email; skipping.`);
    return;
  }

  const { subject, html, text } = buildEmail(payload);

  await ses.send(
    new SendEmailCommand({
      Source: SENDER,
      Destination: { ToAddresses: [payload.buyerEmail] },
      Message: {
        Subject: { Data: subject, Charset: "UTF-8" },
        Body: {
          Html: { Data: html, Charset: "UTF-8" },
          Text: { Data: text, Charset: "UTF-8" },
        },
      },
    })
  );

  console.log(`Email sent to ${payload.buyerEmail} for order ${payload.orderCode}.`);
}
