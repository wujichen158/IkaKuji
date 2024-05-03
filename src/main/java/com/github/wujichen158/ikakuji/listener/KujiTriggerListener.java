package com.github.wujichen158.ikakuji.listener;

import com.github.wujichen158.ikakuji.config.KujiObj;
import com.github.wujichen158.ikakuji.kuji.KujiExecutor;
import com.github.wujichen158.ikakuji.util.CrateFactory;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Optional;

/**
 * Execute priority: Item > entity = block
 *
 * @author wujichen158
 */
public class KujiTriggerListener {
    @SubscribeEvent
    public void onPlayerRightClick(PlayerInteractEvent.RightClickItem event) {
        // Must check in the beginning, or it'll execute twice
        if (event.getHand() != EnumHand.MAIN_HAND) {
            return;
        }
        KujiObj.Crate crate = CrateFactory.tryGetItemCrate(event.getItemStack());
        if (!Optional.ofNullable(crate).isPresent()) {
            return;
        }

        event.setCanceled(true);
        event.setCancellationResult(EnumActionResult.FAIL);

        KujiExecutor.executeKujiLogic(event, crate, true);
    }

    @SubscribeEvent
    public void onPlayerRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getHand() != EnumHand.MAIN_HAND) {
            return;
        }

        if (!isItemCrate(event)) {
            BlockPos blockPos = new BlockPos(event.getHitVec());
            KujiObj.Crate crate = CrateFactory.tryGetWorldPosCrate(event.getEntityPlayer().world,
                    blockPos.getX(),
                    blockPos.getY(),
                    blockPos.getZ());
            if (!Optional.ofNullable(crate).isPresent()) {
                return;
            }

            event.setCanceled(true);
            event.setCancellationResult(EnumActionResult.FAIL);

            KujiExecutor.executeKujiLogic(event, crate, false);
        }
    }

    @SubscribeEvent
    public void onPlayerRightClickEntity(PlayerInteractEvent.EntityInteractSpecific event) {
        if (event.getHand() != EnumHand.MAIN_HAND) {
            return;
        }

        if (!isItemCrate(event)) {
            KujiObj.Crate crate = CrateFactory.tryGetEntityCrate(event.getTarget().getName());
            if (!Optional.ofNullable(crate).isPresent()) {
                return;
            }

            event.setCanceled(true);
            event.setCancellationResult(EnumActionResult.FAIL);

            KujiExecutor.executeKujiLogic(event, crate, false);
        }
    }

    private boolean isItemCrate(PlayerInteractEvent event) {
        KujiObj.Crate crate = CrateFactory.tryGetItemCrate(event.getItemStack());
        if (!Optional.ofNullable(crate).isPresent()) {
            return false;
        }

        event.setCanceled(true);
        event.setCancellationResult(EnumActionResult.FAIL);

        return true;
    }
}
