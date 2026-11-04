

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;

final class LegacyForeignOrder {
    private final String orderId; private final double gbpAmount;
    LegacyForeignOrder(String id, double amount) { orderId=id; gbpAmount=amount; }
    String getOrderId() { return orderId; } double getGbpAmount() { return gbpAmount; }
}
final class LegacyCurrencyConverter {
    double convert(double amount, String from, String to) {
        if ("GBP".equalsIgnoreCase(from) && "USD".equalsIgnoreCase(to)) return amount * 1.25;
        return amount;
    }
}
interface PricingService { double calculateTotal(); String getReceiptBreakdown(); }
final class CurrencyOrderAdapter implements PricingService {
    private final LegacyForeignOrder order; private final LegacyCurrencyConverter converter; private final String currency;
    CurrencyOrderAdapter(LegacyForeignOrder order, LegacyCurrencyConverter converter, String currency) {
        this.order=order; this.converter=converter; this.currency=currency;
    }
    public double calculateTotal() { return converter.convert(order.getGbpAmount(), "GBP", currency); }
    public String getReceiptBreakdown() { return String.format(Locale.US, "Order [%s] Base Item: $%.2f %s (Converted from %.2f GBP)", order.getOrderId(), calculateTotal(), currency, order.getGbpAmount()); }
}
abstract class PricingDecorator implements PricingService {
    protected final PricingService wrappedService;
    PricingDecorator(PricingService service) { if(service==null) throw new IllegalArgumentException("Wrapped service cannot be null"); wrappedService=service; }
    public double calculateTotal() { return wrappedService.calculateTotal(); }
    public String getReceiptBreakdown() { return wrappedService.getReceiptBreakdown(); }
}
final class GiftWrapDecorator extends PricingDecorator {
    GiftWrapDecorator(PricingService s) { super(s); }
    public double calculateTotal() { return super.calculateTotal()+5.00; }
    public String getReceiptBreakdown() { return super.getReceiptBreakdown()+" + [Gift Wrap: $5.00]"; }
}
final class ExpressCourierDecorator extends PricingDecorator {
    ExpressCourierDecorator(PricingService s) { super(s); }
    public double calculateTotal() { return super.calculateTotal()+15.00; }
    public String getReceiptBreakdown() { return super.getReceiptBreakdown()+" + [Express Courier: $15.00]"; }
}
final class CustomsDutyDecorator extends PricingDecorator {
    CustomsDutyDecorator(PricingService s) { super(s); }
    public double calculateTotal() { return BigDecimal.valueOf(super.calculateTotal()).multiply(new BigDecimal("1.085")).setScale(2, RoundingMode.HALF_UP).doubleValue(); }
    public String getReceiptBreakdown() { return super.getReceiptBreakdown()+" + [Customs Duty: 8.5%]"; }
}
