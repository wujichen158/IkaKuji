package com.github.wujichen158.ikakuji.kuji.executor;

import com.envyful.api.forge.player.ForgeEnvyPlayer;
import com.github.wujichen158.ikakuji.IkaKuji;
import com.github.wujichen158.ikakuji.config.IkaKujiLocaleCfg;
import com.github.wujichen158.ikakuji.config.KujiObj;
import com.github.wujichen158.ikakuji.kuji.KujiExecutor;
import com.github.wujichen158.ikakuji.kuji.KujiGuiManager;
import com.github.wujichen158.ikakuji.lib.PermissionNodes;
import com.github.wujichen158.ikakuji.util.MsgUtil;
import com.github.wujichen158.ikakuji.util.PlayerKujiFactory;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.AtomicDouble;
import com.mojang.datafixers.util.Pair;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class PreviewExecutor {

    /**
     * Preview using command
     *
     * @param player
     * @param crate
     */
    public static void preview(PlayerEntity player, KujiObj.Crate crate, int page) {
        ForgeEnvyPlayer envyPlayer = IkaKuji.getInstance().getPlayerManager().getPlayer((ServerPlayerEntity) player);
        String crateName = crate.getDisplayName();
        List<String> playerDrawn = PlayerKujiFactory.get(envyPlayer.getUniqueId())
                .getKujiData().getOrDefault(crateName, Lists.newArrayList());
        IkaKujiLocaleCfg.Messages messages = IkaKuji.getInstance().getLocale().getMessages();

        preview(player, crate, crateName, messages, playerDrawn, envyPlayer, page);
    }

    /**
     * Preview logic
     *
     * @param player
     * @param crate
     * @param crateName
     * @param messages
     * @param playerDrawn
     * @param envyPlayer
     */
    public static void preview(PlayerEntity player, KujiObj.Crate crate, String crateName, IkaKujiLocaleCfg.Messages messages, List<String> playerDrawn, ForgeEnvyPlayer envyPlayer, int page) {
        // Perm check
        if (!IkaKuji.getInstance().getCommandFactory().hasPermission(player, PermissionNodes.getPreviewPermNode(crateName))) {
            player.sendMessage(MsgUtil.prefixedColorMsg(messages.getNoPreviewPermMsg(), crateName), player.getUUID());
            return;
        }

        // Pre cal available rewards here to speed up page changing
        AtomicDouble currentTotalWeight = new AtomicDouble(0d);
        Map<String, Integer> weightOverrideMap = KujiExecutor.getCurrentWeightOverride(crate, playerDrawn.size() + 1);
        KujiGuiManager.preview(crate, envyPlayer, calAvailableRewards(playerDrawn.stream()
                        .collect(Collectors.groupingBy(Function.identity(), Collectors.counting())),
                crate, currentTotalWeight, weightOverrideMap), currentTotalWeight, weightOverrideMap, page);
    }

    /**
     * Preview global kuji
     *
     * @param player
     * @param globalKujiData
     * @param crate
     * @param kujiName
     * @param page
     */
    public static void previewGlobal(PlayerEntity player, KujiObj.GlobalData globalKujiData, KujiObj.Crate crate, String kujiName, int page) {
        IkaKujiLocaleCfg.Messages messages = IkaKuji.getInstance().getLocale().getMessages();

        // Perm check
        if (!IkaKuji.getInstance().getCommandFactory().hasPermission(player,
                PermissionNodes.getGlobalPreviewPermNode(kujiName))) {
            player.sendMessage(MsgUtil.prefixedColorMsg(messages.getNoPreviewPermMsg(), kujiName), player.getUUID());
            return;
        }

        ForgeEnvyPlayer envyPlayer = IkaKuji.getInstance().getPlayerManager().getPlayer((ServerPlayerEntity) player);

        // Don't need to pre cal available rewards, since it may be changed by other players' draws
        Map<String, Integer> weightOverrideMap = KujiExecutor.getCurrentWeightOverride(crate,
                globalKujiData.getDrawnCount() + 1);
        KujiGuiManager.previewGlobal(globalKujiData, crate, envyPlayer, weightOverrideMap, page);
    }

    public static List<Pair<KujiObj.Reward, Integer>> calAvailableRewards(Map<String, Long> drawnCountMap, KujiObj.Crate crate, AtomicDouble currentTotalWeight, Map<String, Integer> weightOverrideMap) {
        return crate.getRewards().stream()
                .filter(KujiObj.Reward::isCanPreview)
                .map(reward -> new Pair<>(reward, KujiExecutor.getAvailableAmount(reward.getAmountPerKuji(), drawnCountMap.get(reward.getId()))))
                .filter(pair -> pair.getSecond() > 0)
                .peek(pair -> currentTotalWeight.addAndGet(pair.getFirst().calWeightPerReward(weightOverrideMap) * pair.getSecond()))
                .collect(Collectors.toList());
    }

}
