package com.github.wujichen158.ikakuji.command;

import com.envyful.api.command.annotate.Command;
import com.envyful.api.command.annotate.executor.CommandProcessor;
import com.envyful.api.command.annotate.executor.Sender;
import com.envyful.api.command.annotate.permission.Permissible;
import com.github.wujichen158.ikakuji.IkaKuji;
import com.github.wujichen158.ikakuji.config.IkaKujiLocaleCfg;
import com.github.wujichen158.ikakuji.lib.PermissionNodes;
import com.github.wujichen158.ikakuji.util.CmdUtil;
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
    public void run(@Sender ServerPlayerEntity player, String[] args) {
        Optional.ofNullable(PlayerKujiFactory.get(player.getUUID())).ifPresent(playerData -> {
            int pageSize = 8;
            int page = CmdUtil.getPageFromArgs(args) - 1;

            boolean indexStartOutBound = page * pageSize >= playerData.getAvailableCrates().size();
            int indexStart = indexStartOutBound ? 0 : page * pageSize;
            int indexEnd = indexStartOutBound ? 0 : Math.min(playerData.getAvailableCrates().size(), page * pageSize + pageSize);
            List<String> pagedCrateList = playerData.getAvailableCrates().subList(indexStart, indexEnd);
            IkaKujiLocaleCfg.Commands commands = IkaKuji.getInstance().getLocale().getCommands();

            // in style
            player.sendMessage(MsgUtil.colorMsg(commands.getListTitle()), player.getUUID());
            pagedCrateList.forEach(crateName ->
                    player.sendMessage(MsgUtil.colorMsg(commands.getListElemPrefix(), crateName), player.getUUID()));
            for (int i = 0; i < pageSize - pagedCrateList.size(); i++) {
                player.sendMessage(new StringTextComponent(""), player.getUUID());
            }
            player.sendMessage(MsgUtil.colorMsg(commands.getListFooter()), player.getUUID());
        });
    }
}