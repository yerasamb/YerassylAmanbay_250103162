public class Main {
    public static void main(String[] args) {
        // Main knows NOTHING about the 5 underlying subsystems.
        FlightBookingFacade bookingFacade = new FlightBookingFacade();

        System.out.println("=== TEST 1: Standard Successful Booking ===");
        String ticket1 = bookingFacade.bookFlight(
                "Amina Suleimenova",
                "KC101",
                "14A",
                320.00,
                2,
                23.0,
                "EUR",
                "4532015112830366"
        );
        System.out.println("Result 1: " + ticket1 + "\n");

        System.out.println("=== TEST 2: Blocked Crew Seat (Row 13) ===");
        String ticket2 = bookingFacade.bookFlight(
                "Daniyar Kassymov",
                "KC101",
                "13C",
                320.00,
                1,
                15.0,
                "USD",
                "4532015112830366"
        );
        System.out.println("Result 2: " + ticket2 + "\n");

        System.out.println("=== TEST 3: Payment Failure & Automatic Seat Rollback ===");
        String ticket3 = bookingFacade.bookFlight(
                "Timur Bekov",
                "KC101",
                "07D",
                320.00,
                1,
                18.0,
                "KZT",
                "9999" // Invalid short card number triggers rollback
        );
        System.out.println("Result 3: " + ticket3 + "\n");

        System.out.println("=== TEST 4: SILVER Loyalty Tier (no baggage fee + promo) ===");
        String ticket4 = bookingFacade.bookFlight(
                "Aruzhan Nurlanova",
                "KC101",
                "21F",
                320.00,
                3,
                30.0,
                "EUR",
                "4532015112830366",
                "SILVER"
        );
        System.out.println("Result 4: " + ticket4 + "\n");

        System.out.println("=== TEST 5: GOLD Loyalty Tier (no baggage fee, no promo) ===");
        String ticket5 = bookingFacade.bookFlight(
                "Yerlan Abenov",
                "KC101",
                "02A",
                320.00,
                3,
                30.0,
                "USD",
                "4532015112830366",
                "GOLD"
        );
        System.out.println("Result 5: " + ticket5 + "\n");

        // Scenarios A, B, C from Section 3 of the lab task
        System.out.println("=== SCENARIO A: Valid Booking (KZ204, 14B, 2 bags 24.5 kg, EUR) ===");
        String scenarioA = bookingFacade.bookFlight(
                "Aigerim Sadykova",
                "KZ204",
                "14B",
                280.00,
                2,
                24.5,
                "EUR",
                "4532015112830366"
        );
        System.out.println("Result A: " + scenarioA + "\n");

        System.out.println("=== SCENARIO B: Blocked Seat 13A ===");
        String scenarioB = bookingFacade.bookFlight(
                "Bauyrzhan Ospanov",
                "KZ204",
                "13A",
                280.00,
                1,
                10.0,
                "USD",
                "4532015112830366"
        );
        System.out.println("Result B: " + scenarioB + "\n");

        System.out.println("=== SCENARIO C: Rollback on Invalid Card (08C, card 1234) ===");
        String scenarioC = bookingFacade.bookFlight(
                "Madina Tulegenova",
                "KZ204",
                "08C",
                280.00,
                1,
                12.0,
                "USD",
                "1234"
        );
        System.out.println("Result C: " + scenarioC + "\n");
    }
}
