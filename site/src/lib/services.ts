/**
 * Third-party services. Everything is OFF until its ID is configured at build time, so the site
 * never ships a banner for nothing or a form that posts nowhere.
 *
 *   PUBLIC_GA_ID             Google Analytics measurement id → enables the LGPD consent banner and
 *                            loads GA only after the visitor accepts the analytics category.
 *   PUBLIC_NEWSLETTER_ACTION Form action URL of the newsletter provider → shows the signup card.
 */
export const services = {
  gaMeasurementId: import.meta.env.PUBLIC_GA_ID ?? "",
  newsletterAction: import.meta.env.PUBLIC_NEWSLETTER_ACTION ?? "",
} as const;

export const consentEnabled = services.gaMeasurementId !== "";
export const newsletterEnabled = services.newsletterAction !== "";
