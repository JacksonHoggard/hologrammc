package me.jacksonhoggard.holoframes.client.mixin;

import me.jacksonhoggard.holoframes.client.ItemFrameEntityRenderStateAccessor;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.render.entity.state.ItemFrameEntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemFrameEntityRenderState.class)
public class ItemFrameEntityRenderStateMixin extends EntityRenderState implements ItemFrameEntityRenderStateAccessor {
    @Unique
    private String hologramFile = null;
    @Unique
    private int hologramRotation = 0;

    @Override
    public String holoFrames$getModelFile() {
        return this.hologramFile;
    }

    @Override
    public void holoFrames$setModelFile(String file) {
        this.hologramFile = file;
    }

    @Override
    public int holoFrames$getHologramRotation() {
        return hologramRotation;
    }

    @Override
    public void holoFrames$setHologramRotation(int rotation) {
        this.hologramRotation = rotation;
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void onInit(CallbackInfo ci) {
        this.hologramFile = "";
        this.hologramRotation = 0;
    }

}
