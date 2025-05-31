package me.jacksonhoggard.holoframes;

import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.Optional;

public class HologramCommand {
    public static final IdentifierArgumentType FILE_ARGUMENT = IdentifierArgumentType.identifier();

    public static int executeSetHologram(CommandContext<ServerCommandSource> context) {
        ServerPlayerEntity player = context.getSource().getPlayer();
        String fileName = IdentifierArgumentType.getIdentifier(context, "file").getPath();

        EntityHitResult frame = getPlayerPOVHitResult(player, 25.0D);
        if(frame != null && frame.getEntity().getType().equals(EntityType.ITEM_FRAME)) {
            ((ItemFrameEntityMixinAccess) (Object) frame.getEntity()).holoFrames$setModelFile(fileName);
            context.getSource().sendFeedback(() -> Text.literal("Assigned hologram file: " + fileName), false);
        } else {
            context.getSource().sendError(Text.literal("You must be looking at an item frame!"));
        }
        return 1;
    }

    private static EntityHitResult getPlayerPOVHitResult(ServerPlayerEntity player, double distance) {
        double playerRotX = player.getRotationClient().x;
        double playerRotY = player.getRotationClient().y;
        Vec3d startPos = player.getEyePos();
        double d2 = Math.cos(-playerRotY * (Math.PI / 180D) - Math.PI);
        double d3 = Math.sin(-playerRotY * (Math.PI / 180D) - Math.PI);
        double d4 = -Math.cos(-playerRotX * (Math.PI / 180D));
        double additionY = Math.sin(-playerRotX * (Math.PI / 180D));
        double additionX = d3 * d4;
        double additionZ = d2 * d4;
        Vec3d endPos = startPos.add(additionX * distance, additionY * distance, additionZ * distance);
        Box startEndBox = new Box(startPos, endPos);
        Entity entity = null;
        for(Entity entity1 : player.getWorld().getOtherEntities(player, startEndBox, (val) -> true)) {
            Box box = entity1.getBoundingBox().expand(0.2D);
            Optional<Vec3d> optional = box.raycast(startPos, endPos);
            if(box.contains(startPos)) {
                if(distance >= 0.0D) {
                    entity = entity1;
                    startPos = optional.orElse(startPos);
                    distance = 0.0D;
                }
            } else if (optional.isPresent()) {
                Vec3d vec31 = optional.get();
                double d1 = startPos.squaredDistanceTo(vec31);
                if (d1 < distance || distance == 0.0D) {
                    if (entity1.getRootVehicle() == player.getRootVehicle() && !entity1.shouldControlVehicles()) {
                        if(distance == 0.0D) {
                            entity = entity1;
                            startPos = vec31;
                        }
                    } else {
                        entity = entity1;
                        startPos = vec31;
                        distance = d1;
                    }
                }
            }
        }
        return (entity == null) ? null : new EntityHitResult(entity);
    }
}
