package com.github.wujichen158.ikakuji.command.individual;

import com.envyful.api.command.annotate.Command;
import com.envyful.api.command.annotate.executor.Argument;
import com.envyful.api.command.annotate.executor.CommandProcessor;
import com.envyful.api.command.annotate.executor.Completable;
import com.envyful.api.command.annotate.executor.Sender;
import com.envyful.api.command.annotate.permission.Permissible;
import com.envyful.api.config.type.ExtendedConfigItem;
import com.envyful.api.forge.command.completion.player.PlayerTabCompleter;
import com.envyful.api.forge.config.UtilConfigItem;
import com.github.wujichen158.ikakuji.IkaKuji;
import com.github.wujichen158.ikakuji.command.completion.CrateDeliverCompleter;
import com.github.wujichen158.ikakuji.command.completion.CrateNameCompleter;
import com.github.wujichen158.ikakuji.config.IkaKujiLocaleCfg;
import com.github.wujichen158.ikakuji.kuji.EnumCrateType;
import com.github.wujichen158.ikakuji.lib.PermissionNodes;
import com.github.wujichen158.ikakuji.util.CrateFactory;
import com.github.wujichen158.ikakuji.util.MsgUtil;
import com.google.common.collect.Lists;
import net.minecraft.command.ICommandSource;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;

import java.util.List;
import java.util.Optional;

@Command(
        value = "give"
)
@Permissible(PermissionNodes.GIVE_NODE)
public class GiveCmd {

    @CommandProcessor
    public void run(@Sender ICommandSource sender,
                    @Completable(CrateDeliverCompleter.class) @Argument String type,
                    @Completable(PlayerTabCompleter.class) @Argument ServerPlayerEntity targetPlayer,
                    @Completable(CrateNameCompleter.class) @Argument String crateName) {
        Optional.ofNullable(CrateFactory.get(crateName)).ifPresent(crate -> {
            List<ItemStack> itemStacks = null;
            IkaKujiLocaleCfg.Commands commands = IkaKuji.getInstance().getLocale().getCommands();
            if (CrateDeliverCompleter.KEY.equalsIgnoreCase(type)) {
                ExtendedConfigItem key = crate.getKey();
                if (Optional.ofNullable(key).isPresent()) {
                    itemStacks = Lists.newArrayList();
                    itemStacks.add(UtilConfigItem.fromConfigItem(key));
                    sender.sendMessage(MsgUtil.prefixedColorMsg(commands.getGiveKey(), crateName, targetPlayer.getName().getString()), targetPlayer.getUUID());
                } else {
                    sender.sendMessage(MsgUtil.prefixedColorMsg(commands.getNoKey(), crateName), targetPlayer.getUUID());
                }
            } else if (CrateDeliverCompleter.CRATE.equalsIgnoreCase(type)) {
                if (crate.getCrateType() == EnumCrateType.item && CrateFactory.getAll().containsKey(crateName)) {
                    List<ItemStack> itemStacks1 = CrateFactory.getItemsFromName(crateName);
                    if (Optional.ofNullable(itemStacks1).isPresent()) {
                        itemStacks = itemStacks1;
                        sender.sendMessage(MsgUtil.prefixedColorMsg(commands.getGiveCrate(), crateName, targetPlayer.getName().getString()), targetPlayer.getUUID());
                    } else {
                        sender.sendMessage(MsgUtil.prefixedColorMsg(commands.getInvalidItemCrate(), crateName), targetPlayer.getUUID());
                    }
                } else {
                    sender.sendMessage(MsgUtil.prefixedColorMsg(commands.getNotItemCrate(), crateName), targetPlayer.getUUID());
                }
            }

            // Give
            Optional.ofNullable(itemStacks).ifPresent(itemStacksNonNull -> itemStacksNonNull.forEach(targetPlayer::addItem));
        });
    }
}
