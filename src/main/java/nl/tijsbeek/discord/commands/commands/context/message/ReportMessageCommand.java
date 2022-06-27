package nl.tijsbeek.discord.commands.commands.context.message;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.components.Modal;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import nl.tijsbeek.discord.commands.InteractionCommandVisibility;
import nl.tijsbeek.discord.commands.MessageContextCommand;
import nl.tijsbeek.discord.commands.abstractions.AbstractInteractionCommand;
import nl.tijsbeek.utils.DiscordClientAction;
import nl.tijsbeek.utils.LocaleHelper;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.List;
import java.util.ResourceBundle;

import static nl.tijsbeek.discord.commands.commands.slash.ReportSlashCommand.handleReportLogChannel;
import static nl.tijsbeek.utils.MentionUtils.mentionUserById;

public class ReportMessageCommand extends AbstractInteractionCommand implements MessageContextCommand {

    private static final String REASON_COMPONENT_ID = "reason";
    private static final String ATTACHMENT_COMPONENT_ID = "attachments";

    public ReportMessageCommand() {
        super(Commands.message("report"), InteractionCommandVisibility.GUILD_ONLY);
    }

    @Override
    public void onMessageContextInteraction(@NotNull MessageContextInteractionEvent event) {
        ResourceBundle resource = LocaleHelper.getBotResource(event.getUserLocale());

        MessageChannel messageChannel = handleReportLogChannel(database, event);

        if (messageChannel == null) {
            return;
        }

        Message targetMessage = event.getTarget();


        String targetGuildId = event.getGuild().getId();
        String targetChannelId = event.getChannel().getId();
        String targetMessageId = targetMessage.getId();
        String reporterId = event.getMember().getId();
        String reporteeId = targetMessage.getAuthor().getId();
        String rawMessageContent = targetMessage.getContentRaw();


        String customId = generateId(targetGuildId, targetChannelId, targetMessageId, reporterId, reporteeId, rawMessageContent);


        Modal modal = Modal.create(customId, "Report")
                .addActionRow(TextInput.create(REASON_COMPONENT_ID, "reason", TextInputStyle.SHORT).build())
                .addActionRow(TextInput.create(ATTACHMENT_COMPONENT_ID, "attachment URL's", TextInputStyle.SHORT).build())
                .build();


        event.replyModal(modal).queue();
    }

    @Override
    public void onModalInteraction(@NotNull final ModalInteractionEvent event) {
        ResourceBundle resource = LocaleHelper.getBotResource(event.getUserLocale());

        MessageChannel messageChannel = handleReportLogChannel(database, event);

        if (messageChannel == null) {
            return;
        }


        List<String> argumentsComponent = getArgumentsComponent(event.getModalId());

        String targetGuildId = argumentsComponent.get(0);
        String targetChannelId = argumentsComponent.get(1);
        String targetMessageId = argumentsComponent.get(2);
        String reporterId = argumentsComponent.get(3);
        String reporteeId = argumentsComponent.get(4);
        String rawMessageContent = argumentsComponent.get(5);


        EmbedBuilder builder = new EmbedBuilder()
                .setColor(Color.RED)
                .setTitle(resource.getString("command.report.title"))
                .setDescription(resource.getString("command.report.message").formatted(
                        mentionUserById(reporteeId), reporteeId,
                        mentionUserById(reporterId), reporterId,
                        event.getValue(ATTACHMENT_COMPONENT_ID).getAsString(),
                        event.getValue(REASON_COMPONENT_ID).getAsString()
                ));

        String messageUrl = "https://discord.com/channels/%s/%s/%s".formatted(targetGuildId, targetChannelId, targetMessageId);

        EmbedBuilder targetMessageEmbed = new EmbedBuilder()
                .setColor(Color.RED)
                .setTitle(resource.getString("command.report.targetmessage.title"), messageUrl)
                .setDescription(rawMessageContent);



        messageChannel.sendMessageEmbeds(builder.build(), targetMessageEmbed.build())
                .setActionRow(List.of(
                        DiscordClientAction.General.USER.asLinkButton(resource.getString("command.report.reporter.profile"), reporterId),
                        DiscordClientAction.General.USER.asLinkButton(resource.getString("command.report.reportee.profile"), reporteeId),
                        DiscordClientAction.Channels.GUILD_CHANNEL_MESSAGE.asLinkButton(resource.getString("command.report.targetmessage.link"),  targetGuildId, targetChannelId, targetMessageId)
                )).queue();

        event.reply(resource.getString("command.report.success")).setEphemeral(true).queue();
    }
}