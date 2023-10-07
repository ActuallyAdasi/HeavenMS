package constants;

public class ServerConstants {
    // Not present in YamlConfig, but also not being used
    public static final long PET_LOOT_UPON_ATTACK = (long)(0.7 * 1000); //Time the pet must wait before trying to pick items up.

    // Present in YamlConfig, but still being used
    public static final int CHANNEL_LOCKS = 20;
    public static final boolean USE_DEBUG = false;
    public static final boolean USE_FAMILY_SYSTEM = true;
    public static final boolean USE_ULTRA_RECOVERY = false;     //Massive recovery amounts overtime.
}
