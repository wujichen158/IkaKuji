package com.github.wujichen158.ikakuji.listener;

import com.github.wujichen158.ikakuji.config.KujiObj;
import com.github.wujichen158.ikakuji.kuji.KujiExecutor;
import com.github.wujichen158.ikakuji.util.CrateFactory;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Optional;

/**
 * Execute priority: Item > entity = block
 *
 * @author wujichen158
 */
public class KujiTriggerListener {

    //TODO: Support permissions

    @SubscribeEvent
    public void onPlayerRightClick(PlayerInteractEvent.RightClickItem event) {
        this.handleItemInteract(event);
    }

    @SubscribeEvent
    public void onPlayerRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        this.handleItemInteract(event);
        if (!event.isCanceled()) {
            BlockPos blockPos = event.getHitVec().getBlockPos();
            KujiObj.Crate crate = CrateFactory.tryGetWorldPosCrate(event.getPlayer().level,
                    blockPos.getX(),
                    blockPos.getY(),
                    blockPos.getZ());
            if (Optional.ofNullable(crate).isEmpty()) {
                return;
            }

            event.setCanceled(true);
            event.setUseBlock(Event.Result.DENY);

            KujiExecutor.executeKujiLogic(event, crate, false);
        }
    }

    @SubscribeEvent
    public void onPlayerRightClickEntity(PlayerInteractEvent.EntityInteractSpecific event) {
        this.handleItemInteract(event);
        if (!event.isCanceled()) {
            KujiObj.Crate crate = CrateFactory.tryGetEntityCrate(event.getTarget().getName().getString());
            if (Optional.ofNullable(crate).isEmpty()) {
                return;
            }

            event.setCanceled(true);
            //TODO: Has doubt on this:
            event.setCancellationResult(ActionResultType.FAIL);

            KujiExecutor.executeKujiLogic(event, crate, false);
        }
    }

    private void handleItemInteract(PlayerInteractEvent event) {
        KujiObj.Crate crate = CrateFactory.tryGetItemCrate(event.getItemStack());
        if (Optional.ofNullable(crate).isEmpty()) {
            return;
        }

        event.setCanceled(true);
        event.setCancellationResult(ActionResultType.FAIL);

        KujiExecutor.executeKujiLogic(event, crate, true);
    }
}
