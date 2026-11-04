final class UserSession {
    private final String username, role;
    UserSession(String username, String role) {
        this.username=username; this.role=role;
    }
    String getUsername() { return username; } String getRole() { return role; }
}


interface StorageDriver {
    byte[] loadRawBlob(String resourceUri); String getStorageType();
}

final class S3StorageDriver implements StorageDriver {
    public byte[] loadRawBlob(String uri) { System.out.printf("[AWS S3 CLIENT] Performing HTTPS GET on bucket uri: s3://%s%n",uri); return new byte[]{0x1F,(byte)0x8B,0x08,0x00}; }
    public String getStorageType() { return "AMAZON_S3_CLOUD"; }
}


final class NfsStorageDriver implements StorageDriver {
    public byte[] loadRawBlob(String uri) { System.out.printf("[NFS POSIX CLIENT] Performing direct IO read on /mnt/nfs/%s%n",uri); return new byte[]{0x4F,0x67,0x67,0x53}; }
    public String getStorageType() { return "LOCAL_NFS_CLUSTER"; }
}


final class StorageDriverProxy implements StorageDriver {
    private final StorageDriver realDriver; private final UserSession session;
    StorageDriverProxy(StorageDriver driver, UserSession session) {
        if(driver==null || session==null) throw new IllegalArgumentException("Dependencies cannot be null");
        realDriver=driver; this.session=session;
    }
    public byte[] loadRawBlob(String uri) {
        if("GUEST".equalsIgnoreCase(session.getRole())) {
            System.out.printf("[PROXY BLOCKED] Access Denied for User '%s' [Role: %s] on resource: %s%n",session.getUsername(),session.getRole(),uri);
            return new byte[0];
        }
        System.out.printf("[PROXY AUDIT] Authorized user '%s' [Role: %s] accessing backend: %s%n",session.getUsername(),session.getRole(),realDriver.getStorageType());
        return realDriver.loadRawBlob(uri);
    }
    public String getStorageType() { return "PROXY_GUARDED("+realDriver.getStorageType()+")"; }
}


abstract class DigitalAsset {
    protected final StorageDriver driver; protected final String assetPath;
    DigitalAsset(StorageDriver driver,String path) { this.driver=driver; assetPath=path; }
    public abstract void render();
}


final class VideoAsset extends DigitalAsset {
    VideoAsset(StorageDriver d,String p) { super(d,p); }
    public void render() {
        System.out.printf("Initializing Video Player for: %s%n",assetPath); byte[] data=driver.loadRawBlob(assetPath);
        if(data.length>0) System.out.printf("SUCCESS: Video playback buffered %d bytes via %s%n",data.length,driver.getStorageType());
        else System.out.println("FAILURE: Video buffer empty. Playback aborted.");
    }
}


final class AudioAsset extends DigitalAsset {
    AudioAsset(StorageDriver d,String p) { super(d,p); }
    public void render() {
        System.out.printf("Initializing Audio Stream for: %s%n",assetPath); byte[] data=driver.loadRawBlob(assetPath);
        if(data.length>0) System.out.printf("SUCCESS: Audio bitstream synchronized %d bytes via %s%n",data.length,driver.getStorageType());
        else System.out.println("FAILURE: Audio stream inaccessible.");
    }
}
