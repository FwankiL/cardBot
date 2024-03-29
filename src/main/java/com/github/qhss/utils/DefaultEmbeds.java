package com.github.qhss.utils;

import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.component.ActionRow;
import org.javacord.api.entity.message.component.Button;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.message.mention.AllowedMentions;
import org.javacord.api.entity.message.mention.AllowedMentionsBuilder;
import org.javacord.api.entity.user.User;

import com.github.qhss.card.game.Blackjack;

import java.awt.Color;
import java.io.File;

public interface DefaultEmbeds {
    static MessageBuilder defaultMessage(String message, User user, Blackjack bj) {
        EmbedBuilder embed =
                new EmbedBuilder()
                        .setTitle("Blackjack")
                        .setDescription(message)
                        .addField("Dealer's Cards: ", "VALUE")
                        .addField("Your Cards: ", String.valueOf(bj.getScore(bj.getPlayer())))
                        .setColor(Color.CYAN)
                        .setFooter("Your bet: $" + bj.getBetAmount())
                        .setImage(
                                new File(
                                        "src/main/resources/assets/combined"
                                                + user.getDiscriminatedName()
                                                + ".png"))
                        .setThumbnail(
                                new File("src/main/resources/assets/profile.png"));

        AllowedMentions allowedMentions =
                new AllowedMentionsBuilder()
                        .addUser(user.getId())
                        .setMentionRoles(true)
                        .setMentionEveryoneAndHere(false)
                        .build();

        return new MessageBuilder()
                .setAllowedMentions(allowedMentions)
                .append(user.getMentionTag())
                .setEmbed(embed)
                .addComponents(
                        ActionRow.of(
                                Button.success("hit", "Hit", "👏", false),
                                Button.primary("stand", "Stand", "🧍", false),
                                Button.danger("double-down", "Double Down", "🤑", false),
                                Button.secondary("exit", "Exit", "🚪", false)));
    }

    static EmbedBuilder finalEmbed(String message, String username, Blackjack bj, int change) {
        String finalMsg =
                message.contains("won")
                        ? "You won: "
                        : message.contains("lost") ? "You lost: " : "You tied.";
        return new EmbedBuilder()
                .setTitle("Blackjack")
                .setDescription(message)
                .addField("Dealer's Cards: ", String.valueOf(bj.getScore(bj.getDealer())))
                .addField("Your Cards: ", String.valueOf(bj.getScore(bj.getPlayer())))
                .addInlineField(finalMsg, String.valueOf(change))
                .setColor(Color.CYAN)
                .setFooter("Your bet: $" + bj.getBetAmount())
                .setImage(new File("src/main/resources/assets/combined" + username + ".png"))
                .setThumbnail(new File("src/main/resources/assets/profile.png"));
    }
}
