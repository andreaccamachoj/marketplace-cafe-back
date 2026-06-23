const STATUS_LABELS = {
  pending: "Pendiente de pago",
  paid: "Pago confirmado",
  confirmed: "Confirmado",
  preparing: "En preparación",
  shipped: "Enviado",
  delivered: "Entregado",
  cancelled: "Cancelado",
};

const STATUS_MESSAGES = {
  confirmed: "Tu pedido fue confirmado y pronto comenzaremos a prepararlo.",
  preparing: "Estamos preparando tu pedido con mucho cuidado.",
  shipped: "¡Tu pedido va en camino!",
  delivered: "Tu pedido fue entregado. ¡Esperamos que disfrutes tu café!",
  cancelled: "Tu pedido fue cancelado. Si tienes alguna duda, contáctanos.",
};

function label(status) {
  return STATUS_LABELS[status] ?? status;
}

function formatCurrency(amount) {
  if (amount === null || amount === undefined) return "";
  return new Intl.NumberFormat("es-CO", {
    style: "currency",
    currency: "COP",
    maximumFractionDigits: 0,
  }).format(Number(amount));
}

/**
 * Builds the email (subject, HTML and plain-text bodies) for an
 * order-status-changed event.
 */
export function buildEmail(e) {
  const newLabel = label(e.newStatus);
  const subject = `Pedido ${e.orderCode ?? ""} — ${newLabel}`;
  const message =
    STATUS_MESSAGES[e.newStatus] ?? `El estado de tu pedido cambió a "${newLabel}".`;
  const total = formatCurrency(e.totalAmount);
  const note = e.note ? String(e.note) : "";

  const html = `<!DOCTYPE html>
<html lang="es">
  <body style="margin:0;padding:0;background:#f5f0e8;font-family:Arial,Helvetica,sans-serif;color:#3b2f2a;">
    <table role="presentation" width="100%" cellpadding="0" cellspacing="0" style="background:#f5f0e8;padding:24px 0;">
      <tr>
        <td align="center">
          <table role="presentation" width="600" cellpadding="0" cellspacing="0" style="background:#ffffff;border-radius:12px;overflow:hidden;max-width:600px;width:100%;">
            <tr>
              <td style="background:#6f4e37;padding:24px 32px;color:#ffffff;font-size:20px;font-weight:bold;">
                ☕ World Coffee Marketplace
              </td>
            </tr>
            <tr>
              <td style="padding:32px;">
                <h1 style="margin:0 0 8px;font-size:22px;color:#6f4e37;">${newLabel}</h1>
                <p style="margin:0 0 20px;font-size:15px;line-height:1.5;">${message}</p>
                <table role="presentation" width="100%" cellpadding="0" cellspacing="0" style="border:1px solid #e7ddd0;border-radius:8px;">
                  <tr>
                    <td style="padding:12px 16px;font-size:14px;color:#7a6a5f;">Pedido</td>
                    <td style="padding:12px 16px;font-size:14px;text-align:right;font-weight:bold;">${e.orderCode ?? ""}</td>
                  </tr>
                  <tr>
                    <td style="padding:12px 16px;font-size:14px;color:#7a6a5f;border-top:1px solid #f0e9df;">Estado anterior</td>
                    <td style="padding:12px 16px;font-size:14px;text-align:right;border-top:1px solid #f0e9df;">${label(e.previousStatus)}</td>
                  </tr>
                  <tr>
                    <td style="padding:12px 16px;font-size:14px;color:#7a6a5f;border-top:1px solid #f0e9df;">Estado actual</td>
                    <td style="padding:12px 16px;font-size:14px;text-align:right;font-weight:bold;color:#6f4e37;border-top:1px solid #f0e9df;">${newLabel}</td>
                  </tr>
                  ${total ? `<tr>
                    <td style="padding:12px 16px;font-size:14px;color:#7a6a5f;border-top:1px solid #f0e9df;">Total</td>
                    <td style="padding:12px 16px;font-size:14px;text-align:right;border-top:1px solid #f0e9df;">${total}</td>
                  </tr>` : ""}
                </table>
                ${note ? `<p style="margin:20px 0 0;font-size:14px;color:#7a6a5f;"><strong>Nota:</strong> ${note}</p>` : ""}
              </td>
            </tr>
            <tr>
              <td style="padding:20px 32px;background:#faf6f0;font-size:12px;color:#9b8b7e;">
                Este es un mensaje automático de World Coffee Marketplace. Por favor no respondas a este correo.
              </td>
            </tr>
          </table>
        </td>
      </tr>
    </table>
  </body>
</html>`;

  const text =
    `World Coffee Marketplace\n\n` +
    `${newLabel}\n${message}\n\n` +
    `Pedido: ${e.orderCode ?? ""}\n` +
    `Estado anterior: ${label(e.previousStatus)}\n` +
    `Estado actual: ${newLabel}\n` +
    (total ? `Total: ${total}\n` : "") +
    (note ? `Nota: ${note}\n` : "") +
    `\nEste es un mensaje automático. Por favor no respondas a este correo.`;

  return { subject, html, text };
}
