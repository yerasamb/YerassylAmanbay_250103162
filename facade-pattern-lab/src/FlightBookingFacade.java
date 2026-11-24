public class FlightBookingFacade {

    private static final String TIER_STANDARD = "STANDARD";
    private static final String TIER_SILVER = "SILVER";
    private static final String TIER_GOLD = "GOLD";

    private static final String SILVER_PROMO_CODE = "SILVER-FREE-BAG";

    private final SeatInventorySystem seatSystem;
    private final BaggagePricingEngine baggageEngine;
    private final CurrencyConverter currencyConverter;
    private final PaymentProcessor paymentProcessor;
    private final TicketIssuer ticketIssuer;

    public FlightBookingFacade() {
        this.seatSystem = new SeatInventorySystem();
        this.baggageEngine = new BaggagePricingEngine();
        this.currencyConverter = new CurrencyConverter();
        this.paymentProcessor = new PaymentProcessor();
        this.ticketIssuer = new TicketIssuer();
    }

    public String bookFlight(String passengerName,
                             String flightCode,
                             String seatNumber,
                             double baseFareUSD,
                             int bagCount,
                             double bagWeightKg,
                             String currency,
                             String cardNumber) {
        return bookFlight(passengerName, flightCode, seatNumber, baseFareUSD,
                          bagCount, bagWeightKg, currency, cardNumber, TIER_STANDARD);
    }

    public String bookFlight(String passengerName,
                             String flightCode,
                             String seatNumber,
                             double baseFareUSD,
                             int bagCount,
                             double bagWeightKg,
                             String currency,
                             String cardNumber,
                             String loyaltyTier) {
        String tier = normalizeTier(loyaltyTier);
        String lockToken = null;

        try {
            seatSystem.connectToReservationServer();
            lockToken = seatSystem.lockSeat(flightCode, seatNumber, passengerName);

            double baggageFeeUSD = 0.0;
            if (isBaggageExempt(tier)) {
                System.out.println("[Facade] " + tier + " tier: baggage fees waived.");
            } else {
                baggageFeeUSD = baggageEngine.calculateBaggageFeeUSD(bagCount, bagWeightKg);
            }
            double totalUSD = baseFareUSD + baggageFeeUSD;

            double convertedTotal = currencyConverter.convertFromUSD(totalUSD, currency);

            paymentProcessor.initializeGateway();
            if (TIER_SILVER.equals(tier)) {
                paymentProcessor.applyPromoCode(SILVER_PROMO_CODE);
            }
            String receiptId = paymentProcessor.chargeCard(cardNumber, convertedTotal, currency);

            return ticketIssuer.generateTicket(passengerName, flightCode, lockToken, receiptId);

        } catch (RuntimeException ex) {
            if (lockToken != null) {
                seatSystem.releaseSeatLock(lockToken);
            }
            System.out.println("[Facade] Booking failed for " + passengerName + ": " + ex.getMessage());
            return "BOOKING_FAILED: " + ex.getMessage();

        } finally {
            seatSystem.disconnect();
        }
    }

    private static String normalizeTier(String loyaltyTier) {
        if (loyaltyTier == null || loyaltyTier.trim().isEmpty()) {
            return TIER_STANDARD;
        }
        return loyaltyTier.trim().toUpperCase();
    }

    private static boolean isBaggageExempt(String tier) {
        return TIER_SILVER.equals(tier) || TIER_GOLD.equals(tier);
    }
}
