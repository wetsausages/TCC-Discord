package cc.tankers.discord;

import cc.tankers.discord.utils.listeners.BotEvent;
import cc.tankers.discord.utils.listeners.ButtonEvent;
import cc.tankers.discord.utils.listeners.CommandEvent;
import cc.tankers.discord.utils.listeners.ModalEvent;
import io.github.cdimascio.dotenv.Dotenv;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

import javax.security.auth.login.LoginException;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Bot {

    private final Dotenv config;
    private final ShardManager shardManager;

    public Bot() throws LoginException {
        config = Dotenv.configure().load();

        DefaultShardManagerBuilder smb = DefaultShardManagerBuilder.createDefault(config.get("TOKEN"));
        smb.setActivity(Activity.playing("with myself"));
        smb.enableIntents(
                GatewayIntent.GUILD_MEMBERS,
                GatewayIntent.GUILD_MESSAGES,
                GatewayIntent.GUILD_MESSAGE_REACTIONS,
                GatewayIntent.GUILD_EMOJIS_AND_STICKERS,
                GatewayIntent.GUILD_BANS,
                GatewayIntent.GUILD_VOICE_STATES);
        smb.setMemberCachePolicy(MemberCachePolicy.ALL);
        smb.setChunkingFilter(ChunkingFilter.ALL);
        smb.enableCache(CacheFlag.ROLE_TAGS);
        shardManager = smb.build();

        shardManager.addEventListener(new BotEvent(), new CommandEvent(), new ButtonEvent(), new ModalEvent()); //, new MessageEvent()
    }

    public Dotenv getConfig() {
        return config;
    }

    public ShardManager getShardManager() {
        return shardManager;
    }

    public static void main(String[] args) throws IOException {
        //Validate .env
        if (!new File(".env").exists()) {
            File env = new File(".env");
            env.createNewFile();
            FileWriter writer = new FileWriter(env);
            writer.write("TOKEN=");
            writer.close();
            System.out.println("[X] .env was not found so it was created. Please add the token and restart the bot.");
            System.exit(0);
        }

        try {
            Bot bot = new Bot();
        } catch (LoginException e) {
            System.out.println("[X] Invalid token.");
        }
    }
}
