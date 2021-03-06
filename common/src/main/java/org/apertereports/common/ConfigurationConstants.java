package org.apertereports.common;

/**
 * Contains configuration keys used in the application.
 */
public interface ConfigurationConstants {
    /**
     * Corresponds to a JNDI name used to get the mail session's user property.
     */
    String MAIL_SESSION_USER = "mail.session.user";

    /**
     * The key of a message provider base resource bundle name.
     */
    String MESSAGE_PROVIDER_RESOURCE = "message.provider.resource";

    /**
     * The key of an email message title. This should be used along with a MessageProvider instance to get
     * the relevant email title from a resource bundle.
     */
    String MESSAGE_PROVIDER_BGREPORT_EMAIL_TITLE = "message.provider.bgreport.email.title";

    /**
     * The key of an email message template. This should be used along with a MessageProvider instance to get
     * the relevant email message template from a resource bundle.
     */
    String MESSAGE_PROVIDER_BGREPORT_EMAIL_MESSAGE = "message.provider.bgreport.email.msg";

    /**
     * Corresponds to a JNDI name used to get the mail session from an initial context.
     */
    String JNDI_MAIL_SESSION = "jndi.mail.session";

    /**
     * The key to retrieve the delay between sending each mail. This is useful when a SMTP server prevents from sending
     * too many emails too quickly.
     */
    String MAIL_SEND_DELAY = "mail.send.delay";

    /**
     * The key used to set Jasper's character encoding.
     */
    String JASPER_REPORTS_CHARACTER_ENCODING = "jasper_reports.character_encoding";

    /**
     * Defines a key corresponding to configuration cache timeout.
     */
    String CONFIGURATION_CACHE_TIMEOUT_IN_MINUTES = "configuration.cache.timeout.in_minutes";
}
