package de.timo.foreverproductionmonitor.mixin;

import appeng.api.stacks.AEKey;
import appeng.client.gui.me.common.PinnedKeys;
import java.util.Comparator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** AE2 19.2.x computes a negative subList bound when its tenth pin is added. */
@Mixin(value = PinnedKeys.class, remap = false)
public abstract class PinnedKeysMixin {
    @Inject(method = "pinKey", at = @At("HEAD"), remap = false)
    private static void forever$makeRoomForCraftingPin(AEKey key, PinnedKeys.PinReason reason,
                                                       CallbackInfo ci) {
        if (PinnedKeys.isPinned(key)) return;
        while (PinnedKeys.getPinnedKeys().size() >= PinnedKeys.MAX_PINNED) {
            AEKey oldest = PinnedKeys.getPinnedKeys().stream()
                    .min(Comparator.comparing(pinned -> PinnedKeys.getPinInfo(pinned).since))
                    .orElse(null);
            if (oldest == null) return;
            PinnedKeys.unpin(oldest);
        }
    }
}
