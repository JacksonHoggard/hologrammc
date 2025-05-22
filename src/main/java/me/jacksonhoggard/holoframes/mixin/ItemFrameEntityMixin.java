package me.jacksonhoggard.holoframes.mixin;

import me.jacksonhoggard.holoframes.ItemFrameEntityMixinAccess;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemFrameEntity.class)
public abstract class ItemFrameEntityMixin implements ItemFrameEntityMixinAccess {

    @Unique
    private static final TrackedData<String> HOLOGRAM_FILE = DataTracker.registerData(ItemFrameEntity.class, TrackedDataHandlerRegistry.STRING);
    @Unique
    private static final TrackedData<Integer> HOLOGRAM_ROTATION = DataTracker.registerData(ItemFrameEntity.class, TrackedDataHandlerRegistry.INTEGER);

    @Inject(method = "initDataTracker", at = @At("TAIL"))
    private void onInitDataTracker(DataTracker.Builder builder, CallbackInfo ci) {
        builder.add(HOLOGRAM_FILE, "");
        builder.add(HOLOGRAM_ROTATION, 0);
    }

    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    private void onWriteCustomDataToNbt(NbtCompound nbt, CallbackInfo ci) {
        nbt.putString("HoloFile", holoFrames$getModelFile());
        nbt.putInt("HologramRotation", holoFrames$getHologramRotation());
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    private void onReadCustomDataFromNbt(NbtCompound nbt, CallbackInfo ci) {
        if (nbt.contains("HoloFile")) {
            this.holoFrames$setModelFile(nbt.getString("HoloFile", ""));
        }
        if (nbt.contains("HologramRotation")) {
            this.holoFrames$setHologramRotation(nbt.getInt("HologramRotation", 0));
        }
    }

    @Inject(method = "interact", at = @At("HEAD"), cancellable = true)
    private void onInteract(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        if(!this.holoFrames$getModelFile().isEmpty()) {
            this.holoFrames$setHologramRotation(this.holoFrames$getHologramRotation() + 1);
            cir.setReturnValue(ActionResult.SUCCESS);
            cir.cancel();
        }
    }



    @Override
    public String holoFrames$getModelFile() {
        return ((ItemFrameEntity) (Object) this).getDataTracker().get(HOLOGRAM_FILE);
    }

    @Override
    public void holoFrames$setModelFile(String fileName) {
        ((ItemFrameEntity) (Object) this).getDataTracker().set(HOLOGRAM_FILE, fileName);
    }

    @Override
    public int holoFrames$getHologramRotation() {
        return ((ItemFrameEntity) (Object) this).getDataTracker().get(HOLOGRAM_ROTATION);
    }

    @Override
    public void holoFrames$setHologramRotation(int rotation) {
        ((ItemFrameEntity) (Object) this).getDataTracker().set(HOLOGRAM_ROTATION, rotation % 6);
    }
}
