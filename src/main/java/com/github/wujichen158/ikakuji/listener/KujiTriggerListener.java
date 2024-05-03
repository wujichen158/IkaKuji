package com.github.wujichen158.ikakuji.listener;

import com.github.wujichen158.ikakuji.config.KujiObj;
import com.github.wujichen158.ikakuji.kuji.KujiExecutor;
import com.github.wujichen158.ikakuji.util.CrateFactory;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

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
        if (event.getHand() != InteractionHand.MAIN_HAND) {
            return;
        }
        KujiObj.Crate crate = CrateFactory.tryGetItemCrate(event.getItemStack());
        if (Optional.ofNullable(crate).isEmpty()) {
            return;
        }

        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.FAIL);

        KujiExecutor.executeKujiLogic(event, crate, true);
    }

    @SubscribeEvent
    public void onPlayerRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getHand() != InteractionHand.MAIN_HAND) {
            return;
        }

        if (!isItemCrate(event)) {
            BlockPos blockPos = event.getHitVec().getBlockPos();
            KujiObj.Crate crate = CrateFactory.tryGetWorldPosCrate(event.getEntity().level(),
                    blockPos.getX(),
                    blockPos.getY(),
                    blockPos.getZ());
            if (Optional.ofNullable(crate).isEmpty()) {
                return;
            }

            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.FAIL);

            KujiExecutor.executeKujiLogic(event, crate, false);
        }
    }

    @SubscribeEvent
    public void onPlayerRightClickEntity(PlayerInteractEvent.EntityInteractSpecific event) {
        if (event.getHand() != InteractionHand.MAIN_HAND) {
            return;
        }

        if (!isItemCrate(event)) {
            KujiObj.Crate crate = CrateFactory.tryGetEntityCrate(event.getTarget().getName().getString());
            if (Optional.ofNullable(crate).isEmpty()) {
                return;
            }

            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.FAIL);

            KujiExecutor.executeKujiLogic(event, crate, false);
        }
    }

    private boolean isItemCrate(PlayerInteractEvent event) {
        KujiObj.Crate crate = CrateFactory.tryGetItemCrate(event.getItemStack());
        if (Optional.ofNullable(crate).isEmpty()) {
            return false;
        }

        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.FAIL);

        return true;
    }
}
