package me.jacksonhoggard.splatframes.mixin;

import me.jacksonhoggard.splatframes.ItemFrameEntityMixinAccess;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.decoration.AbstractDecorationEntity;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemFrameEntity.class)
public abstract class ItemFrameEntityMixin extends AbstractDecorationEntity implements ItemFrameEntityMixinAccess {

    @Shadow public abstract SoundEvent getPlaceSound();

    @Unique
    private static final TrackedData<String> HOLOGRAM_SPLAT_FILE = DataTracker.registerData(ItemFrameEntity.class, TrackedDataHandlerRegistry.STRING);

    protected ItemFrameEntityMixin(EntityType<? extends AbstractDecorationEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    private void onWriteCustomDataToNbt(NbtCompound nbt, CallbackInfo ci) {
        nbt.putString("SplatFile", this.getDataTracker().get(HOLOGRAM_SPLAT_FILE));
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    private void onReadCustomDataFromNbt(NbtCompound nbt, CallbackInfo ci) {
        if (nbt.contains("SplatFile")) {
            this.getDataTracker().set(HOLOGRAM_SPLAT_FILE, String.valueOf(nbt.getString("SplatFile")));
        }
    }

    @Override
    public String splatFrames$getSplatFile() {
        return this.getDataTracker().get(HOLOGRAM_SPLAT_FILE);
    }

    @Override
    public void splatFrames$setSplatFile(String fileName) {
        this.getDataTracker().set(HOLOGRAM_SPLAT_FILE, fileName);
    }

    @Override
    protected Box calculateBoundingBox(BlockPos pos, Direction side) {
        float f = 0.46875F;
        Vec3d vec3d = Vec3d.ofCenter(pos).offset(side, -0.46875);
        Direction.Axis axis = side.getAxis();
        double d = axis == Direction.Axis.X ? 0.0625 : 0.75;
        double e = axis == Direction.Axis.Y ? 0.0625 : 0.75;
        double g = axis == Direction.Axis.Z ? 0.0625 : 0.75;
        return Box.of(vec3d, d, e, g);
    }

    @Override
    public void onPlace() {
        this.playSound(this.getPlaceSound(), 1.0F, 1.0F);
    }

    @Override
    public void onBreak(ServerWorld world, @Nullable Entity breaker) {

    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {

    }
}
