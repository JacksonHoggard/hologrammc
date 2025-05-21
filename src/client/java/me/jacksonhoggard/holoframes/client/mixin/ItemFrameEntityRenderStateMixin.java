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

    @Override
    public String holoFrames$getModelFile() {
        return this.hologramFile;
    }

    @Override
    public void holoFrames$setModelFile(String file) {
        this.hologramFile = file;
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void onInit(CallbackInfo ci) {
        this.hologramFile = "";
    }

}
