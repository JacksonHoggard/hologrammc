package me.jacksonhoggard.holoframes.mixin;

import me.jacksonhoggard.holoframes.ItemFrameEntityMixinAccess;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.nbt.NbtCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemFrameEntity.class)
public abstract class ItemFrameEntityMixin implements ItemFrameEntityMixinAccess {

    @Unique
    private static final TrackedData<String> HOLOGRAM_FILE = DataTracker.registerData(ItemFrameEntity.class, TrackedDataHandlerRegistry.STRING);

    @Inject(method = "initDataTracker", at = @At("TAIL"))
    private void onInitDataTracker(DataTracker.Builder builder, CallbackInfo ci) {
        builder.add(HOLOGRAM_FILE, "");
    }

    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    private void onWriteCustomDataToNbt(NbtCompound nbt, CallbackInfo ci) {
        nbt.putString("HoloFile", holoFrames$getModelFile());
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    private void onReadCustomDataFromNbt(NbtCompound nbt, CallbackInfo ci) {
        if (nbt.contains("HoloFile")) {
            this.holoFrames$setModelFile(nbt.getString("HoloFile", ""));
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
}
