package name.krot.smartlinks.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Url {
    private String id;
    private String longUrl;
    private long creationTime;
    private long lastAccessTime;
    private int accessCount;
}