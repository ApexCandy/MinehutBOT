package com.minehut.discordbot.commands.music;

import com.arsenarsen.lavaplayerbridge.player.Player;
import com.minehut.discordbot.MinehutBot;
import com.minehut.discordbot.commands.Command;
import com.minehut.discordbot.commands.management.ToggleMusicCommand;
import com.minehut.discordbot.util.Chat;
import com.minehut.discordbot.util.UserClient;
import com.minehut.discordbot.util.exceptions.CommandException;
import net.dv8tion.jda.core.entities.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Made by the developer of SwagBot.
 * Changed by MatrixTunnel on 1/9/2017.
 */
public class SkipCommand extends Command {

    private MinehutBot bot = MinehutBot.get();

    public SkipCommand() {
        super(CommandType.MUSIC, null, "skip");
    }

    public static List<String> votes = new ArrayList<>();
    private static int maxSkips = 0;

    @Override
    public boolean onCommand(UserClient sender, Guild guild, TextChannel channel, Message message, String[] args) throws CommandException {
        Chat.removeMessage(message);

        Member member = guild.getMember(sender.getUser());
        Player player = bot.getMusicManager().getPlayer(guild.getId());
        VoiceChannel voiceChannel = guild.getSelfMember().getVoiceState().getChannel();

        if (!ToggleMusicCommand.canQueue.get(guild.getId()) && !sender.isStaff()) {
            Chat.sendMessage(member.getAsMention() + " Music commands are currently disabled. " +
                    "If you believe this is an error, please contact a staff member", channel, 10);
            return true;
        }

        if (!guild.getAudioManager().isConnected() || bot.getMusicManager().getPlayer(guild.getId()).getPlayingTrack() == null) {
            Chat.sendMessage("The player is not playing!", channel, 15);
            return true;
        }
        if (args.length == 1 && args[0].equals("force") && sender.isStaff()) {
            votes.clear();
            Chat.sendMessage(member.getAsMention() + " Force skipped **" + player.getPlayingTrack().getTrack().getInfo().title + "**", channel, 15);
            player.skip();
            return true;
        }
        if (guild.getSelfMember().getVoiceState().getChannel() == null) {
            Chat.sendMessage(member.getAsMention() + " The bot is not in a voice channel!", channel, 10);
            return true;
        }
        if (!guild.getSelfMember().getVoiceState().getChannel().equals(member.getVoiceState().getChannel())) {
            Chat.sendMessage(member.getAsMention() + " you must be in the channel in order to skip songs!", channel, 10);
            return true;
        }
        if (sender.getUser() == bot.getDiscordClient().getUserById(player.getPlayingTrack().getMeta().get("requester").toString())) {
            votes.clear();
            Chat.sendMessage("Skipped **" + player.getPlayingTrack().getTrack().getInfo().title + "**", channel, 20);
            player.skip();
            return true;
        }
        if (votes.contains(sender.getUser().getId())) {
            Chat.sendMessage(member.getAsMention() + " you have already voted to skip this song!", channel, 10);
            return true;
        }
        votes.add(sender.getUser().getId());

        if (voiceChannel != null && maxSkips != -1) {
            if (voiceChannel.getMembers().size() > 2) {
                maxSkips = (int) ((voiceChannel.getMembers().size() - 1) * 2 / 3.0 + 0.5);
            } else {
                maxSkips = 1;
            }

            if (maxSkips - votes.size() <= 0 || maxSkips == -1) {
                votes.clear();
                Chat.sendMessage("Skipped **" + player.getPlayingTrack().getTrack().getInfo().title + "**", channel, 20);
                player.skip();
            } else {
                Chat.sendMessage(member.getAsMention() + " voted to skip!\n **" +
                        (maxSkips - votes.size()) + "** more votes are required to skip the current song.", channel, 20);
            }
        }

        return true;
    }

}
