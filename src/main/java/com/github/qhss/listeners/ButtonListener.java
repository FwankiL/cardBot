package com.github.qhss.listeners;

import com.github.qhss.Main;
import com.github.qhss.card.Player;
import com.github.qhss.card.game.Blackjack;
import com.github.qhss.utils.DefaultEmbeds;
import com.github.qhss.utils.ImageUtils;
import com.github.qhss.utils.JsonUtils;

import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.MessageFlag;
import org.javacord.api.event.interaction.ButtonClickEvent;
import org.javacord.api.interaction.ButtonInteraction;
import org.javacord.api.listener.interaction.ButtonClickListener;

import java.util.HashMap;
import java.util.function.Function;

public class ButtonListener implements ButtonClickListener {

    @Override
    public void onButtonClick(ButtonClickEvent event) {
        HashMap<String, Blackjack> games = Main.userGame;
        ButtonInteraction interaction = event.getButtonInteraction();
        String username = interaction.getUser().getDiscriminatedName();
        TextChannel channel = interaction.getChannel().get();

        // default response via lambda (captures environment)
        Function<String, Void> respond =
                msg -> {
                    interaction
                            .createImmediateResponder()
                            .setContent(msg)
                            .setFlags(MessageFlag.EPHEMERAL)
                            .respond();
                    return null;
                };

        // check if player is playing a game and if they're playing in the channel
        if (games.containsKey(username) && Main.channelUser.get(channel).equals(username)) {
            Blackjack bj = games.get(username);
            Player player = bj.getPlayer();

            // depending on which button they click
            switch (interaction.getCustomId()) {
                case "stand" -> {
                    String message;
                    bj.stand();
                    bj.dealerPlay();
                    int bet = bj.getBetAmount();

                    if (bj.getScore(bj.getDealer()) == 21 && bj.getDealerCards().length == 2) {
                        message = "You lost! Dealer got Blackjack!";
                        bet = (int) (-2.5 * bet);
                    } else {
                        message = bj.checkWinner();
                        bet =
                                message.contains("won")
                                        ? bj.getBetAmount()
                                        : message.contains("lost") ? -bj.getBetAmount() : 0;
                    }
                    ImageUtils.appendImages(bj, true, username);
                    

                    player.setMoney(player.getMoney() + bet);
                    JsonUtils.changeMoney(username, player);

                    if (games.containsKey(username)) {
                        interaction.getMessage().delete();
                        new MessageBuilder()
                                .setEmbed(
                                        DefaultEmbeds.finalEmbed(
                                                message, username, bj, Math.abs(bet)))
                                .send(channel);
                        Main.removeFromMaps(username, channel);
                    }
                }
                case "hit" -> {
                    interaction.getMessage().delete();
                    // if hit and value > 21
                    if (!bj.hit()) {
                        ImageUtils.appendImages(bj, true, username);
                        int moneyLost = bj.getBetAmount();
                        String message = "";
                        if (bj.getScore(bj.getDealer()) == 21) {
                            moneyLost *= 2.5;
                            message = " The opponent got Blackjack!";
                        }
                        
                        

                        player.setMoney(player.getMoney() - moneyLost);
                        JsonUtils.changeMoney(username, player);

                        new MessageBuilder()
                                .setEmbed(
                                        DefaultEmbeds.finalEmbed(
                                                "You hit and lost!" + message,
                                                username,
                                                bj,
                                                bj.getBetAmount()))
                                .send(channel);
                        Main.removeFromMaps(username, channel);
                        break;
                    }

                    if (bj.getPlayer().getHand().size() == 5) {
                        ImageUtils.appendImages(bj, true, username);
                        
                        int money = bj.getBetAmount();
                        String message = ", so you won!";
                        
                        if (bj.getScore(bj.getDealer()) == 21) {
                            money = (int) (2.5 * -money);
                            message = ", but the dealer has Blackjack, so you lost!";
                        }
                        

                        player.setMoney(player.getMoney() + money);
                        JsonUtils.changeMoney(username, player);

                        new MessageBuilder()
                                .setEmbed(
                                        DefaultEmbeds.finalEmbed(
                                                "You hit and got 5 in a row" + message,
                                                username,
                                                bj,
                                                bj.getBetAmount()))
                                .send(channel);
                        Main.removeFromMaps(username, channel);
                        break;
                    }

                    if (bj.getScore(bj.getPlayer()) == 21) {
                        ImageUtils.appendImages(bj, true, username);
                        String message = "You hit and won by getting 21!";
                        int money = bj.getBetAmount();

                        if (bj.getScore(bj.getDealer()) == 21) {
                            message = "You hit and got 21, but the dealer has Blackjack, so you lost!";
                            money = (int) -(2.5 * money);
                        }
                        
                        player.setMoney(player.getMoney() + money);
                        JsonUtils.changeMoney(username, player);

                        Main.removeFromMaps(username, channel);
                        new MessageBuilder()
                                .setEmbed(
                                        DefaultEmbeds.finalEmbed(
                                                message,
                                                username,
                                                bj,
                                                bj.getBetAmount()))
                                .send(channel);
                        break;
                    }

                    ImageUtils.appendImages(bj, false, username);
                    DefaultEmbeds.defaultMessage("You hit!", interaction.getUser(), bj)
                            .send(channel);
                }

                /*
                 * Only 2 cards: draw 1 card, stand
                 */
                case "double-down" -> {
                    if (bj.getPlayer().getMoney() < 2 * bj.getBetAmount()) {
                        respond.apply("You can't double down with not enough money!");
                        break;
                    }
                    if (!bj.doubleDown()) {
                        respond.apply("You've already picked up! Not allowed to do that!");
                        break;
                    }
                
                    interaction.getMessage().delete();
                    ImageUtils.appendImages(bj, true, username);
                    
                    if (bj.getScore(bj.getDealer()) == 21 && bj.getDealerCards().length == 2) {
                        player.setMoney(player.getMoney() - 5 * bj.getBetAmount());
                        JsonUtils.changeMoney(username, player);
                        new MessageBuilder()
                                .setEmbed(
                                        DefaultEmbeds.finalEmbed(
                                                "You doubled-down and lost to Blackjack!",
                                                username,
                                                bj,
                                                5 * bj.getBetAmount()))
                                .send(channel);

                        Main.removeFromMaps(username, channel);
                        break;
                    }

                    bj.dealerPlay();
                    ImageUtils.appendImages(bj, true, username);
                    String message = bj.checkWinner();
                    
                    int bet =
                            message.contains("won")
                                    ? bj.getBetAmount()
                                    : message.contains("lost") ? -bj.getBetAmount() : 0;

                    player.setMoney(player.getMoney() + 2 * bet);
                    JsonUtils.changeMoney(username, player);

                    new MessageBuilder()
                            .setEmbed(
                                    DefaultEmbeds.finalEmbed(
                                            "You doubled-down" + " and " + message.toLowerCase(),
                                            username,
                                            bj,
                                            Math.abs(2 * bet)))
                            .send(channel);

                    Main.removeFromMaps(username, channel);
                }
                case "exit" -> {
                    if (games.containsKey(username)) {
                        if (games.get(username).getDealerCards().length == 2
                                && games.get(username).getPlayerCards().length == 2) {
                            interaction.getMessage().delete();
                            respond.apply("Exited game");
                            Main.removeFromMaps(username, channel);
                            break;
                        }
                    }
                    respond.apply("You're either not the player or you already began playing!");
                }
            }
        } else {
            respond.apply("You're not playing this game! Start your own with /cb");
        }
    }
}
