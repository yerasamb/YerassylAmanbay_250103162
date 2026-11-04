

import java.util.Locale;

interface DispatchStrategy {
    boolean shouldDispatch(int severityLevel, int currentHour);
    String getStrategyName();
}
final class ImmediateStrategy implements DispatchStrategy {
    public boolean shouldDispatch(int severity, int hour) { return true; }
    public String getStrategyName() { return "IMMEDIATE_DISPATCH"; }
}
final class BusinessHoursStrategy implements DispatchStrategy {
    public boolean shouldDispatch(int severity, int hour) { return severity >= 5 || (hour >= 9 && hour <= 17); }
    public String getStrategyName() { return "BUSINESS_HOURS_ONLY (09:00 - 17:00)"; }
}
interface MessageChannel { void transmit(String recipient, String message); }
final class EmailChannel implements MessageChannel {
    public void transmit(String recipient, String message) { System.out.printf(Locale.US, "[EMAIL PROTOCOL -> %s]\n%s%n", recipient, message); }
}
final class SmsChannel implements MessageChannel {
    public void transmit(String recipient, String message) {
        System.out.printf(Locale.US, "[SMS PROTOCOL -> %s] %s%n", recipient, message.replace("\n", " - "));
    }
}
abstract class Alert {
    protected final MessageChannel channel;
    protected final DispatchStrategy strategy;
    Alert(MessageChannel channel, DispatchStrategy strategy) {
        if (channel == null || strategy == null) throw new IllegalArgumentException("Dependencies cannot be null");
        this.channel=channel; this.strategy=strategy;
    }
    public void processAlert(String recipient, String raw, int hour) {
        int severity=getSeverityLevel();
        if (strategy.shouldDispatch(severity, hour)) channel.transmit(recipient, formatAlertBody(raw));
        else System.out.printf(Locale.US, "[DISPATCH SUPPRESSED] Alert blocked by policy '%s' at hour %02d:00 (Severity: %d)%n", strategy.getStrategyName(), hour, severity);
    }
    public abstract int getSeverityLevel();
    public abstract String formatAlertBody(String raw);
}
final class CriticalSystemAlert extends Alert {
    CriticalSystemAlert(MessageChannel c, DispatchStrategy s) { super(c,s); }
    public int getSeverityLevel() { return 5; }
    public String formatAlertBody(String raw) { return "!!! CRITICAL PRODUCTION ALERT !!!\nIncident Report: " + raw + "\nACTION REQUIRED: SRE on-call immediate intervention."; }
}
final class DailySummaryAlert extends Alert {
    DailySummaryAlert(MessageChannel c, DispatchStrategy s) { super(c,s); }
    public int getSeverityLevel() { return 1; }
    public String formatAlertBody(String raw) { return "Daily Digest:\n" + raw; }
}

