import java.util.HashMap;

public class ThermostatAdapter implements SmartDevice{
    private final LegacyThermostat legacyThermostat;

    public ThermostatAdapter(LegacyThermostat legacyThermostat){
        if(legacyThermostat == null){
            throw new IllegalArgumentException("Can not be null");
        }
        this.legacyThermostat = legacyThermostat;
    }

    @Override
    public void turnOn(){
       String newDial =  legacyThermostat.checkDial();
       if("IDLE".equals(newDial)){
           legacyThermostat.rotateDial("LOW");
       }
    }

    @Override
    public void turnOff(){
        legacyThermostat.rotateDial("IDLE");
    }

    @Override
    public boolean isOn(){
        String newDial = legacyThermostat.checkDial();
        if("LOW".equals(newDial) || "MAX".equals(newDial) || "MEDIUM".equals(newDial)){
            return true;
        }else{
            return false;
        }
    }

    @Override
    public int getPowerPercent(){
        String newDial = legacyThermostat.checkDial();
        HashMap<String,Integer> myMap = new HashMap<>();
        if(newDial.equals("IDLE")){
            myMap.put("IDLE",0);
        } else if (newDial.equals("LOW")) {
            myMap.put("LOW",33);
        } else if (newDial.equals("MEDIUM")) {
            myMap.put("MEDIUM",66);
        } else if (newDial.equals("MAX")) {
            myMap.put("MAX",100);
        }else{
            return -1;
        }
        return 0;
    }
}
