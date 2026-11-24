# Facade Pattern Lab: Flight Booking

Java implementation of the **Facade design pattern**. The `FlightBookingFacade` class hides five airline booking subsystems behind one method: `bookFlight(...)`.

## Project structure

```
src/
├── SeatInventorySystem.java    # Subsystem 1 (provided, unchanged)
├── BaggagePricingEngine.java   # Subsystem 2 (provided, unchanged)
├── CurrencyConverter.java      # Subsystem 3 (provided, unchanged)
├── PaymentProcessor.java       # Subsystem 4 (provided, unchanged)
├── TicketIssuer.java           # Subsystem 5 (provided, unchanged)
├── FlightBookingFacade.java    # The Facade
└── Main.java                   # Client: uses only FlightBookingFacade
```

## How to run

Requires Java 15 or newer.

```bash
javac -d out src/*.java
java -cp out Main
```

## What the Facade does

`bookFlight(...)` runs the subsystems in this order:

1. Connects to the seat server and locks the seat.
2. Calculates the baggage fee in USD and adds it to the base fare.
3. Converts the total into the passenger's currency.
4. Initializes the payment gateway and charges the card.
5. Issues the e-ticket.

If any step fails, the Facade releases the seat lock and returns `BOOKING_FAILED: <reason>`. The `finally` block always disconnects from the seat server.

### Phase 2: loyalty tiers

- `SILVER` and `GOLD`: no baggage fees. `BaggagePricingEngine` is not called.
- `SILVER`: the promo code `SILVER-FREE-BAG` is applied before the card is charged, which gives a 10% discount.
- The original 8-parameter `bookFlight(...)` still works. It passes the call to the new 9-parameter version with the `"STANDARD"` tier.

## Test scenarios in `Main`

| Test | Case | Expected result |
|------|------|-----------------|
| 1 | Seat 14A, 2 bags (23 kg), EUR | Ticket issued |
| 2 | Seat 13C (crew row) | `BOOKING_FAILED`, program keeps running |
| 3 | Seat 07D, card `9999` | Lock `LOCK-KC101-07D` released, then disconnect |
| 4 | `SILVER`, 3 bags, 30 kg | No `[BaggageEngine]` line, promo applied |
| 5 | `GOLD`, 3 bags, 30 kg | No baggage fee, no promo |
| A/B/C | Section 3 scenarios (KZ204: 14B / 13A / 08C with card `1234`) | Success / failure / rollback |
