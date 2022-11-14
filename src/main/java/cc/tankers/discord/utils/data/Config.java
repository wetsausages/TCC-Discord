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
    private boolean debug = false;

    public String getGuildPublic() {
        return guildPublic;
    }

    public void setGuildPublic(String guildPublic) {
        this.guildPublic = guildPublic;
    }

    public String getGuildPrivate() {
        return guildPrivate;
    }

    public void setGuildPrivate(String guildPrivate) {
        this.guildPrivate = guildPrivate;
    }

    public String getModRoles() {
        return modRoles;
    }

    public void setModRoles(String modRoles) {
        this.modRoles = modRoles;
    }

    public String getModChannel() {
        return modChannel;
    }

    public void setModChannel(String modChannel) {
        this.modChannel = modChannel;
    }

    public String getDataPublicChannel() {
        return dataPublicChannel;
    }

    public void setDataPublicChannel(String dataPublicChannel) {
        this.dataPublicChannel = dataPublicChannel;
    }

    public String getDataPrivateChannel() {
        return dataPrivateChannel;
    }

    public void setDataPrivateChannel(String dataPrivateChannel) {
        this.dataPrivateChannel = dataPrivateChannel;
    }

    public String getPollRole() {
        return pollRole;
    }

    public void setPollRole(String pollRole) {
        this.pollRole = pollRole;
    }

    public String getPollChannel() {
        return pollChannel;
    }

    public void setPollChannel(String pollChannel) {
        this.pollChannel = pollChannel;
    }

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }
}

