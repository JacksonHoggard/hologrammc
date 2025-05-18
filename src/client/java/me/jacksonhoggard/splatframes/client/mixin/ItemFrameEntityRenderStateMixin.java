package me.jacksonhoggard.splatframes.client.mixin;

import me.jacksonhoggard.splatframes.client.ItemFrameEntityRenderStateAccessor;
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
    private String hologramSplatFile = null;

    @Override
    public String splatFrames$getSplatFile() {
        return this.hologramSplatFile;
    }

    @Override
    public void splatFrames$setSplatFile(String splatFile) {
        this.hologramSplatFile = splatFile;
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(CallbackInfo ci) {
        this.hologramSplatFile = null;
    }

}
