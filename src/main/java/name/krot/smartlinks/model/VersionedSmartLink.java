package name.krot.smartlinks.model;

/** A definition and its opaque optimistic-concurrency token. */
public record VersionedSmartLink(SmartLink definition, String version) { }
