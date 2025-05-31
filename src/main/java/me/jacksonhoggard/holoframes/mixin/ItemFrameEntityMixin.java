package me.jacksonhoggard.holoframes.mixin;

import me.jacksonhoggard.holoframes.HoloframesComponents;
import me.jacksonhoggard.holoframes.ItemFrameEntityMixinAccess;
import me.jacksonhoggard.holoframes.item.HoloframesItems;
import me.jacksonhoggard.holoframes.item.HologramModelItem;
import net.minecraft.entity.Entity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.event.GameEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemFrameEntity.class)
public abstract class ItemFrameEntityMixin implements ItemFrameEntityMixinAccess {

    @Shadow public abstract void setHeldItemStack(ItemStack stack);
    @Shadow public abstract ItemStack getHeldItemStack();

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
            if(player.getWorld().isClient) {
                this.holoFrames$setHologramRotation(this.holoFrames$getHologramRotation() + 1);
                ((ItemFrameEntity) (Object) this).emitGameEvent(GameEvent.BLOCK_CHANGE, player);
            }
            cir.setReturnValue(ActionResult.SUCCESS);
            cir.cancel();
            return;
        }
        ItemStack playerStack = player.getStackInHand(hand);
        ItemStack frameStack = this.getHeldItemStack();
        boolean isHologramModelItem = playerStack.isOf(HoloframesItems.HOLOGRAM_MODEL_ITEM);
        boolean isFrameEmpty = frameStack.isEmpty() && this.holoFrames$getModelFile().isEmpty();
        if(isFrameEmpty && isHologramModelItem) {
            if(!player.getWorld().isClient) {
                Text hologramFile = playerStack.get(HoloframesComponents.SELECTED_HOLOGRAM_FILE);
                if(hologramFile == null || hologramFile.getString().isEmpty()) {
                    player.sendMessage(Text.translatable("item.holoframes.hologram_model_item.no_file").withColor(0xFF0000), true);
                    cir.setReturnValue(ActionResult.FAIL);
                    cir.cancel();
                    return;
                }
                ((ItemFrameEntity) (Object) this).emitGameEvent(GameEvent.BLOCK_CHANGE, player);
                this.holoFrames$setModelFile(hologramFile.getString());
                playerStack.decrementUnlessCreative(1, player);
            }
            cir.setReturnValue(ActionResult.SUCCESS);
            cir.cancel();
        }
    }

    @Inject(method = "onBreak", at = @At("HEAD"))
    private void onBreak(ServerWorld world, Entity breaker, CallbackInfo ci) {
        if(!this.holoFrames$getModelFile().isEmpty()) {
            ItemStack stack = new ItemStack(HoloframesItems.HOLOGRAM_MODEL_ITEM);
            stack.set(HoloframesComponents.SELECTED_HOLOGRAM_FILE, Text.of(this.holoFrames$getModelFile()));
            this.setHeldItemStack(stack);
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
