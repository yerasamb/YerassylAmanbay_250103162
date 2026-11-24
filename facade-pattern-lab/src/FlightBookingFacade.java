/**
 * Facade over the five airline booking subsystems.
 *
 * Client code talks only to this class. All initialization order,
 * business rules (loyalty tiers), rollback and resource cleanup
 * are hidden here.
 */
public class FlightBookingFacade {

    // Loyalty tiers supported by the airline
    private static final String TIER_STANDARD = "STANDARD";
    private static final String TIER_SILVER = "SILVER";
    private static final String TIER_GOLD = "GOLD";

    private static final String SILVER_PROMO_CODE = "SILVER-FREE-BAG";

    // Encapsulated subsystem references
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

    /**
     * Phase 1 signature, kept for backward compatibility.
     * Delegates to the 9-parameter version with the default "STANDARD" tier.
     */
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

    /**
     * Single entry point for booking a complete flight itinerary.
     * Coordinates seat locking, baggage pricing, currency conversion,
     * payment execution, rollback on failure, and ticket issuance.
     *
     * Loyalty rules (Phase 2):
     *  - SILVER and GOLD: no baggage fees, BaggagePricingEngine is skipped.
     *  - SILVER: promo code "SILVER-FREE-BAG" is applied before charging the card.
     */
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
            // 1. Connect to seat server and lock the requested seat.
            seatSystem.connectToReservationServer();
            lockToken = seatSystem.lockSeat(flightCode, seatNumber, passengerName);

            // 2. Compute baggage fee in USD (skipped for SILVER / GOLD) and add to base fare.
            double baggageFeeUSD = 0.0;
            if (isBaggageExempt(tier)) {
                System.out.println("[Facade] " + tier + " tier: baggage fees waived.");
            } else {
                baggageFeeUSD = baggageEngine.calculateBaggageFeeUSD(bagCount, bagWeightKg);
            }
            double totalUSD = baseFareUSD + baggageFeeUSD;

            // 3. Convert total USD amount into target currency.
            double convertedTotal = currencyConverter.convertFromUSD(totalUSD, currency);

            // 4. Initialize payment gateway, apply SILVER promo if needed, and charge the card.
            paymentProcessor.initializeGateway();
            if (TIER_SILVER.equals(tier)) {
                paymentProcessor.applyPromoCode(SILVER_PROMO_CODE);
            }
            String receiptId = paymentProcessor.chargeCard(cardNumber, convertedTotal, currency);

            // 5. Generate and return the final e-ticket code.
            return ticketIssuer.generateTicket(passengerName, flightCode, lockToken, receiptId);

        } catch (RuntimeException ex) {
            // 6. If a seat was already locked, release it before reporting failure.
            if (lockToken != null) {
                seatSystem.releaseSeatLock(lockToken);
            }
            System.out.println("[Facade] Booking failed for " + passengerName + ": " + ex.getMessage());
            return "BOOKING_FAILED: " + ex.getMessage();

        } finally {
            // 7. Ensure seat server disconnects regardless of success or failure.
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
