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
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;
import java.util.Optional;

@Command(
        value = "list"
)
@Permissible(PermissionNodes.LIST_NODE)
public class ListCmd {

    @CommandProcessor
    public void run(@Sender ServerPlayer player, String[] args) {
        Optional.ofNullable(PlayerKujiFactory.get(player.getUUID())).ifPresent(playerData -> {
            int onePageSize = 8;
            int page = 0;
            if (args.length > 0) {
                try {
                    page = Integer.parseInt(args[0]) - 1;
                } catch (NumberFormatException ignored) {
                }
            }

            boolean indexStartOutBound = page * onePageSize >= playerData.getAvailableCrates().size();
            int indexStart = indexStartOutBound ? 0 : page * onePageSize;
            int indexEnd = indexStartOutBound ? 0 : Math.min(playerData.getAvailableCrates().size(), page * onePageSize + onePageSize);
            List<String> pagedCrateList = playerData.getAvailableCrates().subList(indexStart, indexEnd);
            IkaKujiLocaleCfg.Commands commands = IkaKuji.getInstance().getLocale().getCommands();

            // in style
            player.sendSystemMessage(MsgUtil.colorMsg(commands.getListTitle()));
            pagedCrateList.forEach(crateName ->
                    player.sendSystemMessage(MsgUtil.colorMsg(commands.getListElemPrefix(), crateName)));
            for (int i = 0; i < onePageSize - pagedCrateList.size(); i++) {
                player.sendSystemMessage(Component.literal(""));
            }
            player.sendSystemMessage(MsgUtil.colorMsg(commands.getListFooter()));
        });
    }
}