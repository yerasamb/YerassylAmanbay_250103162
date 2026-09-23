public class BulbAdapter implements SmartDevice{
    private final LegacyBulb legacyBulb;
    private final int k;

    public BulbAdapter(LegacyBulb legacyBulb){
        if(legacyBulb == null){
            throw new IllegalArgumentException("LegacyBulb is not null");
        }
        this.legacyBulb = legacyBulb;
        k = 250103162;
    }
    @Override
    public void turnOn(){
        legacyBulb.setBrightness(255);
    }

    @Override
    public void turnOff(){
        legacyBulb.setBrightness(0);
    }

    @Override
    public boolean isOn(){
        return legacyBulb.hasPower() && legacyBulb.readBrightness() > 0;
    }

    @Override
    public int getPowerPercent(){
        if(legacyBulb.hasPower() == false || legacyBulb.readBrightness() == 0){
            return 0;
        }else{
            int base = (legacyBulb.readBrightness()*100/255);
            int calibrated = base + (k % 10);
            if(calibrated > 100){
                return 100;
            }else{
                return calibrated;
            }
        }
    }
}
