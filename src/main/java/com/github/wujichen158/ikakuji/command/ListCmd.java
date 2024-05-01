package com.github.wujichen158.ikakuji.command;

import com.envyful.api.command.annotate.Command;
import com.envyful.api.command.annotate.executor.CommandProcessor;
import com.envyful.api.command.annotate.executor.Sender;
import com.envyful.api.command.annotate.permission.Permissible;
import com.github.wujichen158.ikakuji.IkaKuji;
import com.github.wujichen158.ikakuji.config.IkaKujiLocaleCfg;
import com.github.wujichen158.ikakuji.lib.PermissionNodes;
import com.github.wujichen158.ikakuji.util.MsgUtil;
import com.github.wujichen158.ikakuji.util.PlayerKujiFactory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.StringTextComponent;

import java.util.List;
import java.util.Optional;

@Command(
        value = "list"
)
@Permissible(PermissionNodes.LIST_NODE)
public class ListCmd {

    @CommandProcessor
    public void run(@Sender ServerPlayerEntity player, Integer arg) {
        Optional.ofNullable(PlayerKujiFactory.get(player.getUUID())).ifPresent(playerData -> {
            int onePageSize = 10;
            int page = Optional.ofNullable(arg).map(arg1 -> arg1 - 1).orElse(0);
            List<String> pagedCrateList = playerData.getAvailableCrates().subList(page, page + onePageSize);
            IkaKujiLocaleCfg.Messages messages = IkaKuji.getInstance().getLocale().getMessages();

            // style
            player.sendMessage(MsgUtil.prefixedColorMsg(messages.getListTitle()), player.getUUID());
            pagedCrateList.forEach(crateName ->
                    player.sendMessage(MsgUtil.prefixedColorMsg(messages.getListElemPrefix(), crateName), player.getUUID()));
            for (int i = 0; i < onePageSize - pagedCrateList.size(); i++) {
                player.sendMessage(new StringTextComponent(""), player.getUUID());
            }
            player.sendMessage(MsgUtil.prefixedColorMsg(messages.getListFooter()), player.getUUID());
        });
    }
}