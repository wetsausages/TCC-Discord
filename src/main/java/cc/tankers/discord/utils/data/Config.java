package cc.tankers.discord.utils.data;

public class Config {
    private String guildPublic = "";

    private String guildPrivate = "";

    private String modRoles = "";

    private String modChannel = "";

    private String dataPublicChannel = "";

    private String dataPrivateChannel = "";

    private String pollRole = "";

    private String pollChannel = "";

    private String approvalChannel = "";

    private String playerDataChannel = "";

    private String dropDataChannel = "";

    private String playerDataEmbed = "";

    private String lootChannel = "";

    private String eventDataChannel = "";

    private String eventDataEmbed = "";

    private String eventLBChannel = "";

    private String pcBoss = "none";

    private String kotsSkill = "none";

    private boolean debug = false;

    public String getGuildPublic() {
        return this.guildPublic;
    }

    public void setGuildPublic(String guildPublic) {
        this.guildPublic = guildPublic;
    }

    public String getGuildPrivate() {
        return this.guildPrivate;
    }

    public void setGuildPrivate(String guildPrivate) {
        this.guildPrivate = guildPrivate;
    }

    public String getModRoles() {
        return this.modRoles;
    }

    public void setModRoles(String modRoles) {
        this.modRoles = modRoles;
    }

    public String getModChannel() {
        return this.modChannel;
    }

    public void setModChannel(String modChannel) {
        this.modChannel = modChannel;
    }

    public String getDataPublicChannel() {
        return this.dataPublicChannel;
    }

    public void setDataPublicChannel(String dataPublicChannel) {
        this.dataPublicChannel = dataPublicChannel;
    }

    public String getDataPrivateChannel() {
        return this.dataPrivateChannel;
    }

    public void setDataPrivateChannel(String dataPrivateChannel) {
        this.dataPrivateChannel = dataPrivateChannel;
    }

    public String getPollRole() {
        return this.pollRole;
    }

    public void setPollRole(String pollRole) {
        this.pollRole = pollRole;
    }

    public String getPollChannel() {
        return this.pollChannel;
    }

    public void setPollChannel(String pollChannel) {
        this.pollChannel = pollChannel;
    }

    public String getApprovalChannel() {
        return this.approvalChannel;
    }

    public void setApprovalChannel(String approvalChannel) {
        this.approvalChannel = approvalChannel;
    }

    public String getPlayerDataChannel() {
        return this.playerDataChannel;
    }

    public void setPlayerDataChannel(String playerDataChannel) {
        this.playerDataChannel = playerDataChannel;
    }

    public String getDropDataChannel() {
        return this.dropDataChannel;
    }

    public void setDropDataChannel(String dropDataChannel) {
        this.dropDataChannel = dropDataChannel;
    }

    public String getPlayerDataEmbed() {
        return this.playerDataEmbed;
    }

    public void setPlayerDataEmbed(String playerDataEmbed) {
        this.playerDataEmbed = playerDataEmbed;
    }

    public String getLootChannel() {
        return this.lootChannel;
    }

    public void setLootChannel(String lootChannel) {
        this.lootChannel = lootChannel;
    }

    public String getEventDataChannel() {
        return this.eventDataChannel;
    }

    public void setEventDataChannel(String eventDataChannel) {
        this.eventDataChannel = eventDataChannel;
    }

    public String getEventDataEmbed() {
        return this.eventDataEmbed;
    }

    public void setEventDataEmbed(String eventDataEmbed) {
        this.eventDataEmbed = eventDataEmbed;
    }

    public String getEventLBChannel() {
        return this.eventLBChannel;
    }

    public void setEventLBChannel(String eventLBChannel) {
        this.eventLBChannel = eventLBChannel;
    }

    public String getPCBoss() {
        return this.pcBoss;
    }

    public void setPCBoss(String boss) {
        this.pcBoss = boss;
    }

    public String getKOTSSkill() {
        return this.kotsSkill;
    }

    public void setKOTSSkill(String skill) {
        this.kotsSkill = skill;
    }

    public boolean isDebug() {
        return this.debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }
}
