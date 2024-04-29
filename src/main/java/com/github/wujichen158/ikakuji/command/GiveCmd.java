package com.github.wujichen158.ikakuji.command;

import com.envyful.api.command.annotate.Command;
import com.envyful.api.command.annotate.executor.Argument;
import com.envyful.api.command.annotate.executor.CommandProcessor;
import com.envyful.api.command.annotate.executor.Completable;
import com.envyful.api.command.annotate.executor.Sender;
import com.envyful.api.command.annotate.permission.Permissible;
import com.envyful.api.forge.command.completion.player.PlayerTabCompleter;
import com.envyful.api.forge.config.UtilConfigItem;
import com.github.wujichen158.ikakuji.command.completion.CrateDeliverCompleter;
import com.github.wujichen158.ikakuji.command.completion.CrateNameCompleter;
import com.github.wujichen158.ikakuji.kuji.EnumCrateType;
import com.github.wujichen158.ikakuji.lib.PermissionNodes;
import com.github.wujichen158.ikakuji.util.CrateFactory;
import com.google.common.collect.Lists;
import net.minecraft.command.ICommandSource;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.Map;
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
            List<ItemStack> itemStacks = Lists.newArrayList();
            if (CrateDeliverCompleter.KEY.equals(type)) {
                itemStacks.add(UtilConfigItem.fromConfigItem(crate.getKey()));
            } else if (CrateDeliverCompleter.CRATE.equals(type)) {
                if (crate.getCrateType().equals(EnumCrateType.item) && CrateFactory.getAll().containsKey(crateName)) {
                    for (Map<String, String> typeDatum : crate.getTypeData()) {
                        String itemName = typeDatum.get(type);
                        if (Optional.ofNullable(itemName).isPresent()) {
                            itemStacks.add(new ItemStack(ForgeRegistries.ITEMS.getValue(ResourceLocation.tryParse(itemName))));
                        }
                    }
                }
            }
            itemStacks.forEach(targetPlayer::addItem);
        });
    }
}
