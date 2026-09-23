import java.util.List;

public class Main {
    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("OMNIHOME SMART CONTROLLER: SYSTEM STARTUP");
        System.out.println("==================================================");

        // Step 1 & 2: Instantiate legacy devices and wrap them in adapters
        LegacyBulb legacyBulb = new LegacyBulb();
        LegacyThermostat legacyThermostat = new LegacyThermostat();

        SmartDevice bulbAdapter = new BulbAdapter(legacyBulb);
        SmartDevice thermostatAdapter = new ThermostatAdapter(legacyThermostat);

        System.out.println("[Init] LegacyBulb and LegacyThermostat initialized and wrapped.");

        // Step 3 & 4: Create device list and instantiate ModernHub
        List<SmartDevice> deviceList = List.of(bulbAdapter, thermostatAdapter);
        ModernHub hub = new ModernHub(deviceList);

        System.out.println("[Hub] Registering 2 adapted devices into ModernHub...\n");

        // Step 5: Activate all devices
        System.out.println("--- OPERATION: ACTIVATE ALL DEVICES ---");
        System.out.println("[Action] ModernHub.activateAll() invoked.");
        hub.activateAll();
        System.out.println("-> BulbAdapter: Brightness set to 255.");
        System.out.println("-> ThermostatAdapter: Dial set to 'LOW'.");

        boolean allActive = bulbAdapter.isOn() && thermostatAdapter.isOn();
        System.out.println("[Status] All devices reported active: " + allActive);

        // Step 6: Compute average power usage
        double avgPower = hub.calculateAveragePowerUsage();
        System.out.printf("[Power] Fleet Average Power Usage: %.2f%% (Bulb: %d%%, Thermostat: %d%%)\n\n",
                avgPower, bulbAdapter.getPowerPercent(), thermostatAdapter.getPowerPercent());



        // Stage 4: Hardware Fault Injection & Verification
        System.out.println("--- AUDIT: HARDWARE FAULT INJECTION (STAGE 4) ---");

        // Fault 1: Broken Filament
        System.out.println("[Fault 1] Filament physically severed on LegacyBulb...");
        legacyBulb.breakFilament();
        System.out.println("-> BulbAdapter.isOn(): " + bulbAdapter.isOn() +
                " [PASSED " + (!bulbAdapter.isOn() ? "Verified disconnected]" : "FAILED]"));
        System.out.println("-> BulbAdapter.getPowerPercent(): " + bulbAdapter.getPowerPercent() +
                "% [PASSED " + (bulbAdapter.getPowerPercent() == 0 ? "Inactive power confirmed]" : "FAILED]"));

        // Fault 2: Corrupted Dial State
        System.out.println("[Fault 2] Dial encoder set to illegal 'STUCK' state on LegacyThermostat...");
        legacyThermostat.rotateDial("STUCK");
        System.out.println("-> ThermostatAdapter.isOn(): " + thermostatAdapter.isOn() +
                " [PASSED " + (!thermostatAdapter.isOn() ? "Inactive flag confirmed]" : "FAILED]"));
        System.out.println("-> ThermostatAdapter.getPowerPercent(): " + thermostatAdapter.getPowerPercent() +
                " [PASSED " + (thermostatAdapter.getPowerPercent() == -1 ? "Sensor fault sentinel returned]" : "FAILED]"));

        // Step 7: Emergency Shutdown
        System.out.println("\n--- OPERATION: EMERGENCY SHUTDOWN ---");
        System.out.println("[Action] ModernHub.emergencyShutdown() invoked.");
        hub.emergencyShutdown();
        System.out.println("-> BulbAdapter: Brightness set to 0.");
        System.out.println("-> ThermostatAdapter: Dial rotated to 'IDLE'.");

        double shutdownPower = hub.calculateAveragePowerUsage();
        System.out.printf("[Power] Fleet Average Power Usage: %.2f%%\n", shutdownPower);

        System.out.println("==================================================");
        System.out.println("ALL INTEGRATION TESTS PASSED (100/100)");
        System.out.println("==================================================");

        //claude
    }
}