

import java.util.HashMap;
import java.util.Map;
import java.util.Locale;

final class LegacyStockFeed {
    String fetchRawTickerPacket(String ticker) {
        try { Thread.sleep(250); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        if ("AAPL".equalsIgnoreCase(ticker)) return "TICKER=AAPL;BID=182.50;ASK=183.50;STATUS=OPEN;TIMESTAMP=1730000000";
        if ("GOOGL".equalsIgnoreCase(ticker)) return "TICKER=GOOGL;BID=164.20;ASK=164.80;STATUS=OPEN;TIMESTAMP=1730000000";
        return "TICKER=" + ticker + ";BID=0.00;ASK=0.00;STATUS=UNKNOWN;TIMESTAMP=0";
    }
}
interface StockProvider { double getMarketPrice(String ticker); String getStatus(String ticker); }
final class StockFeedAdapter implements StockProvider {
    private final LegacyStockFeed legacyFeed;
    StockFeedAdapter(LegacyStockFeed feed) {
        if (feed == null) throw new IllegalArgumentException("Legacy feed cannot be null");
        legacyFeed = feed;
    }
    private Map<String, String> parse(String raw) {
        Map<String, String> fields = new HashMap<String, String>();
        for (String token : raw.split(";")) {
            String[] pair = token.split("=", 2);
            if (pair.length == 2) fields.put(pair[0].trim(), pair[1].trim());
        }
        return fields;
    }
    public double getMarketPrice(String ticker) {
        Map<String, String> packet = parse(legacyFeed.fetchRawTickerPacket(ticker));
        return (Double.parseDouble(packet.getOrDefault("BID", "0")) + Double.parseDouble(packet.getOrDefault("ASK", "0"))) / 2.0;
    }
    public String getStatus(String ticker) { return parse(legacyFeed.fetchRawTickerPacket(ticker)).getOrDefault("STATUS", "UNAVAILABLE"); }
}
final class CachedStockProxy implements StockProvider {
    private static final class Entry { final double price; final long time; Entry(double p, long t) { price=p; time=t; } }
    private final StockProvider realProvider;
    private final long ttlMillis;
    private final Map<String, Entry> cache = new HashMap<String, Entry>();
    CachedStockProxy(StockProvider provider, long ttlMillis) {
        if (provider == null) throw new IllegalArgumentException("Real provider cannot be null");
        realProvider = provider; this.ttlMillis = ttlMillis;
    }
    public synchronized double getMarketPrice(String ticker) {
        long now = System.currentTimeMillis(); Entry e = cache.get(ticker);
        if (e != null && now - e.time < ttlMillis) {
            System.out.printf(Locale.US, "[CACHE HIT] Returning cached price for %s: $%.2f (Age: %d ms)%n", ticker, e.price, now-e.time);
            return e.price;
        }
        System.out.printf(Locale.US, "[NETWORK FETCH] Cache miss or expired for %s. Calling upstream adapter...%n", ticker);
        double price = realProvider.getMarketPrice(ticker); cache.put(ticker, new Entry(price, now)); return price;
    }
    public String getStatus(String ticker) { return realProvider.getStatus(ticker); }
}

